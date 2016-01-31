# forsuredbplugin
A gradle plugin for the forsuredbproject that makes it easier to integrate with the forsuredbcompiler via your build.gradle script.

## Why should you use forsuredbplugin?
The following are the advantages of using the plugin that you don't get otherwise:

* Integration of forsuredb with your normal build process
* Automatic copying of generated assets (migration files) to your application's assets directory after successful compilation.
* Integration with other gradle plugins that should be able to run the forsuredbcompiler

## How do I configure the forsuredbplugin?
Add this to your build.gradle file:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        /*
         * You'll need to ensure that you include a plugin to run annotation processing.
         * For Android: com.neenbedankt.gradle.plugins:android-apt:1.6 seems to work well.
         * For Java, I haven't yet tested an existing annotation processor plugin, so I can't make a suggestion.
         */
        classpath 'com.fsryan:forsuredbplugin:0.2.0'
    }
}
```

Then add whatever you need to use the annotation processor, for example (if you're using the com.neenbedankt.gradle.plugins:android-apt:1.6 annotation processor):
```groovy
apply plugin: 'android-apt'
/* ... */
dependencies {
    /* ... */
    apt 'com.fsryan:forsuredbcompiler:0.4.1'
    /* ... */
}

forsuredb {
    applicationPackageName = 'com.forsuredb.testapp'            // The base package for your app
    resultParameter = "android.net.Uri"                         // The class you would like to use as the result of saving records
    recordContainer = "com.forsuredb.provider.FSContentValues"  // The class you would like to put record information into before saving
    migrationDirectory = 'app/src/main/assets'                  // The assests directory for your app relative to the working directory of your build
    appProjectDirectory = 'app'                                 // The base directory for your app relative to the working directory of your build
}
```

## What if I don't want to or can't use forsuredbplugin?
Many projects may not use gradle as a build system. forsuredbplugin is not absolutely necessary in order to use the forsuredb project. If you either cannot or don't want ot use forsuredbplugin, then pass the system properties forsuredbcompiler requires on the command line when you build your project.
For example:
```
$ <your build script> -DapplicationPackageName=com.forsuredb.testapp \
    -DresultParameter=android.net.Uri \
    -DrecordContainer=com.forsuredb.provider.FSContentValues \
    -DmigrationDirectory=app/src/main/assets
    -DappProjectDirectory=app
```
Also, ensure that the java annotation processor will run forsuredbcompiler using whatever build system you use.