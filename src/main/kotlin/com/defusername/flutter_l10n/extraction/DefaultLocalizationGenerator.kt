package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.project.Project

class DefaultLocalizationGenerator(
    private val runner: (Project) -> Unit,
) : LocalizationGenerator {
    override fun run(project: Project) {
        runner(project)
    }
}
