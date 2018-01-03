package service;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class EC2Service {
	private AmazonEC2 amazonEc2Client;
	private String keyName;
	private String sgName;
	static int instanceNumber;
	CloudWatchService cloudWatchService;

	public EC2Service() {
		amazonEc2Client = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").build();
		cloudWatchService = new CloudWatchService();
		keyName = "aws-ram-suraj";
		sgName = "cloudpi";
	}

	public EC2Service(String securityGroupName) {
		amazonEc2Client = AmazonEC2ClientBuilder.standard().withRegion("us-west-2").build();
		keyName = "aws-ram-suraj";
		sgName = securityGroupName;
	}

	public AmazonEC2 getAmazonEc2Client() {
		return amazonEc2Client;
	}

	public void setAmazonEc2Client(AmazonEC2 amazonEc2Client) {
		this.amazonEc2Client = amazonEc2Client;
	}

	public String createInstance(String imageId, int instanceIndex) {
		/*StringBuilder userData = new StringBuilder("#!/bin/bash\n");
		userData.append("service initiate-cloudpi-app-tier start\n");
		userData.append("chkconfig initiate-cloudpi-app-tier on\n");
		userData.append("mkdir allclear");
		UserData u = new UserData();
		u.setData(userData.toString());
		String formattedUserData = Base64.encodeBase64String(userData.toString().getBytes());
		*/
		RunInstancesRequest run = new RunInstancesRequest();
		run.withImageId(imageId).withInstanceType("t2.micro").withMinCount(1).withMaxCount(1).withKeyName(keyName)
				.withSecurityGroups(sgName);
		run.withMonitoring(true);
		//run.withUserData(formattedUserData);
		RunInstancesResult result = amazonEc2Client.runInstances(run);
		List<Instance> instances = result.getReservation().getInstances();
		for (Instance instance : instances) {
			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			createTagsRequest.withResources(instance.getInstanceId()) //
					.withTags(new Tag("Name", "cloudpi-app-tier-" + instanceIndex), new Tag("isProcessing","0"));
			amazonEc2Client.createTags(createTagsRequest);
			instanceIndex++;
		}

		System.out.println("Instance Description:" + result.toString());
		return result.getReservation().getInstances().get(0).getInstanceId();
	}

	public void setTagForInstance(String instanceId, String key, String value) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		describeInstancesRequest.setInstanceIds(instanceIds);
		DescribeInstancesResult result = amazonEc2Client.describeInstances(describeInstancesRequest);
		List<Reservation> listReservations = result.getReservations();
		for (Reservation reservation : listReservations) { // each reservation
															// will have a group
															// of instances
			List<Instance> listInstances = reservation.getInstances();
			for (Instance instance : listInstances) {
				if(instance.getInstanceId().equals(instanceId)) {
					CreateTagsRequest createTagsRequest = new CreateTagsRequest();
					createTagsRequest.withResources(instance.getInstanceId()) //
							.withTags(new Tag(key,value));
					amazonEc2Client.createTags(createTagsRequest);
					System.out.println("tag changed");
				}
			}
		}
	}

	public void startInstance(String instanceId) {
	}

	public void stopInstance(String instanceId) {
		StopInstancesRequest stop = new StopInstancesRequest();
		stop.withInstanceIds(instanceId);
		StopInstancesResult result = amazonEc2Client.stopInstances(stop);
		System.out.println("Stop Instance Request Result: " + result.toString());
	}

	public void terminateInstance(String instanceId) {
		TerminateInstancesRequest terminate = new TerminateInstancesRequest();
		terminate.withInstanceIds(instanceId);
		TerminateInstancesResult result = amazonEc2Client.terminateInstances(terminate);
		System.out.println("Terminate Instance Request Result: " + result.toString());
	}

	public void listInstances() {
		System.out.println("List Instances:");
		DescribeInstancesResult result = amazonEc2Client.describeInstances();
		List<Reservation> listReservations = result.getReservations();
		for (Reservation reservation : listReservations) { // each reservation
															// will have a group
															// of instances
			List<Instance> listInstances = reservation.getInstances();
			for (Instance instance : listInstances) {
				System.out.println(instance.getState().getName());
				System.out.println("Instance Description: " + instance.toString());
			}
		}
	}

	public int getNumberOfRunningAppInstances() {
		DescribeInstancesResult result = amazonEc2Client.describeInstances();
		List<Reservation> listReservations = result.getReservations();
		int numberOfRunningAppInstances = 0;
		for (Reservation reservation : listReservations) { // each reservation
															// will have a group
															// of instances
			List<Instance> listInstances = reservation.getInstances();
			for (Instance instance : listInstances) {
				if(instance.getState().getName().equals("running") || instance.getState().getName().equals("rebooting") || instance.getState().getName().equals("pending")) {
					for (Tag tag : instance.getTags()) {
						if (tag.getKey().equals("Name") && tag.getValue().contains("cloudpi-app-tier")) {
							numberOfRunningAppInstances++;
						}
					}
				}
				// System.out.println(instance.getState().getName());
				// System.out.println("Instance Description: " +
				// instance.toString());
			}
		}
		return numberOfRunningAppInstances;
	}

	public boolean isInstanceIdle(String instanceId) throws InterruptedException {
		boolean isIdle = false;
		Double cpuThreshold = new Double(0.25);
		int counter = 0;
		GetMetricStatisticsResult getMetricStatisticsResult = cloudWatchService.getMetricsOfEC2(instanceId, 10);
		// To read the Data
		for (Datapoint dp : getMetricStatisticsResult.getDatapoints()) {
			//System.out.println(dp.getTimestamp() + ":" + dp.getMaximum());
			if(dp.getMaximum() <= cpuThreshold) {
				isIdle = true;
			}
		}
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(instanceId);
		describeInstancesRequest.setInstanceIds(instanceIds);
		DescribeInstancesResult result = amazonEc2Client.describeInstances(describeInstancesRequest);
		List<Reservation> listReservations = result.getReservations();
		for (Reservation reservation : listReservations) { // each reservation
															// will have a group
															// of instances
			List<Instance> listInstances = reservation.getInstances();
			for (Instance instance : listInstances) {
				if(instance.getInstanceId().equals(instanceId)) {
					for(Tag tag:instance.getTags()) {
						if(tag.getKey().equals("isProcessing")) {
							if(tag.getValue().equals("1")) {
								isIdle = false;
							} else {
								isIdle = true;
							}
						}
					}
				}
			}
		}
		
		return isIdle;
	}
}
