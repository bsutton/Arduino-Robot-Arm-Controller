package robot;

public class NotConnectedException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public String getMessage()
	{
		return "The robot is not connected.";
	}

}
