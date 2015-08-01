package robot;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class RobotConfigDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
    private volatile boolean stopping = false;

	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");

	GridLayout gridLayout = new GridLayout(Robot.MAX_MOTORS + 1, 5);
	private iRobot robot;
	private MotorFields fields[];

	public RobotConfigDialog(JFrame parent, iRobot robot)
	{
		super(parent, "Robot Configuration");

		this.robot = robot;
		// setResizable(false);
		addComponentsToPane(getContentPane(), ((Robot) robot).getMotors());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		// force dialog to be centred
		setPreferredSize(new Dimension(600, 500));
		setSize(new Dimension(600, 500));
		setLocationRelativeTo(null);

		pack();
		setVisible(true);
		
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		executorService.execute(new Runnable() {

			public void run() {
		        
		    	while (!stopping)
		    	{
		    		Robot theRobot = (Robot)robot;
		    		if (theRobot.getConfiguration().hasMotorMoved())
		    		{
		    			Motor[] motors = theRobot.getMotors();
		    			for (int i=0; i < Robot.MAX_MOTORS; i++)
		    			{
		    				Motor motor = motors[i];
		    				final int iField = i;
		    				SwingUtilities.invokeLater(() -> fields[iField].current.setText(""+motor.getCurrent()));
		    				
		    			}
		    		}
		    		try
					{
						Thread.sleep(500);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    }
		});

	}

	class MotorFields
	{
		JTextField name;
		JTextField pin;
		JTextField min;
		JTextField max;
		JTextField current;

		MotorFields(Motor motor)
		{
			name = new JTextField(motor.getName());
			pin = new JTextField("" + motor.getPin());
			min = new JTextField("" + motor.getMin());
			max = new JTextField("" + motor.getMax());
			current = new JTextField("" + motor.getCurrent());
		}

		public JTextField getName()
		{
			return name;
		}

		public JTextField getPin()
		{
			return pin;
		}

		public JTextField getMin()
		{
			return min;
		}

		public JTextField getMax()
		{
			return max;
		}

		public JTextField getCurrent()
		{
			return current;
		}
		
		
	}

	public void addComponentsToPane(final Container pane, Motor[] motors)
	{
		final JPanel motorPanel = new JPanel();
		motorPanel.setLayout(gridLayout);

		// Set the labels in the first row
		motorPanel.add(new JLabel("Name"));
		motorPanel.add(new JLabel("Pin"));
		motorPanel.add(new JLabel("Min"));
		motorPanel.add(new JLabel("Max"));
		motorPanel.add(new JLabel("Current"));

		// Added input controls for each motor setting their value to the motors
		// current value
		fields = new MotorFields[Robot.MAX_MOTORS];
		for (int i = 0; i < Robot.MAX_MOTORS; i++)
		{
			fields[i] = new MotorFields(motors[i]);
			motorPanel.add(fields[i].getName());
			motorPanel.add(fields[i].getPin());
			motorPanel.add(fields[i].getMin());
			motorPanel.add(fields[i].getMax());
			motorPanel.add(fields[i].getCurrent());
		}

		JPanel controls = new JPanel();
		controls.add(cancelButton);
		controls.add(okButton);

		// Handle button clicks
		okButton.addActionListener(e -> {
			stopping=true;
			setVisible(false);
			copyToConfiguration();
			((Robot) robot).save();
			dispose();
		});
		cancelButton.addActionListener(e -> {
			stopping=true;
			setVisible(false);
			dispose();
		});

		pane.add(motorPanel, BorderLayout.NORTH);
		pane.add(new JSeparator(), BorderLayout.CENTER);
		pane.add(controls, BorderLayout.SOUTH);
	}

	private void copyToConfiguration()
	{
		Motor[] motors = ((Robot) robot).getMotors();

		for (int i = 0; i < Robot.MAX_MOTORS; i++)
		{
			motors[i].setName(fields[i].getName().getText());
			motors[i].setPin(fields[i].getPin().getText());
			motors[i].setMin(fields[i].getMin().getText());
			motors[i].setMax(fields[i].getMax().getText());
			motors[i].setCurrent(fields[i].getCurrent().getText());
		}
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
				stopping=true;
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