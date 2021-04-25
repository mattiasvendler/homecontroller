package se.vendler;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import se.vendler.floorheat.FloorheatController;
import se.vendler.floorheat.config.FloorHeatConfiguration;
import se.vendler.floorheat.impl.ESPFloorheatController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static se.vendler.FloorheatState.FLOORHEAT_STATE_OFF;
import static se.vendler.FloorheatState.FLOORHEAT_STATE_ON;

/**
 * Created by mattias on 2017-10-10.
 */
public class FloorheatService extends Thread {
	private FloorHeatConfiguration floorHeatConfiguration;
	private FloorheatController floorheatController;
	private static Logger logger = Logger.getLogger(FloorheatService.class);
	private static HashMap<Long, RoomInformation> sensorRoomMapping;
	private static Sql2o sql2o;
	private static final int HEAT_EXCERCISE_PERIOD = 10 * 60 * 1000;
	
	static {
		sql2o = new Sql2o("jdbc:mysql://192.168.1.181:3306/node?useSSL=false", "homecenter", "homecenter");
		Connection connection = sql2o.open();
		Query query = connection.createQuery("select roomId,sensorId, targetTemperature, name , inverted from node.room_config order by sensorId desc;");
		List<RoomInformation> roomInformations = query.executeAndFetch(RoomInformation.class);
		
		sensorRoomMapping = new HashMap<>();
		for (RoomInformation roomInformation : roomInformations) {
			roomInformation.setState(FLOORHEAT_STATE_OFF);
			sensorRoomMapping.put(roomInformation.getSensorId(), roomInformation);
		}
		connection.close();
	}
	
	public FloorheatService() throws IOException, TimeoutException {
		floorHeatConfiguration = new FloorHeatConfiguration();
		floorHeatConfiguration.setIp("192.168.1.237");
		floorHeatConfiguration.setPort(8080);
		floorHeatConfiguration.setOutputCount(8);
		floorheatController = new ESPFloorheatController(floorHeatConfiguration.getIp(), floorHeatConfiguration.getPort());
		floorheatController.configure(floorHeatConfiguration);
		logger.info("Configure floorheat controller done");
		this.start();
		
		
	}
	
