package com.n3twork.maven.opsworks;

import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.opsworks.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Minimal implementation, just enough to test other goals
 */
@Mojo(name = "create-app", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateAppMojo extends OpsworksMojo {

    @Parameter(property = "appName", required = true)
    private String appName;

    @Parameter(property = "appType", required = true)
    private String appType;

    public void run() throws IOException {
        List<Stack> stacks = opsworksUtil.getStacksByName(stackName);
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("No stack found with name '" + stackName + "'");
        }

        for (Stack stack : stacks) {

            CreateAppRequest request = new CreateAppRequest();
            request.setStackId(stack.getStackId());
            request.setName(appName);
            request.setType(AppType.fromValue(appType));
            opsworks.createApp(request);
        }
    }
}
