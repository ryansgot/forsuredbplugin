package com.fsryan.gradle.legacy;

import org.gradle.api.tasks.TaskAction;

public class RegisterCustomDbmsIntegratorTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomDbmsIntegratorClass";

    public RegisterCustomDbmsIntegratorTask() {
        super("com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator", "dbmsIntegratorClass");
    }

    @TaskAction
    public void writeToMetaInf() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must set all forsuredb extension properties");
        }
        if (extension.getDbmsIntegratorClass() == null) {
            throw new IllegalStateException("Must set forsuredb.dbmsIntegratorClass property in order to build");
        }
        writeMetaInfFile(extension.getResourcesDirectory(), extension.getDbmsIntegratorClass());
    }

    @Override
    protected String taskName() {
        return NAME;
    }
}
