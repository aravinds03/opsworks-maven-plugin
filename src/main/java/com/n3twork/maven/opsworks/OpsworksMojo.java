package com.n3twork.maven.opsworks;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.opsworks.AWSOpsWorks;
import com.amazonaws.services.opsworks.AWSOpsWorksClient;
import com.amazonaws.services.opsworks.model.*;
import com.n3twork.aws.opsworks.OpsworksUtil;
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
    protected OpsworksUtil opsworksUtil;

    protected void init() {
        String username, password;

        Server server = settings.getServer(serverId);
        if (server == null) {
            // Try environment variables
            username = System.getenv("AWS_ACCESS_KEY_ID");
            password = System.getenv("AWS_SECRET_ACCESS_KEY");

            if (username == null || password == null) {
                throw new IllegalArgumentException("No AWS credentials found. Please add the '" + serverId
                + "' server to your settings.xml file, or set the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables");
            }
        }
        else {
            username = server.getUsername();
            password = server.getPassword();
        }
        AWSCredentials credentials = new BasicAWSCredentials(username, password);
        opsworks = new AWSOpsWorksClient(credentials);
        iam = new AmazonIdentityManagementClient(credentials);
        opsworksUtil = new OpsworksUtil(opsworks);
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
