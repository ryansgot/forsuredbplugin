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

public class ForSureExtension {

    private String applicationPackageName;
    private String resultParameter;
    private String recordContainer;
    private String migrationDirectory;
    private String appProjectDirectory = "";

    @Override
    public String toString() {
        return new StringBuffer(ForSureExtension.class.getSimpleName()).append("{")
                .append("applicationPackageName=").append(applicationPackageName)
                .append(", resultParameter=").append(resultParameter)
                .append(", recordContainer=").append(recordContainer)
                .append(", migrationDirectory=").append(migrationDirectory)
                .append(", appProjectDirectory=").append(appProjectDirectory)
                .append("}").toString();
    }

    public String getApplicationPackageName() {
        return applicationPackageName;
    }

    public void setApplicationPackageName(String applicationPackageName) {
        this.applicationPackageName = applicationPackageName;
    }

    public String getResultParameter() {
        return resultParameter;
    }

    public void setResultParameter(String resultParameter) {
        this.resultParameter = resultParameter;
    }

    public String getRecordContainer() {
        return recordContainer;
    }

    public void setRecordContainer(String recordContainer) {
        this.recordContainer = recordContainer;
    }

    public String getMigrationDirectory() {
        return migrationDirectory;
    }

    public void setMigrationDirectory(String migrationDirectory) {
        this.migrationDirectory = migrationDirectory;
    }

    public String getAppProjectDirectory() {
        return appProjectDirectory;
    }

    public void setAppProjectDirectory(String appProjectDirectory) {
        this.appProjectDirectory = appProjectDirectory;
    }
}
