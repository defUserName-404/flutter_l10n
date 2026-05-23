package com.defusername.flutter_l10n.gen

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

object GenL10nRunner {
    fun run(project: Project) {
        val basePath = project.basePath ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running flutter gen-l10n", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Generating localizations..."

                val flutterPath = resolveFlutter()
                if (flutterPath == null) {
                    notify(
                        project, NotificationType.WARNING, "Flutter L10n",
                        "Could not find 'flutter' on PATH. " +
                                "Run 'flutter gen-l10n' manually or set FLUTTER_ROOT environment variable.",
                    )
                    return
                }

                val command = GeneralCommandLine(flutterPath, "gen-l10n")
                    .withWorkDirectory(basePath)
                    .withEnvironment(System.getenv())

                val output = runCatching {
                    CapturingProcessHandler(command).runProcess(120_000)
                }.getOrNull()

                if (output != null && output.exitCode == 0) {
                    LocalFileSystem.getInstance().refresh(true)
                    notify(project, NotificationType.INFORMATION, "Flutter L10n", "flutter gen-l10n completed.")
                } else {
                    val stderr = output?.stderr.orEmpty().trim()
                    val stdout = output?.stdout.orEmpty().trim()
                    val message = if (stderr.isNotBlank()) {
                        if (stderr.contains("untranslated"))
                            stderr.lines().firstOrNull { it.contains("untranslated") }
                                ?.let { "$it\nRun flutter gen-l10n manually for full details." }
                                ?: stderr.takeLast(500)
                        else stderr.takeLast(500)
                    } else stdout.ifBlank { "Failed to run flutter gen-l10n." }
                    notify(project, NotificationType.WARNING, "Flutter L10n", message)
                }
            }
        })
    }

    private fun resolveFlutter(): String? {
        val fromEnv = System.getenv("FLUTTER_ROOT")
        if (fromEnv != null) {
            val candidate = "$fromEnv/bin/flutter"
            if (File(candidate).exists()) return candidate
        }

        return runCatching {
            val proc = Runtime.getRuntime().exec(arrayOf("which", "flutter"))
            proc.waitFor()
            proc.inputStream.bufferedReader().readText().trim().ifBlank { null }
        }.getOrNull()
    }

    private fun notify(project: Project, type: NotificationType, title: String, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Flutter L10n")
            .createNotification(title, content, type)
            .notify(project)
    }
}
