package robot;

public class InvalidMotorException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private String motor;
	
	public InvalidMotorException(int motor)
	{
		this.motor = "" + motor;
	}
	public InvalidMotorException(String motor)
	{
		this.motor = motor;
	}
	public String getMessage()
	{
		return "Invalid Motor no.: " + motor;
	}
}
