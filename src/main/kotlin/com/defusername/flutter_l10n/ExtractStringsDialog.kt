package com.defusername.flutter_l10n

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel

class ExtractStringsDialog(
    project: Project,
    private val extracted: List<ExtractedString>,
    existingKeys: Set<String>,
) : DialogWrapper(project) {
    private val tableModel = object : DefaultTableModel(
        arrayOf("Include", "Raw String", "Generated Key", "Status"),
        0,
    ) {
        override fun getColumnClass(columnIndex: Int): Class<*> {
            return if (columnIndex == 0) Boolean::class.java else String::class.java
        }

        override fun isCellEditable(row: Int, column: Int): Boolean {
            return column == 0 || column == 2
        }
    }

    init {
        title = "Extract Strings to L10n"

        extracted.forEach { item ->
            val keyExists = existingKeys.contains(item.suggestedKey)
            tableModel.addRow(
                arrayOf(
                    !keyExists,
                    item.raw,
                    item.suggestedKey,
                    if (keyExists) "Key already exists" else "New",
                ),
            )
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        val table = JBTable(tableModel).apply {
            rowHeight = 26
            columnModel.getColumn(0).preferredWidth = 70
            columnModel.getColumn(1).preferredWidth = 320
            columnModel.getColumn(2).preferredWidth = 220
            columnModel.getColumn(3).preferredWidth = 120
        }

        val scroll = JBScrollPane(table).apply {
            preferredSize = Dimension(760, 380)
        }

        return JPanel(BorderLayout(0, 8)).apply {
            add(JLabel("Review extracted strings and adjust generated keys before applying."), BorderLayout.NORTH)
            add(scroll, BorderLayout.CENTER)
            add(JLabel("Only checked rows are extracted."), BorderLayout.SOUTH)
        }
    }

    fun getSelectedEntries(): List<Pair<ExtractedString, String>> {
        val selected = mutableListOf<Pair<ExtractedString, String>>()

        for (row in 0 until tableModel.rowCount) {
            val include = tableModel.getValueAt(row, 0) as? Boolean ?: false
            if (!include) continue

            val key = (tableModel.getValueAt(row, 2) as? String).orEmpty().trim()
            if (key.isBlank()) continue

            selected.add(extracted[row] to key)
        }

        return selected
    }
}
