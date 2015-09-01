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
