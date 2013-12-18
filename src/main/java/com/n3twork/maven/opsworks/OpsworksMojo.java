package com.n3twork.maven.opsworks;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.opsworks.AWSOpsWorks;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;

public abstract class OpsworksMojo extends AbstractMojo {
    protected Log log = getLog();

    @Parameter(defaultValue = "aws.amazon.com", property = "serverId", required = true)
    private String serverId;

    @Parameter(property = "stackName", required = true)
    protected String stackName;

    @Component
    private Settings settings;

    protected AWSOpsWorks opsworks;
    protected AmazonIdentityManagement iam;

    protected void init() {
        Server server = settings.getServer(serverId);
        if (server == null) {
            throw new IllegalArgumentException("Unknown server '" + serverId + "'; is it in your settings.xml file?");
        }
        AWSCredentials credentials = new BasicAWSCredentials(server.getUsername(), server.getPassword());
        opsworks = new AWSOpsWorksClient(credentials);
        iam = new AmazonIdentityManagementClient(credentials);
    }

    @Override
    final public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
            run();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            try {
                cleanup();
            }
            catch (Exception ignored) {}
        }

    }

    /**
     * Gets the ID of the first stack with the given name
     * @return
     */
    protected Stack getStack() {
        DescribeStacksResult stacks = opsworks.describeStacks(new DescribeStacksRequest());
        for (Stack stack : stacks.getStacks()) {
            if (stack.getName().equals(stackName))
                return stack;
        }
        throw new IllegalArgumentException("No stack found with name '" + stackName + "'");
    }



    protected abstract void run() throws Exception;

    protected void cleanup() {
        if (opsworks != null) {
            opsworks.shutdown();
        }
        if (iam != null) {
            iam.shutdown();
        }
    }
}
