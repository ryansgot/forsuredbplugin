package com.fsryan.gradle;

import org.gradle.api.tasks.TaskAction;

public class RegisterCustomFSSerializerFactoryTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomFSSerializerFactory";

    public RegisterCustomFSSerializerFactoryTask() {
        super("com.fsryan.forsuredb.api.adapter.FSSerializerFactory", "fsSerializerFactoryClass");
    }

    @TaskAction
    public void writeToMetaInf() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must set all forsuredb extension properties");
        }
        writeMetaInfFile(extension.getResourcesDirectory(), extension.getFsSerializerFactoryClass());
    }

    @Override
    protected String taskName() {
        return NAME;
    }
}
