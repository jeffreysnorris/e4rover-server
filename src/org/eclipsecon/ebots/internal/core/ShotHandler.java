package org.eclipsecon.ebots.internal.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipsecon.ebots.core.IShot;
import org.eclipsecon.ebots.internal.core.ArenaServerApplication.ROBOT;

import com.phidgets.PhidgetException;
import com.phidgets.RFIDPhidget;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.TagGainEvent;
import com.phidgets.event.TagGainListener;
import com.phidgets.event.TagLossEvent;
import com.phidgets.event.TagLossListener;

public class ShotHandler implements TagGainListener, TagLossListener, AttachListener {

	ArrayBlockingQueue<IShot> shotQueue = new ArrayBlockingQueue<IShot>(1);
	private RFIDPhidget phidget;
	public static final ShotHandler instance = new ShotHandler();
	private static Map<String, Shot.PUCK> SPIRIT_ID_TO_PUCK_MAP = new HashMap<String, Shot.PUCK>();
	private static Map<String, Shot.PUCK> OPPY_ID_TO_PUCK_MAP = new HashMap<String, Shot.PUCK>();
	private static Map<String, Shot.PUCK> idToPuckMap;
	static {
		// TODO: Populate these with the real RFIDs
		SPIRIT_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.A);
		SPIRIT_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.B);
		SPIRIT_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.C);
		SPIRIT_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.D);

		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.A);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.B);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.C);
		OPPY_ID_TO_PUCK_MAP.put("FOO", Shot.PUCK.D);
		
		if (ArenaServerApplication.getRobotName().equals(ROBOT.SPIRIT))
			idToPuckMap = SPIRIT_ID_TO_PUCK_MAP;
		else
			idToPuckMap = OPPY_ID_TO_PUCK_MAP;
	}
	
	public ShotHandler() {
		try {
			phidget = new RFIDPhidget();
			phidget.addAttachListener(this);
			phidget.addTagGainListener(this);
			phidget.addTagLossListener(this);
			phidget.openAny();
			phidget.waitForAttachment();
			phidget.close();
			phidget = null;
		} catch (PhidgetException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayBlockingQueue<IShot> getShotQueue() {
		return shotQueue;
	}
	
	public void tagGained(TagGainEvent e) {
		
	}

	public void tagLost(TagLossEvent e) {
			
	}

	public void attached(AttachEvent arg0) {
		System.err.println("yatta!");
 	}

	
	
}
