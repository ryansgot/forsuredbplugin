package com.fsryan.gradle.forsuredb

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.util.GUtil

import java.nio.file.Files
import java.nio.file.Paths

class ForSureDBPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def forsuredbExt = project.extensions.create("forsuredb", ForSureDBExt)
        def dbMigrateTask = project.tasks.create("dbMigrate", ForSureDBMigrateTask)
        dbMigrateTask.outputs.upToDateWhen { false }
        def registerDbmsIntegratorTask = project.tasks.create(RegisterCustomDbmsIntegratorTask.NAME, RegisterCustomDbmsIntegratorTask)
        def registerFSSerializerFactoryTask = project.tasks.create(RegisterCustomFSSerializerFactoryTask.NAME, RegisterCustomFSSerializerFactoryTask)

        if (isJava(project)) {
            TaskLog.i("detected Java project")

            if (isKotlin(project)) {
                TaskLog.i('detected kotlin project')
                def kaptExt = kaptExt(project)
                if (kaptExt == null) {
                    throw new IllegalStateException("Must apply kotlin-kapt plugin to use forsuredb in a kotlin project")
                }
                addKaptArguments(kaptExt, fsdbExt(project), migrateRequested(project), true)
                findNonTestKotlinCompileTasks(project).forEach { enforceTaskDependency(it, dbMigrateTask) }
            }

            // ensure that forsuredb SPI plugins are in place on processing resources
            processResourcesTasks(project).findAll { t -> !t.name.contains("Test") }.forEach { t ->
                dependUponPluginRegistryTasks(project, t)
                if (migrateRequested(project)) {
                    enforceTaskDependency(dbMigrateTask, t)
                }
            }

            project.afterEvaluate {
                project.tasks.withType(JavaCompile).findAll { !it.name.contains("Test") }.forEach { t ->
                    TaskLog.i("Setting processor arguments for ${t.name}")
                    addProcessorArgs(project, t, migrateRequested(project), true)
                    enforceTaskDependency(t, dbMigrateTask)
                }
            }
        } else if (isAndroid(project)) {
            project.afterEvaluate {
                TaskLog.i("detected Android project")

                // TODO: allow for the compile configuration for migrations
                def hasSetDbMigrateCompileTaskDependency = false
                findAndroidVariants(project).forEach { v ->
                    TaskLog.i("Setting processor args for java compile task: ${v.javaCompile.name}")
                    addProcessorArgs(project, v.javaCompile, migrateRequested(project), false)
                    dependUponPluginRegistryTasks(project, v.getMergeResources())

                    if (!hasSetDbMigrateCompileTaskDependency && v.buildType.debuggable) {
                        hasSetDbMigrateCompileTaskDependency = true
                        enforceTaskDependency(v.javaCompile, dbMigrateTask)

                        if (fabricPluginAdded(project)) {
                            // see method
                            warnFabricWorkaround(v)
                        } else {
                            // Android seems to be okay with merging assets either before
                            // or after compilation. This moves the asset merging until
                            // after compilation and the assets output have been copied
                            // to their correct locations.
                            enforceTaskDependency(dbMigrateTask, v.getMergeAssets())
                        }
                        def kotlinCompileTask = project.tasks.findByName('compile' + GUtil.toCamelCase(v.name) + 'Kotlin')
                        if (kotlinCompileTask != null) {
                            enforceTaskDependency(kotlinCompileTask, dbMigrateTask)
                        }
                    }
                }
            }
        } else {
            throw new IllegalStateException("Could not detect project type--not Android and not Java")
        }
    }

    static void enforceTaskDependency(Task dependency, Task dependent) {
        TaskLog.i("Setting ${dependent.name} depends on ${dependency.name}")
        dependent.dependsOn(dependency)
    }

    static def findNonTestKotlinCompileTasks(Project p) {
        return p.tasks.findAll { !it.name.contains('Test') && it.name.contains('compile') && it.name.contains('Kotlin') }
    }

    static ForSureDBExt fsdbExt(Project project) {
        return project.extensions.findByType(ForSureDBExt)
    }

    static def kaptExt(Project project) {
        return project.extensions.findByName('kapt')
    }

    static boolean isKotlin(Project p) {
        return p.plugins.hasPlugin('kotlin')
    }

    static def dependUponPluginRegistryTasks(Project project, Task t) {
        if (t == null) {
            throw new IllegalArgumentException("Cannot set plugin registry tasks as dependency for null task")
        }
        enforceTaskDependency(registerCustomDbmsIntegratorTask(project), t)
        enforceTaskDependency(registerCustomFSSerializerFactoryTask(project), t)
    }

    static RegisterCustomDbmsIntegratorTask registerCustomDbmsIntegratorTask(Project project) {
        return project.tasks.withType(RegisterCustomDbmsIntegratorTask).first()
    }

    static RegisterCustomFSSerializerFactoryTask registerCustomFSSerializerFactoryTask(Project project) {
        return project.tasks.withType(RegisterCustomFSSerializerFactoryTask).first()
    }

    static def findAndroidVariants(Project project) {
        if (project.android == null) {
            return []
        }
        return project.android.hasProperty('applicationVariants') ? project.android.applicationVariants : project.android.libraryVariants
    }

    static TaskCollection<Task> processResourcesTasks(Project project) {
        return project.tasks.withType(ProcessResources)
    }

    static def isAndroid(Project project) {
        return isAndroidApplication(project) || isAndroidLibrary(project)
    }

    static def isAndroidApplication(Project project) {
        return project.plugins.hasPlugin("com.android.application")
    }

    static def isAndroidLibrary(Project project) {
        return project.plugins.hasPlugin("com.android.library")
    }

    static boolean migrateRequested(Project project) {
        return project.gradle.startParameter.taskRequests.collect { it.args }.flatten().find { requested ->
            requested.endsWith("${project.name}:dbMigrate") || requested == "dbMigrate"
        } != null
    }

    static def isJava(Project project) {
        return project.plugins.hasPlugin(JavaPlugin)
    }

    static def addProcessorArgs(Project project, JavaCompile t, boolean migrate, boolean includeGeneratedAnnotation) {
        def forsuredbExt = fsdbExt(project)
        addProcessorArg(t, "applicationPackageName", forsuredbExt.applicationPackageName)
        addProcessorArg(t, "resourcesDirectory", forsuredbExt.resourcesDirectory)
        addProcessorArg(t, "resultParameter", forsuredbExt.resultParameter)
        addProcessorArg(t, "recordContainer", forsuredbExt.recordContainer)
        addProcessorArg(t, "migrationDirectory", forsuredbExt.migrationDirectory)
        addProcessorArg(t, "appProjectDirectory", forsuredbExt.appProjectDirectory)
        addProcessorArg(t, "addGeneratedAnnotation", includeGeneratedAnnotation ? "true" : "false")
        addProcessorArg(t, "createMigrations", migrate ? "true" : "false")

        def kaptExt = project.extensions.findByName('kapt')
        if (kaptExt == null) {
            TaskLog.w("Did not find kapt extension property, not adding processor args to kapt")
            return
        }
        addKaptArguments(kaptExt, forsuredbExt, migrate, includeGeneratedAnnotation)
    }

    private static def fabricPluginAdded(Project p) {
        return p.plugins.findPlugin('io.fabric') != null
    }

    private static def warnFabricWorkaround(v) {
        def message = "\n**********\n\n"
        message += "Detected dbMigrate task and fabric plugin task would create circular task "
        message += "dependency attempting to merge migration assets into an assembled binary. "
        message += "The dbMigrate task depends upon ${v.getMergeAssets().name} running AFTER "
        message += "compilation, but the fabric plugin depends upon it running BEFORE compilation. "
        message += "There is no way to resolve this except to run a build with dbMigrate first, "
        message += "then follow with ${v.getMergeAssets().name} in a subsequent build. I'm sorry "
        message += "for the inconvenience.\n\n**********"
        TaskLog.w(message)
    }

    private static def addProcessorArg(JavaCompile t, String key, String value) {
        t.options.compilerArgs.add("-Aforsuredb.$key=$value")
    }

    private static def addKaptArguments(kaptExt, ForSureDBExt forsuredbExt, boolean migrate, boolean includeGeneratedAnnotation) {
        TaskLog.i("Adding processor args to kapt")
        kaptExt.arguments {
            arg("forsuredb.applicationPackageName", forsuredbExt.applicationPackageName)
            arg("forsuredb.resourcesDirectory", forsuredbExt.resourcesDirectory)
            arg("forsuredb.resultParameter", forsuredbExt.resultParameter)
            arg("forsuredb.recordContainer", forsuredbExt.recordContainer)
            arg("forsuredb.migrationDirectory", forsuredbExt.migrationDirectory)
            arg("forsuredb.appProjectDirectory", forsuredbExt.appProjectDirectory)
            arg("forsuredb.addGeneratedAnnotation", includeGeneratedAnnotation ? "true" : "false")
            arg("forsuredb.createMigrations", migrate ? "true" : "false")
        }
    }
}

