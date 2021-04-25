package se.vendler;

/**
 * Created by mattias on 2017-10-10.
 */
public interface EventConsumer {
    void consume(Event event);
    void consume(FloorheatUpdate floorheatUpdate);
}
