package se.vendler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rabbitmq.client.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Created by mattias on 2017-10-10.
 */
public class EnvironmentWorker {
    public static final String EXCHANGE_ENVIRONMENT = "se.vendler.exchange.environment";
    public static final String NODE_ENVIRONMENT = "se.vendler.radio.node.environment";
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Logger logger = Logger.getLogger(this.getClass());
    private Gson gson;
    List<TemperatureConsumer> temperatureConsumers;
    List<TargetTemperatureConsumer> targetTemperatureConsumers;
    List<HumidityConsumer> humidityConsumers;

    public EnvironmentWorker() throws IOException, TimeoutException {

        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.1.181");
        connectionFactory.setPort(5672);
        connection = connectionFactory.newConnection();
        gson = new Gson();
        temperatureConsumers = new ArrayList<>();
        humidityConsumers = new ArrayList<>();
        targetTemperatureConsumers = new ArrayList<>();
    }

    public void addTemperatureConsumer(TemperatureConsumer eventConsumer) {
        temperatureConsumers.add(eventConsumer);
    }

    public void addTargetTemperatureConsumer(TargetTemperatureConsumer eventConsumer) {
        targetTemperatureConsumers.add(eventConsumer);
    }

    public void addHumidityConsumer(HumidityConsumer eventConsumer) {
        humidityConsumers.add(eventConsumer);
    }


    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare(EXCHANGE_ENVIRONMENT, "direct");
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_ENVIRONMENT, NODE_ENVIRONMENT);
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            String messageBody = new String(body, "UTF-8");

                            logger.info(String.format("Got message: %s", messageBody));
                            try {
                                EnvironmentEvent event = gson.fromJson(messageBody, EnvironmentEvent.class);
                                switch (event.getType()) {
                                    case TEMPERATURE:
                                        fireTemperatureEvent(event);
                                        break;
                                    case HUMIDITY:
                                        fireHumidityEvent(event);
                                        break;
                                    case TEMPERATURE_UPDATE:
                                        logger.info("Update target temperature for sensor " + event.getSourceId() + " to " + event.getValue());
                                        fireTargetTemperatureEvent(event);
                                        break;
                                }
                            } catch (JsonSyntaxException e) {
                                logger.error(e.getMessage());
                            } catch (Exception e) {
                                logger.error(e.getMessage());
                            }
                        }
                    };
                    channel.basicConsume(queueName, true, consumer);
                } catch (IOException e) {
                    logger.error(e.getMessage());
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                }

            }
        }).start();

    }

    private void fireTargetTemperatureEvent(EnvironmentEvent event) {
        for (TargetTemperatureConsumer c : targetTemperatureConsumers) {
            logger.info("Fire target temperature event to consumer " + c.getClass().getSimpleName());
            c.consume(event);
        }
    }

    private void fireTemperatureEvent(EnvironmentEvent event) {
        for (TemperatureConsumer c : temperatureConsumers) {
            logger.info("Fire temperature event to consumer " + c.getClass().getSimpleName());
            c.consume(event);
        }
    }

    private void fireHumidityEvent(EnvironmentEvent event) {
        for (HumidityConsumer c : humidityConsumers) {
            logger.info("Fire humidity event to consumer " + c.getClass().getSimpleName());
            c.consume(event);
        }
    }
}
