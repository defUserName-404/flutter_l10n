package com.defusername.flutter_l10n

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RiverpodDetectorTest {
    @Test
    fun `looksLikeNotifierScope matches Notifier subclass`() {
        assertTrue(
            RiverpodDetector.looksLikeNotifierScope(
                "Class",
                "class MyNotifier extends Notifier<int> {}",
            ),
        )
    }

    @Test
    fun `looksLikeNotifierScope matches AsyncNotifier subclass`() {
        assertTrue(
            RiverpodDetector.looksLikeNotifierScope(
                "Class",
                "class MyNotifier extends AsyncNotifier<String> {}",
            ),
        )
    }

    @Test
    fun `looksLikeNotifierScope matches AutoDisposeNotifier subclass`() {
        assertTrue(
            RiverpodDetector.looksLikeNotifierScope(
                "Class",
                "class MyNotifier extends AutoDisposeNotifier<bool> {}",
            ),
        )
    }

    @Test
    fun `looksLikeNotifierScope matches underscore prefix pattern`() {
        assertTrue(
            RiverpodDetector.looksLikeNotifierScope(
                "Class",
                "class MyNotifier extends _\$MyNotifier {}",
            ),
        )
    }

    @Test
    fun `looksLikeNotifierScope returns false for non-class elements`() {
        assertFalse(
            RiverpodDetector.looksLikeNotifierScope(
                "Variable",
                "final x = 1;",
            ),
        )
    }

    @Test
    fun `looksLikeWidgetRefParamScope matches WidgetRef ref parameter`() {
        assertTrue(
            RiverpodDetector.looksLikeWidgetRefParamScope(
                "Method",
                "Widget build(BuildContext context, WidgetRef ref) {}",
            ),
        )
    }

    @Test
    fun `looksLikeWidgetRefParamScope matches Consumer builder pattern`() {
        assertTrue(
            RiverpodDetector.looksLikeWidgetRefParamScope(
                "Function",
                "Consumer(builder: (context, ref) => ...)",
            ),
        )
    }

    @Test
    fun `looksLikeWidgetRefParamScope returns false for non-method elements`() {
        assertFalse(
            RiverpodDetector.looksLikeWidgetRefParamScope(
                "Class",
                "class MyWidget extends ConsumerWidget {}",
            ),
        )
    }

    @Test
    fun `looksLikeProviderRefParamScope matches Ref ref parameter`() {
        assertTrue(
            RiverpodDetector.looksLikeProviderRefParamScope(
                "Function",
                "(Ref ref) { return ...; }",
            ),
        )
    }

    @Test
    fun `looksLikeProviderRefParamScope matches WidgetRef ref parameter`() {
        assertTrue(
            RiverpodDetector.looksLikeProviderRefParamScope(
                "Method",
                "myProvider(WidgetRef ref) {}",
            ),
        )
    }

    @Test
    fun `looksLikeProviderRefParamScope returns false for non-function elements`() {
        assertFalse(
            RiverpodDetector.looksLikeProviderRefParamScope(
                "Variable",
                "final ref = something;",
            ),
        )
    }
}
