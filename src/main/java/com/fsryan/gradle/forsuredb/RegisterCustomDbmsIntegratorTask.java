package com.fsryan.gradle.forsuredb;

import org.gradle.api.tasks.TaskAction;

public class RegisterCustomDbmsIntegratorTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomDbmsIntegratorClass";

    public RegisterCustomDbmsIntegratorTask() {
        super("com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator", "dbmsIntegratorClass");
    }

    @TaskAction
    public void writeToMetaInf() {
        String dbmsIntegratorClass = getConfigProperty("dbmsIntegratorClass");
        if (dbmsIntegratorClass == null) {
            throw new IllegalStateException("Must set forsuredb.dbmsIntegratorClass property in order to build");
        }

        writeMetaInfFile(dbmsIntegratorClass);
    }

    @Override
    protected String taskName() {
        return NAME;
    }
}
