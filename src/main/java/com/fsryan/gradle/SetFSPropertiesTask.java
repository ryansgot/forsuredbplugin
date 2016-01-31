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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class SetFSPropertiesTask extends DefaultTask {

    public static final String NAME = "setProperties";

    @TaskAction
    public void directAnnotaitonProcessor() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must set all forsuredb extension properties");
        }

        setSystemProperty("applicationPackageName", extension.getApplicationPackageName());
        setSystemProperty("resultParameter", extension.getResultParameter());
        setSystemProperty("recordContainer", extension.getRecordContainer());
        if (ProjectUtil.wasRequestedTask(getProject(), ForSureDBMigrateTask.NAME)) {
            setSystemProperty("createMigrations", "true");
            setSystemProperty("migrationDirectory", extension.getMigrationDirectory());
        } else {
            TaskLog.d(NAME, "Not setting migration system properties");
        }
    }

    private void setSystemProperty(String key, String value) {
        if (value == null || value.length() == 0) {
            TaskLog.w(NAME, "Not setting system property" + key + " because it was " + (value == null ? "null" : "empty") + "; be prepared for compilation to fail");
            return;
        }
        System.setProperty(key, value);
        TaskLog.i(NAME, "set system property " + key + "=" + value);
    }
}
