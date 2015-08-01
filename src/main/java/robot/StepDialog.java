package robot;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import robotarm.iDisplay;

public class StepDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	JButton closeButton = new JButton("Close");
	JButton stepButton = new JButton("Step");

	GridLayout gridLayout = new GridLayout(Robot.MAX_MOTORS + 1, 5);
	private iRobot robot;
	private String[] cmds;
	private int nextCommand = 0;
	private iDisplay display;
	private JTextField nextCommandField;

	public StepDialog(JFrame parent, iRobot robot, iDisplay display, String commands)
	{
		super(parent, "Robot Configuration");

		this.robot = robot;
		this.display = display;
		
		cmds = commands.split("\n");

		// setResizable(false);
		addComponentsToPane(getContentPane(), ((Robot) robot).getMotors());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// force dialog to be centred
		setPreferredSize(new Dimension(600, 500));
		setSize(new Dimension(600, 500));
		setLocationRelativeTo(null);
		
		if (this.nextCommand < cmds.length)
			nextCommandField.setText(cmds[this.nextCommand]);


		pack();
		setVisible(true);
	}


	public void addComponentsToPane(final Container pane, Motor[] motors)
	{
		final JPanel motorPanel = new JPanel();
		motorPanel.setLayout(gridLayout);

	
		nextCommandField = new JTextField();
		motorPanel.add(nextCommandField);
		
		JPanel controls = new JPanel();
		controls.add(stepButton);
		controls.add(closeButton);

		// Handle button clicks
		closeButton.addActionListener(e -> {
			setVisible(false);
			
			((Robot) robot).save();
			dispose();
		});
		stepButton.addActionListener(e -> {

			try
			{
				this.robot.sendCmd(cmds[this.nextCommand].trim(), display);
				this.nextCommand++;
				
				if (this.nextCommand < cmds.length)
					nextCommandField.setText(cmds[this.nextCommand]);
				else
				{
					nextCommand = 0;
					nextCommandField.setText(cmds[this.nextCommand]);
					display.showError("Sequence complete. Resetting to start");
				}
			}
			catch (Exception e1)
			{
				display.showException(e1);
			}
		});

		pane.add(motorPanel, BorderLayout.NORTH);
		pane.add(new JSeparator(), BorderLayout.CENTER);
		pane.add(controls, BorderLayout.SOUTH);
	}


	// Get the escape function working.
	public JRootPane createRootPane()
	{
		JRootPane rootPane = new JRootPane();
		KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");

		Action action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
				dispose();
			}
		};

		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(stroke, "ESCAPE");
		rootPane.getActionMap().put("ESCAPE", action);
		return rootPane;
	}

}