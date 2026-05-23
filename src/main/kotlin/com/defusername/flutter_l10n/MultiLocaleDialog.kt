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

class MultiLocaleDialog(
    project: Project,
    selectedEntries: List<SelectedEntry>,
    availableLocales: List<String>,
    templateLocale: String,
) : DialogWrapper(project) {
    private val targetLocales = availableLocales.filter { it != templateLocale }

    private val columns: Array<String> = buildList {
        add("Key")
        add("${templateLocale.uppercase()} Source")
        addAll(targetLocales)
    }.toTypedArray()

    private val tableModel = object : DefaultTableModel(columns, 0) {
        override fun isCellEditable(row: Int, column: Int): Boolean {
            return column >= 2
        }

        override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java
    }

    init {
        title = "Translations for Other Locales"

        selectedEntries.forEach { entry ->
            val row = MutableList<Any>(columns.size) { "" }
            row[0] = entry.key
            row[1] = entry.editedValue
            tableModel.addRow(row.toTypedArray())
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        if (targetLocales.isEmpty()) {
            return JPanel(BorderLayout()).apply {
                add(JLabel("No extra locale ARB files found. Only template ARB will be updated."), BorderLayout.CENTER)
            }
        }

        val table = JBTable(tableModel).apply {
            rowHeight = 28
            columnModel.getColumn(0).preferredWidth = 180
            columnModel.getColumn(1).preferredWidth = 260
            for (i in 2 until columnCount) {
                columnModel.getColumn(i).preferredWidth = 180
            }
        }

        val scroll = JBScrollPane(table).apply {
            preferredSize = Dimension(840, 360)
        }

        return JPanel(BorderLayout(0, 8)).apply {
            add(JLabel("Fill translations now or leave blank to insert TODO placeholders."), BorderLayout.NORTH)
            add(scroll, BorderLayout.CENTER)
            add(JLabel("You can edit ARB files manually anytime."), BorderLayout.SOUTH)
        }
    }

    fun getTranslationsByKey(): Map<String, Map<String, String>> {
        val result = mutableMapOf<String, MutableMap<String, String>>()

        for (row in 0 until tableModel.rowCount) {
            val key = (tableModel.getValueAt(row, 0) as? String).orEmpty()
            if (key.isBlank()) continue

            val localeMap = mutableMapOf<String, String>()
            targetLocales.forEachIndexed { idx, locale ->
                val value = (tableModel.getValueAt(row, idx + 2) as? String).orEmpty().trim()
                if (value.isNotBlank()) {
                    localeMap[locale] = value
                }
            }
            result[key] = localeMap
        }

        return result
    }
}
