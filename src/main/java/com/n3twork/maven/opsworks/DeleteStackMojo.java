package com.n3twork.maven.opsworks;

import com.amazonaws.services.opsworks.model.DeleteStackRequest;
import com.amazonaws.services.opsworks.model.Stack;
import com.amazonaws.services.opsworks.model.UpdateStackRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Just enough for our current needs
 */
@Mojo(name = "delete-stack", defaultPhase = LifecyclePhase.DEPLOY)
public class DeleteStackMojo extends OpsworksMojo {

    public void run() throws IOException {
        List<Stack> stacks = opsworksUtil.getStacksByName(stackName);
        if (stacks.isEmpty()) {
            throw new IllegalArgumentException("No stack found with name '" + stackName + "'");
        }

        for (Stack stack : stacks) {
            DeleteStackRequest request = new DeleteStackRequest();
            request.setStackId(stack.getStackId());
            opsworks.deleteStack(request);
        }
    }


    // From http://stackoverflow.com/questions/9895041/merging-two-json-documents-using-jackson
    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {

        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field doesn't exist or is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }

        }

        return mainNode;
    }

}
