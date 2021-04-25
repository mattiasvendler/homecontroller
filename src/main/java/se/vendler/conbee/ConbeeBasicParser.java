package se.vendler.conbee;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import se.vendler.HumidityConsumer;
import se.vendler.TemperatureConsumer;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ConbeeBasicParser extends Thread{
    private BlockingQueue<String> in = new ArrayBlockingQueue<String>(10);
    private Gson gson = new Gson();
    private Logger logger = Logger.getLogger(this.getName());
    private OnOffParser onOffParser;
    private EnvironmentParser environmentParser;
    private TemperatureConsumer temperatureConsumer;
    private HumidityConsumer humidityConsumer;

    public ConbeeBasicParser() {
        onOffParser = new OnOffParser(gson);
        environmentParser = new EnvironmentParser(gson);
    }

    public ConbeeBasicParser(TemperatureConsumer temperatureConsumer, HumidityConsumer humidityConsumer) {
        this();
        this.temperatureConsumer = temperatureConsumer;
        this.humidityConsumer = humidityConsumer;
    }

    public void parse(String message) {
        in.add(message);
    }
    @Override
    public void run() {
        onOffParser.start();
        environmentParser.setHumidityConsumer(humidityConsumer);
        environmentParser.setTemperatureConsumer(temperatureConsumer);
        environmentParser.start();

        while(true) {
            ConbeeBasicMessage message = null;
            try {
                String data = in.take();
                message = gson.fromJson(data, ConbeeBasicMessage.class);

                if (message != null) {
                    message.extract();
                    logger.info(String.format("Message parsed, deviceid: %d sensor: %d type %d",message.getDeviceId(), message.getSensor(), message.getType()));

                    switch(message.getType()) {
                        case 6:
                        case 406:
                            onOffParser.parse(data);
                            break;
                        case 402:
                            environmentParser.parse(data);
                            break;
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
}
