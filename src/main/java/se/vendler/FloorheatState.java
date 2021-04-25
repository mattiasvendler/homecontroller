package se.vendler;

/**
 * Created by mattias on 2017-10-12.
 */
enum FloorheatState {
    FLOORHEAT_STATE_ON(true),
    FLOORHEAT_STATE_OFF(false);
    
    private boolean on;
    
    private FloorheatState(boolean on){
        this.on = on;
    }
    
    public boolean isOn() {
        return on;
    }
}
