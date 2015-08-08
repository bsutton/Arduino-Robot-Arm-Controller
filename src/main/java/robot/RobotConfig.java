package robot;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RobotConfig
{

	private static final String NAME = "Name";
	private static final String PIN = "Pin";
	private static final String MIN_PWM = "Min";
	private static final String MAX_PWM = "Max";
	private static final String CURRENT = "Current";
	private static final String LAST_SAVE_DIRECTORY = "LastSaveDirectory";
	private static final String MIN_ANGLE = "MinAngle";
	private static final String MAX_ANGLE = "MaxAngle";

	private File lastSaveDirectory = new File(".");

	private Motor[] motors = new Motor[Robot.MAX_MOTORS];

	RobotConfig()
	{
		for (int i = 0; i < Robot.MAX_MOTORS; i++)
			motors[i] = new Motor(i);
	}

	Motor getMotor(int motor) throws InvalidMotorException
	{
		if (motor < 0 || motor >= Robot.MAX_MOTORS)
			throw new InvalidMotorException(motor);

		return motors[motor];
	}

	public void save()
	{
		// Retrieve the user preference node for the package com.mycompany
		Preferences prefs = Preferences.userNodeForPackage(RobotConfig.class);

		prefs.put(LAST_SAVE_DIRECTORY, lastSaveDirectory.getAbsolutePath());

		for (int i = 0; i < Robot.MAX_MOTORS; i++)
		{
			// Set the value of the preference
			prefs.put(NAME + ":" + i, motors[i].getName());
			prefs.putInt(PIN + ":" + i, motors[i].getPin());
			prefs.putDouble(MIN_PWM + ":" + i, motors[i].getMinPwm());
			prefs.putDouble(MIN_ANGLE + ":" + i, motors[i].getMinAngle());
			prefs.putDouble(MAX_PWM + ":" + i, motors[i].getMaxPwm());
			prefs.putDouble(MAX_ANGLE + ":" + i, motors[i].getMaxAngle());
			prefs.putDouble(CURRENT + ":" + i, motors[i].getCurrentPWM());
		}
	}

	public void load() throws BackingStoreException
	{
		// Retrieve the user preference node for the package com.mycompany
		Preferences prefs = Preferences.userNodeForPackage(RobotConfig.class);

		lastSaveDirectory = new File(prefs.get(LAST_SAVE_DIRECTORY, "."));

		for (int i = 0; i < Robot.MAX_MOTORS; i++)
		{
			// Set the value of the preference
			motors[i].setName(prefs.get(NAME + ":" + i, ""));
			motors[i].setPin(prefs.getInt(PIN + ":" + i, i));
			motors[i].setMinPWM(prefs.getDouble(MIN_PWM + ":" + i, 0));
			motors[i].setMaxPWM(prefs.getDouble(MAX_PWM + ":" + i, 0));
			motors[i].setCurrentPWM(prefs.getInt(CURRENT + ":" + i, 0));
		}
		prefs.flush();
	}

	public Motor[] getMotors()
	{
		return motors;
	}

	public File getLastSaveDirectory()
	{
		return lastSaveDirectory;
	}

	public void setLastSaveDirectory(File lastSaveDirectory)
	{
		this.lastSaveDirectory = lastSaveDirectory;
	}

	public Motor getMotor(String motor) throws InvalidMotorException
	{
		int motorInt = -1;
		try
		{
			motorInt = Integer.valueOf(motor);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidMotorException(motor);
		}

		return getMotor(motorInt);

	}

	public boolean hasMotorMoved()
	{
		boolean moved = false;
		for (Motor motor : motors)
		{
			if (motor.hasMoved())
			{
				moved = true;
				break;
			}
		}
		return moved;
	}

}
