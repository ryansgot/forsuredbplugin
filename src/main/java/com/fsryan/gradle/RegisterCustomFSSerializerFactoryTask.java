package com.fsryan.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterCustomFSSerializerFactoryTask extends DefaultTask {

    public static final String NAME = "registerCustomFSSerializerFactory";

    @TaskAction
    public void writeToMetaInf() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must set all forsuredb extension properties");
        }
        if (extension.getFsSerializerFactoryClass() == null) {
            TaskLog.w(NAME, "Cannot register custom fsSerializerFactory without setting forsuredb.fsSerializerFactoryClass property");
            return;
        }
        if (extension.getResourcesDirectory() == null) {
            TaskLog.w(NAME, "Cannot register custom fsSerializerFactory without setting forsuredb.resourcesDirectory property");
            return;
        }

        String metaInfDirStr = extension.getResourcesDirectory() + File.separator + "META-INF" + File.separator + "services";
        File metaInfDir = new File(metaInfDirStr);
        metaInfDir.mkdirs();

        String spFileStr = metaInfDirStr + File.separator + "com.fsryan.forsuredb.api.adapter.FSSerializerFactory";
        File spFile = new File(spFileStr);
        spFile.delete();
        BufferedWriter bw = null;
        try {
            spFile.createNewFile();
            bw = new BufferedWriter(new FileWriter(spFile));
            bw.write(extension.getFsSerializerFactoryClass());
            TaskLog.d(NAME, "Wrote META-INF/services resource");
        } catch (IOException ioe) {
            TaskLog.e(NAME, "Failed to write to service provider file: " + spFileStr + "; your custom FSSerializerFactory will not be used");
        } finally {
            try {
                bw.close();
            } catch (Exception e) {}
        }
    }
}
