/*
    forsuredbplugin, a gradle plugin compainion for the forsuredb project
    Copyright (C) 2015  Ryan Scott

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Contact Ryan at ryansgot@gmail.com
 */
package com.fsryan.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class ForSureDBPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getExtensions().create("forsuredb", ForSureExtension.class);
        project.getTasks().create(SetFSPropertiesTask.NAME, SetFSPropertiesTask.class);
        project.getTasks().create(ForSureDBMigrateTask.NAME, ForSureDBMigrateTask.class);
        project.getTasks().create(RegisterCustomFSSerializerFactoryTask.NAME, RegisterCustomFSSerializerFactoryTask.class);
        project.getTasks().create(RegisterCustomDbmsIntegratorTask.NAME, RegisterCustomDbmsIntegratorTask.class);

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                TaskLog.d("forsuredb extension: " + project.getExtensions().findByType(ForSureExtension.class).toString());

                // set dependencies for dbmigrate and any java compilation task
                Task dbMigrateTask = project.getTasks().getByName("dbmigrate");
                dbMigrateTask.dependsOn("clean");

                // if a java compilation task was specifically requested, then make dbmigrate dependent upon that task
                boolean javaCompilationDependencyAdded = dbmigrateDependencySetByRequestedTasks(project, dbMigrateTask);
                for (Task task : project.getTasks()) {
                    if (isAssembleTask(task.getName())) {
                        TaskLog.d("setting task " + task.getName() + " to depend upon " + RegisterCustomFSSerializerFactoryTask.NAME);
                        task.dependsOn(RegisterCustomFSSerializerFactoryTask.NAME);
                        task.dependsOn(RegisterCustomDbmsIntegratorTask.NAME);
                    }
                    if (!isJavaCompilationTask(task.getName())) {
                        continue;   // <-- we don't care about any tasks that are not java compilation tasks
                    }

                    if (!javaCompilationDependencyAdded && !isTestCompile(task.getName())) {
                        javaCompilationDependencyAdded = true;
                        TaskLog.d("setting task dbmigrate to depend upon " + task.getName());
                        dbMigrateTask.dependsOn(task.getName());
                    }
                    TaskLog.d("setting task " + task.getName() + " to depend upon setProperties");
                    task.dependsOn(SetFSPropertiesTask.NAME);
                }

                // if the java compilation dependency has not yet been added, then this plugin is probably being misused
                if (!javaCompilationDependencyAdded) {
                    throw new IllegalStateException("Java compilation task not found in project. "
                            + "Forsuredb requires a java compilation task. "
                            + "This means you will probably need to apply the java plugin or the com.android.library or "
                            + "com.android.application plugins to your build.gradle file.");
                }
            }
        });
    }

    private boolean dbmigrateDependencySetByRequestedTasks(Project project, Task dbMigrateTask) {
        for (String requestedTaskName : ProjectUtil.requestedTaskNames(project)) {
            if (!isJavaCompilationTask(requestedTaskName) || isTestCompile(requestedTaskName)) {
                continue;
            }
            dbMigrateTask.dependsOn(requestedTaskName);
            TaskLog.d("setting task dbmigrate to depend upon " + requestedTaskName);
            return true;
        }
        return false;
    }

    private boolean isJavaCompilationTask(String taskName) {
        return (taskName.contains("compile") || taskName.contains("Compile"))
                && (taskName.contains("Java") || taskName.contains("java"));
    }

    private boolean isAssembleTask(String name) {
        return name != null && (name.contains("assemble") || name.contains("Assemble"));
    }

    private boolean isTestCompile(String taskName) {
        return taskName.contains("test") || taskName.contains("Test");
    }
}
