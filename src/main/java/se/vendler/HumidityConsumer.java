package se.vendler;

import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class HumidityConsumer extends Thread implements EventConsumer {
	private BlockingQueue<EnvironmentEvent> queue;
	Logger logger = Logger.getLogger(HumidityConsumer.class.getSimpleName());
	private HumidityWriter humidityWriter;
	public HumidityConsumer() {
		queue = new ArrayBlockingQueue<>(100);
		humidityWriter = new HumidityWriter();
		this.start();
	}
	
	@Override
	public void consume(Event event) {
		if(event instanceof EnvironmentEvent)
			queue.add((EnvironmentEvent) event);
	}
	
	@Override
	public void consume(FloorheatUpdate floorheatUpdate) {
	
	}
	
	@Override
	public void run() {
		while(true){
			try {
				EnvironmentEvent event = queue.take();
				logger.info(String.format("Humidity %.2f sensor id %d",event.getValue(), event.getSourceId()));
				humidityWriter.addWrite(event.getSourceId(), event.getValue());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
