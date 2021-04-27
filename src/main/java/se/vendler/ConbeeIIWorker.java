package se.vendler;

import com.rabbitmq.client.*;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vendler.conbee.ConbeeBasicParser;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ConbeeIIWorker extends Thread{
    private ConnectionFactory connectionFactory;
    private Channel channel;
    private Connection connection;
    private Consumer consumer;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExecutorService executorService = new ScheduledThreadPoolExecutor(5);
    private final ConbeeBasicParser conbeeBasicParser;
    public ConbeeIIWorker(HumidityConsumer humidityConsumer, TemperatureConsumer temperatureConsumer){

        this.conbeeBasicParser = new ConbeeBasicParser(temperatureConsumer, humidityConsumer);
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
                            executorService.submit(() -> {
                                setName("Parser thread");
                                conbeeBasicParser.parse(message);
                            });
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
