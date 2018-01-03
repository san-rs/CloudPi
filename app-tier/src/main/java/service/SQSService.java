package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class SQSService {

	String queueUrl;
	AmazonSQS sqs;

	public SQSService(String queueUrl) {
		this.queueUrl = queueUrl;
		sqs = AmazonSQSClientBuilder.standard().withRegion("us-west-2").build();
	}

	public void setQueueUrl(String queueUrl) {
		this.queueUrl = queueUrl;
	}

	public String getQueueUrl() {
		return queueUrl;
	}

	public boolean isRequestProcessed(String messageId) throws InterruptedException {
		// TODO Auto-generated method stub
		//System.out.println("Looking for : " + messageId);
		while (true) {
			List<Message> messages = getRequestFromQueue(queueUrl);
			if (messages.size() > 0) {
				for(Message message : messages) {
					//System.out.println(message.getBody());
					if(messageId.equals(message.getBody())) {
//						System.out.println("Request Processed by AppTier!");
						DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(queueUrl, message.getReceiptHandle());
						DeleteMessageResult deleteMessageResult = sqs.deleteMessage(deleteMessageRequest);
						//System.out.println(deleteMessageResult);
						return true;
					}
				}
			}
			new Thread().sleep(2000);
		}
	}

	public List<Message> getRequestFromQueue(String queueUrl) {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		return messages;
	}

	public int getNumberOfMessagesInQueue(String queueUrl) {
		List<String> attributeNames = new ArrayList<String>();
		attributeNames.add("ApproximateNumberOfMessages");
		GetQueueAttributesRequest sqsRequest = new GetQueueAttributesRequest(queueUrl, attributeNames);
		GetQueueAttributesResult sqsResponse = sqs.getQueueAttributes(sqsRequest);
		return Integer.valueOf(sqsResponse.getAttributes().get("ApproximateNumberOfMessages"));
	}
}
