package com.defusername.flutter_l10n.detection

import com.intellij.psi.PsiElement

object ContextDetector {
    fun hasBuildContextInScope(element: PsiElement): Boolean {
        var current: PsiElement? = element
        var depth = 0

        while (current != null && depth < 24) {
            val text = current.text ?: ""
            val className = current.javaClass.simpleName

            if (className.contains("Method", ignoreCase = true) ||
                className.contains("Function", ignoreCase = true)
            ) {
                if (text.contains("BuildContext context")) return true
                if (Regex("""\([^)]*\bcontext\b[^)]*\)""").containsMatchIn(text) && text.contains("builder")) {
                    return true
                }
            }

            current = current.parent
            depth++
        }

        return false
    }
}
