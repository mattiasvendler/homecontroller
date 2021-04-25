package se.vendler.conbee;

import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OnOffParser extends Thread{
    private BlockingQueue<String> in = new ArrayBlockingQueue<String>(10);
    private Gson parser;
    private Logger logger = Logger.getLogger(this.getName());
    public OnOffParser(Gson parser) {
        this.parser = parser;
    }
    public void parse(String data) {
        in.add(data);
    }
    @Override
    public void run() {
        while(true) {
            try {
                String data = in.take();
                OnOffMessage onoff = parser.fromJson(data, OnOffMessage.class);
                if (onoff != null && onoff.getState() != null && onoff.complete()) {
                    onoff.extract();
                    logger.info(String.format("Message parsed onoff %s deviceId %d",onoff.getState().getPresence(), onoff.getDeviceId()));
                    continue;
                }
                ButtonMessage buttonMessage = parser.fromJson(data, ButtonMessage.class);
                if (buttonMessage != null && buttonMessage.getState() != null && buttonMessage.complete()) {
                    buttonMessage.extract();
                    logger.info(String.format("Message parsed button state %d deviceId %d", buttonMessage.getState().getButtonState(),buttonMessage.getDeviceId()));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch(NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
