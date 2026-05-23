package com.defusername.flutter_l10n.extraction

import com.defusername.flutter_l10n.ArbEntry
import com.defusername.flutter_l10n.SelectedEntry

class DefaultArbEntryMapper : ArbEntryMapper {
    override fun toArbEntries(
        selectedEntries: List<SelectedEntry>,
        translationsByKey: Map<String, Map<String, String>>,
    ): List<ArbEntry> {
        return selectedEntries.map { selected ->
            ArbEntry(
                key = selected.key,
                value = selected.editedValue,
                translations = translationsByKey[selected.key].orEmpty(),
            )
        }
    }
}
