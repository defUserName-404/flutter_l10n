package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

class DocumentSourceRewriter : SourceRewriter {
    override fun replaceRawStrings(project: Project, editor: Editor, psiFile: PsiFile, replacements: List<ResolvedReplacement>) {
        WriteCommandAction.runWriteCommandAction(project, "Extract Strings to L10n", null, {
            val document = editor.document
            var updatedText = document.text

            for (item in replacements.sortedByDescending { it.extracted.startOffset }) {
                updatedText = updatedText.substring(0, item.extracted.startOffset) +
                    item.replacement.expression +
                    updatedText.substring(item.extracted.endOffset)
            }

            document.setText(updatedText)
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }, psiFile)

        PsiDocumentManager.getInstance(project).commitAllDocuments()
    }
}
