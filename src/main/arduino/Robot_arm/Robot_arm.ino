#include <Wire.h>

#include <Adafruit_PWMServoDriver.h>

#include <Servo.h>

Adafruit_PWMServoDriver servo = Adafruit_PWMServoDriver ();

Servo servo_1;
Servo servo_2;
Servo servo_3;


//introducing variables


int V = 3; // variable to control speed  
int Ampl = 180; // variable to control aplitude
int Pd = 0.1; // phase difference variable from 0 to 1


int pos1 = 180;
int pos2 = 90;
int pos3 = 90;


void setup()
{
  //servo_1.attach(9, 800, 2200); 
  //servo_2.attach(10, 800, 2200);
  //servo_3.attach(11, 800, 2200); 

  Serial.begin(9600);
  Serial.setTimeout(50);
  servo.begin();
  servo.setPWMFreq(60);

  Serial.println("Ready for action");
}


void loop()
{
  // Loop and wait for a command we recognise
  String cmd;

  int inbyte;

  // wait for a command to be typed.
  while(true)
  {
    // what for a char to be available
    while (Serial.available() == 0)
      ;


    inbyte = Serial.read();
    if (inbyte == '\n' || inbyte == ' ')
      break;

    if (inbyte != 0)
      cmd += (char(inbyte));
  } 

  // \n or : so we now look for what cmd we have

    if (cmd.equalsIgnoreCase("hi"))
    cmdHi();
  else   if (cmd.equalsIgnoreCase("mov"))
    cmdMoveMotor();
     else   if (cmd.equalsIgnoreCase("wait"))
    cmdWait();
  else   if (cmd.equalsIgnoreCase("stop"))
    cmdStopMotor();
  else   if (cmd.equalsIgnoreCase("status"))
  {
    Serial.println("Feeling good.");

  }
  else
  {
    Serial.print("Unrecoginised command: ");
    Serial.println(cmd);
  }


}

void cmdHi()
{
  Serial.println("Hello back");
}

void cmdWait()
{
   int wait = getNumber();
  Serial.print("Wait: ");
  Serial.println(wait);
  delay(wait);
}

void cmdMoveMotor()
{
  int motor= getNumber();
  Serial.print("Mov: motor ");
  Serial.print(motor);
  
  int frequency = getNumber();
  Serial.print(", frequency ");
  Serial.println(frequency);

  servo.setPWM(motor,0, frequency);
}

void cmdStopMotor()
{
  int motor= getNumber();
  Serial.print("Stop: motor ");
  Serial.println(motor);
 
  servo.setPWM(motor,0, 0);
}



long getNumber()
{
  long serialdata = 0;
  int inbyte = 0;
  inbyte = Serial.read();

  while (inbyte != ',' && inbyte != '\n')
  {

    if (inbyte > 0 && inbyte != ' ')
    {
      serialdata = serialdata * 10 + inbyte - '0';

    }
    inbyte = Serial.read(); 

  }


  return serialdata;
}





