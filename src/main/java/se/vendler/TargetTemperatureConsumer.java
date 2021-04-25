package se.vendler;

import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TargetTemperatureConsumer extends Thread implements EventConsumer {
	private Logger logger = Logger.getLogger(this.getClass());
	private boolean running;
	private BlockingQueue<EnvironmentEvent> queue;
	private TargetTemperatureWriter targetTemperatureWriter;
	
	public TargetTemperatureConsumer(){
		running = false;
		queue = new ArrayBlockingQueue<EnvironmentEvent>(100);
		targetTemperatureWriter = new TargetTemperatureWriter();
		this.start();
		
	}
	
	@Override
	public void consume(Event event) {
		if (event instanceof EnvironmentEvent) {
			EnvironmentEvent temperatureEvent = (EnvironmentEvent) event;
			queue.add(temperatureEvent);
			logger.debug(String.format("Target temperature event consumed size %d", queue.size()));
		}
		
	}
	
	@Override
	public void consume(FloorheatUpdate floorheatUpdate) {
	
	}
	
	@Override
	public void run() {
		running = true;
		while (running) {
			EnvironmentEvent temperatureEvent = null;
			try {
				temperatureEvent = queue.take();
				logger.debug(String.format("Temperature event poped from queue size %d", queue.size()));
				targetTemperatureWriter.addWrite(temperatureEvent.getSourceId(),temperatureEvent.getValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
