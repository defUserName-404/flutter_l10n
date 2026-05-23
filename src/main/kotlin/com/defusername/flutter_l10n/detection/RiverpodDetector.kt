package com.defusername.flutter_l10n.detection

import com.intellij.psi.PsiElement

object RiverpodDetector {
    enum class RefScope {
        WidgetRefParam,
        NotifierField,
        ProviderRefParam,
        None,
    }

    fun detect(element: PsiElement): RefScope {
        var current: PsiElement? = element
        var depth = 0

        while (current != null && depth < 24) {
            val text = current.text ?: ""
            val className = current.javaClass.simpleName

            if (looksLikeNotifierScope(className, text)) {
                return RefScope.NotifierField
            }

            if (looksLikeWidgetRefParamScope(className, text)) {
                return RefScope.WidgetRefParam
            }

            if (looksLikeProviderRefParamScope(className, text)) {
                return RefScope.ProviderRefParam
            }

            current = current.parent
            depth++
        }

        return RefScope.None
    }

    fun looksLikeNotifierScope(className: String, text: String): Boolean {
        if (!className.contains("Class", ignoreCase = true) &&
            !className.contains("Method", ignoreCase = true)
        ) {
            return false
        }

        return text.contains("extends Notifier") ||
            text.contains("extends AsyncNotifier") ||
            text.contains("extends AutoDisposeNotifier") ||
            text.contains("extends AutoDisposeAsyncNotifier") ||
            text.contains(" extends _$")
    }

    fun looksLikeWidgetRefParamScope(className: String, text: String): Boolean {
        if (!className.contains("Method", ignoreCase = true) &&
            !className.contains("Function", ignoreCase = true)
        ) {
            return false
        }

        if (text.contains("WidgetRef ref")) return true
        if (text.contains("(context, ref") || text.contains("(ctx, ref")) return true
        if (Regex("""\([^)]*\bWidgetRef\b[^)]*\)""").containsMatchIn(text)) return true
        if (Regex("""\([^)]*\bref\b[^)]*\)""").containsMatchIn(text) && text.contains("Consumer")) return true

        return false
    }

    fun looksLikeProviderRefParamScope(className: String, text: String): Boolean {
        if (!className.contains("Function", ignoreCase = true) &&
            !className.contains("Method", ignoreCase = true)
        ) {
            return false
        }

        if (Regex("""\([^)]*\bRef\s+ref\b[^)]*\)""").containsMatchIn(text)) return true
        if (Regex("""\([^)]*\bWidgetRef\s+ref\b[^)]*\)""").containsMatchIn(text)) return true

        return false
    }
}
