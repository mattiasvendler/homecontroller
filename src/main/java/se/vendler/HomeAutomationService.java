package se.vendler;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class HomeAutomationService {
    static Logger logger = Logger.getLogger(HomeAutomationService.class);
    static Gson gson = new Gson();

    public static void main(String[] args) throws IOException, TimeoutException {
        FloorheatService floorheatService = new FloorheatService();

        EnvironmentWorker environmentWorker = new EnvironmentWorker();
        TemperatureConsumer temperatureConsumer = new TemperatureConsumer(floorheatService);
        environmentWorker.addTemperatureConsumer(temperatureConsumer);
        HumidityConsumer humidityConsumer = new HumidityConsumer();
        environmentWorker.addHumidityConsumer(humidityConsumer);
        environmentWorker.addTargetTemperatureConsumer(new TargetTemperatureConsumer());
        environmentWorker.start();

        FloorheatWorker floorheatWorker = new FloorheatWorker();
        floorheatWorker.addConsumer(new FloorheatUpdateConsumer(floorheatService));
        floorheatWorker.start();

        ConbeeIIWorker conbeeIIWorker = new ConbeeIIWorker(humidityConsumer, temperatureConsumer);
        conbeeIIWorker.start();

        logger.info("Started");
    }
}
