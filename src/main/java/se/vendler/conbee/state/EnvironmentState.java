package se.vendler.conbee.state;

import com.google.gson.annotations.SerializedName;

public class EnvironmentState extends ConbeeIIBasicState{
    @SerializedName(value = "value", alternate = {"temperature", "humidity"})
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

}
