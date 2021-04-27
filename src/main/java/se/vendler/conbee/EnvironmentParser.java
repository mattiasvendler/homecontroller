package se.vendler.conbee;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vendler.HumidityConsumer;
import se.vendler.HumidityEvent;
import se.vendler.TemperatureConsumer;
import se.vendler.TemperatureEvent;

public class EnvironmentParser {
    private Gson parser;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private HumidityConsumer humidityConsumer;
    private TemperatureConsumer temperatureConsumer;

    public EnvironmentParser(Gson parser, TemperatureConsumer temperatureConsumer, HumidityConsumer humidityConsumer) {
        this.parser = parser;
        this.humidityConsumer = humidityConsumer;
        this.temperatureConsumer = temperatureConsumer;
    }

    public void parse(String inMessage) {
        EnvironmentMessage message = parser.fromJson(inMessage, EnvironmentMessage.class);
        if (message != null && message.complete()) {
            message.extract();
            if (message.getType() == 402) {
                if (temperatureConsumer != null) {
                    TemperatureEvent event = new TemperatureEvent();
                    event.setTemperature(message.getState().getValue() / 100.0f);
                    event.setValue(message.getState().getValue() / 100.0f);
                    event.setSourceId(message.getDeviceId());
                    event.setTargetId(1);
                    temperatureConsumer.consume(event);
                    logger.info(String.format("Temperature %.2f C", message.getState().getValue() / 100.0));
                }
            } else if (message.getType() == 405) {
                if (humidityConsumer != null) {
                    HumidityEvent event = new HumidityEvent();
                    event.setHumidity(message.getState().getValue() / 100.0f);
                    event.setValue(message.getState().getValue() / 100.0f);
                    event.setSourceId(message.getDeviceId());
                    event.setTargetId(1);
                    humidityConsumer.consume(event);
                    logger.info(String.format("Humidity %.2f ", message.getState().getValue() / 100.0));
                }
            }
        }

    }
}
