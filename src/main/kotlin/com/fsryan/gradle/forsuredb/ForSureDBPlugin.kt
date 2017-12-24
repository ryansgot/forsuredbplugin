package com.fsryan.gradle.forsuredb

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class ForSureDBPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        with(project) {
            val setupTask = tasks.create("forsuredb", ForSureDBSetupTask::class.java)
            setupTask.outputs.upToDateWhen { false }
            val dbMigrateTask = tasks.create("dbMigrate", ForSureDBMigrateTask::class.java)
            val registerDbmsIntegratorTask = tasks.create(RegisterCustomDbmsIntegratorTask.NAME, RegisterCustomDbmsIntegratorTask::class.java)
            val registerFSSerializerFactoryTask = tasks.create(RegisterCustomFSSerializerFactoryTask.NAME, RegisterCustomFSSerializerFactoryTask::class.java)

            // ensure that forsuredb SPI plugins are in place on assemble
            assembleTasks(project).filter { t -> !t.name.contains("Test") }.forEach { t ->
                println("setting task ${t.name} to depend upon plugin registry tasks")
                t.dependsOn(registerFSSerializerFactoryTask, registerDbmsIntegratorTask)
            }

            tasks.withType(JavaCompile::class.java).forEach { compileTask ->
                compileTask.dependsOn(setupTask)

                // TODO: android projects will have possibly many non-test compile variants that should get filtered.
                if (!compileTask.name.contains("Test")) {
                    dbMigrateTask.dependsOn(compileTask)
                }
            }
        }
    }

    fun assembleTasks(project: Project): List<Task> = project.tasks.filter { t ->
        t.name.contains("assemble") || t.name.contains("Assemble")
    }

    fun getAndroidPlugin(project: Project): Plugin<Any> {
        if (isAndroidApplication(project)) {
            return project.plugins.getPlugin("com.android.application")
        }
        return project.plugins.getPlugin("com.android.library")
    }

    fun getConfigProperty(name: String, project: Project): String {
        val t = project.tasks.getByName("forsuredb")
        return t.property(name) as String
    }

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

    @Override
    override fun toString(): String {
        return "ForSureDBSetupTask(resourcesDirectory='$resourcesDirectory', fsSerializerFactoryClass=$fsSerializerFactoryClass, dbmsIntegratorClass=$dbmsIntegratorClass, applicationPackageName=$applicationPackageName, resultParameter=$resultParameter, recordContainer=$recordContainer, migrationDirectory=$migrationDirectory, appProjectDirectory=$appProjectDirectory)"
    }

    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        println("setuptask: ${this}")

        val plugin = project.plugins.getPlugin(ForSureDBPlugin::class.java)

        if (plugin.isJava(project)) {
            project.tasks.withType(JavaCompile::class.java).filter { t -> !t.name.contains("Test") }.forEach { t ->
                t.options.compilerArgs.add("-AapplicationPackageName=$applicationPackageName")
                t.options.compilerArgs.add("-AresourcesDirectory=$resourcesDirectory")
                t.options.compilerArgs.add("-AresultParameter=$resultParameter")
                t.options.compilerArgs.add("-ArecordContainer=$recordContainer")
                t.options.compilerArgs.add("-AmigrationDirectory=$migrationDirectory")
                t.options.compilerArgs.add("-AappProjectDirectory=$appProjectDirectory")
                if (plugin.migrateRequested(project)) {
                    t.options.compilerArgs.add("-AcreateMigrations=true")
                }
            }
        } else if (plugin.isAndroidApplication(project) || plugin.isAndroidLibrary(project)) {
            val androidPlugin = plugin.getAndroidPlugin(project)
            val defaultConfig = androidPlugin.javaClass.getField("defaultConfig").get(androidPlugin)
            val javaCompileOptions = defaultConfig.javaClass.getField("javaCompileOptions").get(defaultConfig)
            val annotationProcessorOptions = javaCompileOptions.javaClass.getField("annotationProcessorOptions").get(javaCompileOptions)
            val arguments: MutableMap<String, String> = annotationProcessorOptions.javaClass.getField("arguments").get(annotationProcessorOptions) as MutableMap<String, String>
            arguments.put("applicationPackageName", applicationPackageName)
            arguments.put("resourcesDirectory", resourcesDirectory)
            arguments.put("resultParameter", resultParameter)
            arguments.put("recordContainer", recordContainer)
            arguments.put("migrationDirectory", migrationDirectory)
            arguments.put("appProjectDirectory", appProjectDirectory)
            if (plugin.migrateRequested(project)) {
                arguments.put("createMigrations", "true")
            }
        } else {
            // TODO: techincally groovy could be supported, but I don't care so much at the moment
            throw IllegalStateException("forsuredbplugin only supports android library, android application, and java projects.")
        }
    }
}

open class ForSureDBMigrateTask : DefaultTask() {
    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        MigrationFileCopier(project).copyMigrations()
    }
}