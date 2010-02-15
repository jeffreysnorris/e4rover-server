package org.eclipsecon.ebots.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipsecon.ebots.core.IShot;
import org.eclipsecon.ebots.core.IShot.GOAL;
import org.eclipsecon.ebots.internal.core.ArenaServerApplication.ROBOT;

import com.phidgets.PhidgetException;
import com.phidgets.RFIDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;

public class ShotHandler implements TagGainListener, AttachListener, ErrorListener {

	ArrayBlockingQueue<IShot> shotQueue = new ArrayBlockingQueue<IShot>(1);
	
	private static final Map<Integer, Shot.GOAL> PHIDGET_ID_TO_GOAL_MAP = new HashMap<Integer, Shot.GOAL>();
	private static final Set<RFIDPhidget> phidgets = new HashSet<RFIDPhidget>();

	private static final Map<String, Shot.PUCK> SPIRIT_ID_TO_PUCK_MAP = new HashMap<String, Shot.PUCK>();
	private static final Map<String, Shot.PUCK> OPPY_ID_TO_PUCK_MAP = new HashMap<String, Shot.PUCK>();
	private static final Map<String, Shot.PUCK> idToPuckMap;

	static {
		SPIRIT_ID_TO_PUCK_MAP.put("16005150ef", Shot.PUCK.A);
		SPIRIT_ID_TO_PUCK_MAP.put("17004aabcd", Shot.PUCK.B);
		SPIRIT_ID_TO_PUCK_MAP.put("16005163fd", Shot.PUCK.C);
		SPIRIT_ID_TO_PUCK_MAP.put("1600518eab", Shot.PUCK.D);

		// TODO: Populate these with the real RFIDs
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.A);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.B);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.C);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.D);

		// TODO: Add the rest of the phidget IDs
		PHIDGET_ID_TO_GOAL_MAP.put(90673, GOAL.G1);

		if (ArenaServerApplication.getRobotName().equals(ROBOT.SPIRIT))
			idToPuckMap = SPIRIT_ID_TO_PUCK_MAP;
		else
			idToPuckMap = OPPY_ID_TO_PUCK_MAP;
	}

	public ShotHandler()  {
		try {
			for (Integer serial: PHIDGET_ID_TO_GOAL_MAP.keySet()) {
				RFIDPhidget phidget = new RFIDPhidget();
				phidget.addAttachListener(this);
				phidget.addTagGainListener(this);
				phidget.addErrorListener(this);
				phidget.open(serial);
				phidgets.add(phidget);
			}
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}

	public ArrayBlockingQueue<IShot> getShotQueue() {
		return shotQueue;
	}

	public void tagGained(TagGainEvent e) {
		//		System.err.println(e);
		try {
			Shot.GOAL goal = PHIDGET_ID_TO_GOAL_MAP.get(e.getSource().getSerialNumber());
			Shot.PUCK puck = idToPuckMap.get(e.getValue());
			Shot shot = new Shot(goal, puck);
			shotQueue.put(shot);

		} catch (PhidgetException pe) {
			pe.printStackTrace();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	public void attached(AttachEvent ae) {
		//		System.err.println(ae);
		try {
			((RFIDPhidget) ae.getSource()).setLEDOn(true);
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}

	public void error(ErrorEvent ee) {
		System.err.println(ee);
	}

	public void shutdown() {
		System.out.println("Closing Phidgets...");
		try {
			for (RFIDPhidget phidget: phidgets) {
				phidget.setLEDOn(false);
				phidget.close();
			}
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
		System.out.println("   ...Phidgets closed.");
	}


}
