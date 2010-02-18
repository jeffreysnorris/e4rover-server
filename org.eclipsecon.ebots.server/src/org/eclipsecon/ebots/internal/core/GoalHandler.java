package org.eclipsecon.ebots.internal.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipsecon.ebots.core.IGoal;
import org.eclipsecon.ebots.core.IGoal.TARGET;
import org.eclipsecon.ebots.internal.core.ArenaServerApplication.ROBOT;

import com.phidgets.PhidgetException;
import com.phidgets.RFIDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.ErrorEvent;
import com.phidgets.event.ErrorListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;

public class GoalHandler implements TagGainListener, AttachListener, ErrorListener {

	ArrayBlockingQueue<IGoal> goalQueue = new ArrayBlockingQueue<IGoal>(1);
	
	private static final Map<Integer, IGoal.TARGET> PHIDGET_ID_TO_TARGET_MAP = new HashMap<Integer, IGoal.TARGET>();
	private static final Set<RFIDPhidget> phidgets = new HashSet<RFIDPhidget>();

	private static final Map<String, IGoal.INSTRUMENT> SPIRIT_ID_TO_INSTRUMENT_MAP = new HashMap<String, IGoal.INSTRUMENT>();
	private static final Map<String, IGoal.INSTRUMENT> OPPY_ID_TO_INSTRUMENT_MAP = new HashMap<String, IGoal.INSTRUMENT>();
	private static final Map<String, IGoal.INSTRUMENT> idToInstrumentMap;

	static {
		SPIRIT_ID_TO_INSTRUMENT_MAP.put("16005150ef", IGoal.INSTRUMENT.MICROSCOPE);
		SPIRIT_ID_TO_INSTRUMENT_MAP.put("17004aabcd", IGoal.INSTRUMENT.SPECTROMETER);
		SPIRIT_ID_TO_INSTRUMENT_MAP.put("16005163fd", IGoal.INSTRUMENT.DRILL); 
		SPIRIT_ID_TO_INSTRUMENT_MAP.put("1600518eab", IGoal.INSTRUMENT.BRUSH);

		// TODO: Populate these with the real RFIDs
		OPPY_ID_TO_INSTRUMENT_MAP.put("FOO", IGoal.INSTRUMENT.MICROSCOPE);
		OPPY_ID_TO_INSTRUMENT_MAP.put("FOO", IGoal.INSTRUMENT.SPECTROMETER);
		OPPY_ID_TO_INSTRUMENT_MAP.put("FOO", IGoal.INSTRUMENT.DRILL);
		OPPY_ID_TO_INSTRUMENT_MAP.put("FOO", IGoal.INSTRUMENT.BRUSH); 

		// TODO: Add the rest of the phidget IDs
		PHIDGET_ID_TO_TARGET_MAP.put(90673, TARGET.G1);

		if (ArenaServerApplication.getRobotName().equals(ROBOT.SPIRIT))
			idToInstrumentMap = SPIRIT_ID_TO_INSTRUMENT_MAP;
		else
			idToInstrumentMap = OPPY_ID_TO_INSTRUMENT_MAP;
	}

	public GoalHandler()  {
		try {
			for (Integer serial: PHIDGET_ID_TO_TARGET_MAP.keySet()) {
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

	public ArrayBlockingQueue<IGoal> getGoalQueue() {
		return goalQueue;
	}

	public void tagGained(TagGainEvent e) {
		//		System.err.println(e);
		try {
			IGoal.TARGET target = PHIDGET_ID_TO_TARGET_MAP.get(e.getSource().getSerialNumber());
			IGoal.INSTRUMENT instrument = idToInstrumentMap.get(e.getValue());
			Goal goal = new Goal(target, instrument);
			goalQueue.put(goal);

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
