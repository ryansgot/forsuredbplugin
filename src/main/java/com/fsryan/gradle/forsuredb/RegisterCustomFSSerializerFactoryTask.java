package com.fsryan.gradle.forsuredb;

import org.gradle.api.tasks.TaskAction;

public class RegisterCustomFSSerializerFactoryTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomFSSerializerFactory";

    public RegisterCustomFSSerializerFactoryTask() {
        super("com.fsryan.forsuredb.api.adapter.FSSerializerFactory", "fsSerializerFactoryClass");
    }

    @TaskAction
    public void writeToMetaInf() {
        String fsSerializerFactoryClass = getConfigProperty("fsSerializerFactoryClass");
        if (fsSerializerFactoryClass == null || fsSerializerFactoryClass.isEmpty()) {
            TaskLog.w(NAME, "no fsSerializerFactoryClass found; will fall back forsuredb's default Java serializable serializer");
            return;
        }
        writeMetaInfFile(fsSerializerFactoryClass);
    }

    @Override
    protected String taskName() {
        return NAME;
    }
}
