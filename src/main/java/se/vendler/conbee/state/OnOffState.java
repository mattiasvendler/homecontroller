package se.vendler.conbee.state;

import com.google.gson.annotations.SerializedName;

public class OnOffState extends ConbeeIIBasicState{
    private Boolean dark;
    @SerializedName(value = "presence", alternate = {"open"})
    private Boolean presence;

    public Boolean getDark() {
        return dark;
    }

    public void setDark(Boolean dark) {
        this.dark = dark;
    }

    public Boolean getPresence() {
        return presence;
    }

    public void setPresence(Boolean presence) {
        this.presence = presence;
    }
}
