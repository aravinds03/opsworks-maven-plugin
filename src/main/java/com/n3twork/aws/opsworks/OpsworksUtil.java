package com.n3twork.aws.opsworks;

import com.amazonaws.services.opsworks.AWSOpsWorks;
import com.amazonaws.services.opsworks.model.DescribeStacksRequest;
import com.amazonaws.services.opsworks.model.DescribeStacksResult;
import com.amazonaws.services.opsworks.model.Stack;

import java.util.ArrayList;
import java.util.List;

public class OpsworksUtil {
    private AWSOpsWorks opsworks;

    public OpsworksUtil(AWSOpsWorks opsworks) {
        this.opsworks = opsworks;
    }

    public List<Stack> getStacksByName(String name) {
        List<Stack> result = new ArrayList<Stack>();
        DescribeStacksResult stacks = opsworks.describeStacks(new DescribeStacksRequest());
        for (Stack stack : stacks.getStacks()) {
            if (stack.getName().equals(name))
                result.add(stack);
        }
        return result;
    }
}
