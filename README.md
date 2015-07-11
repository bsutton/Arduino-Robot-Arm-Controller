# Arduino-Robot-Arm-Controller
A simple UI to control a Arduino based Robotic Arm using the Adfruit PWM contoller.

This is a very simple project which provides a Java based (swing) application which allows direct control over motors attached to Adafruit PWM Servo Driver.

The application lets you send structured commands to the Arduino to move the motors.

You can also save a set of commands to a file, reload the file and play them as a 'pre-programmed' sequence.

The application is designed to allows you to do simple testing of your hardware.

Example command sequence:


	hi
	mov 1, 200
	mov 2, 340
	mov 3, 150
	wait 3000
	stop 1
	stop 2
	stop 3


The command structure is:

	<cmd> <arg, arg, ...> <newline>

The following commands are supported:

* hi
* mov
* wait
* stop
* status

##Comments
When running a sequence file you can comment out a line by prefixing it with:

// 

e.g.

	// mov 1, 200

The above line will be ignored.




##Command: Hi

###Overview

Used to ensure the Arduino controller is responding and to retrieve the version number of the on board control software.

Args: none

Action: none

Returns:

	version: 1.0

##Command: mov

###Overview

Moves a motor to a given position.

Args: 

	<motor>, <frequency>

	<motor> - integer from 0-15 which maps to the adafruit PWM pin outs for each server.

	<frequency> - integer from 0 - 4000 (?) which sets the PWM frequency on the adafruit PWM for the selected motor.

<b>Action:</b> 

Sets the given motor to the given frequency

Returns:

	Mov: motor <motor>, frequency <frequency>

##Command: wait

###Overview

Puts the controller to sleep for a given number of milliseconds. Use this command to allow motors the time required to complete their movement.
Currently the controller will respond to no other commands until the wait period has elapsed.

Args:
 
	<delay>

	<delay> - time in milliseconds to wait

<b>Actions:</b>

Makes the controller sleep for the given number of milliseconds.

Returns:

	Wait: <wait>

##Command: stop

##Overview

Stops the selected motor by setting the frequency to zero. Motor will be un powered and will rotate freely under load.

Args: 

	<motor>

	<motor> - integer from 0-15 which maps to the adafruit PWM pin outs for each server.

<b>Actions:</b>

Stops the selected motor

Returns: 

	Stop: motor <motor>


## Command: status
###Overview
Returns the controllers current status.

Args: none

<b>Actions:</b>

Returns the controller's current status.

Returns:
Feeling groovy.


#Install Arduino control software

The Arduino controller is located in:
src/main/arduino/Controller/Controller.ino

The controller requires the Adafruit PWM Servo Library to be installed.

[Servo Library](https://github.com/adafruit/Adafruit-PWM-Servo-Driver-Library)

Once the adafruit library is installed then simply upload the Controller.ino to your Arduino and its ready to go.

If you open the Arduino IDE serial monitor (Tools | Serial Monitor) you can now directly interact with the controller.

Type a command at the terminal and press enter to run it.

	Hi
	version: 1.0
	
If the 'Hi' command works then the controller is up and running.

#Install java swing UI

The UI is written in java and has been complied with Java 8.

Development is done using Eclipse Luna and Maven 3

You can build the application from the command line (once maven is installed) by running:

	cd <root directory where you saved the java source>
	mvn install
	
#Run java swing UI


To run the UI

	cd target
	java -jar robotarm-1.0-jar-with-dependencies.jar


##Using earlier versions of java
You can probably run it with a earlier version of java by changing the 'source' and 'target' properties in the pom.xml file:

For java 7

Change

	<source>1.8</source>
	<target>1.8</target>
To

	<source>1.7</source>
    <target>1.7</target>
          
then run:
	
	mvn install
	


# Using the UI to control your robot

After starting the UI you first need to select a port from the drop list.

If your port is missing click the 'Refresh Ports' button to refresh the list of ports. You will often need to do this if you have unplugged and then plugged the USB connector in.

With you Arduino up and running and the USB connected click the 'Connect' button to connect to the Arduino.

The Arduino should respond with the Controller's version no.

You can now type commands into the command bar to the left of the 'Send Action' button.

Responses from the Controller are displayed in the log area below the Command Bar.

Use the 'Stop All' button to power down all of the motors.

The 'Sequences' button opens a UI that allows you to enter/save and reload a sequence of commands which you can then Run on the Arduino.

Just make certain you only enter one command per line.

you can comment out a line by inserting leading // characters (standard java/C/C++ single line comment)














