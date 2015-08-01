package robotarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

import robot.IllegalCommandException;
import robot.InvaidMotorFrequency;
import robot.InvalidMotorConfiguration;
import robot.InvalidMotorException;
import robot.NotConnectedException;
import robot.StepDialog;
import robot.iRobot;

public class SequenceUI extends JFrame
{
	private static final long serialVersionUID = 1L;
	private File activeFile = null;
	private iDisplay display;
	private iRobot robot;
	private File lastDir;

	SequenceUI(iDisplay display, iRobot robot)
	{
		this.display = display;
		this.robot = robot;
		this.lastDir = robot.getLastSaveDirectory();

		this.setTitle("Sequence Runner");
		JTextArea commands = new JTextArea();
		DefaultCaret caret = (DefaultCaret) commands.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		this.setContentPane(panel);

		JScrollPane scrollPane = new JScrollPane(commands);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		panel.add(buttonPanel, BorderLayout.SOUTH);
		JButton run = new JButton("Run");
		run.addActionListener(e -> runSequence(commands.getText(), SequenceMode.RUN));
		JButton step = new JButton("Step");
		step.addActionListener(e -> runSequence(commands.getText(), SequenceMode.STEP));
		JButton open = new JButton("Open");
		open.addActionListener(e -> open(this, commands));
		JButton save = new JButton("Save");
		save.addActionListener(e -> save(this, commands));
		JButton newButton = new JButton("New");
		newButton.addActionListener(e -> newButton(commands));

		JButton saveAs = new JButton("Save As...");
		saveAs.addActionListener(e -> saveAs(this, commands));
		buttonPanel.add(run);
		buttonPanel.add(step);
		buttonPanel.add(newButton);
		buttonPanel.add(open);
		buttonPanel.add(save);
		buttonPanel.add(saveAs);
		this.setSize(new Dimension(800, 700));
		this.setVisible(true);

	}

	private void saveAs(JFrame frame, JTextArea commands)
	{
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(lastDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Robot Move files", "rmf");
		fc.setFileFilter(filter);
		// In response to a button click:
		int returnVal = fc.showSaveDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			activeFile = fc.getSelectedFile();
			lastDir = activeFile.getParentFile();
			if (!activeFile.getName().endsWith(".rmf"))
				activeFile = new File(activeFile.getParentFile(), activeFile.getName() + ".rmf");
			save(frame, commands);
			robot.setLastSaveDirectory(fc.getCurrentDirectory());
		}
	}

	private void newButton(JTextArea commands)
	{
		// default icon, custom title
		int n = JOptionPane.showConfirmDialog(null, "Are you sure? You will loose any unsaved changes",
				"Confirm New action", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION)
		{
			activeFile = null;
			commands.setText("");
		}
	}

	private void save(JFrame frame, JTextArea commands)
	{

		if (activeFile != null)
		{
			BufferedWriter writer;
			try
			{
				writer = new BufferedWriter(new FileWriter(activeFile));
				writer.write(commands.getText());
				writer.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage());

			}

		}
		else
			saveAs(frame, commands);

	}

	private void open(JFrame frame, JTextArea commands)
	{
		// Create a file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(lastDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Robot Move files", "rmf");
		fc.setFileFilter(filter);
		// In response to a button click:
		int returnVal = fc.showOpenDialog(frame);

		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				activeFile = fc.getSelectedFile();
				lastDir = activeFile.getParentFile();
				robot.setLastSaveDirectory(fc.getCurrentDirectory());

				commands.setText("");
				try (Stream<String> lines = Files.lines(activeFile.toPath()))
				{
					lines.forEach(s -> commands.append(s + "\n"));
				}
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}

	public void runSequence(String text, SequenceMode mode)
	{
		if (!robot.isConnected())
			this.display.showError("Device is not connected");
		else
		{
			if (mode == SequenceMode.RUN)
			{
				// push into the background so we don't lock the UI.
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.submit(() -> {
					try
					{
						String[] cmds = text.split("\n");

						for (String cmd : cmds)
						{
							if (cmd != null)
								this.robot.sendCmd(cmd.trim(), display);
						}
						SwingUtilities.invokeLater(() -> this.display.append("Sequence sent in full\n"));
					}
					catch (NotConnectedException | IOException | TimeoutException | InvalidMotorConfiguration
							| InvaidMotorFrequency | InvalidMotorException | IllegalCommandException e)
					{
						SwingUtilities.invokeLater(() -> display.showException(e));
					}
				});
			}
			else
			{
				new StepDialog(null, robot, display, text);
				
			}
		}

	}

}
