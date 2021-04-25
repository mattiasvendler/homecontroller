package se.vendler;

public class HumidityEvent extends EnvironmentEvent {
	private float humidity;
	
	public float getHumidity() {
		return humidity;
	}
	
	public void setHumidity(float humidity) {
		this.humidity = humidity;
	}
}
