package com.defusername.flutter_l10n.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.defusername.flutter_l10n.SelectedEntry
import com.defusername.flutter_l10n.translation.TranslationService
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.table.DefaultTableModel

class MultiLocaleDialog(
    project: Project,
    selectedEntries: List<SelectedEntry>,
    availableLocales: List<String>,
    val templateLocale: String,
    private val translationService: TranslationService,
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

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 4))
        buildTranslateButtons(buttonPanel)

        return JPanel(BorderLayout(0, 8)).apply {
            add(JLabel("Fill translations now or leave blank to insert TODO placeholders."), BorderLayout.NORTH)
            add(scroll, BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
    }

    private fun buildTranslateButtons(panel: JPanel) {
        for ((idx, locale) in targetLocales.withIndex()) {
            val name = java.util.Locale(locale).displayLanguage
            val col = idx + 2
            val btn = JButton("Auto-translate $name (Ollama)")
            btn.addActionListener {
                btn.isEnabled = false
                btn.text = "Translating..."
                Thread {
                    batchTranslateColumn(col, locale)
                    SwingUtilities.invokeLater {
                        btn.text = "Auto-translate $name (Ollama)"
                        btn.isEnabled = true
                    }
                }.start()
            }
            panel.add(btn)
        }
    }

    private data class RowEntry(val index: Int, val source: String)

    private fun batchTranslateColumn(column: Int, targetLocale: String) {
        val sourceColumn = 1
        val rows = mutableListOf<RowEntry>()
        for (row in 0 until tableModel.rowCount) {
            val source = (tableModel.getValueAt(row, sourceColumn) as? String).orEmpty()
            val existing = (tableModel.getValueAt(row, column) as? String).orEmpty()
            if (source.isNotBlank() && existing.isBlank()) {
                rows.add(RowEntry(row, source))
            }
        }
        if (rows.isEmpty()) return

        val texts = rows.map { it.source }
        val results = translationService.translateBatch(texts, targetLocale)

        SwingUtilities.invokeLater {
            for ((i, entry) in rows.withIndex()) {
                val translation = results.getOrNull(i)
                if (!translation.isNullOrBlank()) {
                    tableModel.setValueAt(translation, entry.index, column)
                }
            }
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
