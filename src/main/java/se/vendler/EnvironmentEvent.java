package se.vendler;

public class EnvironmentEvent extends Event {
	private float value;
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public EnvironmentType getType(){
		return EnvironmentType.valueOf(getEventType());
	}
}
