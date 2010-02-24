package org.eclipsecon.ebots.internal.core;

import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipsecon.ebots.core.IS3Constants;

public class CommandRelay implements IS3Constants {

	private static final HttpClient client = new HttpClient();
	public final String playerName;
	public static final String BUCKET_NAME = "khawaja";
	private RobotController rc = RobotController.getDefault();



	private Boolean active = false;

	public CommandRelay(String playerName) {
		this.playerName = playerName;
	}

	public void start() {
		active = true;

		new Thread() {
			@Override
			public void run() {
				//start polling
				while (true) {
					try {

						// Get the latest command file. If the file can't be
						// read or hasn't changed at all since the last time we
						// read it, we'll get back a 0-length result and make
						// another read attempt.
						GetMethod get = new GetMethod(REST_BASE_URL + "cmd/" + playerName);

						try {
							client.executeMethod(get);
							byte[] msg = get.getResponseBody();
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
										rc.setWheelVelocity(wheel1, wheel2);
									}
									else 
										return;
								}
							}
						} finally {
							get.releaseConnection();
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

//		GameStatus game = new GameStatus();
//		game.enterCountdownState("Bob");
//		new CommandRelay("Bob").start();

		Commander cmder = new Commander("Bob");
		Random r = new Random();
		for (int i = 0; i<5000; ++i) {
			cmder.setWheelVelocity((100 - r.nextInt(200)), (100 - r.nextInt(200)));
			Thread.sleep(0);
		}
		Thread.sleep(30000);
	}
}
