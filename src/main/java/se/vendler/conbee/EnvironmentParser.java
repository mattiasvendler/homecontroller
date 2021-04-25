package se.vendler.conbee;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import se.vendler.HumidityConsumer;
import se.vendler.HumidityEvent;
import se.vendler.TemperatureConsumer;
import se.vendler.TemperatureEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EnvironmentParser extends Thread{
    private Gson parser;
    private BlockingQueue<String> in = new ArrayBlockingQueue<String>(10);
    private Logger logger = Logger.getLogger(this.getName());
    private HumidityConsumer humidityConsumer;
    private TemperatureConsumer temperatureConsumer;
    public EnvironmentParser(Gson parser) {
        this.parser = parser;
    }

    public void setHumidityConsumer(HumidityConsumer humidityConsumer) {
        this.humidityConsumer = humidityConsumer;
    }

    public void setTemperatureConsumer(TemperatureConsumer temperatureConsumer) {
        this.temperatureConsumer = temperatureConsumer;
    }

    public void parse(String message) {
        in.add(message);
    }

    @Override
    public void run() {
        while(true) {
            try {
                String data = in.take();
                EnvironmentMessage message = parser.fromJson(data, EnvironmentMessage.class);
                if (message != null && message.complete()) {
                    message.extract();
                    if (message.getType() == 402) {
                        if (temperatureConsumer != null) {
                            TemperatureEvent event = new TemperatureEvent();
                            event.setTemperature(message.getState().getValue()/100.0f);
                            event.setValue(message.getState().getValue()/100.0f);
                            event.setSourceId(message.getDeviceId());
                            event.setTargetId(1);
                            temperatureConsumer.consume(event);
                            logger.info(String.format("Temperature %.2f C", message.getState().getValue() / 100.0));
                        }
                    } else if (message.getType() == 405) {
                        if (humidityConsumer != null) {
                            HumidityEvent event = new HumidityEvent();
                            event.setHumidity(message.getState().getValue()/100.0f);
                            event.setValue(message.getState().getValue()/100.0f);
                            event.setSourceId(message.getDeviceId());
                            event.setTargetId(1);
                            humidityConsumer.consume(event);
                            logger.info(String.format("Humidity %.2f ", message.getState().getValue()/100.0));
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
