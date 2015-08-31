package com.fsryan.gradle;

import org.gradle.TaskExecutionRequest;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class SetFSPropertiesTask extends DefaultTask {

    @TaskAction
    public void directAnnotaitonProcessor() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must set all forsuredb extension properties");
        }

        System.out.println(extension.toString());

        System.setProperty("applicationPackageName", extension.getApplicationPackageName());
        System.setProperty("resultParameter", extension.getResultParameter());
        System.out.println("[setProperties]: set property applicationPackageName=" + System.getProperty("applicationPackageName"));
        System.out.println("[setProperties]: set property resultParameter=" + System.getProperty("resultParameter"));

        if (wasRequestedTask("dbmigrate")) {
            System.setProperty("dbtype", extension.getDbType());
            System.setProperty("createMigrations", "true");
            System.setProperty("migrationDirectory", extension.getMigrationDirectory());
            System.out.println("[setProperties]: set property dbtype=" + System.getProperty("dbtype"));
            System.out.println("[setProperties]: set property createMigrations=" + System.getProperty("createMigrations"));
            System.out.println("[setProperties]: set property migrationDirectory=" + System.getProperty("migrationDirectory"));
        }
    }

    private boolean wasRequestedTask(String taskName) {
        for (TaskExecutionRequest taskExecutionRequest : getProject().getGradle().getStartParameter().getTaskRequests()) {
            for (String arg : taskExecutionRequest.getArgs()) {
                if (arg.equals(taskName)) {
                    return true;
                }
            }
        }

        return false;
    }
}
