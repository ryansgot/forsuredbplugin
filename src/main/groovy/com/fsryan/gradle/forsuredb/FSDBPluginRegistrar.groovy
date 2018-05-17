package com.fsryan.gradle.forsuredb

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class FSDBPluginRegistrar extends DefaultTask {

    private final String interfaceClassName
    private final String forsuredbExtensionPropertyName
    final ForSureDBExt forsuredbExt

    FSDBPluginRegistrar(String interfaceClassName, String forsuredbExtensionPropertyName) {
        this.interfaceClassName =interfaceClassName
        this.forsuredbExtensionPropertyName = forsuredbExtensionPropertyName
        forsuredbExt = project.extensions.findByType(ForSureDBExt)
    }

    void writeMetaInfFile(String implementationClassName) {
        writeMetaInfFile(forsuredbExt.resourcesDirectory, implementationClassName)
    }

    protected abstract String taskName()

    void writeMetaInfFile(String resourcesDirectory, String implementationClassName) {
        if (resourcesDirectory == null) {
            TaskLog.w(taskName(), "Cannot register custom " + interfaceClassName + " without setting forsuredb.resourcesDirectory property")
            return
        }
        if (implementationClassName == null) {
            TaskLog.w(taskName(), "Cannot register custom " + interfaceClassName + " without setting forsuredb." + forsuredbExtensionPropertyName + " property")
            return
        }

        String metaInfDirStr = resourcesDirectory + File.separator + "META-INF" + File.separator + "services"
        File metaInfDir = new File(metaInfDirStr)
        metaInfDir.mkdirs()

        String spFileStr = metaInfDirStr + File.separator + interfaceClassName
        File spFile = new File(spFileStr)
        spFile.delete()
        BufferedWriter bw = null
        try {
            spFile.createNewFile()
            bw = new BufferedWriter(new FileWriter(spFile))
            bw.write(implementationClassName)
            TaskLog.d(taskName(), "Wrote META-INF/services resource")
        } catch (IOException ioe) {
            throw new RuntimeException("Failed to write to service provider file: $spFileStr; your custom $interfaceClassName will not be used", ioe)
        } finally {
            bw.flush()
            bw.close()
        }
    }
}

class RegisterCustomFSSerializerFactoryTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomFSSerializerFactory"

    RegisterCustomFSSerializerFactoryTask() {
        super("com.fsryan.forsuredb.api.adapter.FSSerializerFactory", "fsSerializerFactoryClass")
    }

    @TaskAction
    void writeToMetaInf() {
        if (forsuredbExt.fsSerializerFactoryClass == null || forsuredbExt.fsSerializerFactoryClass.isEmpty()) {
            TaskLog.w(NAME, "no fsSerializerFactoryClass found; will fall back forsuredb's default Java serializable serializer")
            return
        }
        writeMetaInfFile(forsuredbExt.fsSerializerFactoryClass)
    }

    @Override
    protected String taskName() {
        return NAME
    }
}

class RegisterCustomDbmsIntegratorTask extends FSDBPluginRegistrar {

    public static final String NAME = "registerCustomDbmsIntegratorClass"

    RegisterCustomDbmsIntegratorTask() {
        super("com.fsryan.forsuredb.api.sqlgeneration.DBMSIntegrator", "dbmsIntegratorClass")
    }

    @TaskAction
    void writeToMetaInf() {
        if (forsuredbExt.dbmsIntegratorClass == null || forsuredbExt.dbmsIntegratorClass.isEmpty()) {
            throw new IllegalStateException("Must set forsuredb.dbmsIntegratorClass property in order to build")
        }
        writeMetaInfFile(forsuredbExt.dbmsIntegratorClass)
    }

    @Override
    protected String taskName() {
        return NAME
    }
}