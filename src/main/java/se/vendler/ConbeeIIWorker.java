package se.vendler;

import com.rabbitmq.client.*;
import org.apache.log4j.Logger;
import se.vendler.conbee.ConbeeBasicParser;

import java.io.IOException;

public class ConbeeIIWorker extends Thread{
    private ConnectionFactory connectionFactory;
    private Channel channel;
    private Connection connection;
    private Consumer consumer;
    private Logger logger = Logger.getLogger(this.getClass());
    private ConbeeBasicParser conbeeBasicParser;
    private HumidityConsumer humidityConsumer;
    private TemperatureConsumer temperatureConsumer;
    public ConbeeIIWorker(HumidityConsumer humidityConsumer, TemperatureConsumer temperatureConsumer){

        conbeeBasicParser = new ConbeeBasicParser(temperatureConsumer, humidityConsumer);
        conbeeBasicParser.start();
    }

    private void connectToBus() {
        if(channel == null) {
            try {
                connectionFactory = new ConnectionFactory();
                connectionFactory.setHost("192.168.1.181");
                connectionFactory.setPort(5672);
//                connectionFactory.setUsername("conbee");
//                connectionFactory.setPassword("conbee");
//                connectionFactory.setVirtualHost("storage-collector-dev");
                connection = connectionFactory.newConnection();

                channel = connection.createChannel();
                String queue = channel.queueDeclare().getQueue();
                logger.info(queue);
                channel.queueBind(queue, "exchange.conbeeII", "");
                if (consumer == null) {
                    consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            String message = new String(body);
                            logger.info(message);
                            conbeeBasicParser.parse(message);
                            channel.basicAck(envelope.getDeliveryTag(),false);
                        }
                    };
                    channel.basicConsume(queue, consumer);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    @Override
    public void run(){
        connectToBus();

        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
