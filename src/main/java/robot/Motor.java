package robot;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import robotarm.iDisplay;

public class Motor
{
	// The pin the motor is attached to.
	private int pin;
	
	// name of the motor
	private String name;

	// max frequency for this motor
	private int max;

	// min frequency for this motor
	private int min;

	// the current location for the motor one known.
	private int current = 0;

	private boolean changed=false;

	public Motor(int pin)
	{
		this.pin = pin;
	}

	int getPin()
	{
		return pin;
	}

	void setPin(int pin)
	{
		this.pin = pin;
	}

	String getName()
	{
		return name;
	}

	void setName(String name)
	{
		this.name = name.trim();
	}

	int getMax()
	{
		return max;
	}

	void setMax(int max)
	{
		this.max = max;
	}

	int getMin()
	{
		return min;
	}

	void setMin(int min)
	{
		this.min = min;
	}

	int getCurrent()
	{
		return current;
	}

	void setCurrent(Integer current)
	{
		changed = true;
		this.current = current;
	}

	public void setPin(String text)
	{
		this.pin = Integer.valueOf(text);
		
	}
	
	public void setMin(String text)
	{
		this.min = Integer.valueOf(text);
		
	}

	public void setMax(String text)
	{
		this.max = Integer.valueOf(text);
		
	}

	public void setCurrent(String text)
	{
		changed=true;
		this.current = Integer.valueOf(text);
		
	}
	
	public String toString()
	{
		return "Motor: " + name + " pin: " + pin + " min: " + min + " max: " + max + " current: " + current;
	}

	public void move(Robot robot, iDisplay display, String frequencyString) throws InvaidMotorFrequency, InvalidMotorConfiguration, NotConnectedException, IOException, TimeoutException, InvalidMotorException, IllegalCommandException
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
		if (this.min == 0 && this.max == 0)
			throw new InvalidMotorConfiguration(this, "You have not set max/min for this motor.");
		
		if (frequency > this.max)
			throw new InvaidMotorFrequency(this, "The selected frequency is greater than the motors max.", frequency);
		

		if (frequency < this.min)
			throw new InvaidMotorFrequency(this, "The selected frequency is less than the motors min.", frequency);

		robot.sendToRobot("mov," + this.getPin() + "," + frequency);

		changed=true;
		this.current = frequency;
		
	}

	public void stop(Robot robot, iDisplay display) throws NotConnectedException, IOException, TimeoutException, InvalidMotorException, InvaidMotorFrequency, InvalidMotorConfiguration, IllegalCommandException
	{
		robot.sendCmd("stop," + this.pin, display);
		
	}

	public boolean hasMoved()
	{
		boolean hasMoved=changed;
		changed=false;
		
		return hasMoved;
	}


}
