package com.defusername.flutter_l10n

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

object GenL10nRunner {
    fun run(project: Project) {
        val basePath = project.basePath ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running flutter gen-l10n", false) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Generating localizations..."

                val command = GeneralCommandLine("flutter", "gen-l10n")
                    .withWorkDirectory(basePath)
                    .withEnvironment(System.getenv())

                val output = runCatching {
                    CapturingProcessHandler(command).runProcess(60_000)
                }.getOrNull()

                if (output != null && output.exitCode == 0) {
                    LocalFileSystem.getInstance().refresh(true)
                    notify(project, NotificationType.INFORMATION, "Flutter L10n", "flutter gen-l10n completed.")
                } else {
                    val stderr = output?.stderr.orEmpty().trim()
                    val stdout = output?.stdout.orEmpty().trim()
                    val message = if (stderr.isNotBlank()) stderr else stdout.ifBlank { "Failed to run flutter gen-l10n." }
                    notify(project, NotificationType.ERROR, "Flutter L10n", message.takeLast(500))
                }
            }
        })
    }

    private fun notify(project: Project, type: NotificationType, title: String, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Flutter L10n")
            .createNotification(title, content, type)
            .notify(project)
    }
}
