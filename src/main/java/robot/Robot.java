package robot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;
import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import robotarm.iDisplay;
import robotics.Pose;
import robotics.Unbranded6dof.ServoCalculator;

public class Robot implements iRobot
{
	Logger logger = LogManager.getLogger();

	static final int MAX_MOTORS = 16;

	private volatile boolean stopping = false;

	private FileInputStream fis;
	private static HashMap<String, String> variables = new HashMap<>();

	private boolean isConnected = false;

	private File serialDevice;

	private RobotConfig configuration = new RobotConfig();

	private FileOutputStream fosDevice;

	private iDisplay display;

	public Robot(iDisplay display) throws BackingStoreException
	{
		this.display = display;

		configuration.load();

		// Set initial motor name to pin variables

		for (Motor motor : configuration.getMotors())
		{
			if (motor.getName().length() > 0)
				variables.put(motor.getName(), "" + motor.getPin());
		}
	}

	public void connect(String port) throws IOException, NotConnectedException
	{

		if (port != null)
		{
			String[] cmd =
			// { "/bin/sh", "-c", "stty raw -F " + port };

			{
					"/bin/sh",
					"-c",
					"stty raw -F /dev/"
							+ port
							+ " cs8 115200 ignbrk -brkint -icrnl -imaxbel -opost -onlcr -isig -icanon -iexten -echo -echoe -echok -echoctl -echoke noflsh -ixon -crtscts" };

			// +
			// " -parenb -parodd -cmspar cs8 hupcl -cstopb cread clocal -crtscts"
			// +
			// " -ignbrk -brkint -ignpar -parmrk -inpck -istrip -inlcr -igncr -icrnl -ixon -ixoff -iuclc -ixany -imaxbel"
			// + " -iutf8"
			// +
			// " -opost -olcuc -ocrnl -onlcr -onocr -onlret -ofill -ofdel nl0 cr0 tab0 bs0 vt0 ff0"
			// +
			// " -isig -icanon -iexten -echo -echoe -echok -echonl noflsh -xcase -tostop -echoprt -echoctl -echoke"
			// };

			Runtime.getRuntime().exec(cmd);

			// the stty command seems to lock the port for a moment
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				// won't happen
			}

			File portFile = new File("/dev", port);
			start(portFile);
			fosDevice = new FileOutputStream(portFile);
		}
		else
			JOptionPane.showMessageDialog(null, "Please select a port first.");

	}

	/**
	 * Start the robot listener thread and connect to the robot.
	 * 
	 * We are connecting via the serial port using a tty device (e.g.
	 * /dev/ttyUSB0.
	 * 
	 * We do this because its easier than getting the crappy java serial io
	 * libraries working.
	 * 
	 * @param serialPortDevice
	 * @throws FileNotFoundException
	 */
	private synchronized void start(File serialPortDevice) throws FileNotFoundException
	{
		this.serialDevice = serialPortDevice;
		this.stopping = false;

		try
		{
			fis = new FileInputStream(serialPortDevice);
			this.isConnected = true;
		}
		catch (FileNotFoundException e)
		{
			logger.error("Serial port {} not found.", this.serialDevice.getAbsolutePath());
			throw e;
		}
	}

	/**
	 * Stop the robot listener thread and close the connection to the robot.
	 */
	public synchronized void stop()
	{
		try
		{
			this.isConnected = false;
			stopping = true;
			if (fis != null)
				fis.close();
			if (fosDevice != null)
				fosDevice.close();
		}
		catch (IOException e)
		{
			logger.warn("Error {} closing serial device {}", e.getMessage(), this.serialDevice.getAbsoluteFile());
		}
	}

	public void sendCmd(String originalCmd, iDisplay display) throws NotConnectedException, IOException,
			TimeoutException, InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration,
			IllegalCommandException
	{
		originalCmd = originalCmd.trim();
		// make certain the cmd doesnt' have a newline at this point as it
		// confuses the tokenisation.
		if (originalCmd.endsWith("\n"))
			originalCmd = originalCmd.substring(0, originalCmd.length() - 1);

		String cmd = preprocessCommand(originalCmd, display);
		if (cmd != null)
		{
			display.append("Send:" + originalCmd + "\n");

			if (!interceptMoves(cmd))
				sendToRobot(cmd);
		}
	}

	private String preprocessCommand(String cmd, iDisplay display) throws InvalidMotorException,
			IllegalCommandException
	{
		// remove all spaces from the command
		cmd.replaceAll("\\s", "");

		if (cmd.length() == 0)
			return null;

		// ignore comment lines
		if (cmd.startsWith("//"))
		{
			return null;
		}
		else if (cmd.startsWith("set"))
		{
			String[] tokens = cmd.split("[,]");

			if (tokens.length != 3)
				display.append("Invalid set command: **** " + cmd + "****\n");
			else
			{
				String varName = tokens[1];
				String varValue = tokens[2];
				variables.put(varName, varValue);
			}
			cmd = null;
		}
		else if (cmd.startsWith("on"))
		{
			cmd = substituteVariables(cmd);
			String[] tokens = cmd.split("[,]");

			// Substitute 'on' for moving the motor to its current known
			// position.
			Motor motor = configuration.getMotor(tokens[1]);
			int current = motor.getCurrentPWM();
			if (motor.isValidPWM(current))
				cmd = "mov," + tokens[1] + "," + current;
			else
			{
				double difference = motor.getMaxPwm() - motor.getMinPwm();
				double halfway = difference / 2 + motor.getMinPwm();
				cmd = "mov," + tokens[1] + "," + (int) halfway;
			}
		}
		else if (cmd.startsWith("pose"))
		{
			cmd = substituteVariables(cmd);
			String[] tokens = cmd.split("[,]");

			if (tokens.length != 4)
				throw new IllegalCommandException("Pose MUST have three arguments: " + cmd);

			ServoCalculator calculator = new ServoCalculator(configuration.getMotor("base"),
					configuration.getMotor("shoulder"), configuration.getMotor("elbow"));
			calculator.setPosition(new Pose(Integer.valueOf(tokens[1]), Integer.valueOf(tokens[2]), Integer
					.valueOf(tokens[3]), 0, 0, 0));
			double turrent = calculator.getBasePwm();
			double shoulder = calculator.getShoulderPwm();
			double elbow = calculator.getElbowPwm();

			// Substitute 'on' for moving the motor to its current known
			// position.
			cmd = "pose," + calculator.getBaseMotor().getPin() + "," + turrent + ","
					+ calculator.getShoulderMotor().getPin() + "," + shoulder + ","
					+ calculator.getElbowMotor().getPin() + "," + elbow;
		}

		else
		{
			cmd = substituteVariables(cmd);

		}
		return cmd;
	}

	// We intercept mov command so we can track the robots current location.
	private boolean interceptMoves(String cmd) throws NotConnectedException, IOException, TimeoutException,
			InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException
	{
		boolean intercepted = false;

		if (cmd.startsWith("mov"))
		{
			String[] tokens = cmd.split("[,]");
			if (tokens.length < 3)
				throw new IllegalCommandException("The command does not have the correct number of arguments:" + cmd);
			moveMotor(tokens[1], tokens[2]);
			intercepted = true;
		}

		return intercepted;
	}

	void sendToRobot(String message) throws NotConnectedException, IOException, TimeoutException
	{
		if (!isConnected)
			throw new NotConnectedException();

		String response = null;
		if (fosDevice != null)
		{
			// ensure last character is always a newline
			if (!message.endsWith("\n"))
			{
				logger.debug("Cmd sent with no newline:" + message);
				message += "\n";
			}

			// dont' send blank lines
			if (message.length() == 1)
				return;

			fosDevice.write(message.getBytes());
			fosDevice.flush();
			logger.error("Sending: {}", message);

			// await the response
			response = readLineFully();
			display.append("Recv:" + response + "\n");

		}
		else
			throw new NotConnectedException();
	}

	private String readLineFully() throws IOException // , StoppingException
			, TimeoutException, NotConnectedException
	{

		if (!isConnected)
			throw new NotConnectedException();

		StringBuilder line = new StringBuilder("");
		boolean lineFound = false;

		Calendar timeout = Calendar.getInstance();
		timeout.add(Calendar.SECOND, 5);

		while (!stopping && !lineFound)
		{

			if (new Date().after(timeout.getTime()))
				throw new TimeoutException("Timeout occured waiting for response from robot.");

			int sleepcounter = 0;
			while (fis.available() != 0)// && !stopping)
			{
				int in;

				in = fis.read();
				line.append((char) in);
				// logger.debug("read:" + in);
				if (in == '\n')
				{
					lineFound = true;
					break;
				}
			}
			try
			{
				sleepcounter++;
				if (sleepcounter % 10 == 0)
					logger.debug("slepping");
				Thread.sleep(50);
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
			}
		}
		logger.debug("Recv: " + line.toString());
		return line.toString();
	}

	void moveMotor(String motorPin, String frequency) throws NotConnectedException, IOException, TimeoutException,
			InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException
	{
		Motor motor = configuration.getMotor(motorPin);
		motor.move(this, display, frequency);
	}

	public ArrayList<String> getPorts()
	{

		ArrayList<String> ports = new ArrayList<>();

		File folder = new File("/dev");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles)
		{
			if (file.getName().startsWith("ttyACM") || file.getName().startsWith("cu.usb"))
			{
				ports.add(file.getName());
			}
		}
		return ports;

	}

	public void stopMotors() throws NotConnectedException, IOException, TimeoutException, InvalidMotorException,
			InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException
	{
		for (Motor motor : configuration.getMotors())
			motor.stop(this, display);

	}

	private String substituteVariables(String cmd)
	{
		String newCmd;
		String[] tokens = cmd.split("[,]");

		if (tokens.length > 0)
		{
			newCmd = tokens[0];
			// do substitution
			for (int i = 1; i < tokens.length; i++)
			{
				String value = variables.get(tokens[i]);
				if (value == null)
				{
					value = tokens[i];
				}
				newCmd += ",";
				newCmd += value;
			}
		}
		else
			newCmd = cmd;
		return newCmd;
	}

	@Override
	public boolean isConnected()
	{
		return isConnected;
	}

	public Motor[] getMotors()
	{
		return configuration.getMotors();
	}

	public void save()
	{
		configuration.save();

	}

	@Override
	public File getLastSaveDirectory()
	{
		return configuration.getLastSaveDirectory();
	}

	@Override
	public void setLastSaveDirectory(File currentDirectory)
	{
		configuration.setLastSaveDirectory(currentDirectory);

	}

	public RobotConfig getConfiguration()
	{
		// TODO Auto-generated method stub
		return configuration;
	}

}
