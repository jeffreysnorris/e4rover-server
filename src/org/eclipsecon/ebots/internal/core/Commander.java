package org.eclipsecon.ebots.internal.core;

import java.io.IOException;

import gov.nasa.jpl.maestro.cloud.s3.S3Utils;


public class Commander {
	
	private final String bucket;
	
	public Commander(String bucket) {
		this.bucket = bucket;
	}
	
	
	

	public void sendCommand(String command) throws IOException {
		S3Utils.uploadFile(bucket, "command.xml", command);
	}

}
