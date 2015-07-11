package robotarm;

public interface iCmdRunner
{

	void sendCmd(String cmd) throws NotConnectedException;

	boolean isConnected();

}
