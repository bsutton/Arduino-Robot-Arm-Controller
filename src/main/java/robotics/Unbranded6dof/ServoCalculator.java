package robotics.Unbranded6dof;

import robot.Motor;
import robotics.Joint;
import robotics.ServoAngleToPwmCalculator;
import robotics.iMotor;

public class ServoCalculator extends Kinematics
{

	private ServoAngleToPwmCalculator base;
	private ServoAngleToPwmCalculator shoulder;
	private ServoAngleToPwmCalculator elbow;
	private iMotor baseMotor;
	private iMotor shoulderMotor;
	private iMotor elbowMotor;

	public ServoCalculator(iMotor base, iMotor shoulder, iMotor elbow)
	{
		this.baseMotor = base;
		this.shoulderMotor = shoulder;
		this.elbowMotor  = elbow;
		this.base = new ServoAngleToPwmCalculator(base);

		this.shoulder = new ServoAngleToPwmCalculator(shoulder); 

		this.elbow = new ServoAngleToPwmCalculator(elbow); 
	}

	public double getBasePwm()
	{
		return base.getPwmValue(getJoint(BASE_JOINT).getJointAngle());
	}

	public double getShoulderPwm()
	{
		Joint joint = getJoint(SHOULDER_JOINT);
		
		Double jointAngle = joint.getJointAngle();
		double pwmValue = shoulder.getPwmValue(jointAngle);
		System.out.println(joint.getName() +  Math.toDegrees(jointAngle) + " pwm: " + pwmValue);
		return pwmValue;
	}

	public double getElbowPwm()
	{
		Joint joint = getJoint(ELBOW_JOINT);
		Double jointAngle =joint.getJointAngle();
		double pwmValue = elbow.getPwmValue(jointAngle);
		System.out.println(joint.getName() +  Math.toDegrees(jointAngle) + " pwm: " + pwmValue);
		return pwmValue;
	}

	public void setBaseAngle(double i)
	{
		getJoint(BASE_JOINT).setJointAngle(i);
	}

	public void setShoulderAngle(double radians)
	{
		getJoint(SHOULDER_JOINT).setJointAngle(radians);
	}

	public void setArmElbowAngle(double radians)
	{
		getJoint(ELBOW_JOINT).setJointAngle(radians);
	}

	public Motor getBaseMotor()
	{
		return (Motor) baseMotor;
	}

	public Motor getShoulderMotor()
	{
		return (Motor) shoulderMotor;
	}
	
	public Motor getElbowMotor()
	{
		return (Motor) elbowMotor;
	}
}
