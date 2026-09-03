package com.alzimerahmed.oasisbrowser.reader

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A lightweight article extractor that turns a raw page HTML snapshot into a clean reader view.
 */
@Singleton
class ReaderModel @Inject constructor() {

    /**
     * Extracts a readable article from [html] for the given [url] and [title].
     *
     * @return A reader view HTML string or null if no article content could be found.
     */
    fun extractArticle(html: String, url: String, title: String): String? {
        if (html.isBlank()) return null

        val document = Jsoup.parse(html, url)
        val body = document.body()
            ?: return null

        val candidate = selectArticleElement(body)
            ?: return null

        if (candidate.text().length < MIN_ARTICLE_LENGTH) return null

        return wrapReaderHtml(document, title, url, candidate)
    }

    /**
     * Returns plain text for TTS from the cleaned reader HTML.
     */
    fun extractText(readerHtml: String): String {
        val doc = Jsoup.parse(readerHtml)
        return doc.body()?.text()?.trim() ?: ""
    }

    private fun selectArticleElement(body: Element): Element? {
        // Prefer semantic article/main tags when they contain enough text.
        body.selectFirst("article")?.let { if (it.text().length >= MIN_ARTICLE_LENGTH) return it }
        body.selectFirst("main")?.let { if (it.text().length >= MIN_ARTICLE_LENGTH) return it }

        // Fall back to the block with the highest paragraph/text ratio.
        val candidates = body.select("div, section")
            .filter { it.text().length >= MIN_ARTICLE_LENGTH }
            .sortedByDescending { it.select("p").size * it.text().length }

        return candidates.firstOrNull()
    }

    private fun wrapReaderHtml(document: Document, title: String, url: String, content: Element): String {
        // Strip elements that shouldn't appear in a reader view.
        content.select("script, style, nav, header, footer, aside, form, iframe, svg, button, input").remove()

        val safeTitle = Entities.escape(title)
        val safeUrl = Entities.escape(url)
        val cleanedContent = content.html()

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>$safeTitle</title>
                <style>
                    :root {
                        --bg: #ffffff;
                        --fg: #1c1b1f;
                        --muted: #49454f;
                        --link: #6750a4;
                        --font: 'Georgia', 'Times New Roman', serif;
                        --size: 18px;
                        --line: 1.7;
                    }
                    @media (prefers-color-scheme: dark) {
                        :root {
                            --bg: #1c1b1f;
                            --fg: #e6e1e5;
                            --muted: #cab6ff;
                            --link: #d0bcff;
                        }
                    }
                    .sepia { --bg: #f4ecd8; --fg: #433422; --muted: #5e4b35; --link: #8b5e3c; }
                    .dark  { --bg: #1c1b1f; --fg: #e6e1e5; --muted: #cab6ff; --link: #d0bcff; }
                    * { box-sizing: border-box; }
                    body {
                        background: var(--bg);
                        color: var(--fg);
                        font-family: var(--font);
                        font-size: var(--size);
                        line-height: var(--line);
                        margin: 0 auto;
                        max-width: 720px;
                        padding: 24px 16px 64px;
                        transition: background 0.2s, color 0.2s;
                    }
                    h1 { font-size: 1.6em; margin-bottom: 0.25em; }
                    .meta { color: var(--muted); font-size: 0.85em; margin-bottom: 1.5em; }
                    a { color: var(--link); }
                    p, figure, blockquote, li { margin: 0 0 1em; }
                    img { max-width: 100%; height: auto; display: block; margin: 1em auto; }
                    figcaption { color: var(--muted); font-size: 0.85em; text-align: center; }
                    blockquote { border-left: 4px solid var(--muted); padding-left: 16px; color: var(--muted); }
                </style>
            </head>
            <body>
                <h1>$safeTitle</h1>
                <div class="meta">$safeUrl</div>
                <article>$cleanedContent</article>
            </body>
            </html>
        """.trimIndent()
    }

    private companion object {
        private const val MIN_ARTICLE_LENGTH = 200
    }
}
