package org.eclipsecon.ebots.internal.core;

import java.io.IOException;

import lejos.nxt.remote.NXTCommand;
import lejos.nxt.remote.OutputState;
import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommException;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTInfo;

public class RobotController extends Thread {
	private static RobotController singleton;
	static {
		singleton = new RobotController();
		singleton.start();
	}
	
	public static RobotController getDefault() {
		return singleton;
	}
	
	private static final int RIGHT_WHEEL_PORT = 2;
	private static final int LEFT_WHEEL_PORT = 1;
	private NXTCommand nxtCommand;
	private boolean shutdown = false;
	
	public RobotController() {
		super("Robot Controller Thread");
	}
	
	@Override
	public void run() {
		while(true) {

			nxtCommand = new NXTCommand();
			System.out.println("Attempting to connect to robot...");
			// Stubbornly attempt to establish connection
			while (!connected()) {
				try {				
					connect();
					if (nxtCommand.isOpen())
						break;
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Failed to connect to robot, retrying...");
				}
			}
			
			System.out.println("Connected to robot successfully.  Commencing montoring of telemetry...");
			try {
				nxtCommand.playTone(1700, 400);
				Thread.sleep(400);
				nxtCommand.playTone(2000, 400);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Fetch telemetry periodically
			try {
				while (!shutdown) {
					Thread.sleep(500);

					int batteryLevel = nxtCommand.getBatteryLevel();
					int leftOdom = nxtCommand.getTachoCount(LEFT_WHEEL_PORT);
					int rightOdom = nxtCommand.getTachoCount(RIGHT_WHEEL_PORT);

					OutputState outputState = nxtCommand.getOutputState(LEFT_WHEEL_PORT);
					int leftPower = outputState.powerSetpoint;
					outputState = nxtCommand.getOutputState(RIGHT_WHEEL_PORT);
					int rightPower = outputState.powerSetpoint;
					
					Persister.updateToServer(new Robot(batteryLevel, leftPower, rightPower, leftOdom, rightOdom));
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Lost connection, attempting to reconnect.");
			} finally {
				try {
					nxtCommand.close();
				} catch (IOException e) {/*empty*/}
			}

			// If we get here without shutdown, we lost connection with the
			// robot and it is time to reconnect
			
			if (shutdown)
				break;  // kill the monitoring thread
		}
		System.out.println("Monitoring thread exited normally.");

	}

	private void connect() throws NXTCommException {
		NXTComm nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
		NXTInfo nxtInfo = new NXTInfo(NXTCommFactory.BLUETOOTH, "NXT", "00:16:53:03:A6:D3");
		nxtComm.open(nxtInfo);
		nxtCommand.setNXTComm(nxtComm);
	}

	public void setWheelVelocity(int left, int right) {

		// If we've lost connection, just wait until connection is re-established
		while (!connected()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {/*empty*/}
		}

		try {
			nxtCommand.setOutputState(LEFT_WHEEL_PORT, (byte)left, 0, 0, 0, 0, 9999);
			nxtCommand.setOutputState(RIGHT_WHEEL_PORT, (byte)right, 0, 0, 0, 0, 9999);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean connected() {
		return ((nxtCommand != null) && nxtCommand.isOpen());
	}

	public void shutdown() {
		try {
			shutdown = true;
			System.out.println("Shutting down robot controller...");
			Thread.sleep(1500);  // let monitoring thread die
			nxtCommand.close();
			nxtCommand = null;
			System.out.println("   ...Robot controller shudown complete.");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
