package com.fsryan.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class ForSureDBPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("forsuredb", ForSureExtension.class);
        project.getTasks().create("setProperties", SetFSPropertiesTask.class);
        project.getTasks().create("dbmigrate", ForSureDBMigrateTask.class);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                Task dbMigrateTask = project.getTasks().getByName("dbmigrate");
                dbMigrateTask.dependsOn("clean");
                dbMigrateTask.dependsOn("compileDebugJava");
                project.getTasks().getByName("compileDebugJava").dependsOn("setProperties");
            }
        });
    }
}
