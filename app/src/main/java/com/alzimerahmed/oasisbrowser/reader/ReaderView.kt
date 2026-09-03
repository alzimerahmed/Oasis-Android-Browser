package com.alzimerahmed.oasisbrowser.reader

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.webkit.WebView
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors

/**
 * An overlay view that hosts the cleaned reader article and light formatting controls.
 */
class ReaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val webView: WebView
    private val closeButton: ImageButton
    private val ttsButton: MaterialButton
    private val themeLight: MaterialButton
    private val themeSepia: MaterialButton
    private val themeDark: MaterialButton
    private val fontSizeDecrease: MaterialButton
    private val fontSizeIncrease: MaterialButton

    var onCloseClick: (() -> Unit)? = null
    var onTtsClick: (() -> Unit)? = null

    private var currentBaseFontSize = DEFAULT_FONT_SIZE

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        setBackgroundColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, 0))

        val toolbar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            gravity = Gravity.CENTER_VERTICAL
        }

        closeButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setOnClickListener { onCloseClick?.invoke() }
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        val titleView = TextView(context).apply {
            text = context.getString(com.alzimerahmed.oasisbrowser.R.string.action_reader_mode)
            textSize = 18f
            setPadding(16.dp, 0, 0, 0)
            setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0))
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
        }

        ttsButton = MaterialButton(context).apply {
            text = context.getString(com.alzimerahmed.oasisbrowser.R.string.reader_tts)
            isCheckable = true
            setOnClickListener { onTtsClick?.invoke() }
        }

        toolbar.addView(closeButton)
        toolbar.addView(titleView)
        toolbar.addView(ttsButton)

        val controls = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
        }

        fontSizeDecrease = MaterialButton(context).apply {
            text = "A-"
            setOnClickListener { changeFontSize(-2) }
        }
        fontSizeIncrease = MaterialButton(context).apply {
            text = "A+"
            setOnClickListener { changeFontSize(2) }
        }
        themeLight = MaterialButton(context).apply {
            text = "Light"
            setOnClickListener { applyTheme(ReaderTheme.LIGHT) }
        }
        themeSepia = MaterialButton(context).apply {
            text = "Sepia"
            setOnClickListener { applyTheme(ReaderTheme.SEPIA) }
        }
        themeDark = MaterialButton(context).apply {
            text = "Dark"
            setOnClickListener { applyTheme(ReaderTheme.DARK) }
        }

        controls.addView(fontSizeDecrease)
        controls.addView(fontSizeIncrease)
        controls.addView(themeLight)
        controls.addView(themeSepia)
        controls.addView(themeDark)

        @SuppressLint("SetJavaScriptEnabled")
        webView = WebView(context).apply {
            settings.javaScriptEnabled = false
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
        }

        addView(toolbar)
        addView(controls)
        addView(webView)
    }

    fun loadHtml(html: String) {
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun applyTheme(theme: ReaderTheme) {
        webView.evaluateJavascript(
            """
                document.body.classList.remove('sepia', 'dark');
                if ('${theme.jsClass}' !== '') document.body.classList.add('${theme.jsClass}');
            """.trimIndent(),
            null
        )
    }

    private fun changeFontSize(delta: Int) {
        currentBaseFontSize = (currentBaseFontSize + delta).coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)
        webView.settings.textZoom = currentBaseFontSize
    }

    enum class ReaderTheme(val jsClass: String) {
        LIGHT(""),
        SEPIA("sepia"),
        DARK("dark"),
    }

    private companion object {
        private const val DEFAULT_FONT_SIZE = 100
        private const val MIN_FONT_SIZE = 75
        private const val MAX_FONT_SIZE = 200
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
