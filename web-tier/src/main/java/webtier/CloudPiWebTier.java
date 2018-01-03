package webtier;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import apptier.S3Service;

public class CloudPiWebTier {
	static AmazonEC2 amazonEc2Client;
	static AmazonS3 s3Client;
	static AmazonSQS sqs;
	static String keyName = "aws-ram-suraj";
	static String sgName = "default";
	static String applicationTierTag = "cloudpi-app-tier";
	static String s3Bucket = "cloudpi-ram-suraj";
	static SQSService sqsService;
	static S3Service s3Service;

	public static void main(String args[]) throws InterruptedException {
		amazonEc2Client = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").build();
		s3Client = new AmazonS3Client();
		sqs = AmazonSQSClientBuilder.standard().withRegion("us-west-2").build();
		sqsService = new SQSService("cloudpi-output.fifo");
		s3Service = new S3Service();
		String messageIdOfRequest, output;
		if (args.length != 0) {
			//findApplicationInstances(args[0]);
			if(!s3Service.isFileInS3(args[0])) {
				messageIdOfRequest = pushRequestToQueue(args[0]);
				output = pollResponseQueueForOutput(messageIdOfRequest, args[0]);
			} else {
				//file is present in S3, so fetch the content of the file
				output = s3Service.getFileData(args[0]);
			}
			System.out.println(output);
			//System.out.println("OutPut in Web Tier Read from the S3 bucket:\n" + output);
		} else {
			System.out.println("Invalid Arguments!");
		}
	}

	static String pollResponseQueueForOutput(String messageIdOfRequest, String fileName) throws InterruptedException {
		String output = "";
		//System.out.println("Poll started");
		sqsService.isRequestProcessed(messageIdOfRequest);
		//if(sqsService.isRequestProcessed(messageIdOfRequest)) {
			//System.out.println("Now delay before fetch result from S3");
			new Thread().sleep(500);
			//System.out.println("Now fetch result from S3");
			output = s3Service.getFileData(fileName);
		//}
		return output;
	}

	static String pushRequestToQueue(String numberOfIterations) {
		GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl("cloudpi-input.fifo");
		SendMessageRequest sendMessageRequest = new SendMessageRequest(getQueueUrlResult.getQueueUrl(), numberOfIterations);
		SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
		//System.out.println(sendMessageResult.getMessageId());
		return sendMessageResult.getMessageId();
	}

	static void findApplicationInstances(String numberOfIterations) throws InterruptedException {
		//System.out.println("Application Tier Instances:");
		DescribeInstancesResult result = amazonEc2Client.describeInstances();
		List<Reservation> listReservations = result.getReservations();
		for (Reservation reservation : listReservations) { // each reservation
															// will have a group
															// of instances
			List<Instance> listInstances = reservation.getInstances();
			for (Instance instance : listInstances) {
				for (Tag tag : instance.getTags()) {
					if (tag.getValue().equals(applicationTierTag)) {
						// connect to the server if its eligible
						//System.out.println(instance.getPublicDnsName());
						try {
							try {
								connectToInstance(instance.getPublicDnsName(), numberOfIterations);
							} catch (JSchException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	static void connectToInstance(String instanceDNSName, String numberOfIterations) throws JSchException, IOException {
		JSch jsch = new JSch();
		String keyLocationInLocalInEC2 = "/home/ec2-user/cloudpi-cloud/c-pi/aws-new-key.pem";
		String keyLocationInLocal = "/Users/ramanathan/Documents/learning/AWS First Program/src/aws-new-key.pem";
		jsch.addIdentity(keyLocationInLocal);
		jsch.setConfig("StrictHostKeyChecking", "no");

		Session session = jsch.getSession("ec2-user", instanceDNSName, 22);
		session.connect();

		ChannelExec channel = (ChannelExec) session.openChannel("exec");
		BufferedReader in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
		StringBuilder commands = new StringBuilder("cd /home/ec2-user/cloudpi/pifft;");
		commands.append("echo " + numberOfIterations + " > test.in;");
		commands.append("./pifft test.in > output.txt;");
		commands.append("cat output.txt;");
		commands.append("echo 'All Commands Executed Maven';");

		channel.setCommand(commands.toString());
		channel.connect();

		String msg = null;
		StringBuilder cPiResult = new StringBuilder();
		while ((msg = in.readLine()) != null) {
			//System.out.println(msg);
			cPiResult.append(msg);
		}
		channel.disconnect();
		session.disconnect();
		//uploadObjectToS3(numberOfIterations, cPiResult.toString());
	}

	/*
	 * static void connectToInstance(String instanceDNSName) throws IOException,
	 * InterruptedException { Runtime rt = Runtime.getRuntime(); String[]
	 * commands = {
	 * "/Users/ramanathan/Documents/learning/AWS First Program/src/helloworld.sh"
	 * , instanceDNSName };
	 * 
	 * Process p = rt.exec(commands); BufferedReader input = new
	 * BufferedReader(new InputStreamReader(p.getInputStream())); BufferedReader
	 * stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	 * 
	 * System.out.println("Here is the standard output of the command:\n");
	 * String s = null; while ((s = input.readLine()) != null) {
	 * System.out.println(s); } p.waitFor(); }
	 */
}