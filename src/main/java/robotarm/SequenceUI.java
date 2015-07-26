package robotarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;

public class SequenceUI extends JFrame
{
	private static final long serialVersionUID = 1L;
	private File activeFile = null;
	private iDisplay display;
	private iCmdRunner cmdRunner;
	private File lastDir;
	private static HashMap<String, String> variables = new HashMap<>();

	SequenceUI(iDisplay display, iCmdRunner cmdRunner)
	{
		this.display = display;
		this.cmdRunner = cmdRunner;

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
		run.addActionListener(e -> runSequence(commands.getText()));
		JButton open = new JButton("Open");
		open.addActionListener(e -> open(this, commands));
		JButton save = new JButton("Save");
		save.addActionListener(e -> save(this, commands));
		JButton newButton = new JButton("New");
		newButton.addActionListener(e -> newButton(commands));

		JButton saveAs = new JButton("Save As...");
		saveAs.addActionListener(e -> saveAs(this, commands));
		buttonPanel.add(run);
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
			lastDir = activeFile.getParentFile();
			activeFile = fc.getSelectedFile();
			if (!activeFile.getName().endsWith(".rmf"))
				activeFile = new File(activeFile.getParentFile(), activeFile.getName() + ".rmf");
			save(frame, commands);
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

	public void runSequence(String text)
	{
		if (!cmdRunner.isConnected())
			this.display.showError("Device is not connected");
		else
		{
			try
			{
				String[] cmds = text.split("\n");

				for (String cmd : cmds)
				{
					cmd = preprocessCommand(cmd, this.display);
					if (cmd != null)
						this.cmdRunner.sendCmd(cmd);
				}
				this.display.append("Sequence sent in full\n");
			}
			catch (NotConnectedException e)
			{
				display.showException(e);
			}
		}

	}

	static String preprocessCommand(String cmd, iDisplay display)
	{
		String orginalCmd = cmd;
		// remove all spaces from the command
		cmd.replaceAll("\\s","");

		if (cmd.length() == 0)
			return null;

		// ignore comment lines
		if (cmd.startsWith("//"))
		{
			display.append("Ignored: " + orginalCmd + "\n");
			return null;
		}
		else if (cmd.startsWith("set"))
		{
			String[] tokens = cmd.split("[,]");

			if (tokens.length != 3)
				display.append("Invalid set command:" + cmd);
			else
			{
				String varName = tokens[1];
				String varValue = tokens[2];
				variables.put(varName, varValue);
			}
			cmd = null;
		}
		else
		{
			cmd = substituteVariables(cmd);
			
		}
		return cmd;
	}

	private static String substituteVariables(String cmd)
	{
		String newCmd;
		String[] tokens = cmd.split("[,]");

		if (tokens.length > 0)
		{
			newCmd = tokens[0];
			// do substitution
			for (int i = 1; i < tokens.length; i++)
			{
				String value = variables.get(tokens[i]);
				if (value == null)
				{
					value = tokens[i];
				}
				newCmd += ",";
				newCmd += value;
			}
		}
		else
			newCmd = cmd;
		return newCmd;
	}

}
