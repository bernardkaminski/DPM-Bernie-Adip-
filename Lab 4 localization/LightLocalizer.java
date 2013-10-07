import lejos.nxt.ColorSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;

public class LightLocalizer {
	private Odometer odo;
	private TwoWheeledRobot robot;
	private ColorSensor ls;
	private int lightValue;
	private int lineCounter=0; 
	private boolean done = false;
	private double xTheta1,yTheta1,xTheta2,yTheta2;
	private final double LIGHT_SENSOR_DISTANCE_FROM_CENTER = 13.6;
	private final int LIGHT_CONSTANT=-40;
	
	
	public LightLocalizer(Odometer odo, ColorSensor ls) {
		this.odo = odo;
		this.robot = odo.getTwoWheeledRobot();
		this.ls = ls;
		
		// turn on the light
		ls.setFloodlight(true);
	}
	
	public void doLocalization() 
	{
		lightValue = ls.getNormalizedLightValue();
		Motor.A.setSpeed(100);
		Motor.B.setSpeed(100);
		while (!done)
		{
			try { Thread.sleep(50); } catch (InterruptedException e) {}
			Motor.A.forward();
			Motor.B.backward();
			RConsole.println(Integer.toString(ls.getNormalizedLightValue()));
			if(lightValue-ls.getNormalizedLightValue()<(LIGHT_CONSTANT))
			{
				++lineCounter;
				//Sound.beep();
				if(lineCounter == 1 )
				{
					xTheta1 = odo.getTheta();
				}
				if(lineCounter ==2)
				{
					yTheta1 = odo.getTheta();
				}
				if(lineCounter ==3)
				{
					xTheta2 = odo.getTheta();
				}
				if(lineCounter ==4)
				{
					yTheta2= odo.getTheta();
					//done = true;
				}
			}
			
			
		}
		Motor.A.stop();
		Motor.B.stop();
		double xArc = xTheta2-xTheta1;
		double yArc = yTheta2- yTheta1;
		
		odo.setX(-LIGHT_SENSOR_DISTANCE_FROM_CENTER*Math.cos(Math.toRadians(yArc/2)));
		odo.setY(-LIGHT_SENSOR_DISTANCE_FROM_CENTER*Math.cos(Math.toRadians(xArc/2)));
		
		
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
	}


}
