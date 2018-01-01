package com.fsryan.gradle.forsuredb

import org.gradle.api.*
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.lang.reflect.Method

class ForSureDBPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            val setupTask = tasks.create("forsuredb", ForSureDBSetupTask::class.java)
            setupTask.outputs.upToDateWhen { false }
            val dbMigrateTask = tasks.create("dbMigrate", ForSureDBMigrateTask::class.java)
            dbMigrateTask.outputs.upToDateWhen { false }
            val registerDbmsIntegratorTask = tasks.create(RegisterCustomDbmsIntegratorTask.NAME, RegisterCustomDbmsIntegratorTask::class.java)
            val registerFSSerializerFactoryTask = tasks.create(RegisterCustomFSSerializerFactoryTask.NAME, RegisterCustomFSSerializerFactoryTask::class.java)

            // ensure that forsuredb SPI plugins are in place on assemble
            assembleTasks(project).filter { t -> !t.name.contains("Test") }.forEach { t ->
                TaskLog.i("forsuredb", "setting task ${t.name} to depend upon plugin registry tasks")
                t.dependsOn(registerFSSerializerFactoryTask, registerDbmsIntegratorTask)
            }

            if (isJava(project)) {
                TaskLog.i("forsuredb", "detected Java project")
                tasks.withType(JavaCompile::class.java).filter {t -> !t.name.contains("Test") }.forEach { compileTask ->
                    TaskLog.i("forsuredb", "Setting task ${compileTask.name} to depend upon the forsuredb setup task")
                    compileTask.dependsOn(setupTask)
                    if (!compileTask.name.contains("Test")) {
                        dbMigrateTask.dependsOn(compileTask)
                    }
                }
            } else if (isAndroid(project)) {
                afterEvaluate {
                    TaskLog.i("forsuredb", "detected Android project")

                    // TODO: allow for the compile configuration for migrations
                    var hasSetDbMigrateCompileTaskDependency = false
                    getWrappedAndroidVariants(project).forEach { v ->
                        val javaCompileTask: JavaCompile = v.getJavaCompileTask()
                        TaskLog.i("forsuredb", "Setting processor args for java compile task: ${javaCompileTask.name}")
                        getForsuredbTask(project).addProcessorArgs(javaCompileTask, migrateRequested(project), false)

                        if (!hasSetDbMigrateCompileTaskDependency && v.isForDebugType()) {
                            hasSetDbMigrateCompileTaskDependency = true
                            TaskLog.i("forsuredb", "Setting ${dbMigrateTask.name} depends on ${javaCompileTask.name}")
                            dbMigrateTask.dependsOn(javaCompileTask)
                        }
                    }
                }
            }
        }
    }

    fun assembleTasks(project: Project): List<Task> = project.tasks.filter { t ->
        t.name.contains("assemble") || t.name.contains("Assemble")
    }

    fun getConfigProperty(name: String, project: Project) = getForsuredbTask(project).property(name) as String

    fun getForsuredbTask(project: Project): ForSureDBSetupTask = project.tasks.getByName("forsuredb") as ForSureDBSetupTask? ?: throw IllegalStateException("forsuredb setup task not found")

    fun getWrappedAndroidVariants(project: Project): List<AndroidVariantWrapper> = getAndroidVariants(project).map { rawVariant -> AndroidVariantWrapper(rawVariant) }

    fun getAndroidVariants(project: Project): DomainObjectSet<Any> {
        val androidExtension = getAndroidExtension(project)
        if (isAndroidApplication(project)) {
            val applicationVariantsM: Method = androidExtension::class.java.getDeclaredMethod("getApplicationVariants")
            return applicationVariantsM.invoke(androidExtension) as DomainObjectSet<Any>
        }
        val libraryVariantsM: Method = androidExtension::class.java.getDeclaredMethod("getLibraryVariants")
        return libraryVariantsM.invoke(androidExtension) as DomainObjectSet<Any>
    }

    fun getAndroidExtension(project: Project): Any = project.extensions.findByName("android") ?: throw IllegalStateException("could not find android app extension or library extension")

    fun isAndroid(project: Project): Boolean = isAndroidApplication(project) || isAndroidLibrary(project)

    fun isAndroidApplication(project: Project): Boolean = project.plugins.hasPlugin("com.android.application")

    fun isAndroidLibrary(project: Project): Boolean = project.plugins.hasPlugin("com.android.library")

    fun migrateRequested(project: Project): Boolean = project.gradle.startParameter.taskRequests
            .map {tr -> tr.args }.flatten().filter { taskName ->
            taskName.endsWith("${project.name}:dbMigrate") || taskName.equals("dbMigrate")
        }.isNotEmpty()

    fun isJava(project: Project): Boolean = project.plugins.hasPlugin(JavaPlugin::class.java)
}

