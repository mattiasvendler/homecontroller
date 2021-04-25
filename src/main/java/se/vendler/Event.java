package se.vendler;

import java.util.Date;

/**
 * Created by mattias on 2017-02-06.
 */
public class Event {
    private long sourceId;
    private long targetId;
    private String eventType;
    private Date date;

    public long getSourceId() {
        return sourceId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setSourceId(long sourceId) {
        this.sourceId = sourceId;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
