package com.fsryan.gradle.forsuredb;

import org.gradle.api.DefaultTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class FSDBPluginRegistrar extends DefaultTask {

    private final String interfaceClassName;
    private final String forsuredbExtensionPropertyName;

    public FSDBPluginRegistrar(String interfaceClassName, String forsuredbExtensionPropertyName) {
        this.interfaceClassName =interfaceClassName;
        this.forsuredbExtensionPropertyName = forsuredbExtensionPropertyName;
    }

    protected void writeMetaInfFile(String implementationClassName) {
        writeMetaInfFile(getConfigProperty("resourcesDirectory"), implementationClassName);
    }

    protected abstract String taskName();

    protected String getConfigProperty(String propertyName) {
        ForSureDBPlugin plugin = getProject().getPlugins().getPlugin(ForSureDBPlugin.class);
        return plugin.getConfigProperty(propertyName, getProject());
    }

    private void writeMetaInfFile(String resourcesDirectory, String implementationClassName) {
        if (resourcesDirectory == null) {
            TaskLog.w(taskName(), "Cannot register custom " + interfaceClassName + " without setting forsuredb.resourcesDirectory property");
            return;
        }
        if (implementationClassName == null) {
            TaskLog.w(taskName(), "Cannot register custom " + interfaceClassName + " without setting forsuredb." + forsuredbExtensionPropertyName + " property");
            return;
        }

        String metaInfDirStr = resourcesDirectory + File.separator + "META-INF" + File.separator + "services";
        File metaInfDir = new File(metaInfDirStr);
        metaInfDir.mkdirs();

        String spFileStr = metaInfDirStr + File.separator + interfaceClassName;
        File spFile = new File(spFileStr);
        spFile.delete();
        BufferedWriter bw = null;
        try {
            spFile.createNewFile();
            bw = new BufferedWriter(new FileWriter(spFile));
            bw.write(implementationClassName);
            TaskLog.d(taskName(), "Wrote META-INF/services resource");
        } catch (IOException ioe) {
            TaskLog.e(taskName(), "Failed to write to service provider file: " + spFileStr + "; your custom " + interfaceClassName + " will not be used");
        } finally {
            try {
                bw.close();
            } catch (Exception e) {}
        }
    }
}
