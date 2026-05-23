plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.2"
}

// Must match <id> in plugin.xml: com.defusername.flutter_l10n
group = "com.defusername"
version = "1.0.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1")   // minimum IDE version to build against
    type.set("IC")           // IC = IntelliJ Community; use "AI" for Android Studio build

    // "Dart" plugin provides all Dart PSI classes:
    //   DartStringLiteralExpression, DartMethodDeclaration,
    //   DartClassDefinition, DartFormalParameterList, etc.
    plugins.set(listOf("Dart"))
}

dependencies {
    // JSON parsing for ARB files (org.json is NOT bundled with IntelliJ)
    implementation("org.json:json:20240303")
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        // Plugin works on IntelliJ 2024.1+ and Android Studio Koala+
        sinceBuild.set("241")
        untilBuild.set("243.*")
    }

    // Produce a zip ready to install via "Install Plugin from Disk"
    buildPlugin {
        archiveBaseName.set("flutter-l10n")
    }
}