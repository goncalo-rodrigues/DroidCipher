package pt.ulisboa.tecnico.sirs.droidcipher.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

import java.util.Date;

import pt.ulisboa.tecnico.sirs.droidcipher.Constants;
import pt.ulisboa.tecnico.sirs.droidcipher.R;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.Connection;
import pt.ulisboa.tecnico.sirs.droidcipher.Services.Events;

/**
 * Created by goncalo on 18-11-2016.
 */

public class Event extends SugarRecord implements Parcelable, Comparable {
    private String description;
    private Date eventDate;
    private int icon;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            Event e = (Event) obj;
            return eventDate.equals(e.getEventDate()) && description.equals(e.getDescription());
        }
        return false;
    }

    public Event(int eventID, Connection conn) {
        icon = -1;
        description = "";
        switch(eventID) {
            case Events.SERVICE_STARTED:
                description = "Service started running";
                icon = Constants.ICON_GOOD;
                break;
            case Events.SERVICE_STOPPED:
                description = "Service stopped running";
                icon = Constants.ICON_GOOD;
                break;
            case Events.ACCEPTED_CONNECTION:
                description = "Connection established with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +")";
                icon = Constants.ICON_GOOD;
                break;
            case Events.NEW_CONNECTION_REQUEST:
                description = "New connection request from " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +")";
                icon = Constants.ICON_GOOD;
                break;
            case Events.FILE_DECRYPT_REQUEST:
                description = "File decryption request from " + conn.getDevice().getName();
                icon = Constants.ICON_GOOD;
                break;
            case Events.CONNECTION_LOST:
                description = "Connection with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +") was dropped";
                icon = Constants.ICON_LIGHTNING;
                break;
            case Events.NEW_DEVICE_ADDED:
                description = "QRCode was read and a new device has been added";
                icon = Constants.ICON_GOOD;
                break;
            case Events.REJECTED_CONNECTION:
                description = "Connection with " + conn.getDevice().getName() +
                        " (ID:" + conn.getConnectionId() +") rejected";
                icon = Constants.ICON_DENY;
                break;
            case Events.FAILED_CONNECTION_REQUEST:
                description = "An incoming connection request from " +
                        conn.getDevice().getName() + " failed. Have you added this device yet?";
                icon = Constants.ICON_DENY;
                break;
            case Events.FAILED_FILE_DECRYPT_REQUEST:
                description = "A file decrypt request from " + conn.getDevice().getName() +
                        " has failed";
                icon = Constants.ICON_DENY;
                break;
            default:
                description = "Unknown event";
                icon = Constants.ICON_GOOD;
                break;
        }

        eventDate = new Date();
    }

    public Event() {

    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.description);
        dest.writeLong(this.eventDate != null ? this.eventDate.getTime() : -1);
        dest.writeInt(this.icon);
    }

    protected Event(Parcel in) {
        this.description = in.readString();
        long tmpEventDate = in.readLong();
        this.eventDate = tmpEventDate == -1 ? null : new Date(tmpEventDate);
        this.icon = in.readInt();
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel source) {
            return new Event(source);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int compareTo(Object o) {
        if (o instanceof Event) {
            return eventDate.compareTo(((Event) o).getEventDate());
        }
        return -1;
    }
}
