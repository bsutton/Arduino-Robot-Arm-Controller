/**
 * 0- base 110-475 centre - 230
1- sholder - 220-600+
2- elbow - 200-630
5 - wrist (rotate) - 200 - 620
7 - wrist (vertica) 200 - 570
15 - gripper 550 (closed), 450 (open)

 */
package robotarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main extends JFrame implements iDisplay, iCmdRunner
{
	Logger logger = LogManager.getLogger(Main.class);

	private static final long serialVersionUID = 1L;
	private static RobotResponseListener robotResponseListener;
	private FileOutputStream fosDevice;
	private JTextArea outputField;
	private JComboBox<String> portField;
	private JButton disconnect;
	private JButton actionButton;
	private JButton connect;
	private JButton sequence;
	private JButton stopAll;
	private boolean isConnected = false;
	private JButton clear;

	private void initUI()
	{

		setTitle("Spike's Robot Controller");
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JPanel contentPane = new JPanel(new BorderLayout());
		this.setContentPane(contentPane);

		// Lay out the labels in a panel.

		JPanel actionPanel = new JPanel(new BorderLayout());
		contentPane.add(actionPanel, BorderLayout.NORTH);
		JTextField actionField = new JTextField();
		actionPanel.add(actionField, BorderLayout.CENTER);

		actionButton = new JButton("Send Action");
		actionButton.setPreferredSize(new Dimension(140, 40));
		actionButton.setEnabled(false);
		this.getRootPane().setDefaultButton(actionButton);
		actionButton.addActionListener(e -> {
			try
			{
				String cmd = SequenceUI.preprocessCommand(actionField.getText(), this);
				if (cmd != null)
					sendCmd(cmd);
			}
			catch (Exception e1)
			{
				logger.error(e1, e1);
			}
			actionField.setText("");
		});
		actionPanel.add(actionButton, BorderLayout.EAST);

		JPanel buttonPanel = new JPanel(new GridLayout(7, 1));
		contentPane.add(buttonPanel, BorderLayout.EAST);
		JButton refresh = new JButton("Refresh Ports");
		refresh.addActionListener(e -> refreshPorts());
		buttonPanel.add(refresh);

		portField = new JComboBox<String>();
		// portField.setSize(new Dimension(15, 40));
		portField.setPreferredSize(new Dimension(140, 40));
		buttonPanel.add(portField);

		connect = new JButton("Connect");
		connect.addActionListener(e -> connect());
		buttonPanel.add(connect);

		disconnect = new JButton("Disconnect");
		disconnect.setEnabled(false);
		disconnect.addActionListener(e -> disconnect());
		buttonPanel.add(disconnect);

		stopAll = new JButton("Stop All");
		stopAll.addActionListener(e -> stopMotors());
		stopAll.setEnabled(false);
		buttonPanel.add(stopAll);

		sequence = new JButton("Sequences");
		sequence.addActionListener(e -> sendSequence());
		buttonPanel.add(sequence);

		clear = new JButton("Clear");
		clear.addActionListener(e -> outputField.setText(""));
		buttonPanel.add(clear);

		outputField = new JTextArea();
		outputField.setEditable(false);
		DefaultCaret caret = (DefaultCaret) outputField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		// outputField.setPreferredSize(new Dimension(200, 300));
		JScrollPane scrollPane = new JScrollPane(outputField);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// Put the panels in this panel, labels on left,
		// text fields on right.
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contentPane.add(scrollPane, BorderLayout.CENTER);

		refreshPorts();

	}

	private void sendSequence()
	{
		new SequenceUI(this, this);

	}

	private void stopMotors()
	{
		try
		{
			for (int i = 0; i < 16; i++)
				sendCmd("stop " + i);
		}
		catch (NotConnectedException e)
		{
			this.isConnected = false;
			showException(e);
		}
	}

	public void sendCmd(String cmd) throws NotConnectedException
	{
		sendToRobot(cmd + "\n");

	}

	private void refreshPorts()
	{
		ArrayList<String> ports = getPorts();

		portField.removeAllItems();
		for (String port : ports)
			portField.addItem(port);

	}

	private void disconnect()
	{
		try
		{
			this.isConnected = false;
			robotResponseListener.stop();
			fosDevice.close();

			connect.setEnabled(true);
			disconnect.setEnabled(false);
			stopAll.setEnabled(false);
			actionButton.setEnabled(false);

		}
		catch (IOException e)
		{
			showException(e);
		}
	}

	private void connect()
	{

		String port = (String) portField.getSelectedItem();

		if (port != null)
		{
			String[] cmd =
			// { "/bin/sh", "-c", "stty raw -F " + port };

			{
					"/bin/sh",
					"-c",
					"stty raw -F "
							+ port
							+ "cs8 115200 ignbrk -brkint -icrnl -imaxbel -opost -onlcr -isig -icanon -iexten -echo -echoe -echok -echoctl -echoke noflsh -ixon -crtscts" };
			try
			{
				Runtime.getRuntime().exec(cmd);
				
				this.outputField.setText("");

				File portFile = new File("/dev", port);
				robotResponseListener.start(portFile);
				fosDevice = new FileOutputStream(portFile);

				try
				{
					sendCmd("hi");
				}
				catch (NotConnectedException e)
				{
					showError("Not Connected");
				}

				isConnected = true;
				connect.setEnabled(false);
				disconnect.setEnabled(true);
				stopAll.setEnabled(true);

				actionButton.setEnabled(true);

			}
			catch (IOException e)
			{
				showException(e);
			}
		}
		else
			JOptionPane.showMessageDialog(null, "Please select a port first.");

	}

	void moveMotor(String motor, String frequency) throws NotConnectedException
	{
		sendToRobot("mov " + motor + "," + frequency + "\n");
	}

	void sendToRobot(String message) throws NotConnectedException
	{
		try
		{
			if (fosDevice != null)
			{
				fosDevice.write(message.getBytes());
				fosDevice.flush();
				logger.error("Sending: {}", message);
				// give the serial port/arduino a chance to catchup
				Thread.sleep(200);
			}

		}
		catch (IOException e)
		{
			showException(e);
		}
		catch (InterruptedException e)
		{
			// should never happen
		}
	}

	ArrayList<String> getPorts()
	{

		ArrayList<String> ports = new ArrayList<>();

		File folder = new File("/dev");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles)
		{
			if (file.getName().startsWith("ttyACM") || file.getName().startsWith("cu.usb"))
			{
				ports.add(file.getName());
			}
		}
		return ports;

	}

	@Override
	public void append(String str)
	{
		outputField.append(str);

	}

	public static void main(String[] args)
	{

		final Main ex = new Main();
		EventQueue.invokeLater(new Runnable()
		{

			public void run()
			{
				ex.initUI();
				ex.setVisible(true);
			}
		});

		robotResponseListener = new RobotResponseListener(ex);
		// new Thread(robotResponseListener, "Robot Response Listener").start();
	}

	@Override
	public boolean isConnected()
	{
		return isConnected;
	}

	@Override
	public void showError(String message)
	{
		JOptionPane.showMessageDialog(null, message);

	}

	public void showException(Exception e)
	{
		JOptionPane.showMessageDialog(null, e.getMessage());
	}

}
