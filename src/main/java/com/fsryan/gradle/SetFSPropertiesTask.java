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
        System.setProperty("recordContainer", extension.getRecordContainer());
        System.out.println("[setProperties]: set property applicationPackageName=" + System.getProperty("applicationPackageName"));
        System.out.println("[setProperties]: set property resultParameter=" + System.getProperty("resultParameter"));
        System.out.println("[setProperties]: set property recordContainer=" + System.getProperty("recordContainer"));

        if (wasRequestedTask("dbmigrate")) {
            System.setProperty("createMigrations", "true");
            System.setProperty("migrationDirectory", extension.getMigrationDirectory());
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
