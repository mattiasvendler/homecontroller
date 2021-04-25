package se.vendler.conbee;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.math.BigInteger;

public class ConbeeBasicMessage {
    protected String uniqueid;
    private Long deviceId;
    private Short sensor;
    private Short type;
    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public Short getSensor() {
        return sensor;
    }

    public Short getType() {
        return type;
    }

    public void extract(){
        String[] s1 = uniqueid.split("-");
        if(s1.length >= 1) {
            String[] s2 = s1[0].split(":");
            StringBuilder sb = new StringBuilder();
            for(String b : s2) {
                sb.append(b);
            }
            try {
                byte[] b = Hex.decodeHex(sb.toString());
                deviceId = 0L;
                deviceId = new BigInteger(b).longValue();
            } catch (DecoderException decoderException) {
                decoderException.printStackTrace();
            }

        }
        if (s1.length >= 2) {
            sensor = Short.valueOf(s1[1]);
        }

        if (s1.length >= 3) {
            type = Short.valueOf(s1[2]);
        }
    }

    public boolean complete() {
        return false;
    }
}
