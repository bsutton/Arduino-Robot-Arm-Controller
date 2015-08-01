package robot;

public class IllegalCommandException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	private String message;

	public IllegalCommandException(String error)
	{
		message = "Error: " + error ;
				
	}
	
	public String getMessage()
	{
		return message;
	}


}
