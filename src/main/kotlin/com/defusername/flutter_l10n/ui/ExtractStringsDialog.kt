package com.defusername.flutter_l10n.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.defusername.flutter_l10n.ExtractedString
import com.defusername.flutter_l10n.SelectedEntry
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.AbstractCellEditor
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class ExtractStringsDialog(
    project: Project,
    private val extracted: List<ExtractedString>,
) : DialogWrapper(project) {
    private lateinit var table: JBTable

    private val tableModel = object : DefaultTableModel(
        arrayOf("Select", "Value", "Key"),
        0,
    ) {
        override fun getColumnClass(columnIndex: Int): Class<*> {
            return if (columnIndex == 0) Boolean::class.java else String::class.java
        }

        override fun isCellEditable(row: Int, column: Int): Boolean = true
    }

    init {
        title = "Extract Strings to L10n"

        extracted.forEach { item ->
            tableModel.addRow(arrayOf<Any>(true, item.raw, item.suggestedKey))
        }

        init()
    }

    override fun createCenterPanel(): JComponent {
        table = JBTable(tableModel).apply {
            rowHeight = 26
            setCellSelectionEnabled(true)
            putClientProperty("JTable.autoStartsEdit", true)
            columnModel.getColumn(0).apply {
                preferredWidth = 60
                cellRenderer = object : TableCellRenderer {
                    private val cb = JCheckBox().apply {
                        isOpaque = true
                        horizontalAlignment = SwingConstants.CENTER
                    }
                    override fun getTableCellRendererComponent(
                        table: JTable, value: Any?, selected: Boolean, focus: Boolean, row: Int, col: Int,
                    ): Component {
                        cb.isSelected = value as? Boolean ?: false
                        cb.background = if (selected) table.selectionBackground else table.background
                        return cb
                    }
                }
                cellEditor = object : AbstractCellEditor(), TableCellEditor {
                    private val cb = JCheckBox().apply {
                        isOpaque = true
                        horizontalAlignment = SwingConstants.CENTER
                        addActionListener { fireEditingStopped() }
                    }
                    override fun getCellEditorValue(): Any = cb.isSelected
                    override fun getTableCellEditorComponent(
                        table: JTable, value: Any?, isSelected: Boolean, row: Int, column: Int,
                    ): Component {
                        cb.isSelected = value as? Boolean ?: false
                        cb.background = if (isSelected) table.selectionBackground else table.background
                        return cb
                    }
                }
            }
            columnModel.getColumn(1).preferredWidth = 350
            columnModel.getColumn(2).preferredWidth = 220
        }

        val scroll = JBScrollPane(table).apply {
            preferredSize = Dimension(760, 380)
        }

        return JPanel(BorderLayout(0, 8)).apply {
            add(JLabel("Check strings to extract. Edit values and keys as needed."), BorderLayout.NORTH)
            add(scroll, BorderLayout.CENTER)
            add(JLabel("Only checked rows are extracted."), BorderLayout.SOUTH)
        }
    }

    override fun doOKAction() {
        commitActiveCellEdit()
        super.doOKAction()
    }

    fun getSelectedEntries(): List<SelectedEntry> {
        commitActiveCellEdit()
        val selected = mutableListOf<SelectedEntry>()

        for (row in 0 until tableModel.rowCount) {
            val include = tableModel.getValueAt(row, 0) as? Boolean ?: false
            if (!include) continue

            val value = (tableModel.getValueAt(row, 1) as? String).orEmpty().trim()
            val key = (tableModel.getValueAt(row, 2) as? String).orEmpty().trim()
            if (value.isBlank() || key.isBlank()) continue

            selected.add(SelectedEntry(extracted[row], value, key))
        }

        return selected
    }

    private fun commitActiveCellEdit() {
        if (::table.isInitialized && table.isEditing) {
            table.cellEditor?.stopCellEditing()
        }
    }
}
