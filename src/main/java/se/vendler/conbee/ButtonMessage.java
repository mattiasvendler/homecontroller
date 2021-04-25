package se.vendler.conbee;

import se.vendler.conbee.state.ButtonState;

public class ButtonMessage extends ConbeeBasicMessage{
    private ButtonState state;

    public ButtonState getState() {
        return state;
    }

    public void setState(ButtonState state) {
        this.state = state;
    }

    @Override
    public boolean complete() {
        return state != null && state.getButtonState() != null;
    }
}
