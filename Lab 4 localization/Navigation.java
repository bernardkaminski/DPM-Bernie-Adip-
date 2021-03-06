import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;
import lejos.util.Delay;


public class Navigation {
	private Odometer odom;
	private final int TURNSPEED=100;
	private int FORWARDSPEED=200;
	private final double ANGLETHRESHOLD = .03;//threshold to which the robot will correct its angle *in rads
	private final double POSITIONTHRESHOLD = 3.0;//threshold to which robot will correct its position *in cm 
	private final NXTRegulatedMotor leftMotor, rightMotor; 
	
	// constructor
	public Navigation(Odometer o,NXTRegulatedMotor l,NXTRegulatedMotor r)
	{
		odom = o;
		leftMotor=l;
		rightMotor=r;
	}
	
	public void travelTo(double x, double y)
	{	 
		LCD.drawString(Double.toString(calOptimalAngle(calDestAngle(x, y))), 0, 3);
		
		double trackAngle =calOptimalAngle(calDestAngle(x, y));//store the angle it is supposed to turning by 
		turnTo(trackAngle);
		while((Math.abs(x-odom.getX())>POSITIONTHRESHOLD || Math.abs(y-odom.getY())>POSITIONTHRESHOLD) )
		{
			
					goStraight();
					
					if(!isAngleOk(trackAngle));//if angle is not close enough t the designated angle adjust speed of wheels to corect angle
					{
						
						ajust(trackAngle);
					}
			
		}
		
		stopMotors();//stop motors when done navigating
	}
	
	public void goStraight()
	{
		rightMotor.setSpeed(FORWARDSPEED);
		leftMotor.setSpeed(FORWARDSPEED);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	//method that makes wheels stop
	public void stopMotors()
	{
		leftMotor.stop();
		rightMotor.stop();
	}
	
	/* the turning method
	//the method takes as input the optimal angle that the robot needs to turn and exicutes the turn
	//so method should really be called "turnBy" **** note origin is considered where robot is facing when it begins its journey 
	 */
	public void turnTo(double theta)
	{
		//keeping track of original orientation so we know when the robot has reached the desired angle which = original + optimal 
		double originalAngle = Math.toRadians(odom.getTheta());
		
		
		if (theta >= 0)//if optimal is positive this means it is turning clockwise
		{
			rightMotor.setSpeed(TURNSPEED);
			leftMotor.setSpeed(TURNSPEED);
			double diff = (originalAngle + theta);
			RConsole.println(Double.toString(originalAngle)+"<-oa---theta->"+Double.toString(theta));
			//if diff is greater then 2 pi subtract two pi whats left is the correct angle
			if(diff> 2*Math.PI)
			{
				diff = diff - (2*Math.PI);
			}
			//as long as the angle does not equal close to the original angle plus the optimal turning angle keep turning
			while(Math.abs(Math.toRadians(odom.getTheta()) - (diff))>.025)
			{
				//Sound.beepSequenceUp();
				RConsole.println(Double.toString(diff));
				turnClockwise();	
			}
			stopMotors();
		}
		
		if (theta < 0)//if optimal is negative this means it is turning counterclockwise
		{
			Sound.buzz();
			rightMotor.setSpeed(TURNSPEED);
			leftMotor.setSpeed(TURNSPEED);
			double diff = (originalAngle + theta);
			RConsole.println(Double.toString(originalAngle)+"<-oa---theta->"+Double.toString(theta));
			if(diff> 2*Math.PI)
			{
				diff = diff - (2*Math.PI);
			}
			//as  long as the angle does not equal close to the original angle plus the optimal turning angle keep turning
			while(Math.abs(Math.toRadians(odom.getTheta()) - (diff))>.025)
			{
				turnCounterClockwise();
			}
			stopMotors();
		}
		
	}
	public double calOptimalAngle(double thetaDestination)
	{
		double op = thetaDestination;
		if(op <-Math.PI)
		{
			op += 2*Math.PI;
		}
		if (op > Math.PI)
		{
			op += -2*Math.PI;
		}
		RConsole.println(Double.toString(op));
		//Button.waitForAnyPress();
		return op;
		
	}
	
	// calculating the destination angle as specified in tutorial notes but with a slight twist in the math
	//**** note origin is considered where robot is facing when it begins its journey
	public double calDestAngle(double xDest,double yDest)
	{
		double odomAngle = Math.toRadians(odom.getTheta());//current position
		double dAngle = 0 ;//angle to get to
		double x = odom.getX();
		double y = odom.getY();
		double changeY = (yDest-y);
		double changeX = (xDest-x);
		if (changeY>0)
		{
			dAngle = Math.atan(( changeY/changeX));//math.atan returns radians
		}
		else
		{
			if (x<0)
			{
				dAngle = Math.atan(( changeY/changeX))-Math.PI;//math.atan returns radians
			}
			else
			{
				dAngle = Math.atan(( changeY/changeX))+Math.PI;
			}
		}
		Sound.beep();
		LCD.drawString(Double.toString(dAngle), 0, 4);
		return dAngle-odomAngle;
	}
	
	//Adjusting methods that continuously adjusts the speed of the wheels while traveling to a point 
	private void ajust(double trackangle)
	{
		//turning on the inside abit if angle is too large
		if(Math.toRadians(odom.getTheta())>trackangle)
		{
			rightMotor.setSpeed(rightMotor.getSpeed()+10);
			rightMotor.forward();
		}
		//turning on the outside abit if angle is too large
		else
		{
			leftMotor.setSpeed(leftMotor.getSpeed()+10);
			leftMotor.forward();
		}
	}
	
	//checks if angle is within threshold
	private boolean isAngleOk(double goodAngle)
	{
		if (Math.abs(Math.toRadians(odom.getTheta())-goodAngle)<ANGLETHRESHOLD)
		{
			return true;
		}
		else 
		{
			return false;
		}
	}	
	
	//help robot get out of trouble when going around obsticle. give it some leway not to cut is so close
	//mostly needed because the sensor is in front of the robot 
	public void travelSetDistanceStraight(double d)
		{
			Sound.beep();
			double startingX = odom.getX();
			double startingY = odom.getY();
			
			while ((Math.pow(odom.getX()-startingX, 2) + Math.pow(odom.getY()-startingY, 2)) < Math.pow(d, 2))
			{
				goStraight();
			}
			stopMotors();
		}
	public void turnClockwise()
	{
		leftMotor.forward();
		rightMotor.backward();
	}
	public void turnCounterClockwise()
	{
		rightMotor.forward();
		leftMotor.backward();
	}
	public void setSpeed(int speed)
	{
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		FORWARDSPEED = speed;
	}
	

}

