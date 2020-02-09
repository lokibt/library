package dk.itu.android.bluetooth.emulation.cmd;

public enum CommandType {
	JOIN(0),
	LEAVE(1),
	DISCOVERY(2),
	ADDSERVICE(3),
	REMOVESERVICE(4),
	CONNECT(5),
	LINK(6);
	
	int intRepr;

	CommandType(int intRepr) {
		this.intRepr = intRepr;
	}
}
