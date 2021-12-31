package dev.moetz.deaddrop.template

import io.ktor.html.*
import kotlinx.html.*

class SiteTemplate(
    private val pathPrefix: String?,
    private val showGithubLinkInFooter: Boolean,
    private val colorCode: String,
    private val showLinkToInfoPage: Boolean = true,
    private val siteTitle: String,
    private val siteTitleShort: String,
) : Template<HTML> {

    val content = Placeholder<FlowContent>()

    private val combinedPathPrefix: String = buildString {
        if (pathPrefix != null) {
            if (pathPrefix.startsWith('/').not()) {
                append('/')
            }
            append(pathPrefix)
            if (pathPrefix.endsWith('/').not()) {
                append("/")
            }
        } else {
            append("/")
        }
    }

    override fun HTML.apply() {
        head {
            charset("utf-8")
            title(siteTitle)
            link(href = "${combinedPathPrefix}static/materialize.min.css", rel = "stylesheet", type = "text/css")

            style {
                unsafe {
                    +":root {"
                    +"--color-text: #000000;"
                    +"--color-background: #ffffff;"
                    +"--color-link: #000000;"
                    +"}"
                    +"/* light mode */"
                    +"@media (prefers-color-scheme: light) {"
                    +":root {"
                    +"--color-background: #FFFFFF;"
                    +"--color-text: #000000;"
                    +"--color-link: #000000;"
                    +"}"
                    +"}"
                    +"/* dark mode */"
                    +"@media (prefers-color-scheme: dark) {"
                    +":root {"
                    +"--color-background: #000000;"
                    +"--color-text: #FFFFFF;"
                    +"--color-link: #$colorCode;"
                    +"}"
                    +"}"
                    +"a { color:#$colorCode; }"
                    +"h3 { color:#$colorCode; }"
                    +"h5 { color:#$colorCode; }"
                }
            }
            link(href = "${combinedPathPrefix}static/styles.css", rel = "stylesheet", type = "text/css")
            script(src = "${combinedPathPrefix}static/sjcl.js") {

            }
            script(src = "${combinedPathPrefix}static/drop.js") {

            }
            script(src = "${combinedPathPrefix}static/frontend.js") {

            }

            meta(name = "robots", content = "index, follow")
            meta(name = "og:title", content = siteTitle)
            meta(name = "description", content = "Create one-time links for securely sending data")
            meta(name = "keywords", content = "drop,password,encrypt,secure,send")
            meta(name = "theme-color", content = "#$colorCode")
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            link(href = "${combinedPathPrefix}apple-touch-icon.png", rel = "apple-touch-icon") { sizes = "180x180" }
            link(href = "${combinedPathPrefix}favicon-32x32.png", type = "image/png", rel = "icon") { sizes = "32x32" }
            link(href = "${combinedPathPrefix}favicon-16x16.png", type = "image/png", rel = "icon") { sizes = "16x16" }
            link(href = "${combinedPathPrefix}site.webmanifest", rel = "manifest")
        }
        body {
            header {
                nav {
                    style = "background-color: #$colorCode;"
                    div(classes = "nav-wrapper") {
                        span(classes = "brand-logo center") {
                            a(href = combinedPathPrefix) { +siteTitleShort }
                        }
                    }
                }
            }
            main {
                div(classes = "general-container") {
                    div(classes = "container") {
                        insert(content)
                    }
                }
            }

            footer(classes = "page-footer") {
                div(classes = "container") {
                    div(classes = "row") {
                        if (showGithubLinkInFooter) {
                            div(classes = "col s6") {
                                a(
                                    classes = "link-color",
                                    href = "https://github.com/FlowMo7/dead-drop"
                                ) { +"Open Source on GitHub" }
                            }
                            div(classes = "col s6") {
                                if (showLinkToInfoPage) {
                                    a(
                                        classes = "right link-color",
                                        href = "${combinedPathPrefix}info"
                                    ) { +"How is this safe?" }
                                }
                            }
                        } else {
                            div(classes = "col s12") {
                                if (showLinkToInfoPage) {
                                    a(
                                        classes = "right link-color",
                                        href = "${combinedPathPrefix}info"
                                    ) { +"How is this safe?" }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}