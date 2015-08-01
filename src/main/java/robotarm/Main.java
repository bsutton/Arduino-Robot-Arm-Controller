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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.prefs.BackingStoreException;

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
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import robot.IllegalCommandException;
import robot.InvaidMotorFrequency;
import robot.InvalidMotorConfiguration;
import robot.InvalidMotorException;
import robot.NotConnectedException;
import robot.Robot;
import robot.RobotConfigDialog;

public class Main extends JFrame implements iDisplay
{
	Logger logger = LogManager.getLogger(Main.class);

	private static final long serialVersionUID = 1L;
	static Robot robot;
	private JTextArea outputField;
	private JComboBox<String> portField;
	private JButton disconnect;
	private JButton actionButton;
	private JButton connect;
	private JButton sequence;
	private JButton stopAll;
	private JButton clear;

	private JTextField actionField;

	private JButton configurationButton;

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
		actionField = new JTextField();
		actionPanel.add(actionField, BorderLayout.CENTER);

		actionButton = new JButton("Send Action");
		actionButton.setPreferredSize(new Dimension(140, 40));
		actionButton.setEnabled(false);
		this.getRootPane().setDefaultButton(actionButton);
		actionButton.addActionListener(e -> sendAction(actionField.getText()));

		actionPanel.add(actionButton, BorderLayout.EAST);

		// Create the side button panel
		JPanel buttonPanel = new JPanel(new GridLayout(8, 1));
		contentPane.add(buttonPanel, BorderLayout.EAST);

		configurationButton = new JButton("Configuration");
		configurationButton.addActionListener(e -> new RobotConfigDialog(this, robot));
		buttonPanel.add(configurationButton);

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
		stopAll.addActionListener(e -> stopAll());
		stopAll.setEnabled(false);
		buttonPanel.add(stopAll);

		sequence = new JButton("Sequences");
		sequence.addActionListener(e -> showSequenceEditor());
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

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				robot.save();
				try
				{
					if (robot.isConnected())
						robot.stopMotors();
				}
				catch (NotConnectedException | IOException | TimeoutException | InvalidMotorException | InvaidMotorFrequency | InvalidMotorConfiguration | IllegalCommandException e1)
				{
					showException(e1);
				}
			}
		});

	}

	private void sendAction(String cmd)
	{
		try
		{
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(() -> {
				try
				{
					robot.sendCmd(cmd, this);
					SwingUtilities.invokeLater(() -> this.append("Sequence sent in full\n"));
				}
				catch (Throwable e)
				{
					SwingUtilities.invokeLater(() -> this.showException(e));
				}
			});

		}
		catch (Exception e1)
		{
			logger.error(e1, e1);
			showException(e1);
		}
		actionField.setText("");
	}

	private void stopAll()
	{
		try
		{
			robot.stopMotors();
		}
		catch (NotConnectedException | IOException | TimeoutException | InvalidMotorException | InvaidMotorFrequency | InvalidMotorConfiguration | IllegalCommandException e)
		{
			logger.error(e, e);
			showError(e.getMessage());
		}
	}

	private void connect()
	{
		String port = (String) portField.getSelectedItem();

		outputField.setText("");

		try
		{
			robot.connect(port);
			connect.setEnabled(false);
			disconnect.setEnabled(true);
			stopAll.setEnabled(true);
			actionButton.setEnabled(true);
		}
		catch (IOException | NotConnectedException e)
		{
			logger.error(e, e);
			showError(e.getMessage());
		}

	}

	private void showSequenceEditor()
	{
		new SequenceUI(this, robot);

	}

	private void refreshPorts()
	{
		ArrayList<String> ports = robot.getPorts();

		portField.removeAllItems();
		for (String port : ports)
			portField.addItem(port);

	}

	private void disconnect()
	{

		robot.stop();

		connect.setEnabled(true);
		disconnect.setEnabled(false);
		stopAll.setEnabled(false);
		actionButton.setEnabled(false);
	}

	@Override
	public void append(String str)
	{
		outputField.append(str);

	}

	public static void main(String[] args)
	{

		final Main main = new Main();
		EventQueue.invokeLater(new Runnable()
		{

			public void run()
			{
				main.initUI();
				main.setVisible(true);
			}
		});

		try
		{
			robot = new Robot(main);
		}
		catch (BackingStoreException e)
		{
			String message = e.getMessage();
			if (message == null || message.length() == 0)
				message = e.getCause().toString();
			JOptionPane.showMessageDialog(null, message);
		}
	}

	public void showError(String message)
	{
		JOptionPane.showMessageDialog(null, message);

	}

	public void showException(Throwable e)
	{
		String message = e.getMessage();
		
		if (message == null || message.length() == 0)
			message = (e.getCause() != null ? e.getCause().toString() : e.toString());
		
		logger.error(e,e);
		JOptionPane.showMessageDialog(null, message);
	}
}
