/*
    forsuredbplugin, a gradle plugin compainion for the forsuredb project
    Copyright (C) 2015  Ryan Scott

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

    Contact Ryan at ryansgot@gmail.com
 */
package com.fsryan.gradle.legacy;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

public class ForSureDBMigrateTask extends DefaultTask {

    public static final String NAME = "dbmigrate";
    private static final String javaClassesPath = "build" + File.separator + "classes";
    private static final String androidClassesPath = "build" + File.separator + "intermediates" + File.separator + "classes";

    @TaskAction
    public void migrate() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must define all properties in the forsuredb extension");
        }

        TaskLog.i(NAME, "output migrations to default class path with package: " + System.getProperty("applicationPackageName"));

        String assetDir = extension.getMigrationDirectory();
        File classesDir = packageClassesDir(extension.getAppProjectDirectory());
        TaskLog.i(NAME, "class directory: " + classesDir.getAbsolutePath());
        for (File file : classesDir.listFiles()) {
            if (file.getName().endsWith("migration")) {
                String outFile = assetDir + File.separator + file.getName() + ".json";
                TaskLog.i(NAME, "copying " + file.getAbsolutePath() + " to " + outFile);
                try {
                    copyFile(file.getAbsolutePath(), outFile);
                } catch (IOException ioe) {
                    TaskLog.e(NAME, "FAILED copy " + file.getAbsolutePath() + " to " + outFile);
                    ioe.printStackTrace();
                }
            }
        }

        TaskLog.i(NAME, "copied migrations from " + classesDir.getAbsolutePath() + " into " + assetDir);
    }

    private boolean copyFile(String input, String output) throws IOException {
        File inFile = new File(input);
        if (!inFile.exists()) {
            TaskLog.e(NAME, "input " + input + " did not exist");
            return false;
        }

        File outFile = new File(output);
        if (!ensureFileExists(outFile)) {
            TaskLog.e(NAME, "could not create " + outFile.getPath());
            return false;
        }

        FileInputStream inReader = new FileInputStream(inFile);
        FileOutputStream outWriter = new FileOutputStream(outFile);

        int read;
        while (-1 != (read = inReader.read())) {
            outWriter.write(read);
        }
        inReader.close();
        outWriter.close();

        return true;
    }

    private boolean ensureFileExists(File file) throws IOException {
        Stack<File> fileStack = new Stack<File>();

        File temp = file;
        while (temp != null && !temp.exists()) {
            fileStack.push(temp);
            temp = temp.getParentFile();
        }

        while (fileStack.size() > 0) {
            File toCreate = fileStack.pop();
            if (fileStack.size() == 0) {
                TaskLog.w(NAME, "creating file " + toCreate.getPath());
                if (!toCreate.createNewFile()) {
                    return false;
                }
            } else {
                TaskLog.w(NAME, "creating directory " + toCreate.getPath());
                if (!toCreate.mkdir()) {
                    return false;
                }
            }
        }

        return true;
    }

    private File packageClassesDir(String appProjectDirectory) {
        appProjectDirectory = appProjectDirectory == null ? "" : appProjectDirectory;
        final String packagePath = System.getProperty("applicationPackageName").replace(".", File.separator);

        // Try the common place for java project classes to be after compilation
        String baseClassesPath = (appProjectDirectory.isEmpty() ? "" : appProjectDirectory + File.separator) + javaClassesPath;
        File packageClassesDir = packageClassesDirGivenClassesPath(baseClassesPath, packagePath);
        if (packageClassesDir != null) {
            return packageClassesDir;
        }
        TaskLog.w(NAME, baseClassesPath + " either does not exist or is not a directory");

        // Try the common place for android project classes to be after compilation
        baseClassesPath = (appProjectDirectory.isEmpty() ? "" : appProjectDirectory + File.separator) + androidClassesPath;
        packageClassesDir = packageClassesDirGivenClassesPath(baseClassesPath, packagePath);
        if (packageClassesDir == null) {
            throw new IllegalStateException("Could not find dir for compiled classes directory for package: " + System.getProperty("applicationPackageName"));
        }

        return packageClassesDir;
    }

    private File packageClassesDirGivenClassesPath(String classesBasePath, String packagePath) {
        File classesDir = new File(classesBasePath);
        if (!classesDir.exists() || !classesDir.isDirectory()) {
            TaskLog.e(NAME, classesDir.getAbsolutePath() + " " + (classesDir.exists() ? "is not a directory" : "does not exist"));
            return null;
        }

        for (File subfile : classesDir.listFiles()) {
            if (!subfile.isDirectory()) {
                continue;
            }

            // Base case for recursion
            if (containsSubdirectoryPath(subfile, packagePath)) {
                return new File(subfile.getAbsolutePath() + File.separator + packagePath);
            }

            // Recursively check subdirectories of this directory
            File subDir = packageClassesDirGivenClassesPath(subfile.getAbsolutePath(), packagePath);
            if (subDir != null) {
                return subDir;
            }
        }

        TaskLog.e(NAME, "could not find compiled classes directory for : " + System.getProperty("applicationPackageName"));
        return null;
    }

    private boolean containsSubdirectoryPath(File dir, String subdirectoryPath) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return false;
        }

        // cleanup for next loop or base case (subdirectoryPath == "")
        int index = subdirectoryPath.indexOf('/');
        String firstSubdirectory = index == -1 ? subdirectoryPath : subdirectoryPath.substring(0, index);
        subdirectoryPath = index == -1 ? "" : subdirectoryPath.substring(index + 1);

        for (File f : dir.listFiles()) {
            if (!f.isDirectory() || !f.getName().equals(firstSubdirectory)) {
                continue;
            }
            // if the subdirectoryPath is empty, then the final subdirectory has been matched
            if (subdirectoryPath.isEmpty() || containsSubdirectoryPath(f, subdirectoryPath)) {
                return true;
            }
        }
        return false;
    }
}
