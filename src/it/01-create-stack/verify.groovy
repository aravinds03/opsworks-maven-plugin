import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.n3twork.aws.opsworks.OpsworksUtil

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

opsworks = new AWSOpsWorksClient(new BasicAWSCredentials(System.getenv("AWS_ACCESS_KEY_ID"), System.getenv("AWS_SECRET_ACCESS_KEY")))
opsworksUtil = new OpsworksUtil(opsworks);

stackName = "test-opsworks-maven-plugin"
stacks = opsworksUtil.getStacksByName(stackName)
assertTrue("Stack '" + stackName + "' not found", stacks.size() > 0)
stack = stacks.get(0)

assertEquals("{\"root\":{\"key\":\"oldValue\"}}", stack.getCustomJson().replaceAll("\\s", ""))
