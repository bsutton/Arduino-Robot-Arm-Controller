package robot;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import robotarm.iDisplay;
import robotics.iMotor;

public class Motor implements iMotor
{
	// The pin the motor is attached to.
	private int pin;

	// name of the motor
	private String name;

	// max frequency for this motor
	private double maxPWM;

	// The maximum angle of the motor when it is at its maximum PWM
	private double maxAngle;

	// min frequency for this motor
	private double minPWM;

	// The min angle of the motor when it is at its min PWM
	private double minAngle;

	// the current location for the motor one known.
	private double currentPWM = 0;

	private boolean changed = false;

	public Motor(int pin)
	{
		this.pin = pin;
	}

	public int getPin()
	{
		return pin;
	}

	void setPin(int pin)
	{
		this.pin = pin;
	}

	public String getName()
	{
		return name;
	}

	void setName(String name)
	{
		this.name = name.trim();
	}

	public int getCurrentPWM()
	{
		return (int) currentPWM;
	}

	void setCurrentPWM(double current)
	{
		changed = true;
		this.currentPWM = current;
	}

	public void setPin(String text)
	{
		this.pin = Integer.valueOf(text);

	}

	public void setMinPWM(String text)
	{
		this.minPWM = Integer.valueOf(text);

	}

	public void setMinPWM(double minPwm)
	{
		this.minPWM = minPwm;

	}

	public void setMaxPWM(String text)
	{
		this.maxPWM = Double.valueOf(text);

	}

	public void setMaxPWM(double max)
	{
		this.maxPWM = max;

	}

	public void setCurrentPWM(String text)
	{
		changed = true;
		this.currentPWM = Double.valueOf(text);

	}

	public String toString()
	{
		return "Motor: " + name + " pin: " + pin + " min: " + minPWM + " max: " + maxPWM + " current: " + currentPWM;
	}

	public void move(Robot robot, iDisplay display, String frequencyString) throws InvaidMotorFrequency,
			InvalidMotorConfiguration, NotConnectedException, IOException, TimeoutException, InvalidMotorException,
			IllegalCommandException
	{
		int frequency = -1;

		try
		{
			frequency = Integer.valueOf(frequencyString);
		}
		catch (NumberFormatException e)
		{
			throw new InvaidMotorFrequency(this, "Invalid frequency passed", frequency);
		}

		// check frequency range
		if (this.minPWM == 0 && this.maxPWM == 0)
			throw new InvalidMotorConfiguration(this, "You have not set max/min for this motor.");

		if (frequency > this.maxPWM)
			throw new InvaidMotorFrequency(this, "The selected frequency is greater than the motors max.", frequency);

		if (frequency < this.minPWM)
			throw new InvaidMotorFrequency(this, "The selected frequency is less than the motors min.", frequency);

		robot.sendToRobot("mov," + this.getPin() + "," + frequency);

		changed = true;
		this.currentPWM = frequency;

	}

	public void stop(Robot robot, iDisplay display) throws NotConnectedException, IOException, TimeoutException,
			InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException
	{
		robot.sendCmd("stop," + this.pin, display);

	}

	public boolean hasMoved()
	{
		boolean hasMoved = changed;
		changed = false;

		return hasMoved;
	}

	@Override
	public double getMaxPwm()
	{
		return maxPWM;
	}

	@Override
	public double getMinPwm()
	{
		return minPWM;
	}

	@Override
	public double getMinAngle()
	{
		return minAngle;
	}

	@Override
	public double getMaxAngle()
	{
		return maxAngle;
	}

	public void setMinAngle(String text)
	{
		this.minAngle = Double.valueOf(text);

	}

	public void setMaxAngle(String text)
	{
		this.maxAngle = Double.valueOf(text);

	}

	public boolean isValidPWM(int PWM)
	{
		return PWM >= minPWM && PWM <= maxPWM;
	}
}
