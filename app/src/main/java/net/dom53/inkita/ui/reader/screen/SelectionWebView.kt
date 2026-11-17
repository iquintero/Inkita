package net.dom53.inkita.ui.reader.screen

import android.content.Context
import android.graphics.Rect
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import net.dom53.inkita.R
import org.json.JSONArray
import org.json.JSONTokener

/** Returns the selected text and the document's language: `["selected text", "cs"]`. */
private const val SELECTION_JS =
    "(function(){var s=window.getSelection();var d=document.documentElement;var b=document.body;" +
        "var l=d.getAttribute('lang')||d.getAttribute('xml:lang')||(b&&(b.getAttribute('lang')||b.getAttribute('xml:lang')))||'';" +
        "return JSON.stringify([s?s.toString():'',l]);})();"

private const val MENU_DEFINE = 0x1D0001

/**
 * A [WebView] whose text-selection toolbar also offers "Define" (the system already offers Translate, Copy and Share).
 *
 * The selection itself is the system's (long-press, drag handles, copy, share), so it behaves like in
 * any other app. This only adds items to the toolbar and reports when a selection is active, so touch
 * handling can leave a tap that merely dismisses the selection alone.
 */
internal class SelectionWebView(
    context: Context,
) : WebView(context) {
    /** Called with the selected text and the document language (may be blank). */
    var onDefine: ((text: String, language: String?) -> Unit)? = null

    /** True while the selection toolbar is showing. */
    var isSelecting = false
        private set

    override fun startActionMode(
        callback: ActionMode.Callback?,
        type: Int,
    ): ActionMode? {
        if (callback == null || onDefine == null) return super.startActionMode(callback, type)
        return super.startActionMode(SelectionMenuCallback(callback), type)
    }

    /** Reads the current selection, then finishes [mode] so the selection is still there when it is read. */
    private fun withSelection(
        mode: ActionMode,
        block: (text: String, language: String?) -> Unit,
    ) {
        evaluateJavascript(SELECTION_JS) { raw ->
            val parsed =
                runCatching {
                    val json = JSONTokener(raw).nextValue() as? String
                    JSONArray(json)
                }.getOrNull()
            val text = parsed?.optString(0).orEmpty().trim()
            if (text.isNotEmpty()) block(text, parsed?.optString(1)?.takeIf { it.isNotBlank() })
            mode.finish()
        }
    }

    private inner class SelectionMenuCallback(
        private val base: ActionMode.Callback,
    ) : ActionMode.Callback2() {
        override fun onCreateActionMode(
            mode: ActionMode,
            menu: Menu,
        ): Boolean {
            val created = base.onCreateActionMode(mode, menu)
            if (created) {
                isSelecting = true
                addItems(menu)
            }
            return created
        }

        // The system rebuilds its items when the selection changes, so make sure ours are still there.
        override fun onPrepareActionMode(
            mode: ActionMode,
            menu: Menu,
        ): Boolean {
            val prepared = base.onPrepareActionMode(mode, menu)
            addItems(menu)
            return prepared
        }

        override fun onActionItemClicked(
            mode: ActionMode,
            item: MenuItem,
        ): Boolean =
            when (item.itemId) {
                MENU_DEFINE -> {
                    withSelection(mode) { text, language -> onDefine?.invoke(text, language) }
                    true
                }
                else -> base.onActionItemClicked(mode, item)
            }

        override fun onDestroyActionMode(mode: ActionMode) {
            isSelecting = false
            base.onDestroyActionMode(mode)
        }

        override fun onGetContentRect(
            mode: ActionMode,
            view: View,
            outRect: Rect,
        ) {
            val delegate = base as? ActionMode.Callback2
            if (delegate != null) delegate.onGetContentRect(mode, view, outRect) else super.onGetContentRect(mode, view, outRect)
        }

        private fun addItems(menu: Menu) {
            if (onDefine != null && menu.findItem(MENU_DEFINE) == null) {
                menu.add(Menu.NONE, MENU_DEFINE, 0, context.getString(R.string.reader_selection_define))
            }
        }
    }
}
