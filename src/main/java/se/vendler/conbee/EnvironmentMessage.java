package se.vendler.conbee;

import se.vendler.conbee.state.EnvironmentState;

public class EnvironmentMessage extends ConbeeBasicMessage{
    private EnvironmentState state;

    public EnvironmentState getState() {
        return state;
    }

    public void setState(EnvironmentState state) {
        this.state = state;
    }

    @Override
    public boolean complete() {
        return state != null && state.getValue()!=null;
    }
}
