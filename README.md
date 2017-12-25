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
        classpath 'com.fsryan:forsuredbplugin:0.4.0'
    }
}
```

Then add whatever you need to use the annotation processor, for example (if you're using the com.neenbedankt.gradle.plugins:android-apt:1.6 annotation processor):
```groovy
apply plugin: 'android-apt'
apply plugin: apply plugin: 'com.fsryan.forsuredb'
/* ... */
dependencies {
    /* ... */
    compile 'com.fsryan.forsuredb:sqlitelib:0.4.0'              // Necessary only if you use the dbmsIntegratorClass value demonstrated in this example--but you don't absolutely need this specific one
    compile 'com.fsryan.forsuredb:forsuredbapi:0.9.0'
    apt 'com.fsryan.forsuredb:forsuredbcompiler:0.9.0'
    /* ... */
}

forsuredb {
    applicationPackageName = 'com.forsuredb.testapp'            // The base package for your app
    resultParameter = "android.net.Uri"                         // The class you would like to use as the result of saving records
    recordContainer = "com.forsuredb.provider.FSContentValues"  // The class you would like to put record information into before saving
    migrationDirectory = 'app/src/main/assets'                  // The assests directory for your app relative to the working directory of your build
    appProjectDirectory = 'app'                                 // The base directory for your app relative to the working directory of your build
    resourcesDirectory = 'app/src/main/resources'               // The directory that will contain META-INF/services so that your plugins can get picked up at runtime
    fsSerializerFactoryClass = 'my.json.adapter.FactoryClass'  // (optional) A class implementing FSSerializerFactory used to create your own custom serializer for object document storage
    dbmsIntegratorClass = 'com.fsryan.forsuredb.sqlitelib.SqlGenerator' // NOT OPTIONAL as of fosuredbplugin 0.4.0
}
```

## Revisions

### 0.5.0
- Only valid for use with forsuredbcompiler 0.13.0
- reworks the plugin to be compatible with android and with java projects

### 0.4.0
- Added task to properly configure the DBMSIntegrator plugin whenever app is assembled

### 0.3.2
- Changed task that configured the FSJsonAdapterFactory plugin to configure the FSSerializerFactory plugin

### 0.3.1
- Added task to properly configure the FSJsonAdapterFactory plugin whenever app is assembled

### 0.3.0
- Support setting fsJsonAdapterFactoryClass system property.

### 0.2.1
- Some bug fixes and first reliable version
