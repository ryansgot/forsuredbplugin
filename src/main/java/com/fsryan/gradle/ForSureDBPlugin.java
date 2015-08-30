package com.fsryan.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ForSureDBPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("forsuredb", ForSureExtension.class);
        project.getTasks().create("setProperties", SetFSPropertiesTask.class);
        project.getTasks().create("dbmigrate", ForSureDBMigrateTask.class);
    }
}
