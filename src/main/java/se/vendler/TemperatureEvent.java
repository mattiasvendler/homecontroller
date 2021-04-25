package se.vendler;

/**
 * Created by mattias on 2017-10-10.
 */
public class TemperatureEvent extends EnvironmentEvent {
    private float temperature;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

}
