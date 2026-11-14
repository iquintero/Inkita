package net.dom53.inkita.ui.reader.screen

import org.json.JSONObject

/**
 * Optional "Kindle style" pagination for EPUB content.
 *
 * Kavita serves an EPUB as a list of spine files ("pages"), each of which is usually a whole book chapter.
 * In paged mode the loaded document is laid out as CSS columns exactly one screen wide, so a Kavita page
 * becomes N screen-sized pages that are turned horizontally instead of scrolled vertically.
 */
internal const val PAGED_BRIDGE_NAME = "InkitaPaged"

private const val MIN_PAGED_SIDE_PX = 16f
private const val MIN_PAGED_VERTICAL_PX = 24f

internal fun pagedSidePx(paddingPx: Float): Float = maxOf(paddingPx, MIN_PAGED_SIDE_PX)

internal fun pagedVerticalPx(paddingPx: Float): Float = maxOf(paddingPx / 2f, MIN_PAGED_VERTICAL_PX)

/** CSS appended after the regular reader CSS when paged mode is on. */
internal fun pagedCss(paddingPx: Float): String {
    val side = pagedSidePx(paddingPx)
    val vertical = pagedVerticalPx(paddingPx)
    return """
        html {
            overflow: hidden !important;
            width: 100% !important;
            height: 100% !important;
        }
        body {
            box-sizing: border-box !important;
            margin: 0 !important;
            width: 100vw !important;
            height: 100vh !important;
            padding: ${vertical}px ${side}px !important;
            overflow: visible !important;
            column-width: calc(100vw - ${side * 2}px) !important;
            column-gap: ${side * 2}px !important;
            column-fill: auto !important;
        }
        .book-content {
            padding: 0 !important;
            margin: 0 !important;
        }
        img, svg, video {
            max-width: 100% !important;
            max-height: calc(100vh - ${vertical * 2}px) !important;
            object-fit: contain;
            break-inside: avoid;
        }
        h1, h2, h3, h4, h5, h6 { break-after: avoid; }
        """.trimIndent()
}

/**
 * Installs `window.__ik`. Idempotent; safe to run on every document load.
 *
 * Page numbers are 0-based. The bridge object [PAGED_BRIDGE_NAME] receives `onPage(current, total)`.
 */
internal val PAGED_RUNTIME_JS =
    """
    (function() {
        if (window.__ik) return;
        var ik = window.__ik = {};
        var anchor = null;
        var timer = null;

        function root() { return document.scrollingElement || document.documentElement; }
        function width() { return Math.max(1, document.documentElement.clientWidth); }

        ik.count = function() {
            return Math.max(1, Math.ceil((root().scrollWidth - 1) / width()));
        };
        ik.current = function() {
            return Math.max(0, Math.min(ik.count() - 1, Math.round(root().scrollLeft / width())));
        };

        function report() {
            if (window.$PAGED_BRIDGE_NAME) window.$PAGED_BRIDGE_NAME.onPage(ik.current(), ik.count());
        }

        function pageOfX(x) { return Math.floor((x + 1) / width()); }

        function candidates() {
            var all = document.body.querySelectorAll('*');
            var out = [];
            for (var i = 0; i < all.length; i++) {
                var el = all[i];
                var tag = el.tagName.toLowerCase();
                if (tag === 'script' || tag === 'style') continue;
                var own = tag === 'img';
                for (var c = el.firstChild; c && !own; c = c.nextSibling) {
                    if (c.nodeType === 3 && c.nodeValue.trim().length > 0) own = true;
                }
                if (own) out.push(i);
            }
            return { all: all, idx: out };
        }

        // First element whose beginning is laid out on the page that is currently showing.
        function firstElementOnPage() {
            var cur = ik.current();
            var sl = root().scrollLeft;
            var found = candidates();
            var fallback = null;
            for (var k = 0; k < found.idx.length; k++) {
                var el = found.all[found.idx[k]];
                var rects = el.getClientRects();
                if (!rects.length) continue;
                if (pageOfX(rects[0].left + sl) === cur) return { el: el, index: found.idx[k] };
                if (fallback === null) {
                    for (var r = 0; r < rects.length; r++) {
                        if (pageOfX(rects[r].left + sl) === cur) { fallback = { el: el, index: found.idx[k] }; break; }
                    }
                }
            }
            return fallback;
        }

        function remember() {
            var hit = firstElementOnPage();
            anchor = hit ? hit.el : anchor;
        }

        ik.go = function(page) {
            var target = Math.max(0, Math.min(ik.count() - 1, page));
            window.scrollTo(target * width(), 0);
            remember();
            report();
        };
        ik.next = function() {
            var cur = ik.current();
            if (cur >= ik.count() - 1) return false;
            ik.go(cur + 1);
            return true;
        };
        ik.prev = function() {
            var cur = ik.current();
            if (cur <= 0) return false;
            ik.go(cur - 1);
            return true;
        };
        ik.last = function() { ik.go(ik.count() - 1); };

        // Token stored as reading progress: an XPath id() expression, or idx:<n> into body *.
        ik.anchorToken = function() {
            var hit = firstElementOnPage();
            if (!hit) return null;
            if (hit.el.id) return 'id(' + JSON.stringify(hit.el.id) + ')';
            return 'idx:' + hit.index;
        };

        function resolve(token) {
            if (!token) return null;
            if (token.indexOf('idx:') === 0) {
                return document.body.querySelectorAll('*')[parseInt(token.substring(4), 10)] || null;
            }
            try {
                var res = document.evaluate(token, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                if (res.singleNodeValue) return res.singleNodeValue;
            } catch (e) {}
            var plain = token.replace(/^id\(["']/, '').replace(/["']\)${'$'}/, '');
            return document.getElementById(plain);
        }

        function pageOfElement(el) {
            var rects = el.getClientRects();
            if (!rects.length) return null;
            return pageOfX(rects[0].left + root().scrollLeft);
        }

        ik.goToToken = function(token) {
            var el = resolve(token);
            var page = el ? pageOfElement(el) : null;
            if (page === null) return false;
            anchor = el;
            ik.go(page);
            return true;
        };

        // Call after anything that can change the layout (style change, fonts, images).
        ik.relayout = function() {
            var el = anchor;
            var page = el ? pageOfElement(el) : null;
            window.scrollTo(Math.max(0, Math.min(ik.count() - 1, page === null ? ik.current() : page)) * width(), 0);
            report();
        };

        function schedule() {
            if (timer) clearTimeout(timer);
            timer = setTimeout(ik.relayout, 60);
        }

        ik.start = function() {
            var imgs = document.images;
            for (var i = 0; i < imgs.length; i++) {
                if (!imgs[i].complete) imgs[i].addEventListener('load', schedule);
            }
            if (document.fonts && document.fonts.ready) document.fonts.ready.then(schedule);
            window.addEventListener('resize', schedule);
            report();
        };
    })();
    """.trimIndent()

/** Script run once per loaded document: install the runtime, then place the reader. */
internal fun pagedStartJs(
    scrollToken: String?,
    atEnd: Boolean,
): String {
    val token = scrollToken?.let { JSONObject.quote(it) } ?: "null"
    return """
        $PAGED_RUNTIME_JS
        (function() {
            var ik = window.__ik;
            ik.start();
            if ($token !== null && ik.goToToken($token)) return;
            if ($atEnd) ik.last(); else ik.go(0);
        })();
        """.trimIndent()
}

internal fun pagedGoToTokenJs(token: String): String = "(window.__ik && window.__ik.goToToken(${JSONObject.quote(token)}));"
