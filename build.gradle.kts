import godot.entrygenerator.settings.RegisteredNameMode
import godot.entrygenerator.settings.RegistrationFileLayoutMode
//import godot.entrygenerator.settings.RegistrationFileIndentation
import godot.gradle.GodotLanguage

plugins {
    id("com.utopia-rise.godot-kotlin-jvm") version "0.16.2-4.6.3"
}

repositories {
    mavenCentral()
}

godot {
    // --------- Setup ---------
    languages.set(setOf(GodotLanguage.JAVA))
    godotProjectDirectory.set(file(".")) // only change this if the Godot project root is not this Gradle project directory
    registrationFilesDirectory.set(file("gdj")) // defaults to <godotProjectDirectory>/gdj

    registrationFilesLayoutMode.set(RegistrationFileLayoutMode.FLAT)
    registrationNameMode.set(RegisteredNameMode.SIMPLE_NAME)
    //registrationFilesIndentation.set(RegistrationFileIndentation.SPACE)

    // Enable to mark this Gradle project as a reusable library rather than a runnable Godot project.
    //isLibrary.set(true)

    // Disable .gdj registration file handling while keeping class scanning and entry generation enabled.
    //disableGdj.set(true)

    // Enable coroutines integration with Godot signal/lifecycle callbacks.
    //isGodotCoroutinesEnabled.set(true)

    // --------- Toolchain ---------
    //javaVersion.set(17)
    //kotlinVersion.set("2.3.20")
    //scalaVersion.set("3.6.3")
}
