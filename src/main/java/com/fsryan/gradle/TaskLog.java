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
package com.fsryan.gradle;

public class TaskLog {

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

    public static void e(String message) {
        e(null, message);
    }

    public static void e(String task, String message) {
        log('E', task, message);
    }

    private static void log(char level, String task, String message) {
        System.out.println(level + "/[" + tag(task) + "]: " + (message == null ? "" : message));
    }

    private static String tag(String task) {
        return "forsuredbplugin" + (task == null ? "" : "." + task);
    }
}
