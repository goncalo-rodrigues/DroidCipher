package pt.ulisboa.tecnico.sirs.droidcipher.Services;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goncalo on 17-11-2016.
 */

public class ServiceState implements Parcelable {
    public boolean isOn;
    public boolean isWaitingUser;
    public boolean isConnected;
    public BluetoothDevice currentConnection;

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isWaitingUser() {
        return isWaitingUser;
    }

    public void setWaitingUser(boolean waitingUser) {
        isWaitingUser = waitingUser;
    }

    public BluetoothDevice getCurrentConnection() {
        return currentConnection;
    }

    public void setCurrentConnection(BluetoothDevice currentConnection) {
        this.currentConnection = currentConnection;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isOn ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isWaitingUser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isConnected ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.currentConnection, flags);
    }

    public ServiceState() {
    }

    protected ServiceState(Parcel in) {
        this.isOn = in.readByte() != 0;
        this.isWaitingUser = in.readByte() != 0;
        this.isConnected = in.readByte() != 0;
        this.currentConnection = in.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public static final Parcelable.Creator<ServiceState> CREATOR = new Parcelable.Creator<ServiceState>() {
        @Override
        public ServiceState createFromParcel(Parcel source) {
            return new ServiceState(source);
        }

        @Override
        public ServiceState[] newArray(int size) {
            return new ServiceState[size];
        }
    };
}
