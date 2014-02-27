/*
* Author: Lance Baker
* Student No: 3128034
* Course: INFT3940
* Date: September 2010
* Description:
* The Maze Robot
* The class is used to control a Lego MindStorms robot, that was required to navigate a maze to the exit.
* It was a wall follower design, which stuck as close to the wall as possible. If the robot hit a wall, it
* would then reverse and then realign itself to follow the wall again. It had to also incorporate some random
* decisional behaviour, in order to escape 'The Island' maze, which had a centre island wall, surrounded by an
* outside wall. The robot in this maze epically came first in the class, and made it to the exit without trouble.
*/

import josx.platform.rcx.*;

class LightSensor implements SensorListener {
	private int value;
	public LightSensor() {
	}
	
	public void stateChanged(Sensor s, int oldValue, int newValue) {
		try {
			this.value = newValue;
		} catch (Exception ex) {}
	}
	
	public int getValue() {
		return this.value;
	}
}

public class Robot2 {
	private static final int STATUS_ON_WALL = 1;
	private static final int STATUS_HIT_WALL = 2;
	
	private static final int DIRECTION_STRAIGHT  = 1;
	private static final int DIRECTION_LEFT = 2;
	private static final int DIRECTION_RIGHT = 3;
	
	private LightSensor lightSensor;
	private int status;
	private int direction;
	
	public Robot2() {
		this.lightSensor = new LightSensor();
		Sensor.S3.activate();
		Sensor.S3.addSensorListener(this.lightSensor);
	}
	
	public void run() {
		while(true) {
			try {
				LCD.clear();
				// If none of the sensors are hit, and the sensor reads the wall; then just move slightly to the left while going forward.
				if ((!(this.leftHit() && this.rightHit())) && (this.readWall())) {
					if ((this.getStatus() != STATUS_HIT_WALL)) {
						this.setDirection(DIRECTION_STRAIGHT);
						this.slightLeft(50);
						this.forward(250);
					}
					
				// If the left touch sensor is pressed, and the right is untouched.
				} else if (this.leftHit() && (!this.rightHit())) {
					if (!touchingWall()) {
						if ((this.getDirection() == DIRECTION_STRAIGHT) && (this.readWall())) {
							this.setStatus(STATUS_ON_WALL);
							this.slightRight(50);
							this.forward(250);
						} else if (this.getDirection() == DIRECTION_RIGHT) {
							this.backward(150);
							this.hardLeft(100);
							this.goStraight();
						}
					}
				} else if ((!this.leftHit()) && (this.rightHit()) && (!this.readWall())) {
					if (!touchingWall()) {
						this.backward(250);
						this.hardLeft(350);
						this.findLeftWall();
					}
				} else if ((!this.leftHit()) && (this.rightHit()) && (this.readWall())) {
					if (!touchingWall()) {
						this.backward(100);
						this.hardRight(250);
						this.goStraight();
					}
				} else if (this.leftHit() && this.rightHit()) {
					this.collision();
					
				} else {
					this.lost();
				}
			} catch (Exception ex) {}
		}
	}
	
	private boolean touchingWall() {
		this.forward(250);
		return (this.leftHit() && this.rightHit());
	}
	
	private void collision() {
		this.setStatus(STATUS_HIT_WALL);
		this.turnRight();
	}
	
	private void lost() {
		// It was last on a wall, then it must have been a left corner.
		if ((this.getStatus() == STATUS_ON_WALL) || (this.getDirection() == DIRECTION_STRAIGHT)) {
			if (!this.decision()) {
				this.turnLeft();
			} else {
				this.goStraight();
			}
		} else {
			this.findLeftWall();
		}
	}
	
	// Goes straight ahead until it hits something interesting.
	private void goStraight() {
		this.setDirection(DIRECTION_STRAIGHT);
		// Loops until the robot has hit the wall
		while((!(this.leftHit()) && (this.rightHit())) || (!this.readWall())) {
			this.forward(150);
		}
	}
	
	private void turnRight() {
		this.setDirection(DIRECTION_RIGHT);
		this.backward(250); // Too close to wall, reverse a little.
		this.hardRight(250); // Turns to the right
		this.findLeftWall();
	}
	
	private void turnLeft() {
		this.setDirection(DIRECTION_LEFT);
		this.hardLeft(750); // Turns to the left
		this.forward(50);
	}
	
	// Iterates until it has found the wall 
	// on the left hand side again.
	private void findLeftWall() {
		this.setStatus(STATUS_ON_WALL);
		while (!(this.leftHit() || this.readWall())) {
			this.slightLeft(25);
			this.forward(150);
		}
	}
	
	private boolean decision() {
		return (((int)((Math.random() * 5) + 1)) == ((int)((Math.random() * 5) + 1)));
	}
	
	private boolean readWall() {
		return (this.lightSensor.getValue() >= 63);
	}
	
	private boolean leftHit() {
		return Sensor.S1.readBooleanValue();
	}
	
	private boolean rightHit() {
		return Sensor.S2.readBooleanValue();
	}
	
	private void setStatus(int status) {
		this.status = status;
	}
	
	private void setDirection(int direction) {
		this.direction = direction;
	}
	
	private int getDirection() {
		return this.direction;
	}
	
	private int getStatus() {
		return this.status;
	}
	
	private void forward(int duration) {
		Motor.A.forward();
        Motor.C.forward();
		delay(duration);
	}
	
	private void backward(int duration) {
		Motor.A.backward();
        Motor.C.backward();
		delay(duration);
	}
	
	private void stop(int duration) {
		Motor.A.stop();
        Motor.C.stop();
		delay(duration);
	}
	
    private void hardLeft(int duration) {
		Motor.A.backward();
		Motor.C.forward();
		delay(duration);
    }
	
	private void hardRight(int duration) {
		Motor.A.forward();
		Motor.C.backward();
		delay(duration);
    }
	
	private void slightLeft(int duration) {
		Motor.A.stop();
		Motor.C.forward();
		delay(duration);
    }
	
	private void slightRight(int duration) {
		Motor.A.forward();
		Motor.C.stop();
		delay(duration);
    }

	private static void delay(int delay) {
		try {
			Thread.sleep(delay);
		} catch (Exception ex) {}
	}

	public static void main(String[] args) {
		(new Robot2()).run();
	}
}