package com.n3twork.maven.opsworks;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.opsworks.AWSOpsWorks;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.CreateStackRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

/**
 * Minimal implementation, just enough to test other goals
 */
@Mojo(name = "create-stack", defaultPhase = LifecyclePhase.DEPLOY)
public class CreateStackMojo extends AbstractMojo {
    private Log log = getLog();


    @Parameter(defaultValue = "aws.amazon.com", property = "serverId", required = true)
    private String serverId;

    @Parameter(property = "stackName", required = true)
    private String stackName;

    @Parameter(defaultValue = "aws-opsworks-service-role", property = "serviceRole", required = true)
    private String serviceRole;

    @Parameter(property = "region", required = true)
    private String region;

    @Parameter(defaultValue = "aws-opsworks-ec2-role", property = "instanceProfile", required = true)
    private String instanceProfile;

    @Component
    private Settings settings;

    private AWSOpsWorks opsworks;
    private AmazonIdentityManagement iam;

    public void execute()
            throws MojoExecutionException {
        try {
            init();

            CreateStackRequest request = new CreateStackRequest();
            request.setName(stackName);
            request.setRegion(region);
            request.setDefaultInstanceProfileArn(getInstanceProfileArn(instanceProfile));
            request.setServiceRoleArn(getRoleArn(serviceRole));
            opsworks.createStack(request);
        } finally {
            try {
                cleanup();
            }
            catch (Exception ignored) {}
        }
    }

    private void cleanup() {
        if (opsworks != null) {
            opsworks.shutdown();
        }
        if (iam != null) {
            iam.shutdown();
        }
    }

    private String getInstanceProfileArn(String profileName) {
        GetInstanceProfileResult result = iam.getInstanceProfile(new GetInstanceProfileRequest().withInstanceProfileName(profileName));
        InstanceProfile profile = result.getInstanceProfile();
        if (profile == null) {
            throw new IllegalArgumentException("Unknown instance profile: " + profileName);
        }
        else {
            log.debug("Found arn for instance profile '" + profileName + "': " + profile.getArn());
        }
        return profile.getArn();
    }

    private String getRoleArn(String roleName) {
        GetRoleResult result = iam.getRole(new GetRoleRequest().withRoleName(roleName));
        Role role = result.getRole();
        if (role == null) {
            throw new IllegalArgumentException("Unknown role: " + roleName);
        }
        else {
            log.debug("Found arn for role '" + roleName + "': " + role.getArn());
        }
        return role.getArn();
    }

    private void init() {
        Server server = settings.getServer(serverId);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server '" + serverId + "'; is it in your settings.xml file?");
        }
        AWSCredentials credentials = new BasicAWSCredentials(server.getUsername(), server.getPassword());
        opsworks = new AWSOpsWorksClient(credentials);
        iam = new AmazonIdentityManagementClient(credentials);
    }
}
