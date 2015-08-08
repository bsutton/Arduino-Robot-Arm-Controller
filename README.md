# Arduino-Robot-Arm-Controller
A simple UI to control a Arduino based Robotic Arm using the Adfruit PWM contoller.

This is a very simple project which provides a Java based (swing) application which allows direct control over motors attached to Adafruit PWM Servo Driver.

The application lets you send structured commands to the Arduino to move the motors.

You can also save a set of commands to a file, reload the file and play them as a 'pre-programmed' sequence.

The application is designed to allows you to do simple testing of your hardware.

Example command sequence:


	hi
	set,base,1 
	on,1
	mov,1,200
	mov,base,200
	mov,2,340
	mov,3,150
	wait,3000
	stop,1
	stop,2
	stop,3


The command structure is:

	<cmd>,<arg, arg, ...><newline>

The following commands are supported:

* hi
* mov
* on
* set
* stop
* status
* wait


##Comments
When running a sequence file you can comment out a line by prefixing it with:

// 

e.g.

	// mov,1,200

The above line will be ignored.


##Command: Hi

###Overview

Used to ensure the Arduino controller is responding and to retrieve the version number of the on board control software.

Args: none

Action: none

Returns:

	version: 1.0
	
###Example
hi
version 1.0	

##Command: mov

###Overview

Moves a motor to a given position.

Args: 

	<motor>,<frequency>

	<motor> - integer from 0-15 which maps to the adafruit PWM pin outs for each server.

	<frequency> - integer from 0 - 4000 (?) which sets the PWM frequency on the adafruit PWM for the selected motor.
	
	Not: on my test kit the range is generally 100-600

<b>Action:</b> 

Sets the given motor to the given frequency

Returns:

	Mov: motor <motor>, frequency <frequency>
	
###Example
mov,1,200
Mov: motor 1, frequency 200




##Command: on

##Overview

Turns the power on the motor by using the last know frequency. Use this to power up motors so that don't sag when you start moving other motors.

The 'on' command is translated to a 'mov' command before it is sent to the Arduino. The java application caches the last know position to facilitate this.

Args: 

	<motor>

	<motor> - integer from 0-15 which maps to the adafruit PWM pin outs for each server.

<b>Actions:</b>

Turns the selected motor on

Returns: 

	Mov: motor <motor>, frequence <frequency>
	

###Example
on,1
Mov: motor 1, frequency 200


##Command: set

##Overview

The set command is more of a meta command as it lets you define variables that can be used in the rest of the script.

Once a variable is set the system does a simple find and replace on each command argument.

For example you can use set to map each motor no. to a easy to remember name:

e.g.
If you have three motors referenced by 1,2,3 then normally to move them you would use:
mov,1,200
mov,2,200
mov,3,200

With the set command you can:

set,base,1
set,shoulder,2
set,elbow,3

// the mov command no become
mov,base,200
mov,shoulder,200
mov,elbow,200

You can also replace the frequencies using the set command:

e.g.
// var for the elbow at 90 degrees
set,elbow-90,200
// move the elbow motor to its 90 degree position
mov,elbow,elbow-90

Args: 

	<variable name> - name for the variable

	<variable value> - variable value

<b>Actions:</b>

Creates a variable which can be used later in the script.

Returns: 

	no return as not sent to the controller.

###Example
stop,1
Stop: motor 1

## Command: status
###Overview
Returns the controllers current status.

Args: none

<b>Actions:</b>

Returns the controller's current status.

Returns:
Feeling groovy.

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

###Example
stop,1
Stop: motor 1

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
	
###Example
wait,2000
Wait: 2000	


#Install Arduino control software

The Arduino controller is located in:
src/main/arduino/Controller/Controller.ino

The controller requires the Adafruit PWM Servo Library to be installed.

[Servo Library](https://github.com/adafruit/Adafruit-PWM-Servo-Driver-Library)

Once the adafruit library is installed then compile and upload the Controller.ino to your Arduino and its ready to go.

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

If your port is missing click the 'Refresh Ports' button to refresh the list of ports. You will often need to do this if you have unplugged and then plugged the USB connector in as the port name will often change.

With you Arduino up and running and the USB connected click the 'Connect' button to connect to the Arduino.

The Arduino should respond with the Controller's version no.

You can now type commands into the command bar to the left of the 'Send Action' button.

Responses from the Controller are displayed in the log area below the Command Bar.

Use the 'Stop All' button to power down all of the motors.

The 'Sequences' button opens a UI that allows you to enter/save and reload a sequence of commands which you can then Run on the Arduino.

Just make certain you only enter one command per line.

you can comment out a line by inserting leading // characters (standard java/C/C++ single line comment)














