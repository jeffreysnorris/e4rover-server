package org.eclipsecon.ebots.internal.core;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipsecon.e4rover.core.IServerConstants;

/**
 * Just a class for testing client commanding.  Sends random commands.
 */
public class CommandRelay implements IServerConstants {

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
						GetMethod get = new GetMethod(EROVER_UPLINK_SERVER_URI + "cmd/" + playerName);

						try {
							long time = System.currentTimeMillis();
							client.executeMethod(get);
							System.err.println(System.currentTimeMillis() - time);
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
										long time2 = System.currentTimeMillis();
										rc.setWheelVelocity(wheel1, wheel2);
										System.err.println("Time to command: " + (System.currentTimeMillis() - time2));
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
		//disable polling
		synchronized(active) {
			active = false;
		}
		rc.setWheelVelocity(0, 0);
	}

	public static void main(String[] args) throws Exception {

		Commander cmder = new Commander("31fe55312df65416fb4a31ad68cd7b1a40304226");
		Random r = new Random();
		for (int i = 0; i<5000; ++i) {
			long time = System.currentTimeMillis();
			cmder.setWheelVelocity((100 - r.nextInt(200)), (100 - r.nextInt(200)));
			Thread.sleep(100);
			System.err.println(System.currentTimeMillis() - time);
		}
		Thread.sleep(30000);
	}
	


	static class Commander implements IServerConstants {
		
		private  final HttpClient client = new HttpClient();
		private final String playerHash;
		
		public Commander(String playerHash) {
			this.playerHash = playerHash;
		}
		
		public void setWheelVelocity(int leftWheel, int rightWheel) throws IOException {
			PostMethod post = new PostMethod(EROVER_UPLINK_SERVER_URI + "cmd/" + playerHash );
			try {
				String command = leftWheel + "," + rightWheel;
				post.setRequestEntity(new StringRequestEntity(command, "text/xml", "UTF-8"));
				client.executeMethod(post);
			} finally {
				post.releaseConnection();
			}
			
		}

	}

}
