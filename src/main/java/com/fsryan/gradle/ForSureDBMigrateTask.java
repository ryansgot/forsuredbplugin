package com.fsryan.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

public class ForSureDBMigrateTask extends DefaultTask {

    @TaskAction
    public void migrate() {
        ForSureExtension extension = getProject().getExtensions().findByType(ForSureExtension.class);
        if (extension == null) {
            throw new IllegalStateException("Must define all properties in the forsuredb extension");
        }

        System.out.println("[dbmigrate]: output migrations to default class path with package: " + System.getProperty("applicationPackageName"));

        String assetDir = extension.getMigrationDirectory();
        String generatedDir = generatedDirectory(extension.getAppProjectDirectory());
        for (File file : new File(generatedDir).listFiles()) {
            if (file.getName().endsWith("migration")) {
                String outFile = assetDir + File.separator + file.getName() + ".xml";
                System.out.println("[dbmigrate]: copying " + file.getAbsolutePath() + " to " + outFile);
                try {
                    copyFile(file.getAbsolutePath(), outFile);
                } catch (IOException ioe) {
                    System.out.println("[dbmigrate]: FAILED copy " + file.getAbsolutePath() + " to " + outFile);
                    ioe.printStackTrace();
                }
            }
        }

        System.out.println("[dbmigrate]: copied migrations from " + generatedDir + " into " + assetDir);
    }

    private boolean copyFile(String input, String output) throws IOException {
        File inFile = new File(input);
        if (!inFile.exists()) {
            System.out.println("[copyFile]: input " + input + " did not exist");
            return false;
        }

        File outFile = new File(output);
        if (!ensureFileExists(outFile)) {
            System.out.println("[copyFile]: could not create " + outFile.getPath());
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
                System.out.println("[ensureFileExists]: creating file " + toCreate.getPath());
                if (!toCreate.createNewFile()) {
                    return false;
                }
            } else {
                System.out.println("[ensureFileExists]: creating directory " + toCreate.getPath());
                if (!toCreate.mkdir()) {
                    return false;
                }
            }
        }

        return true;
    }

    private String generatedDirectory(String appProjectDirectory) {
        appProjectDirectory = appProjectDirectory == null ? "" : appProjectDirectory;
        return new StringBuffer(appProjectDirectory).append(appProjectDirectory.isEmpty() ? "" : File.separator)
                .append("build").append(File.separator)
                .append("intermediates").append(File.separator)
                .append("classes").append(File.separator)
                .append("debug").append(File.separator) // TODO: avoid forcing the user to compileDebugJava
                .append(System.getProperty("applicationPackageName").replace(".", File.separator))
                .toString();
    }
}
