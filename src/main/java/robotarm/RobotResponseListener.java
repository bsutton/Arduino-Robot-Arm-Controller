package robotarm;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class RobotResponseListener implements Runnable {

	private final Object pauseMonitor = new Object();
	private boolean pauseThreadFlag = false;

	private File device;
	private volatile boolean stopped = false;
	private FileInputStream fis;
	private iDisplay display;

	RobotResponseListener(iDisplay display) {
		this.display = display;
		pauseThread();
	}

	public void stop() {
		pauseThread();
		stopped = true;
		try {
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void start(File portFile) {
		this.device = portFile;

		pauseThread();
		try {
			fis = new FileInputStream(portFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resumeThread();

	}

	public void run() {
		while (!stopped) {

			checkForPaused();
			int in;
			try {
				while (fis.available() != 0) {

					in = fis.read();
					display.append(String.valueOf((char) in));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void checkForPaused() {
		synchronized (pauseMonitor) {
			while (pauseThreadFlag) {
				try {
					pauseMonitor.wait();
				} catch (Exception e) {
				}
			}
		}
	}

	public void pauseThread() {
		pauseThreadFlag = true;
	}

	public void resumeThread() {
		synchronized (pauseMonitor) {
			pauseThreadFlag = false;
			pauseMonitor.notify();
		}
	}
}
