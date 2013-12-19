package com.n3twork.aws.opsworks;

import com.amazonaws.services.opsworks.AWSOpsWorks;
import com.amazonaws.services.opsworks.model.*;

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

    public List<App> getAppsByName(String stackId, String name) {
        List<App> result = new ArrayList<App>();
        DescribeAppsResult apps = opsworks.describeApps(new DescribeAppsRequest().withStackId(stackId));
        for (App app : apps.getApps()) {
            if (app.getName().equals(name)) {
                result.add(app);
            }
        }
        return result;
    }
}
