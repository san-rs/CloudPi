package service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Service {

	String s3Bucket;
	AmazonS3 s3;

	public S3Service() {
		s3 = new AmazonS3Client();
		s3Bucket = "cloudpi-ram-suraj";
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public boolean isFileInS3(String fileName) {
		ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request().withBucketName(s3Bucket);
		ListObjectsV2Result result = s3.listObjectsV2(listObjectsRequest);
		for (S3ObjectSummary s3ObjectSummary : result.getObjectSummaries()) {
			//System.out.println(" - " + s3ObjectSummary.getKey() + "  " + "(size = " + s3ObjectSummary.getSize() + ")");
			if (s3ObjectSummary.getKey().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	public String getFileData(String fileName) {
		String line;
		StringBuilder fileContents = new StringBuilder();
		GetObjectRequest getObjectRequest = new GetObjectRequest(s3Bucket, fileName);
		S3Object s3Object = s3.getObject(getObjectRequest);
		BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()));
		try {
			while((line = reader.readLine()) != null) {
				fileContents.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileContents.toString();
	}

	public void writeDataToS3(String content, String fileName) {
		PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket, fileName, content);
		s3.putObject(putObjectRequest);
	}

	public void uploadObjectToS3(String fileName, String content) {
		try {
			File tempFile = File.createTempFile("temp", "temp");
			InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
			FileUtils.copyInputStreamToFile(stream, tempFile);
			PutObjectRequest putObjectRequest = new PutObjectRequest(s3Bucket, fileName, tempFile);
			s3.putObject(putObjectRequest);
			if(tempFile.exists()) {
				tempFile.delete();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
