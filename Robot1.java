/*
* Author: Lance Baker
* Student No: 3128034
* Course: INFT3940
* Date: September 2010
* Description:
* The Follow Line/ Rescue Robot
* The class is used to control a Lego MindStorms robot, that was required follow a black line - which was
* waving around a yellow plastic map. The robot had two light sensors facing down, which was used to detect 
* the black, and if it identified the yellow colour it would realign itself and continue following the line.
* If the robot reached the end, there was an area that required the robot to perform a rescuing task by pushing
* an object out of the 'swamp' area. It was done by going up and down in a zigzag movement, which ended up
* making contact with the object and pushing it out of the way.
*/

import josx.platform.rcx.*;

class Colour {
		public static final int Nothing = 0;
		public static final int Black = 1;
		public static final int Green = 2;
		public static final int Yellow = 3;
}

class LightSensor implements SensorListener {
		private int colour;
		public LightSensor() {
		}
		
		public void stateChanged(Sensor s, int oldValue, int newValue) {
			try {
				if (newValue >= 30 && newValue <= 39) {
					this.colour = Colour.Black;
				} else if (newValue >= 40 && newValue <= 48) {
					this.colour = Colour.Green;
				} else if (newValue >= 49 && newValue <= 59) {
					this.colour = Colour.Yellow;
				} else {
					this.colour = Colour.Nothing;
				}
				this.value = newValue;
			} catch (Exception ex) {}
		}
		
		public int getColour() {
			return this.colour;
		}
}

class Step {
	private int sensor1;
	private int sensor2;
	private int step;
	
	public void setSensors(int sensor1, int sensor2, int step) {
		this.sensor1 = sensor1;
		this.sensor2 = sensor2;
		this.step = step;
	}
	
	public int getSensor1() {
		return this.sensor1;
	}
	
	public int getSensor2() {
		return this.sensor2;
	}
	
	public int getCount() {
		return this.step;
	}
}

public class Robot1 {
	
	private static int MAX_MEMORY = 5;
	private LightSensor sensor1;
	private LightSensor sensor2;
	private boolean direction;
	private Step[] steps;
	private int index;
	private int step;
	
	public Robot1() {
		this.sensor1 = new LightSensor();
		this.sensor2 = new LightSensor();
		Sensor.S3.activate();
		Sensor.S1.activate();
		Sensor.S3.addSensorListener(this.sensor1);
		Sensor.S1.addSensorListener(this.sensor2);
		this.initMemory();
	}
	
	private void initMemory() {
		this.steps = new Step[MAX_MEMORY];
		for (int i = 0; i < this.steps.length; i++) {
			this.steps[i] = new Step();
		}
	}
	
	private void addToMemory() {
		((Step)this.steps[(this.index)]).setSensors(this.sensor1.getColour(), this.sensor2.getColour(), this.step);
		if (this.index == (MAX_MEMORY -1)) {
			this.index = 0;
		} else {
			this.index++;
		}
	}
	
	public void run() {
		boolean doRescue = false;
		while(true) {
			try {
				if (!doRescue) {
					// Attempts to take the shortcut.
					doRescue = this.followLine();
				} else {
					this.doRescue();
				}
				this.step++;
			} catch (Exception ex) {}
		}
	}
	
	private void doRescue() {   
		if (this.onYellow()) {
			this.forward();
		} else {
			this.stop();
			this.backward();
			if (this.direction) {
				this.hardRight();
			} else {
				this.hardLeft();
			}
			this.direction = (!this.direction);
		}
	}
	
	private boolean followLine() {
		if ((this.sensor1.getColour() == Colour.Black) && (this.sensor2.getColour() == Colour.Black)) {
			this.forward();
		} else if ((this.sensor1.getColour() == Colour.Black) && (this.sensor2.getColour() == Colour.Green)) {
			this.left();			
		} else if ((this.sensor1.getColour() == Colour.Green) && (this.sensor2.getColour() == Colour.Black)) {
			this.right();
		} else if ((this.sensor1.getColour() == Colour.Yellow) && (this.sensor2.getColour() == Colour.Black)) {			
			this.right();
		} else if ((this.sensor1.getColour() == Colour.Green) && (this.sensor2.getColour() == Colour.Green)) {
			this.backtrack();
		} else if ((this.sensor1.getColour() == Colour.Yellow) && (this.sensor2.getColour() == Colour.Yellow)) {
			if (this.step > 500) {
				this.forward();
				return true;
			}
		}
		this.addToMemory();
		return false;
	}
	
	private boolean onBlack() {
		return ((this.sensor1.getColour() == Colour.Black) || (this.sensor2.getColour() == Colour.Black));
	}
	
	private boolean onYellow() {
		return ((this.sensor1.getColour() == Colour.Yellow) || (this.sensor2.getColour() == Colour.Yellow));
	}
	
	private void backtrack() {
		// Needs to move in a mirrored backwards motion until the black line is found once again.
		// Needs to detect what angle the corner is on, by going through the previous steps until black is found.
		int position = this.index;
		Step step = this.steps[position];
		// Iterates until the black line is (hopefully) found
		while ((!this.onBlack()) && (step.getCount() <= this.step)) {
			// If the left sensor encounters green, and the right sensor is encountered black then 
			// it should reverse on the right wheel, and thus go backwards whilst turning towards the right direction
			if (((step.getSensor1() == Colour.Green) || (step.getSensor1() == Colour.Yellow)) && (step.getSensor2() == Colour.Black)) {
				Motor.A.backward();
				Motor.C.stop();
				delay(25);
			// Same as above (but opposite logic).
			} else if ((step.getSensor1() == Colour.Black) && (step.getSensor2() == Colour.Green)) {
				Motor.C.backward();
				Motor.A.stop();
				delay(25);
			// The only time both sensors are green is on the sharp bend towards the end; 
			// therefore, it defaults to go left.
			} else if (step.getSensor1() == Colour.Green && step.getSensor2() == Colour.Green) {
				Motor.C.backward();
				Motor.A.stop();
				delay(20);
			}
			position = (position == 0)? (MAX_MEMORY -1): position-1;
			step = this.steps[position]; // The next step recorded in the array
		}
	}
	
	private void forward() {
		Motor.A.forward();
        Motor.C.forward();
		delay(30);
	}
	
	private void right() {
		Motor.A.stop();
		Motor.C.forward();
		delay(25);
    }
	
	private void hardRight() {
		Motor.A.backward();
		Motor.C.forward();
		delay(1000);
	}
	
	private void hardLeft() {
		Motor.A.forward();
		Motor.C.backward();
		delay(1000);
	}
	
	private void backward() {
		Motor.A.backward();
		Motor.C.backward();
		delay(300);
	}

    private void left() {
		Motor.A.forward();
		Motor.C.stop();
		delay(25);
    }
	
	private void stop() {
		Motor.A.stop();
		Motor.C.stop();
		delay(5);
	}

	private static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {}
	}

	public static void main(String[] args) {
		(new Robot1()).run();
	}
}