package com.fsryan.gradle.forsuredb

class TaskLog {

    static void d(String message) {
        d(null, message)
    }

    static void d(String task, String message) {
        log('D', task, message)
    }

    static void w(String message) {
        w(null, message)
    }

    static void w(String task, String message) {
        log('W', task, message)
    }

    static void i(String message) {
        i(null, message)
    }

    static void i(String task, String message) {
        log('I', task, message)
    }

    static void e(String message) {
        e(null, message)
    }

    static void e(String task, String message) {
        log('E', task, message)
    }

    private static void log(String level, String task, String message) {
        System.out.println(level + "/[" + tag(task) + "]: " + (message == null ? "" : message))
    }

    private static String tag(String task) {
        return "forsuredbplugin" + (task == null ? "" : "." + task)
    }
}

