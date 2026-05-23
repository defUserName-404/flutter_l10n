package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.project.Project
import com.defusername.flutter_l10n.ExtractedString
import com.defusername.flutter_l10n.SelectedEntry
import com.defusername.flutter_l10n.ui.ExtractStringsDialog

class DialogSelectionReview : SelectionReview {
    override fun selectEntries(project: Project, extracted: List<ExtractedString>): List<SelectedEntry>? {
        val dialog = ExtractStringsDialog(project, extracted)
        return if (dialog.showAndGet()) dialog.getSelectedEntries() else null
    }
}
