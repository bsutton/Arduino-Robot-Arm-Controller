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

	<cmd> <args>

The following commands are supported:

* hi
* mov
* wait
* stop
* status





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
none

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

none

##Command: stop

##Overview

Stops the selected motor by setting the frequency to zero. Motor will be un powered and will rotate freely under load.

Args: 

	<motor>

	<motor> - integer from 0-15 which maps to the adafruit PWM pin outs for each server.

<b>Actions:</b>

Stops the selected motor

Returns: 

none


## Command: status
###Overview
Returns the controllers current status.

Args: none

<b>Actions:</b>

Returns the controller's current status.

Returns:
Feeling groovy.

