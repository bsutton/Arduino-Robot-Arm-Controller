package robotarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

public class Main extends JFrame implements iDisplay {

	private static final long serialVersionUID = 1L;
	private static RobotResponseListener robotResponseListener;
	private FileOutputStream fos;
	private JTextArea outputField;
	private JComboBox<String> portField;
	private JButton disconnect;
	private JButton actionButton;
	private JButton connect;
	private JButton sequence;
	private File activeFile = null;

	private void initUI() {

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
		actionButton.setEnabled(false);
		this.getRootPane().setDefaultButton(actionButton);
		actionButton.addActionListener(e -> { sendCmd(actionField.getText()); actionField.setText(""); } );
		actionPanel.add(actionButton, BorderLayout.EAST);	
		
		JPanel buttonPanel = new JPanel(new GridLayout(6,1));
		contentPane.add(buttonPanel, BorderLayout.EAST);
		JButton refresh = new JButton("Refresh Ports");
		refresh.addActionListener(e -> refreshPorts());
		buttonPanel.add(refresh);
		
		portField = new JComboBox<String>();
		portField.setSize(new Dimension(15,40));
		buttonPanel.add(portField);
		
		connect = new JButton("Connect");
		connect.addActionListener(e -> connect());
		buttonPanel.add(connect);
		
		disconnect = new JButton("Disconnect");
		disconnect.setEnabled(false);
		disconnect.addActionListener(e -> disconnect());
		buttonPanel.add(disconnect);
		
		JButton stop = new JButton("Stop All");
		stop.addActionListener(e -> stopMotors());
		buttonPanel.add(stop);

		sequence = new JButton("Sequences");
		sequence.addActionListener(e -> sendSequence());
		sequence.setEnabled(false);
		buttonPanel.add(sequence);


		outputField = new JTextArea();
		outputField.setEditable(false);
		DefaultCaret caret = (DefaultCaret) outputField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		//outputField.setPreferredSize(new Dimension(200, 300));
		JScrollPane scrollPane = new JScrollPane(outputField);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);



		// Put the panels in this panel, labels on left,
		// text fields on right.
		contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contentPane.add(scrollPane, BorderLayout.CENTER);

		refreshPorts();

	}

	private void sendSequence() {
		JFrame frame = new JFrame();

		JTextArea commands = new JTextArea();
		DefaultCaret caret = (DefaultCaret) commands.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		frame.setContentPane(panel);
		
		JScrollPane scrollPane = new JScrollPane(commands);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		panel.add(buttonPanel, BorderLayout.SOUTH);
		JButton run = new JButton("Run");
		run.addActionListener(e -> runSequence(commands.getText()));
		JButton open = new JButton("Open");
		open.addActionListener(e -> open(frame, commands));
		JButton save = new JButton("Save");
		save.addActionListener(e -> save(frame, commands));
		JButton newButton = new JButton("New");
		newButton.addActionListener(e -> newButton(commands));

		JButton saveAs = new JButton("Save As...");
		saveAs.addActionListener(e -> saveAs(frame, commands));
		buttonPanel.add(run);
		buttonPanel.add(newButton);
		buttonPanel.add(open);
		buttonPanel.add(save);
		buttonPanel.add(saveAs);
		frame.setSize(new Dimension(800, 700));
		frame.setVisible(true);

	}

	private void saveAs(JFrame frame, JTextArea commands) {
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Robot Move files", "rmf");
		fc.setFileFilter(filter);
		// In response to a button click:
		int returnVal = fc.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			activeFile = fc.getSelectedFile();
			save(frame, commands);
		}
	}

	private void newButton(JTextArea commands) {
		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null,
				"Are you sure. You will loose any unsaved changes",
				"Confirm New action", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			activeFile = null;
			commands.setText("");
		}
	}

	private void save(JFrame frame, JTextArea commands) {

		if (activeFile != null) {
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(activeFile));
				writer.write(commands.getText());
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage());

			}

		} else
			saveAs(frame, commands);

	}

	private void open(JFrame frame, JTextArea commands) {
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Robot Move files", "mov");
		fc.setFileFilter(filter);
		// In response to a button click:
		int returnVal = fc.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				activeFile = fc.getSelectedFile();
				try (Stream<String> lines = Files.lines(activeFile.toPath())) {
					lines.forEach(s -> commands.append(s + "\n"));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}

	private void runSequence(String text) {
		String[] cmds = text.split("\n");

		for (String cmd : cmds) {
			sendCmd(cmd);
		}
		
		this.append("Sequence sent in full");

	}

	private void stopMotors() {
		for (int i = 0; i < 16; i++)
			sendCmd("stop " + i);
	}

	private void sendCmd(String cmd) {
		sendToRobot(cmd + "\n");

	}

	private void refreshPorts() {
		ArrayList<String> ports = getPorts();

		portField.removeAllItems();
		for (String port : ports)
			portField.addItem(port);

	}

	private void disconnect() {
		try {
			robotResponseListener.stop();
			fos.close();
			connect.setEnabled(true);
			disconnect.setEnabled(false);
			actionButton.setEnabled(false);
			sequence.setEnabled(false);
			outputField.setText("");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	private void connect() {

		String port = (String) portField.getSelectedItem();

		if (port != null) {
			String[] cmd = { "/bin/sh", "-c", "stty raw -F " + port };
			try {
				Runtime.getRuntime().exec(cmd);

				File portFile = new File("/dev", port);
				robotResponseListener.start(portFile);
				fos = new FileOutputStream(portFile);

				connect.setEnabled(false);
				disconnect.setEnabled(true);
				actionButton.setEnabled(true);
				sequence.setEnabled(true);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		} else
			JOptionPane.showMessageDialog(null, "Please select a port first.");

	}

	void moveMotor(String motor, String frequency) {
		sendToRobot("mov " + motor + "," + frequency + "\n");
	}

	void sendToRobot(String message) {
		try {
			fos.write(message.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	ArrayList<String> getPorts() {

		ArrayList<String> ports = new ArrayList<>();

		File folder = new File("/dev");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.getName().startsWith("ttyACM")) {
				ports.add(file.getName());
			}
		}
		return ports;

	}

	@Override
	public void append(String str) {
		outputField.append(str);

	}

	public static void main(String[] args) {

		final Main ex = new Main();
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				ex.initUI();
				ex.setVisible(true);
			}
		});

		robotResponseListener = new RobotResponseListener(ex);
		new Thread(robotResponseListener).start();
	}

}
