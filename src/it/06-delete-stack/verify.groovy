import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.n3twork.aws.opsworks.OpsworksUtil

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

opsworks = new AWSOpsWorksClient(new BasicAWSCredentials(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY")))
opsworksUtil = new OpsworksUtil(opsworks);

stackName = "test-opsworks-maven-plugin"
stacks = opsworksUtil.getStacksByName(stackName)
assertTrue("Stack '" + stackName + "' should be deleted", stacks.size() == 0)
