package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goncalo on 18-11-2016.
 */

public class Connection implements Parcelable {
    private BluetoothDevice device;
    private String connectionId;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public Connection(BluetoothDevice device, String connId) {
        this.device = device;
        this.connectionId = connId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.device, flags);
        dest.writeString(this.connectionId);
    }

    public Connection() {
    }

    protected Connection(Parcel in) {
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.connectionId = in.readString();
    }

    public static final Parcelable.Creator<Connection> CREATOR = new Parcelable.Creator<Connection>() {
        @Override
        public Connection createFromParcel(Parcel source) {
            return new Connection(source);
        }

        @Override
        public Connection[] newArray(int size) {
            return new Connection[size];
        }
    };
}
