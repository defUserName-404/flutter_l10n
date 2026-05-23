package com.defusername.flutter_l10n

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDocumentManager

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

        val extracted = PsiStringExtractor.extract(psiFile)
        if (extracted.isEmpty()) {
            Messages.showInfoMessage(project, "No extractable raw strings were found.", "Flutter L10n")
            return
        }

        val existingKeys = ArbManager.existingMessageKeys(project, config)
        val reviewDialog = ExtractStringsDialog(project, extracted, existingKeys)
        if (!reviewDialog.showAndGet()) return

        val selected = reviewDialog.getSelectedEntries()
        if (selected.isEmpty()) return

        val locales = ArbManager.getAvailableLocales(project, config)
        val translationsByKey = if (locales.size > 1) {
            val multi = MultiLocaleDialog(project, selected, locales, config.templateLocale)
            if (!multi.showAndGet()) return
            multi.getTranslationsByKey()
        } else {
            emptyMap()
        }

        val arbEntries = selected.map { (item, key) ->
            ArbEntry(
                key = key,
                value = item.raw,
                translations = translationsByKey[key].orEmpty(),
            )
        }

        if (!ArbManager.addEntries(project, config, arbEntries)) {
            Messages.showErrorDialog(project, "Failed to update ARB files.", "Flutter L10n")
            return
        }

        data class Resolved(
            val item: ExtractedString,
            val key: String,
            val replacement: ReplacementStrategy.Result,
        )

        val resolved = selected.map { (item, key) ->
            val psiAtOffset = psiFile.findElementAt(item.startOffset)
            val replacement = if (psiAtOffset != null) {
                ReplacementStrategy.resolve(psiAtOffset, key, config.outputClass)
            } else {
                ReplacementStrategy.Result(
                    expression = "${config.outputClass}.of(context)!.$key",
                    kind = ReplacementStrategy.Kind.ContextRaw,
                    needsL10nProvidersImport = false,
                    needsAppLocalizationsImport = true,
                    needsL10nExtensionImport = false,
                )
            }
            Resolved(item, key, replacement)
        }

        WriteCommandAction.runWriteCommandAction(project, "Extract Strings to L10n", null, Runnable {
            val document = editor.document
            var text = document.text

            for (entry in resolved.sortedByDescending { it.item.startOffset }) {
                text = text.substring(0, entry.item.startOffset) +
                    entry.replacement.expression +
                    text.substring(entry.item.endOffset)
            }

            document.setText(text)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }, psiFile)

        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val requirements = ImportManager.ImportRequirements(
            needsAppLocalizations = resolved.any { it.replacement.needsAppLocalizationsImport },
            needsL10nExtension = resolved.any { it.replacement.needsL10nExtensionImport },
            needsL10nProviders = resolved.any { it.replacement.needsL10nProvidersImport },
        )

        ImportManager.generateSupportFiles(project, config)
        ImportManager.ensureImports(project, psiFile, editor.document, config, requirements)

        GenL10nRunner.run(project)

        val riverpodCount = resolved.count { it.replacement.kind == ReplacementStrategy.Kind.RiverpodRef }
        val contextCount = resolved.count { it.replacement.kind == ReplacementStrategy.Kind.ContextL10n }
        val fallbackCount = resolved.count { it.replacement.kind == ReplacementStrategy.Kind.ContextRaw }

        Messages.showInfoMessage(
            project,
            "Extracted ${resolved.size} string(s). Riverpod: $riverpodCount, context.l10n: $contextCount, fallback: $fallbackCount.",
            "Flutter L10n",
        )
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor != null
    }
}
