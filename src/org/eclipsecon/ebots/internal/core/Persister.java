package org.eclipsecon.ebots.internal.core;

import gov.nasa.jpl.maestro.cloud.s3.S3Utils;

import java.io.IOException;

import org.eclipsecon.ebots.core.IGame;
import org.eclipsecon.ebots.core.IPlayers;
import org.eclipsecon.ebots.core.IRobot;

public class Persister {

	private static final String EBOTS_BUCKET_NAME = "ebots";
	public static void updateToServer(IGame game) {
		try {
			S3Utils.uploadFile(EBOTS_BUCKET_NAME, ServerObject.GAME_FILE_NAME, game.toString(),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateToServer(IPlayers players) {
		try {
			S3Utils.uploadFile(EBOTS_BUCKET_NAME, ServerObject.PLAYERS_FILE_NAME, players.toString(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateToServer(IRobot robot) {
		try {
			S3Utils.uploadFile(EBOTS_BUCKET_NAME, ServerObject.ROBOT_FILE_NAME, robot.toString(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		S3Utils.makeBucketPublicReadable(EBOTS_BUCKET_NAME);
	}
}
