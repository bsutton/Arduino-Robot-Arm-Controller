package robotarm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class RobotResponseListener implements Runnable
{
	Logger logger = LogManager.getLogger();

	private final AtomicReference<CountDownLatch> pauseLatch = new AtomicReference<>(new CountDownLatch(1));
	private final AtomicReference<CountDownLatch> pausedLatch = new AtomicReference<>(new CountDownLatch(1));


	private volatile boolean stopped = false;
	private FileInputStream fis;
	private iDisplay display;

	private File serialDevice;

	RobotResponseListener(iDisplay display)
	{
		this.display = display;
	}

	/**
	 * Stop the robot listener thread and close the connection to the robot.
	 */
	public void stop()
	{
		pauseThread();
		stopped = true;
		try
		{
			fis.close();
		}
		catch (IOException e)
		{
			logger.warn("Error {} closing serial device {}", e.getMessage(), this.serialDevice.getAbsoluteFile());
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
	public void start(File serialPortDevice)
	{
		this.serialDevice = serialPortDevice;

		pauseThread();
		try
		{
			fis = new FileInputStream(serialPortDevice);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Serial port {} not found.", this.serialDevice.getAbsolutePath());
		}

		resumeThread();

	}

	/**
	 * The main read loop where we read data from the robot over the serial port
	 * we have open.
	 */
	public void run()
	{
		while (!stopped)
		{

			checkForPaused();
			if (!stopped)
			{
				int in;
				try
				{
					while (fis.available() != 0)
					{
						in = fis.read();
						display.append(String.valueOf((char) in));
					}
				}
				catch (IOException e)
				{
					logger.error("Error {} writing to serial port {}",
							e.getMessage(), this.serialDevice.getAbsolutePath());
				}
			}
		}
	}

	private void checkForPaused()
	{
		try
		{
			// Reset the paused counter so that as soon as we tell the world that we are unpaused
			// we will be ready to be paused again.
			pausedLatch.set(new CountDownLatch(1));
			
			// signal that we are now paused
			pauseLatch.get().countDown();

			// wait until we are unpaused.
			pausedLatch.get().await();
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * signals the thread to pause and returns once the thread is fully paused.
	 * 
	 * @throws InterruptedException
	 */
	synchronized public void pauseThread() 
	{
		// request and wait for the thread to pause
		pauseLatch.set(new CountDownLatch(1));
		try
		{
			pauseLatch.get().await();
		}
		catch (InterruptedException e)
		{
			logger.warn(e);
		}
	}

	synchronized public void resumeThread()
	{
		// signal that we are now unpaused.
		pausedLatch.get().countDown();
	}
}
