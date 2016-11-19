package pt.ulisboa.tecnico.sirs.droidcipher.data;

import java.util.Date;

/**
 * Created by goncalo on 18-11-2016.
 */

public class Event {
    private String description;
    private Date eventDate = new Date();
    private int icon = -1;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
