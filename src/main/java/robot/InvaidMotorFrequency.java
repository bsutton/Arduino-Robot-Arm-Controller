package robot;

public class InvaidMotorFrequency extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private String message;

	public InvaidMotorFrequency(Motor motor, String error, int frequency)
	{
		message = "Error: " + error + " frequency; " + frequency + " " + motor.toString(); 
				
	}
	
	public String getMessage()
	{
		return message;
	}

}
