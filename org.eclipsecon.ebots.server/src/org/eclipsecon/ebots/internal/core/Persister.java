package org.eclipsecon.ebots.internal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipsecon.e4rover.core.IGame;
import org.eclipsecon.e4rover.core.IPlayer;
import org.eclipsecon.e4rover.core.IRobot;
import org.eclipsecon.e4rover.core.IServerConstants;
import org.eclipsecon.e4rover.core.XmlSerializer;
import org.eclipsecon.ebots.s3.S3Utils;

public class Persister {
	
	private static final String EBOTS_BUCKET_NAME = "ebots";
	public static final XmlSerializer serializer;
	protected static HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

	static {
		serializer = new XmlSerializer();
		serializer.getXstream().alias("game", GameStatus.class);
		serializer.getXstream().omitField(GameStatus.class, "gameStartTimeMillis");
		serializer.getXstream().omitField(GameStatus.class, "gameEndTimeMillis");
	}
	
	public static void updateToServer(IGame game) {
		try {
			S3Utils.uploadFile(EBOTS_BUCKET_NAME, IServerConstants.GAME_FILE_NAME, 
					serializer.toXML(game),true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void updateToServer(IPlayer player, String hash) {
		
		PutMethod put = new PutMethod(IServerConstants.EROVER_UPLINK_SERVER_URI + "player/" +hash);
		try {
			put.setRequestEntity(new StringRequestEntity(serializer.toXML(player), null, "UTF-8"));
			httpClient.executeMethod(put);
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			put.releaseConnection();
		}
		
	}
	
	public static void updateToServer(IRobot robot) {
		try {
			S3Utils.uploadFile(EBOTS_BUCKET_NAME, IServerConstants.ROBOT_FILE_NAME, robot.toString(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		S3Utils.makeBucketPublicReadable(EBOTS_BUCKET_NAME);
	}

	public static List<String> getCurrentPlayerAndHashFromQueue() {
		GetMethod get = new GetMethod(IServerConstants.QUEUE_RESTLET);
		List<String> ret = new ArrayList<String>();
		try {
			int resp = httpClient.executeMethod(get);
			if (resp != HttpStatus.SC_OK) {
				return ret;
			}
			ret.add(get.getResponseHeader(IServerConstants.HASH).getValue());
			ret.add(get.getResponseBodyAsString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			get.releaseConnection();
		}
		
		return ret;
		
	}

	public static Object fromXML(String s) {
		return serializer.fromXML(s);
	}
}
