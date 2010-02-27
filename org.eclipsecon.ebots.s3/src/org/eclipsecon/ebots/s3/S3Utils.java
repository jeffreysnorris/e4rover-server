package org.eclipsecon.ebots.s3;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Platform;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;
import org.osgi.framework.Bundle;

public class S3Utils {


	static {
		Bundle bundle = Platform.getBundle("gov.nasa.jpl.maestro.cloud.s3");
		if (bundle != null)
		try {
			String name = bundle.getLocation()+File.separator+"s3.properties";
			System.getProperties().load(new FileInputStream(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static final String LOCATION = "us-west-1";
	private static final String awsAccessKey = System.getProperty("key");
	private static final String awsSecretKey = System.getProperty("password");
	private static final AWSCredentials awsCredentials = 
		new AWSCredentials(awsAccessKey, awsSecretKey);
	private static final S3Service s3Service = getService();

	private static S3Service getService() {
		try {
			return new RestS3Service(awsCredentials);
		} catch (S3ServiceException e) {
			e.printStackTrace();
			return null;
		}
	}


	public static void makeBucketPublicReadable(String bucketName) throws IOException {
		try {
			S3Bucket bucket = s3Service.getBucket(bucketName);
			bucket.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
			s3Service.putBucketAcl(bucket);
		} catch (S3ServiceException e) {
			throw new IOException(e.toString());
		}
	}

	public static void setBucketVersioning(String bucket, boolean on) throws IOException {

		try {
			if (on)
				s3Service.enableBucketVersioning(bucket);
			else
				s3Service.suspendBucketVersioning(bucket);
		} catch (S3ServiceException e) {
			throw new IOException(e.toString());
		}
	}
	public static String downloadFileAsByteArrayIfDifferent(String bucketName, String name, String oldVersion, OutputStream os) throws IOException {

		S3Object obj = null;
		try {
			obj = s3Service.getObject(s3Service.getBucket(bucketName), name);
			String versionId = obj.getVersionId();
			if (!versionId.equals(oldVersion)) {
				InputStream is = obj.getDataInputStream();
				IOUtils.copy(is, os);
				return versionId;
			}

		} catch (S3ServiceException e) {
			throw new IOException(e.toString());
		} finally {
			if (obj != null) obj.closeDataInputStream();
		}
		return oldVersion;
	}




	public static void downloadFile(String bucketName, String name, OutputStream result) throws IOException {
		S3Object obj = null;
		try {
			obj = s3Service.getObject(new S3Bucket(bucketName), name);

			IOUtils.copy(obj.getDataInputStream(), result);
		} catch (S3ServiceException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} finally {
			if (obj !=  null) obj.closeDataInputStream();
		}
	}

	public static void uploadFile(String bucketName, String name, File data) throws IOException {
		uploadFile(bucketName, name, data, false);
	}





	public static void uploadFile(String bucketName, String name, File data,
			boolean publicReadable) throws IOException {
		uploadFile(bucketName, name, new BufferedInputStream(new FileInputStream(data)), publicReadable);

	}


	/**
	 * Uploads the file to S3 and does not make it readable by the public.
	 * @param bucketName the bucket in which you wish to place the data
	 * @param name name of the file on s3
	 * @param data the content of the file as a string
	 * @throws IOException
	 */
	public static void uploadFile(String bucketName, String name, String data) throws IOException {
		uploadFile(bucketName, name, data, false);
	}

	public static void uploadFile(String bucketName, String name, String data, boolean publicReadable)  throws IOException {
		uploadFile(bucketName, name, new ByteArrayInputStream(data.getBytes()), publicReadable);
	}
	/**
	 * Uploads the file to the S3 and may make it public readable depending on publicReadable
	 * @param bucketName the bucket in which you wish to place the data
	 * @param name name of the file on s3
	 * @param data the content of the file as a stream
	 * @param publicReadable if the file should be readable by the public
	 * @throws IOException
	 */
	public static void uploadFile(String bucketName, String name, InputStream data, boolean publicReadable)  throws IOException {
		S3Bucket bucket = new S3Bucket(bucketName, LOCATION);

		try {
			S3Object obj = new S3Object(name);
			obj.setDataInputStream(data);
			if (publicReadable) 
				obj.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
			obj.setContentLength(data.available());
			s3Service.putObject(bucket, obj);
		} catch (Exception e) {
			throw new IOException(e.toString());
		} finally {
			data.close();
		}
	}

	public static void createBucket(String bucketName) throws IOException {
		S3Bucket bucket = new S3Bucket(bucketName, LOCATION);

		try {
			s3Service.createBucket(bucket);
		} catch (S3ServiceException e) {
			throw new IOException(e.toString());
		}

	}
	
	public static S3Service getS3service() {
		return s3Service;
	}

	public static void main(String[] args) throws NoSuchAlgorithmException, S3ServiceException, IOException {
		for (int j=0; j<100; ++j) {
			new Thread() {
				public void run() {

					long time = System.currentTimeMillis();
					for (int i=0; i<68; ++i) {
						String name = "cebots" + DigestUtils.shaHex(i+"");
						S3Bucket b = new S3Bucket(name, LOCATION);
						try {
							s3Service.listObjects(b);
						} catch (S3ServiceException e) {
							e.printStackTrace();
						}
						System.err.println(i);
					}
					System.err.println(System.currentTimeMillis() - time);
				}
			}.start();
		}
		//		S3Bucket b = new S3Bucket("desttest");
		//		S3Object o = new S3Object("test3.txt", "helloworld!");
		//		s3Service.putObject(b, o);
		//
		//		S3Object o2 = s3Service.getObject(b, "test3.txt");
		//		InputStream dis = o2.getDataInputStream();
		//		ByteArrayOutputStream os = new ByteArrayOutputStream();
		//		IOUtils.copy(dis,os);
		//		System.err.println(new String(os.toByteArray()));


	}


	public static void lockBucket(String bucketName) throws IOException {
		try {
			S3Bucket bucket = s3Service.getBucket(bucketName);
			AccessControlList acl = new AccessControlList();
			acl.setOwner(bucket.getOwner());
			bucket.setAcl(acl);
			S3Object[] objs = s3Service.listObjects(bucket);
			for (S3Object obj : objs) { //lock each objs
				obj.setAcl(acl);
				s3Service.putObjectAcl(bucket, obj);
			}
			s3Service.putBucketAcl(bucket);
		} catch (S3ServiceException e) {
			e.printStackTrace();
			throw new IOException(e.toString());
		}
	}
	

}
