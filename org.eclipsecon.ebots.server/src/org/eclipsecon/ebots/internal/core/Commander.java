package org.eclipsecon.ebots.internal.core;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.eclipsecon.ebots.core.IServerConstants;


public class Commander implements IServerConstants {
	
	private static final HttpClient client = new HttpClient();
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