class ForSureDBExt {

    String resourcesDirectory
    String fsSerializerFactoryClass
    String dbmsIntegratorClass
    String applicationPackageName
    String resultParameter
    String recordContainer
    String migrationDirectory
    String appProjectDirectory

    @Override
    String toString() {
        return "ForSureDBExt(resourcesDirectory='$resourcesDirectory', fsSerializerFactoryClass=$fsSerializerFactoryClass, dbmsIntegratorClass='$dbmsIntegratorClass', applicationPackageName='$applicationPackageName', resultParameter='$resultParameter', recordContainer='$recordContainer', migrationDirectory='$migrationDirectory', appProjectDirectory='$appProjectDirectory')"
    }
}

class ForSureDBMigrateTask extends DefaultTask {

    @TaskAction
    def moveMigrationFiles(IncrementalTaskInputs inputs) {
        ForSureDBExt forsuredbExt = project.extensions.findByType(ForSureDBExt)
        File outptDir = new File(project.projectDir, legacyRelativeDirHack(forsuredbExt))
        if (!outptDir.exists()) {
            TaskLog.i("dbMigrate", "creating $outptDir to store migration files")
            if (!outptDir.mkdirs()) {
                throw new IllegalStateException("Failed to create migration directory")
            }
        }
        if (!outptDir.isDirectory()) {
            throw new IllegalStateException("Detected migrationDirectory path was not a directory: ${forsuredbExt.migrationDirectory}")
        }

        new FileNameByRegexFinder()
                .getFileNames(project.buildDir.absolutePath + File.separator + '', /.*\.migration$/)
                .forEach { fPath ->
                    String migFile = fPath.substring(fPath.lastIndexOf(File.separator) + 1) + ".json"
                    if (!new File(outptDir, migFile).exists()) {
                        TaskLog.i("dbMigrate", "copying migration file ${fPath} to ${new File(outptDir, migFile)}")
                        Files.copy(Paths.get(fPath), Paths.get(outptDir.absolutePath, migFile))
                    }
                }
    }

    // Previous versions used to require that you specify paths relative to root project base directory.
    // That was rather silly, so that requirement is taken away in a manner that is backward compatible.
    private static def legacyRelativeDirHack(ForSureDBExt forsuredbExt) {
        String migDir = forsuredbExt.migrationDirectory
        int idx = migDir.indexOf(File.separator)
        String firstPathPart = idx < 0 ? "" : migDir.substring(0, idx)
        return firstPathPart == forsuredbExt.appProjectDirectory ? migDir.substring(idx + 1) : migDir
    }
}
