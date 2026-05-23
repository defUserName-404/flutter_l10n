package com.defusername.flutter_l10n

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartImportStatement
import java.io.File

object ImportManager {
    data class ImportRequirements(
        val needsAppLocalizations: Boolean,
        val needsL10nExtension: Boolean,
        val needsL10nProviders: Boolean,
    )

    fun ensureImports(
        project: Project,
        psiFile: PsiFile,
        document: Document,
        config: L10nProjectConfig,
        requirements: ImportRequirements,
    ) {
        val packageName = packageName(project)
        val outputDirPkg = config.outputDirInPackage

        val appLocalizationImport = "package:$packageName/$outputDirPkg/${config.outputLocalizationFile}"
        val l10nExtensionImport = "package:$packageName/$outputDirPkg/l10n_extension.dart"
        val l10nProvidersImport = "package:$packageName/$outputDirPkg/l10n_providers.dart"

        val existingImports = PsiTreeUtil.findChildrenOfType(psiFile, DartImportStatement::class.java)
            .map { it.text }

        val toAdd = mutableListOf<String>()

        if (requirements.needsAppLocalizations && existingImports.none { it.contains(appLocalizationImport) }) {
            toAdd.add("import '$appLocalizationImport';")
        }

        if (requirements.needsL10nExtension && existingImports.none { it.contains(l10nExtensionImport) }) {
            toAdd.add("import '$l10nExtensionImport';")
        }

        if (requirements.needsL10nProviders && existingImports.none { it.contains(l10nProvidersImport) }) {
            toAdd.add("import '$l10nProvidersImport';")
        }

        if (toAdd.isEmpty()) return

        WriteCommandAction.runWriteCommandAction(project, "Add l10n imports", null, Runnable {
            val imports = PsiTreeUtil.findChildrenOfType(psiFile, DartImportStatement::class.java)
            val insertOffset = imports.lastOrNull()?.textRange?.endOffset?.plus(1) ?: 0

            document.insertString(insertOffset, toAdd.joinToString("\n") + "\n")
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }, psiFile)
    }

    fun generateSupportFiles(project: Project, config: L10nProjectConfig) {
        generateL10nExtensionFile(project, config)
    }

    private fun packageName(project: Project): String {
        val basePath = project.basePath ?: return "app"
        val pubspec = File(basePath, "pubspec.yaml")
        if (!pubspec.exists()) return "app"

        val line = pubspec.readLines().firstOrNull { it.trimStart().startsWith("name:") } ?: return "app"
        return line.substringAfter(':').trim().ifBlank { "app" }
    }

    private fun generateL10nExtensionFile(project: Project, config: L10nProjectConfig) {
        val basePath = project.basePath ?: return
        val outputDir = File(basePath, config.outputDir)
        val extensionFile = File(outputDir, "l10n_extension.dart")
        if (extensionFile.exists()) return

        outputDir.mkdirs()
        extensionFile.writeText(
            """
            import 'package:flutter/widgets.dart';
            import 'package:flutter_riverpod/flutter_riverpod.dart';
            import '${config.outputLocalizationFile}';
            import 'l10n_providers.dart';

            extension AppLocalizationsX on BuildContext {
              ${config.outputClass} get l10n => ${config.outputClass}.of(this)!;
            }

            extension L10nRef on WidgetRef {
              ${config.outputClass} get l10n => watch(l10nProvider);
            }
            """.trimIndent() + "\n",
        )
    }
}
