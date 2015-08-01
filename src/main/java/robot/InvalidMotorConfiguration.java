package robot;

public class InvalidMotorConfiguration extends Exception
{
	private static final long serialVersionUID = 1L;

	private String message;

	public InvalidMotorConfiguration(Motor motor, String error) 
	{
		message = "Error: " + error + " " + motor.toString() ;

	}

	public String getMessage()
	{
		return message;
	}

}
