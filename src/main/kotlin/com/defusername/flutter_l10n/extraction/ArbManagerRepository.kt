package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.project.Project
import com.defusername.flutter_l10n.ArbEntry
import com.defusername.flutter_l10n.arb.ArbManager
import com.defusername.flutter_l10n.config.L10nProjectConfig

class ArbManagerRepository : ArbRepository {
    override fun upsertEntries(project: Project, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean =
        ArbManager.upsertEntries(project, config, entries)
}
