package se.vendler;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by mattias on 2017-10-10.
 */
public class TemperatureConsumer extends Thread implements EventConsumer {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean running;
	private BlockingQueue<EnvironmentEvent> queue;
	private FloorheatService floorheatService;
	private TemperatureWriter temperatureWriter;
	public TemperatureConsumer(FloorheatService floorheatService) throws IOException, TimeoutException {
		
		running = false;
		queue = new ArrayBlockingQueue<EnvironmentEvent>(100);
		this.floorheatService = floorheatService;
		temperatureWriter = new TemperatureWriter();
		this.start();
		
	}
	
	@Override
	public void consume(Event event) {
		if (event instanceof EnvironmentEvent) {
			EnvironmentEvent temperatureEvent = (EnvironmentEvent) event;
			logger.debug(String.format("Temperature event consumed size %d", queue.size()));
			queue.add(temperatureEvent);
		}
	}
	
	@Override
	public void consume(FloorheatUpdate floorheatUpdate) {
	
	}
	
	public void stopConsumer() {
		running = false;
	}
	
	@Override
	public void run() {
		running = true;
		while (running) {
			EnvironmentEvent temperatureEvent = null;
			try {
				temperatureEvent = queue.take();
				logger.debug(String.format("Temperature event poped from queue size %d", queue.size()));
				logger.debug(String.format("Writing temperature %f",temperatureEvent.getValue()));
				temperatureWriter.addWrite(temperatureEvent.getSourceId(),temperatureEvent.getValue());
				logger.debug(String.format("Sending temperature update"));
				floorheatService.updateRoomTemperature(temperatureEvent.getSourceId(), temperatureEvent.getValue(), true);
				logger.debug(String.format("Sending temperature update done"));
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		running = false;
		logger.info("Temperature consumer stopped!");
	}
}
