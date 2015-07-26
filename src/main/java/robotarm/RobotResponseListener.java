package robotarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RobotResponseListener
{
	Logger logger = LogManager.getLogger();
	private volatile boolean stopping = false;

	private FileInputStream fis;
	private iDisplay display;

	private File serialDevice;

	private ExecutorService executor;

	RobotResponseListener(iDisplay display)
	{
		this.display = display;
	}

	/**
	 * Stop the robot listener thread and close the connection to the robot.
	 */
	public synchronized void stop()
	{
		try
		{
			stopping = true;
			executor.shutdown();
			boolean clean = executor.awaitTermination(60, TimeUnit.SECONDS);
			logger.info("Terminated {}", clean);
			fis.close();
		}
		catch (IOException e)
		{
			logger.warn("Error {} closing serial device {}", e.getMessage(), this.serialDevice.getAbsoluteFile());
		}
		catch (InterruptedException e)
		{
			logger.error("Interrupted exception awaiting termination of response listener.");
		}
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
	 */
	public synchronized void start(File serialPortDevice)
	{
		this.serialDevice = serialPortDevice;
		this.stopping = false;

		try
		{
			fis = new FileInputStream(serialPortDevice);

			executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				int in;
				try
				{
					while (!stopping)
					{
						StringBuilder line = new StringBuilder("");
						while (fis.available() != 0)
						{
							in = fis.read();
							line.append((char)in);
							if (in == '\n')
							{
								display.append(line.toString());
								line.setLength(0);
								line.trimToSize();
							}
						}
						try
						{
							Thread.sleep(200);
						}
						catch (Exception e)
						{
							// should never happen.
						}
					}
				}
				catch (IOException e)
				{
					logger.error("Error {} writing to serial port {}", e.getMessage(),
							this.serialDevice.getAbsolutePath());
				}

			});

		}
		catch (FileNotFoundException e)
		{
			logger.error("Serial port {} not found.", this.serialDevice.getAbsolutePath());
		}
	}

}
