package com.fsryan.gradle.forsuredb

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
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
            dbMigrateTask.dependsOn(setupTask)

            tasks.withType(JavaCompile::class.java).forEach { compileTask ->
                compileTask.dependsOn(setupTask)

                // TODO: android projects will have possibly many non-test compile variants that should get filtered.
                if (!compileTask.name.contains("Test")) {
                    dbMigrateTask.dependsOn(compileTask)
                }
            }
        }
    }

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

    override fun toString(): String {
        return "ForSureDBSetupTask(resourcesDirectory='$resourcesDirectory', fsSerializerFactoryClass=$fsSerializerFactoryClass, dbmsIntegratorClass=$dbmsIntegratorClass, applicationPackageName=$applicationPackageName, resultParameter=$resultParameter, recordContainer=$recordContainer, migrationDirectory=$migrationDirectory, appProjectDirectory=$appProjectDirectory)"
    }

    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        println("setuptask: ${this}")

        val plugin = project.plugins.getPlugin(ForSureDBPlugin::class.java)
        val javaPlugin = project.plugins.getPlugin(JavaPlugin::class.java)

        project.tasks.withType(JavaCompile::class.java).filter { t -> !t.name.contains("Test") }.forEach { t ->

            println("Attempting to send options to compiler for task: ${t.name}")

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
    }
}

open class ForSureDBMigrateTask : DefaultTask() {
    @TaskAction
    fun execute(inputs: IncrementalTaskInputs) {
        println("executing dbMigrate")


    }
}