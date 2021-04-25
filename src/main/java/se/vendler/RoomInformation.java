package se.vendler;

import static se.vendler.FloorheatState.FLOORHEAT_STATE_OFF;

/**
 * Created by mattias on 2017-10-10.
 */
class RoomInformation {
    private int roomId;
    private float targetTemperature;
    private FloorheatState state = FLOORHEAT_STATE_OFF;
    private long sensorId;
    private String name;
    private boolean exercising;
    private boolean inverted;

    public RoomInformation(int roomId, long sensorId, float targetTemperature, FloorheatState state) {
        this.roomId = roomId;
        this.sensorId = sensorId;
        this.targetTemperature = targetTemperature;
        this.state = state;
        this.exercising = false;
    }

    public RoomInformation(int roomId, long sensorId, float targetTemperature) {
        this(roomId, sensorId, targetTemperature, FLOORHEAT_STATE_OFF);
    }
    
    public boolean isExercising() {
        return exercising;
    }
    
    public void setExercising(boolean exercising) {
        this.exercising = exercising;
    }
    
    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public float getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(float targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public FloorheatState getState() {
        return state;
    }

    public void setState(FloorheatState state) {
        this.state = state;
    }

    public long getSensorId() {
        return sensorId;
    }

    public void setSensorId(long sensorId) {
        this.sensorId = sensorId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setInverted(boolean inverted){
        this.inverted = inverted;
    }
    public boolean isInverted() {
        return inverted;
    }
}
