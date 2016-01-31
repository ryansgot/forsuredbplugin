package com.fsryan.gradle;

public class TaskLog {

    public static void e(String message) {
        e(null, message);
    }

    public static void e(String task, String message) {
        log('E', task, message);
    }

    public static void d(String message) {
        d(null, message);
    }

    public static void d(String task, String message) {
        log('D', task, message);
    }

    public static void w(String message) {
        w(null, message);
    }

    public static void w(String task, String message) {
        log('W', task, message);
    }

    public static void i(String message) {
        i(null, message);
    }

    public static void i(String task, String message) {
        log('I', task, message);
    }

    private static void log(char level, String task, String message) {
        System.out.println(level + "/[" + tag(task) + "]: " + (message == null ? "" : message));
    }

    private static String tag(String task) {
        return "forsuredbplugin" + (task == null ? "" : "." + task);
    }
}
