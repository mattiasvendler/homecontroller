package se.vendler.conbee;

import se.vendler.conbee.state.OnOffState;

public class OnOffMessage extends ConbeeBasicMessage{
    private OnOffState state;

    public OnOffState getState() {
        return state;
    }

    public void setState(OnOffState state) {
        this.state = state;
    }

    @Override
    public boolean complete() {
        return state != null && state.getPresence() != null;
    }
}
