package org.eclipsecon.ebots.internal.core;

import gov.nasa.jpl.maestro.cloud.s3.S3Utils;

import java.io.ByteArrayOutputStream;

public class CommandRelay {

	public final String bucket;
	public static final String BUCKET_NAME = "khawaja";
	private String lastCommandVersion = "";

	private Boolean active = false;

	public CommandRelay(String bucket) {
		this.bucket = bucket;
	}

	public void start() {
		active = true;

		new Thread() {
			@Override
			public void run() {
				//start polling
				while (true) {
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						
						// Get the latest command file. If the file can't be
						// read or hasn't changed at all since the last time we
						// read it, we'll get back a 0-length result and make
						// another read attempt.
						lastCommandVersion = S3Utils.downloadFileAsByteArrayIfDifferent(BUCKET_NAME, "command.csv", lastCommandVersion, baos);
						byte[] msg = baos.toByteArray();
						if (msg.length > 0) {
							
							//We have a new command update, now we try to send it to the robot controller.
							
							synchronized (active) { //make sure we haven't had a stop() request
								if (active) {
									
									//split the string looking for two whitespace-delimited integers between -100, 100
									String s = new String (msg);
									System.out.println(s);
									String[] tokens = s.split(",");
									if (tokens.length != 2)
										continue;
									int wheel1 = 0, wheel2 = 0;
									try {
										wheel1 = Integer.parseInt(tokens[0]);
										wheel2 = Integer.parseInt(tokens[1]);
									} catch (NumberFormatException e) {
										System.err.println("Bad integer syntax"+e);
										continue;
									}
		
									if (!areValidArguments(wheel1, wheel2))
										continue;
									
									//send the command to the robot controller here
									RobotController.setWheelVelocity(wheel1, wheel2);
								}
								else 
									return;
							}
						}
					} catch (Exception e1) {
//						e1.printStackTrace();
					}
					try {
						sleep(100);
					} catch (InterruptedException e) {/*empty*/}
				}
			}

			private boolean areValidArguments(int wheel1, int wheel2) {
				return wheel1 >= -100 && wheel1 <= 100 && wheel2 >= -100 && wheel2 <= 100;
			}
			
		}.start();
	}

	public void stop() {
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "info");
		//disable polling
		synchronized(active) {
			active = false;
		}
	}
	
	public static void main(String[] args) throws Exception {

		new CommandRelay(BUCKET_NAME).start();

		Commander cmder = new Commander(BUCKET_NAME);
		for (int i = 0; i<500; ++i) {
			String nxtCmd = "0,0";
			cmder.sendCommand(nxtCmd);
			Thread.sleep(0);
		}
		Thread.sleep(30000);
	}
}
