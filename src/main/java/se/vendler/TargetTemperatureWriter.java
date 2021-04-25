package se.vendler;

import org.sql2o.Connection;
import org.sql2o.Query;

public class TargetTemperatureWriter extends DataWriter {
	@Override
	protected Query getQuery(DataWrite targetTemperatureWrite, Connection open) {
		return open.createQuery("update node.room_config set targetTemperature=:data where sensorId=:node_id;")
				.addParameter("node_id", targetTemperatureWrite.getNodeId())
				.addParameter("data", targetTemperatureWrite.getData());
		
	}
}
