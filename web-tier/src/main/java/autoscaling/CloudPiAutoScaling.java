package autoscaling;

import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.sqs.model.Message;

import webtier.SQSService;

public class CloudPiAutoScaling {

	public static void main(String[] args) throws InterruptedException {
		CloudWatchService cloudWatchService = new CloudWatchService();
		EC2Service ec2Service = new EC2Service();
		SQSService sqsService = new SQSService("cloudpi-input");
		String cloudPiAppTierNameprefix = "cloudpi-app-tier-";
		HashMap<String,String> instanceIdMap = new HashMap<String,String>();
		int instanceIndex = 1;
		instanceIdMap.put("cloudpi-app-tier-1", "i-06cb5d399eba3a5f3"); //this is hard coded for now, later change it to name, instanceId which should be obtained from AWS
		String imageId = "ami-fdae249d";
		while(true) {
			int numberOfPendingRequests = sqsService.getNumberOfMessagesInQueue("cloudpi-input");
			int numberOfRunningAppInstances = ec2Service.getNumberOfRunningAppInstances();
			System.out.println("Number of Requests in Input Queue : " + numberOfPendingRequests);
			System.out.println("Number of App Instances running : " + numberOfRunningAppInstances);
			
			//ec2Service.createInstance("ami-f3911f93");
			//ec2Service.isInstanceIdle("i-0202efcb6a7e5a914");
			if(numberOfPendingRequests > numberOfRunningAppInstances) {
				//need to scale up
				if((numberOfPendingRequests-numberOfRunningAppInstances)>=1) {
					System.out.println("App tier needs to be scaled up!");
					if(instanceIndex == 10) {
						System.out.println("But Max Limit reached!");
					} else {
						System.out.println("Scaling up...");
						instanceIndex++;
						String instanceId = ec2Service.createInstance(imageId,instanceIndex);
						instanceIdMap.put(cloudPiAppTierNameprefix+Integer.valueOf(instanceIndex).toString(),instanceId);
					}
				}
			} else if (numberOfPendingRequests < numberOfRunningAppInstances) {
				System.out.println("App tier needs to be scaled down!");
				if(instanceIndex == 1) {
					System.out.println("But Min Limit reached!");
				} else {
					String lastCreatedInstanceName = cloudPiAppTierNameprefix+Integer.valueOf(instanceIndex).toString(); 
					//System.out.println(lastCreatedInstanceName);
					//System.out.println(ec2Service.isInstanceIdle(instanceIdMap.get(lastCreatedInstanceName)));
					if(ec2Service.isInstanceIdle(instanceIdMap.get(lastCreatedInstanceName))) {
						System.out.println("Scaling Down...");
						String id = instanceIdMap.get(lastCreatedInstanceName);
						ec2Service.terminateInstance(id);
						instanceIdMap.remove(lastCreatedInstanceName)
	;					instanceIndex--;
					}
				}
				//need to scale down - after monitoring for 10 seconds based on CPU utilization and if confirmed if a CPU is idle or not
				//end condition , number of running instances should atleast be 1, else do not scale down
				
			}
			Thread.sleep(10000);
			System.out.println();
		}
	}
}
