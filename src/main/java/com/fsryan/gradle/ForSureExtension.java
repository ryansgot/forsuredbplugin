package com.fsryan.gradle;

public class ForSureExtension {

    private String applicationPackageName;
    private String resultParameter;
    private String dbType;
    private String migrationDirectory;
    private String appProjectDirectory = "";

    @Override
    public String toString() {
        return new StringBuffer(ForSureExtension.class.getSimpleName()).append("{")
                .append("applicationPackageName=").append(applicationPackageName)
                .append(", resultParameter=").append(resultParameter)
                .append(", dbType=").append(dbType)
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

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
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
