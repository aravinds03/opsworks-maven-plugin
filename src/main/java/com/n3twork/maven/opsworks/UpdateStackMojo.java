package com.n3twork.maven.opsworks;

import com.amazonaws.services.identitymanagement.model.*;
import com.amazonaws.services.opsworks.model.CreateStackRequest;
import com.amazonaws.services.opsworks.model.Stack;
import com.amazonaws.services.opsworks.model.UpdateStackRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Just enough for our current needs: manage custom JSON
 */
@Mojo(name = "update-stack", defaultPhase = LifecyclePhase.DEPLOY)
public class UpdateStackMojo extends OpsworksMojo {

    @Parameter(property = "customJsonOverride", required = false)
    private String customJsonOverride;

    public void run() throws IOException {
        Stack stack = getStack();
        UpdateStackRequest request = new UpdateStackRequest();
        request.setStackId(stack.getStackId());


        if (customJsonOverride != null) {
            String newJson;

            ObjectMapper json = new ObjectMapper();
            json.enable(SerializationFeature.INDENT_OUTPUT);

            String oldJson = stack.getCustomJson();
            if (oldJson == null || oldJson.length() == 0) {
                newJson = customJsonOverride;
            }
            else {
                JsonNode overrideNode = json.readTree(customJsonOverride);
                JsonNode currentNode = json.readTree(oldJson);
                JsonNode merged = merge(currentNode, overrideNode);
                newJson = json.writeValueAsString(merged);
            }
            request.setCustomJson(newJson);
        }
        opsworks.updateStack(request);
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
