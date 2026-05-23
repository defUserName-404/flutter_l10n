package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.project.Project
import com.defusername.flutter_l10n.arb.ArbManager
import com.defusername.flutter_l10n.config.L10nProjectConfig

class ArbLocaleProvider : LocaleProvider {
    override fun availableLocales(project: Project, config: L10nProjectConfig): List<String> =
        ArbManager.getAvailableLocales(project, config)
}
