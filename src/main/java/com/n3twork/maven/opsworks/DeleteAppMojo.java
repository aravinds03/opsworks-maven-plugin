package com.n3twork.maven.opsworks;

import com.amazonaws.services.opsworks.model.App;
import com.amazonaws.services.opsworks.model.CreateDeploymentRequest;
import com.amazonaws.services.opsworks.model.DeleteAppRequest;
import com.amazonaws.services.opsworks.model.Stack;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.List;

/**
 * Minimal implementation; just what we need to support other goals
 */
@Mojo(name = "delete-app", defaultPhase = LifecyclePhase.DEPLOY)
public class DeleteAppMojo extends OpsworksMojo {

    @Parameter(property = "appName", required = true)
    private String appName;

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
            for (App app : apps) {
                DeleteAppRequest request = new DeleteAppRequest();
                request.setAppId(app.getAppId());
                opsworks.deleteApp(request);
            }
        }
    }
}
