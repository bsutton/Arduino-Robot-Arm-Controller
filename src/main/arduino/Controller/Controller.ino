#include <Wire.h>

#include <Adafruit_PWMServoDriver.h>

Adafruit_PWMServoDriver servo = Adafruit_PWMServoDriver ();

int maxMotor = 15;
int motorPos[16];

int processingCount = 0;
int unrecognisedCount = 0;

void setup()
{

  Serial.begin(115200);
  Serial.setTimeout(50);
  servo.begin();
  servo.setPWMFreq(60);
  
  for (int i = 0; i <= maxMotor; i++)
    motorPos[i] = 0;
    

}


void loop()
{
  // Loop and wait for a command we recognise
  String cmd = "";

  int inbyte;

  // wait for a command to be typed.
  while(true)
  {
    // what for a char to be available
    while (Serial.available() == 0)
      ;


    inbyte = Serial.read();
    if (inbyte == '\n')
      break;

    if (inbyte != 0)
      cmd += (char(inbyte));
  } 

    // \n so we now look for what cmd we have
  
    cmd.trim();
    cmd.toLowerCase();
    
   if (cmd.startsWith("hi"))
    cmdHi();
  else   if (cmd.startsWith("mov"))
    cmdMoveMotor(cmd);
  else   if (cmd.startsWith("pose"))
    cmdPoseMotor(cmd);
  else   if (cmd.startsWith("wait"))
    cmdWait(cmd);
  else   if (cmd.startsWith("stop"))
    cmdStopMotor(cmd);
  else   if (cmd.startsWith("status"))
  {
    Serial.println("Feeling groovy.");

  }
  else if (cmd.equalsIgnoreCase(""))
  {
    Serial.println("Got empty command");
    Serial.flush();
    // empty line so ignore it.
    
  }
  else
  {
    Serial.print("Unrecoginised command: '");
    Serial.print(unrecognisedCount++);
    Serial.print("): '");
    Serial.print(cmd);
    Serial.println("'");
    Serial.flush();

  }
    //Serial.println("Loop");
    //Serial.flush();


}

void cmdHi()
{
  Serial.println("version: 1.0");
}

void cmdWait(String cmd)
{
  cmd = cmd.substring(5);    
  int wait = atoi(getValue(cmd, ',', 0).c_str());
  Serial.print("Wait: ");
  Serial.println(wait);
  delay(wait);
}

void cmdMoveMotor(String cmd)
{

  // strip the command from the start of the string including the comma
  cmd = cmd.substring(4);    
  int motor= atoi(getValue(cmd, ',', 0).c_str());
  
  int frequency = atoi(getValue(cmd, ',', 1).c_str());

  
  if (motor < 0  || motor > maxMotor)
  {
    Serial.print("Invalid motor: ");
    Serial.println(motor);
  }
  else
  {
    // We have a valid motor.    
    if (motorPos[motor] == 0)
        servo.setPWM(motor,0, frequency);
     else
     {
          // We know where the motor is so lets move it progressively into place

         int increment = 5;

        if (motorPos[motor] < frequency)
        {
           for (int i = motorPos[motor]; i < frequency; i+=increment)
           {
               servo.setPWM(motor,0,i);
               delay(50);
           }
        }
        else
        {
           for (int i = motorPos[motor]; i > frequency; i-=increment)
           {
               servo.setPWM(motor,0,i);
               delay(50);
           }
        }
     }
     motorPos[motor] = frequency;
    Serial.print("Mov: motor ");
    Serial.print(motor);
    Serial.print(", frequency ");
    Serial.println(frequency);
  }
   Serial.flush();
}

void cmdPoseMotor(String cmd)
{

  // strip the command from the start of the string including the comma
  cmd = cmd.substring(5);    
  
  int baseMotor = atoi(getValue(cmd, ',', 0).c_str());
  int baseFrequency = atoi(getValue(cmd, ',', 1).c_str());
  if (!validMotor(baseMotor))
      return;
  
  int shoulderMotor = atoi(getValue(cmd, ',', 0).c_str());
  int shoulderFrequency = atoi(getValue(cmd, ',', 1).c_str());
  if (!validMotor(shoulderMotor))
      return;

  int elbowMotor = atoi(getValue(cmd, ',', 0).c_str());
  int elbowFrequency = atoi(getValue(cmd, ',', 1).c_str());
  if (!validMotor(elbowMotor))
      return;

  
  else
  {
    // We have a valid motor.    
    if (motorPos[motor] == 0)
        servo.setPWM(motor,0, frequency);
     else
     {
          // We know where the motor is so lets move it progressively into place

         int increment = 5;

        if (motorPos[motor] < frequency)
        {
           for (int i = motorPos[motor]; i < frequency; i+=increment)
           {
               servo.setPWM(motor,0,i);
               delay(50);
           }
        }
        else
        {
           for (int i = motorPos[motor]; i > frequency; i-=increment)
           {
               servo.setPWM(motor,0,i);
               delay(50);
           }
        }
     }
     motorPos[motor] = frequency;
    Serial.print("Pose Base: motor ");
    Serial.print(baseMotor);
    Serial.print(", frequency ");
    Serial.print(baseFrequency);
    Serial.print("Shoulder: motor ");
    Serial.print(shoulderMotor);
    Serial.print(", frequency ");
    Serial.print(shoulderFrequency);

    Serial.print("Elbow: motor ");
    Serial.print(elbowMotor);
    Serial.print(", frequency ");
    Serial.println(elbowFrequency);

  }
   Serial.flush();
}

boolean validateMotor(int base)
{
  boolean valid = true;
  if (motor < 0  || motor > maxMotor)
  {
    Serial.print("Invalid motor: ");
    Serial.println(motor);
    valid = false;
  }
  return valid;
}

void cmdStopMotor(String cmd)
{
   cmd = cmd.substring(5);    
   int motor = atoi(getValue(cmd, ',', 0).c_str());

 
  servo.setPWM(motor,0, 0);
  Serial.print("Stop: motor ");
  Serial.println(motor);
  Serial.flush();
}



String getValue(String data, char separator, int index)
{

    int maxIndex = data.length()-1;
    int j=0;
    String chunkVal = "";

    for(int i=0; i<=maxIndex && j<=index; i++)
    {
      chunkVal.concat(data[i]);

      if(data[i]==separator)
      {
        j++;

        if(j>index)
        {
          chunkVal.trim();
          return chunkVal;
        }    

        chunkVal = "";    
      }  
    }  
}



