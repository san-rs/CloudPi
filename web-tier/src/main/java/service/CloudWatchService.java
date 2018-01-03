package service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;

public class CloudWatchService {
	private static final Comparator<? super Datapoint> DataPointTimeStampComparator = null;
	private AmazonCloudWatch cloudWatch;
	static String AccessKey = "AKIAJN6ZOFKUNGBTJVFQ";
	static String SecretKey = "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";

	public CloudWatchService() {
		cloudWatch = new AmazonCloudWatchClient(new BasicAWSCredentials(AccessKey, SecretKey));
		cloudWatch.setEndpoint("monitoring.us-west-2.amazonaws.com");
	}

	public AmazonCloudWatch getCloudWatch() {
		return cloudWatch;
	}

	public void setCloudWatch(AmazonCloudWatch cloudWatch) {
		this.cloudWatch = cloudWatch;
	}

	static class DataPointTimeStampComparator implements Comparator<Datapoint> {
		public int compare(Datapoint first, Datapoint second) {
			return first.getTimestamp().compareTo(second.getTimestamp());
		}
	}

	public GetMetricStatisticsResult getMetricsOfEC2(String instanceId, long offsetInSeconds) {
		long offsetInMilliSeconds = offsetInSeconds * 60 * 1000;
		Dimension instanceDimension = new Dimension();
		instanceDimension.setName("InstanceId");
		instanceDimension.setValue(instanceId);

		System.out.println(new Date(new Date().getTime() - offsetInMilliSeconds));
		System.out.println(new Date());
		GetMetricStatisticsRequest getMetricStatisticsRequest = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - offsetInMilliSeconds)).withNamespace("AWS/EC2")
				.withPeriod(60).withMetricName("CPUUtilization").withStatistics("Maximum")
				.withDimensions(Arrays.asList(instanceDimension)).withEndTime(new Date());

		GetMetricStatisticsResult getMetricStatisticsResult = cloudWatch
				.getMetricStatistics(getMetricStatisticsRequest);
		return getMetricStatisticsResult;
	}
}
