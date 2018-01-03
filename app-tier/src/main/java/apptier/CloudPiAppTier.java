package apptier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import autoscaling.EC2Service;

public class CloudPiAppTier implements Runnable {
	static AmazonSQS sqs;
	static String queueUrl = "cloudpi-input.fifo";
	static S3Service s3Service;
	static EC2Service ec2Service;
	public CloudPiAppTier() {

	}

	public static void main(String[] args) {
		sqs = AmazonSQSClientBuilder.standard().withRegion("us-west-2").build();
		s3Service = new S3Service();
		ec2Service = new EC2Service();
		Thread thread = new Thread(new CloudPiAppTier());
		thread.start();
	}

	static List<Message> getRequestFromQueue(String queueUrl) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		return messages;
	}

	public void run() {
		// TODO Auto-generated method stub
		String instanceId = null;
		try {
			instanceId = getInstanceIdOfThisInstanceUsingUrl();
			System.out.println(instanceId);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (true) {
			List<Message> messages = getRequestFromQueue(queueUrl);
			if (messages.size() > 0) {
				Message message = messages.get(0);
				//System.out.println(message.getMessageId());
				System.out.println(message.getBody());
				// process the input using pifft
				String input = message.getBody();
				
				//set tag for this instance to be true
				ec2Service.setTagForInstance(instanceId, "isProcessing", "1");
				
				String output = calculatePi(input);
				
				//System.out.println(output);
				//push output to response or output queue
				pushResponseToQueue(message.getMessageId(), output, input);
				
				// delete the message from the queue
				DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl,
						message.getReceiptHandle());
				DeleteMessageResult deleteMessageResult = sqs.deleteMessage(deleteMessageRequest);
				try {
					new Thread().sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ec2Service.setTagForInstance(instanceId, "isProcessing", "0");
				//System.out.println(deleteMessageResult);
				//set tag of this instance to be false
			}
			System.out.println("App Sleeping");
		}
	}

	static void pushResponseToQueue(String messageId, String output, String fileName) {
		// write this output to a file in the S3
		// s3Service.writeDataToS3(output, fileName); - this is not working due
		// the issue in content length of the file contents
		s3Service.uploadObjectToS3(fileName, output);
		
		// push the messageId as response to the output queue for the web tier
		// to read it
		GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl("cloudpi-output.fifo");
		SendMessageRequest sendMessageRequest = new SendMessageRequest(getQueueUrlResult.getQueueUrl(), messageId);
		SendMessageResult sendMessageResult = sqs.sendMessage(sendMessageRequest);
		//System.out.println(sendMessageResult.getMessageId());
	}

	static String calculatePi(String numberOfIterations) {
		StringBuilder output = new StringBuilder();
		ProcessBuilder pb = new ProcessBuilder("/home/ec2-user/cloudpi-cloud/c-pi/src/main/resources/invoke_pifft.sh",
				numberOfIterations);
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				output.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output.toString();
	}

	static String getInstanceIdOfThisInstance() {
		StringBuilder output = new StringBuilder();
		ProcessBuilder pb = new ProcessBuilder("/home/ec2-user/cloudpi-cloud/c-pi/src/main/resources/get_instance_id.sh");
//		ProcessBuilder pb = new ProcessBuilder("ec2-metadata -i");		
		try {					
//			ProcessBuilder pb = new ProcessBuilder("ec2-metadata", "-i");
//			Process p = Runtime.getRuntime().exec("ec2-metadata", "-i");
			Process p = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
				output.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		String[] words = output.toString().split(" ");
		for (String w : words) {
			System.out.println(w);
		}
		return null;
	}

	public static String getInstanceIdOfThisInstanceUsingUrl() throws Exception {
		String instanceId = "", line ="";
		URL url = new URL("http://169.254.169.254/latest/meta-data/instance-id");
		URLConnection urlConnection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		while ((line = in.readLine()) != null) {
			instanceId = line;
		}
		in.close();
		return instanceId;
	}
}
