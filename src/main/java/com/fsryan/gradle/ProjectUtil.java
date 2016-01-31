package com.fsryan.gradle;

import org.gradle.TaskExecutionRequest;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectUtil {

    public static List<String> requestedTaskNames(Project project) {
        List<String> retList = new ArrayList<String>();
        for (TaskExecutionRequest ter : project.getGradle().getStartParameter().getTaskRequests()) {
            retList.addAll(ter.getArgs());
        }
        return retList;
    }

    public static boolean wasRequestedTask(Project project, String taskName) {
        for (String requestedTaskName : requestedTaskNames(project)) {
            if (requestedTaskName.equals(taskName)) {
                return true;
            }
        }
        return false;
    }
}
