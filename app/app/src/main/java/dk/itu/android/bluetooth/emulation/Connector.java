package dk.itu.android.bluetooth.emulation;

import android.os.Parcel;
import android.os.Parcelable;

public class Connector implements Parcelable {

	public static Parcelable.Creator<Connector> CREATOR = new Parcelable.Creator<Connector>() {
		@Override
		public Connector createFromParcel(Parcel source) {
			Connector out = new Connector();
			out.tcpPort = source.readInt();
			out.uuid = source.readString();
			return out;
		}
		@Override
		public Connector[] newArray(int size) {
			return new Connector[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(tcpPort);
		dest.writeString(uuid);
	}
	
	int tcpPort;
	String uuid;
	
	public Connector(){}
	public Connector(String uuid, int port) {
		this.uuid = uuid;
		this.tcpPort = port;
	}
	
	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public int getTcpPort() {
		return tcpPort;
	}
	public String getUuid() {
		return uuid;
	}

}
