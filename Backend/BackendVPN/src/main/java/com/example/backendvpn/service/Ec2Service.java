package com.example.backendvpn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Service
public class Ec2Service {

    private static final Logger log = LoggerFactory.getLogger(Ec2Service.class);
    private final Ec2Client ec2;

    @Value("${aws.security-group}")
    private String securityGroup;

    @Value("${aws.ami-id}")
    private String amiId;

    @Value("${aws.instance-type}")
    private String instanceType;

    @Value("${aws.key-pair-name}")
    private String keyPairName;

    public Ec2Service(Ec2Client ec2) {
        this.ec2 = ec2;
    }

    public String launchInstance() {
        RunInstancesRequest request = RunInstancesRequest.builder()
                .imageId(amiId)
                .instanceType(InstanceType.fromValue(instanceType))
                .minCount(1)
                .maxCount(1)
                .keyName(keyPairName)
                .securityGroupIds(securityGroup)
                .build();

        try {
            RunInstancesResponse response = ec2.runInstances(request);
            String instanceId = response.instances().get(0).instanceId();
            log.info("Launched instance ID: {}", instanceId);
            waitForInstanceRunning(instanceId);
            return instanceId;
        } catch (Ec2Exception e) {
            log.error("Failed to launch instance", e);
            throw new RuntimeException("EC2 launch failed", e);
        }
    }

    public String getInstancePublicIp(String instanceId) {
        DescribeInstancesResponse response = ec2.describeInstances(
                DescribeInstancesRequest.builder().instanceIds(instanceId).build()
        );
        return response.reservations().get(0).instances().get(0).publicIpAddress();
    }

    public void terminateInstance(String instanceId) {
        try {
            TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                    .instanceIds(instanceId).build();
            ec2.terminateInstances(request);
            log.info("Terminated instance: {}", instanceId);
        } catch (Exception e) {
            log.error("Termination failed", e);
        }
    }

    private void waitForInstanceRunning(String instanceId) {
        while (true) {
            DescribeInstancesResponse response = ec2.describeInstances(
                    DescribeInstancesRequest.builder().instanceIds(instanceId).build()
            );
            InstanceStateName state = response.reservations().get(0)
                    .instances().get(0).state().name();

            if (state == InstanceStateName.RUNNING) break;

            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted during wait", e);
            }
        }
    }
}