	public void run() {
		while (true) {
			try {
				
				Thread.sleep(10000);
				synchronized (sensorRoomMapping) {
					Connection connection = sql2o.open();
					Query query = connection.createQuery("select roomId,sensorId, targetTemperature,name from node.room_config");
					List<RoomInformation> roomInformations = query.executeAndFetch(RoomInformation.class);
					query.close();
					connection.close();
					boolean exersised = false;
					for (RoomInformation roomInformation : roomInformations) {
						RoomInformation info = sensorRoomMapping.get(roomInformation.getSensorId());
						if (info != null) {
							if (info.getTargetTemperature() != roomInformation.getTargetTemperature()) {
								logger.info(String.format("Room %d (%s) target temperature changed from %.1f to %.1f",
										info.getRoomId(), roomInformation.getName(), info.getTargetTemperature(), roomInformation.getTargetTemperature()));
								query = connection.createQuery("select temperature from environment.temperature where node_id = :node_id order by save_date desc;")
										        .addParameter("node_id", info.getSensorId());
								Float lastTemp = query.executeAndFetchFirst(Float.class);
								query.close();
								info.setTargetTemperature(roomInformation.getTargetTemperature());
								updateRoomTemperature(roomInformation.getSensorId(), lastTemp, true);
							}
							connection = sql2o.open();
							Query dateQuery = connection.createQuery("select count(date) from node.room_info_log where room_id = :room and info = 'FLOORHEAT_STATE_ON' and date >= now() - INTERVAL 1 DAY ;")
									                  .addParameter("room", info.getRoomId());
							Integer lastOnDate = dateQuery.executeAndFetchFirst(Integer.class);
							dateQuery.close();
							connection.close();
							if ((lastOnDate == null || lastOnDate == 0) && !exersised) {
								new FloorheatExerciseWorker(info.getRoomId(), HEAT_EXCERCISE_PERIOD, floorheatController, info).start();
								connection = sql2o.open();
								Query tempQuery = connection.createQuery("select temperature from environment.temperature where node_id = :sensor order by sample_date desc limit 1;")
										                  .addParameter("sensor", info.getSensorId());
								List<Short> temps = tempQuery.executeAndFetch(Short.class);
								tempQuery.close();
								connection.close();
								if (temps.size() == 1) {
									logRoomInfo(info, FloorheatState.FLOORHEAT_STATE_ON, temps.get(0));
								}
								logger.info(String.format("Exersicising room %s", info.getName()));
								exersised = true;
							}
						} else {
							logger.info(String.format("New room added %s", roomInformation.getName()));
							roomInformation.setState(FLOORHEAT_STATE_OFF);
							sensorRoomMapping.put(roomInformation.getSensorId(), roomInformation);
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
	}
	
	private RoomInformation getRoomInformation(int room) {
		for (RoomInformation roomInformation : sensorRoomMapping.values()) {
			if (roomInformation.getRoomId() == room) {
				return roomInformation;
			}
		}
		return null;
	}
	
	private void syncRoomState(int room) {
		RoomInformation roomInformation = getRoomInformation(room);
		boolean heatOn = floorheatController.isHeatOn(roomInformation.getRoomId());
		if (heatOn != roomInformation.getState().isOn()) {
			if (heatOn) {
				roomInformation.setState(FLOORHEAT_STATE_ON);
			} else {
				roomInformation.setState(FLOORHEAT_STATE_OFF);
			}
			logger.info(String.format("Room heat state needed to be updated for room %d (%s)", roomInformation.getRoomId(), roomInformation.getName()));
		}
		
	}
	
	private float getTargetTemperature(RoomInformation roomInformation) {
		float targetTemp = roomInformation.getTargetTemperature();
		return targetTemp;
	}
	
	private FloorheatState getRoomState(RoomInformation roomInformation, float temperature) {
		if (getTargetTemperature(roomInformation) >= temperature) {
			return roomInformation.isInverted() ? FLOORHEAT_STATE_OFF : FLOORHEAT_STATE_ON;
		} else {
			return roomInformation.isInverted() ? FLOORHEAT_STATE_ON : FLOORHEAT_STATE_OFF;
		}
	}
	
	private void logRoomInfo(RoomInformation roomInformation, FloorheatState state, float temperature) {
		Connection connection = sql2o.open();
		connection.createQuery("insert into node.room_info_log (room_id, sensor_id, target_temp, temp, info) values (:room_id, :sensor_id, :target_temp, :temp, :info)")
				.addParameter("room_id", roomInformation.getRoomId())
				.addParameter("sensor_id", roomInformation.getSensorId())
				.addParameter("target_temp", getTargetTemperature(roomInformation))
				.addParameter("temp", temperature)
				.addParameter("info", state.toString())
				.executeUpdate();
		connection.close();
	}
	
	private RoomInformation getRoomInformationFromRoomId(int roomId) {
		Set<Long> roomInformationIds = sensorRoomMapping.keySet();
		
		for (Long i : roomInformationIds) {
			RoomInformation roomInformation = sensorRoomMapping.get(i);
			if (roomInformation.getRoomId() == roomId) {
				return roomInformation;
			}
		}
		
		return null;
	}
	
	public void updateRoomTemperature(long sensorId, float temperature, boolean publish) {
		synchronized (sensorRoomMapping) {
			RoomInformation room = sensorRoomMapping.get(sensorId);
			if (room != null) {
				logger.info(String.format("Temperature %.1f from sensor %d room %d (%s) target temp = %.1f", temperature, sensorId, room.getRoomId(), room.getName(), getTargetTemperature(room)));
				if (room.isExercising()) {
					logger.info("Room is exercising skipp update.");
					return;
				}
				syncRoomState(room.getRoomId());
				switch (getRoomState(room, temperature)) {
					case FLOORHEAT_STATE_OFF:
//						if (!FLOORHEAT_STATE_OFF.equals(room.getState())) {
							if (floorheatController.heatOff(room.getRoomId(), publish)) {
								logger.info(String.format("Heat off for room %d", room.getRoomId()));
								logRoomInfo(room, FLOORHEAT_STATE_OFF, temperature);
								room.setState(FLOORHEAT_STATE_OFF);
							} else {
								logger.error("Failed to turn heat off");
							}
//						}
						break;
					case FLOORHEAT_STATE_ON:
//						if (!FLOORHEAT_STATE_ON.equals(room.getState())) {
							if (floorheatController.heatOn(room.getRoomId(), publish)) {
								logger.info(String.format("Heat on for room %d", room.getRoomId()));
								logRoomInfo(room, FLOORHEAT_STATE_ON, temperature);
								room.setState(FLOORHEAT_STATE_ON);
							} else {
								logger.error("Failed to turn heat on");
							}
//						}
						break;
				}
			}
		}
	}
	
	public void updateRoomStateOn(int roomId) {
		RoomInformation roomInformationFromRoomId = getRoomInformationFromRoomId(roomId);
		if (roomInformationFromRoomId != null) {
			updateRoomTemperature(roomInformationFromRoomId.getSensorId(), -100, false);
		}
	}
	
	public void updateRoomStateOff(int roomId) {
		RoomInformation roomInformationFromRoomId = getRoomInformationFromRoomId(roomId);
		if (roomInformationFromRoomId != null) {
			updateRoomTemperature(roomInformationFromRoomId.getSensorId(), 100, false);
		}
		
	}
}
