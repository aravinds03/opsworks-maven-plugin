package com.n3twork.maven.opsworks;

import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.opsworks.model.*;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal implementation; just what we need
 * Deploys an app to all instances
 */
@Mojo(name = "deploy-app", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployAppMojo extends OpsworksMojo {

    @Parameter(property = "appName", required = true)
    private String appName;

    @Parameter(property = "customJsonOverride", required = false)
    private String customJsonOverride;

    public void run() throws IOException {
        List<Stack> stacks = opsworksUtil.getStacksByName(stackName);
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("No stack found with name '" + stackName + "'");
        }

        for (Stack stack : stacks) {
            List<App> apps = opsworksUtil.getAppsByName(stack.getStackId(), appName);
            if (apps.isEmpty()) {
                throw new IllegalArgumentException("No app found with name '" + appName + "'");
            }

            List<Instance> instances =
                    opsworks.describeInstances(new DescribeInstancesRequest().withStackId(stack.getStackId())).getInstances();
            List<String> instanceIds = new ArrayList<String>(instances.size());
            for (Instance instance : instances) {
                instanceIds.add(instance.getInstanceId());
            }

            for (App app : apps) {
                CreateDeploymentRequest request = new CreateDeploymentRequest();
                request.setStackId(stack.getStackId());
                request.setAppId(app.getAppId());
                request.setCustomJson(customJsonOverride);

                if (instanceIds.size() > 0) {
                    request.setInstanceIds(instanceIds);
                }

                opsworks.createDeployment(request);
            }
        }
    }
}
