package com.defusername.flutter_l10n.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.defusername.flutter_l10n.core.L10nConfigLoader
import com.defusername.flutter_l10n.core.ServiceGraph
import com.defusername.flutter_l10n.extraction.ExtractionOutcome

class ExtractStringsAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        if (!psiFile.name.endsWith(".dart")) {
            Messages.showInfoMessage(project, "This action only works on Dart files.", "Flutter L10n")
            return
        }

        val config = L10nConfigLoader.load(project)
        val coordinator = ServiceGraph.coordinator()
        when (val outcome = coordinator.execute(project, editor, psiFile, config)) {
            ExtractionOutcome.Cancelled -> return
            ExtractionOutcome.NoExtractableStrings -> {
                Messages.showInfoMessage(project, "No extractable raw strings were found.", "Flutter L10n")
            }
            ExtractionOutcome.NothingSelected -> return
            is ExtractionOutcome.Failed -> {
                Messages.showErrorDialog(project, outcome.reason, "Flutter L10n")
            }
            is ExtractionOutcome.Completed -> {
                val summary = outcome.summary
                val message = buildString {
                    append("Extracted ${summary.totalCount} string(s). ")
                    append("Riverpod: ${summary.riverpodCount}, context.l10n: ${summary.contextCount}, fallback: ${summary.fallbackCount}")
                    if (summary.staticErrorCount > 0) {
                        append(". ${summary.staticErrorCount} skipped (static context)")
                    }
                    append(".")
                }
                Messages.showInfoMessage(project, message, "Flutter L10n")
            }
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}
