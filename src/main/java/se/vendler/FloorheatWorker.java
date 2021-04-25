package se.vendler;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class FloorheatWorker {
    public static final String EXCHANGE = "se.vendler.exchange.floorheat";
    public static final String KEY = "se.vendler.floorheat.update";
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Logger logger = Logger.getLogger(this.getClass());
    private Gson gson;
    List<EventConsumer> eventConsumers;

    public FloorheatWorker() throws IOException, TimeoutException {
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.1.181");
        connectionFactory.setPort(5672);
        connection = connectionFactory.newConnection();
        gson = new Gson();
        eventConsumers = new ArrayList<>();
    }

    public void addConsumer(EventConsumer eventConsumer){
        eventConsumers.add(eventConsumer);
    }


    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare(EXCHANGE,"direct");
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE, KEY);
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            String messageBody = new String(body, "UTF-8");

                            logger.info(String.format("Got message: %s", messageBody));
                            FloorheatUpdate event = gson.fromJson(messageBody, FloorheatUpdate.class);
                            for(EventConsumer c : eventConsumers){
                                c.consume(event);
                            }
                        }
                    };
                    channel.basicConsume(queueName, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }


}
