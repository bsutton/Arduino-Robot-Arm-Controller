package robot;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import robotarm.iDisplay;

public interface iRobot
{

	void sendCmd(String cmd, iDisplay display) throws NotConnectedException, IOException, TimeoutException, InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException;

	boolean isConnected();

	File getLastSaveDirectory();

	void setLastSaveDirectory(File currentDirectory);


}
