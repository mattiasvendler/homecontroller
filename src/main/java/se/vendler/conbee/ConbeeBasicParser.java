package se.vendler.conbee;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vendler.HumidityConsumer;
import se.vendler.TemperatureConsumer;

public class ConbeeBasicParser extends Thread {
    private Gson gson = new Gson();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private TemperatureConsumer temperatureConsumer;
    private HumidityConsumer humidityConsumer;
    private OnOffParser onOffParser;
    private EnvironmentParser environmentParser;
    public ConbeeBasicParser(TemperatureConsumer temperatureConsumer, HumidityConsumer humidityConsumer) {
        this.temperatureConsumer = temperatureConsumer;
        this.humidityConsumer = humidityConsumer;
        onOffParser = new OnOffParser(gson);
        environmentParser = new EnvironmentParser(gson, temperatureConsumer, humidityConsumer);

    }

    public synchronized void parse(String data) {
        ConbeeBasicMessage message = null;
        try {
            message = gson.fromJson(data, ConbeeBasicMessage.class);

            if (message != null) {
                message.extract();
                logger.info(String.format("Message parsed, deviceid: %d sensor: %d type %d", message.getDeviceId(), message.getSensor(), message.getType()));

                switch (message.getType()) {
                    case 6:
                    case 406:
                        onOffParser.parse(data);
                        break;
                    case 402:
                    case 405:
                        environmentParser.parse(data);
                        break;

                }
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }
}
