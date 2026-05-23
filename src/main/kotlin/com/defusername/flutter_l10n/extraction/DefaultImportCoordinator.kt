package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.defusername.flutter_l10n.config.L10nProjectConfig
import com.defusername.flutter_l10n.imports.ImportManager

class DefaultImportCoordinator : ImportCoordinator {
    override fun ensureImports(
        project: Project,
        psiFile: PsiFile,
        editor: Editor,
        config: L10nProjectConfig,
        replacements: List<ResolvedReplacement>,
    ) {
        val requirements = ImportManager.ImportRequirements(
            needsAppLocalizations = replacements.any { it.replacement.needsAppLocalizationsImport },
            needsL10nExtension = replacements.any { it.replacement.needsL10nExtensionImport },
            needsL10nProviders = replacements.any { it.replacement.needsL10nProvidersImport },
        )

        ImportManager.generateSupportFiles(project, config)
        ImportManager.ensureImports(project, psiFile, editor.document, config, requirements)
    }
}
