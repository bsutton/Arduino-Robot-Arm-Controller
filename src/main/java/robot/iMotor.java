package robot;

// Read only interface to the motor.
public interface iMotor
{
	int getPin();
	String getName();
	int getMax();
	int getMin();
	int getCurrent();
}
