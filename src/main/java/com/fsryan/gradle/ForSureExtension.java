package com.fsryan.gradle;

public class ForSureExtension {

    private String applicationPackageName;
    private String resultParameter;
    private String dbType;
    private String migrationDirectory;

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
}
