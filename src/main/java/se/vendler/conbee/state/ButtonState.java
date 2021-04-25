package se.vendler.conbee.state;

import com.google.gson.annotations.SerializedName;

public class ButtonState extends ConbeeIIBasicState{
    @SerializedName("buttonevent")
    private Integer buttonState;

    public Integer getButtonState() {
        return buttonState;
    }

    public void setButtonState(Integer buttonState) {
        this.buttonState = buttonState;
    }
}
