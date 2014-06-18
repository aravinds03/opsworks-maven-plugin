package com.n3twork.maven.opsworks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import com.amazonaws.services.opsworks.model.App;
import com.amazonaws.services.opsworks.model.CreateDeploymentRequest;
import com.amazonaws.services.opsworks.model.DeploymentCommand;
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest;
import com.amazonaws.services.opsworks.model.Instance;
import com.amazonaws.services.opsworks.model.Source;
import com.amazonaws.services.opsworks.model.SourceType;
import com.amazonaws.services.opsworks.model.Stack;
import com.amazonaws.services.opsworks.model.UpdateAppRequest;

/**
 * Minimal implementation; just what we need
 * Deploys an app to all instances
 */
@Mojo(name = "deploy-app", defaultPhase = LifecyclePhase.DEPLOY)
public class DeployAppMojo extends OpsworksMojo {

    @Parameter(property = "appName", required = true)
    private String appName;

    @Parameter(defaultValue = "deploy", property = "command", required = true)
    private String command;

    @Parameter(property = "customJsonOverride", required = false)
    private String customJsonOverride;

    @Parameter(property = "httpUrl", required = false)
    private String httpUrl;

    @Parameter(defaultValue="${project.version}")
    private String projectVersion;
    
    public void run() throws IOException {
    	// do not deploy snapshots.
    	log.info("DeployAppMojo project.version="+projectVersion);
    	if(projectVersion != null && projectVersion.contains("SNAPSHOT")) {
    		return;
    	}
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
                if (instance.getStatus().equals("online"))
                    instanceIds.add(instance.getInstanceId());
            }

            if (instanceIds.isEmpty()) {
                throw new IllegalStateException("No online instances found");
            }

            for (App app : apps) {
            	updateAppWarUrl(app);
                CreateDeploymentRequest request = new CreateDeploymentRequest();
                request.setStackId(stack.getStackId());
                request.setAppId(app.getAppId());
                request.setCustomJson(customJsonOverride);
                request.setCommand(new DeploymentCommand().withName(command));
                
                if (instanceIds.size() > 0) {
                    request.setInstanceIds(instanceIds);
                }

                opsworks.createDeployment(request);
            }
        }
    }

    private void updateAppWarUrl(App app) {
    	if(StringUtils.isEmpty(httpUrl)) {
    		log.warn("App httpUrl is empty.");
    		return;
    	}
    	log.info("httpUrl to update="+httpUrl);
        UpdateAppRequest appRequest = new UpdateAppRequest();
        appRequest.withAppId(app.getAppId());
        appRequest.setAppSource(new Source().withType(SourceType.Archive).withUrl(httpUrl));
        opsworks.updateApp(appRequest);
    }
}