open class ForSureDBSetupTask : DefaultTask() {

    @Input
    lateinit var resourcesDirectory: String
    @Input
    var fsSerializerFactoryClass: String? = null
    @Input
    lateinit var dbmsIntegratorClass: String
    @Input
    lateinit var applicationPackageName: String
    @Input
    lateinit var resultParameter: String
    @Input
    lateinit var recordContainer: String
    @Input
    lateinit var migrationDirectory: String
    @Input
    lateinit var appProjectDirectory: String

    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        val plugin = project.plugins.getPlugin(ForSureDBPlugin::class.java)
        val migrate = plugin.migrateRequested(project)
        if (plugin.isJava(project)) {
            project.tasks.withType(JavaCompile::class.java).filter { t -> !t.name.contains("Test") }.forEach { t ->
                addProcessorArgs(t, migrate, true)
            }
        } else if (plugin.isAndroid(project)) {
            // do nothing because annotation processor options set in afterEvaluate block
        } else {
            // TODO: techincally groovy could be supported, but I don't care so much at the moment
            throw IllegalStateException("forsuredbplugin only supports android library, android application, and java projects.")
        }
    }

    override fun toString(): String {
        return "ForSureDBSetupTask(resourcesDirectory='$resourcesDirectory', fsSerializerFactoryClass=$fsSerializerFactoryClass, dbmsIntegratorClass='$dbmsIntegratorClass', applicationPackageName='$applicationPackageName', resultParameter='$resultParameter', recordContainer='$recordContainer', migrationDirectory='$migrationDirectory', appProjectDirectory='$appProjectDirectory')"
    }

    fun addProcessorArgs(t: JavaCompile, migrate: Boolean, includeGeneratedAnnotation: Boolean) {
        addProcessorArg(t, "applicationPackageName", applicationPackageName)
        addProcessorArg(t, "resourcesDirectory", resourcesDirectory)
        addProcessorArg(t, "resultParameter", resultParameter)
        addProcessorArg(t, "recordContainer", recordContainer)
        addProcessorArg(t, "migrationDirectory", migrationDirectory)
        addProcessorArg(t, "appProjectDirectory", appProjectDirectory)
        addProcessorArg(t, "addGeneratedAnnotation", if (includeGeneratedAnnotation) "true" else "false")
        if (migrate) {
            addProcessorArg(t, "createMigrations", "true")
        }
    }

    private fun addProcessorArg(t: JavaCompile, key: String, value: String) {
        t.options.compilerArgs.add("-Aforsuredb.$key=$value")
    }
}

open class ForSureDBMigrateTask: DefaultTask() {
    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        MigrationFileCopier(project).copyMigrations()
    }


}

class AndroidVariantWrapper(val variant: Any) {

    fun isForDebugType(): Boolean = getBuildType().isDebuggable()

    fun getJavaCompileTask(): JavaCompile = javaCompileM().invoke(variant) as JavaCompile
    private fun javaCompileM(): Method = variant::class.java.getDeclaredMethod("getJavaCompile")

    fun getName(): String = nameM().invoke(variant) as String
    private fun nameM(): Method = variant::class.java.getDeclaredMethod("getName")

    fun getBuildType(): AndroidBuildType = AndroidBuildType(buildTypeM().invoke(variant))
    private fun buildTypeM(): Method = variant::class.java.getDeclaredMethod("getBuildType")
}

class AndroidBuildType(val buildType: Any) {
    fun isDebuggable(): Boolean = isDebuggableM().invoke(buildType) as Boolean
    private fun isDebuggableM(): Method = buildType::class.java.getDeclaredMethod("isDebuggable")
}