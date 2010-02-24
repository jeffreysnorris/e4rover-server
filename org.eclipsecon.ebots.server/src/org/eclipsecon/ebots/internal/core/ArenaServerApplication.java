package org.eclipsecon.ebots.internal.core;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class ArenaServerApplication implements IApplication {

	private GameController gameController;

	enum ROBOT {SPIRIT, OPPY}
	private static ROBOT robotName = ROBOT.SPIRIT;

	public Object start(IApplicationContext context) throws Exception {
		String args[]=(String[]) context.getArguments().get("application.args");
		if (args.length > 0) {
			if (args[0].equals(ROBOT.SPIRIT.toString())) 
				robotName = ROBOT.SPIRIT;
			else if (args[0].equals(ROBOT.OPPY.toString())) 
				robotName = ROBOT.OPPY;
			else
				throw new IllegalArgumentException("Unrecognized rover name " + args[0]);
		}
		RobotController.getDefault();

		gameController = new GameController();
		gameController.start();
		System.in.read();  // Shutdown when enter key pressed
		
		// SHUTDOWN
		gameController.shutdown();
		RobotController.getDefault().shutdown();
		
		return null;
	}

	public static ROBOT getRobotName() {
		return robotName;
	}

	public void stop() {
		// Do nothing		
	}

}
