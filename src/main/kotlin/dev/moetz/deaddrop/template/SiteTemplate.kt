package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import io.ktor.server.html.*
import kotlinx.html.*

abstract class SiteTemplate(
    protected val pathPrefix: String?,
    protected val showGithubLinkInFooter: Boolean,
    protected val privacyPolicyLink: String?,
    protected val localization: Localization,
    protected val shynet: Shynet,
    protected val showLanguageSelectionInFooter: Boolean,
    protected val subSitePath: String,
    protected val hl: String?,
) : Template<HTML> {

    protected val combinedPathPrefix: String = buildString {
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

    abstract fun FlowContent.content()

    override fun HTML.apply() {
        lang = localization.currentLanguage.identifier
        head {
            charset("utf-8")
            title(localization["html_site_title"])
            link(href = "${combinedPathPrefix}static/materialize.min.css", rel = "stylesheet", type = "text/css")

            style {
                unsafe {
                    +":root {"
                    +"--color-text: #000000;"
                    +"--color-background: #ffffff;"
                    +"--color-link: #000000;"
                    +"}"
                    +"body {background-color: #202123;color: #fff;nav {background-color: #26A69A;}.card {background-color: rgba(255,255,255,0.2);}.btn {background-color: #EE6F73;}.divider {opacity: 0.2;}.sidenav {background-color: #2D2D31;li {a {&:not(.subheader){color: #89B2F5;&:hover {background-color: #3B4043;}}&.subheader {color:#9AA0A6;}.material-icons {color: #9AA0A6;}}}}.collection {border: 1px solid rgba(255,255,255,0.2);.collection-item {background-color: rgba(255,255,255,0.2);border-bottom: 1px solid rgba(255,255,255,0.2);}}}"
                }
            }
            link(href = "${combinedPathPrefix}static/styles.css", rel = "stylesheet", type = "text/css")
            link(href = "${combinedPathPrefix}static/MaterialIcons.css", rel = "stylesheet", type = "text/css")
            script(src = "${combinedPathPrefix}static/materialize.min.js") {

            }
            script(src = "${combinedPathPrefix}static/sjcl.js") {

            }
            script(src = "${combinedPathPrefix}static/drop.js") {

            }
            script(src = "${combinedPathPrefix}static/frontend.js${if (hl != null) "?hl=$hl" else ""}") {

            }

            meta(name = "robots", content = "index, follow")
            meta(name = "og:title", content = localization["html_site_title"])
            meta(name = "description", content = localization["html_meta_description"])
            meta(name = "keywords", content = localization["html_meta_keywords"])
            meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
            link(href = "${combinedPathPrefix}icon/apple-touch-icon.png", rel = "apple-touch-icon") {
                sizes = "180x180"
            }
            link(href = "${combinedPathPrefix}icon/favicon-32x32.png", type = "image/png", rel = "icon") {
                sizes = "32x32"
            }
            link(href = "${combinedPathPrefix}icon/favicon-16x16.png", type = "image/png", rel = "icon") {
                sizes = "16x16"
            }
            link(href = "${combinedPathPrefix}site.webmanifest", rel = "manifest")
        }
        body {
            header {
                nav(classes = "orange darken-1 nav-extended") {
                    div(classes = "container") {
                        div(classes = "nav-wrapper") {
                            a(classes = "brand-logo center") {
                                id = "logo-container"
                                span {
                                    style = "text-wrap: nowrap;"
                                    +localization["html_site_title_short"]
                                }
                            }
                        }
                    }
                }
            }
            main {
                div(classes = "general-container") {
                    div(classes = "container") {
                        content()
                    }
                }
            }

            div(classes = "modal") {
                id = "select-language"
                div(classes = "modal-content") {
                    div(classes = "row") {
                        div(classes = "col s12 black-text") {
                            h3 { +"Language" }
                        }
                    }
                    div(classes = "row") {
                        div(classes = "col s12") {
                            div(classes = "collection") {
                                Localization.Language.entries
                                    .sortedBy { it.languageNameInEnglish }
                                    .forEach { language ->
                                        val classes = if (language == localization.currentLanguage) {
                                            "collection-item active"
                                        } else {
                                            "collection-item"
                                        }
                                        a(classes = classes) {
                                            href =
                                                "${combinedPathPrefix}${subSitePath}?hl=${language.identifier}"
                                            img {
                                                style = "height:0.8em;margin-right:12px;"
                                                src = "${combinedPathPrefix}flags/${language.flagIdentifier}.svg"
                                            }
                                            span {
                                                +language.languageNameInEnglish
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            footer(classes = "page-footer orange") {
                div(classes = "footer-copyright") {
                    div(classes = "container") {
                        div(classes = "row") {
                            val footerLinks = buildList<Pair<String, String>> {

                                if (showGithubLinkInFooter) {
                                    add(
                                        Pair(
                                            localization["footer_open_source_on_github"],
                                            "https://github.com/FlowMo7/dead-drop"
                                        )
                                    )
                                }

                                if (privacyPolicyLink != null) {
                                    add(
                                        Pair(
                                            localization["footer_privacy_policy"],
                                            privacyPolicyLink
                                        )
                                    )
                                }

                                if (showLanguageSelectionInFooter) {
                                    add(
                                        Pair(
                                            "LANGUAGE",
                                            "LANGUAGE"
                                        )
                                    )
                                }
                            }

                            val size = when (footerLinks.size) {
                                0, 1 -> "s12"
                                2 -> "s6"
                                3 -> "s4"
                                4 -> "s3"
                                else -> "s2"
                            }
                            footerLinks.forEachIndexed { index, (label, link) ->
                                val align = when (index) {
                                    0 -> "left-align"
                                    footerLinks.lastIndex -> "right-align"
                                    else -> "center-align"
                                }

                                div(classes = "col $size $align") {
                                    if (label == "LANGUAGE" && link == "LANGUAGE") {
                                        a(classes = "orange-text text-lighten-5") {
                                            style = "cursor:pointer"

                                            val language = localization.currentLanguage

                                            onClick =
                                                "M.Modal.getInstance(document.getElementById('select-language')).open();"

                                            img {
                                                style = "height:0.8em;margin-right:8px;"
                                                src =
                                                    "${combinedPathPrefix}flags/${language.flagIdentifier}.svg"
                                                alt = language.languageNameInEnglish
                                            }
                                            span {
                                                +localization["select_language"]
                                            }
                                        }
                                    } else {
                                        a(
                                            classes = "black-text",
                                            href = link,
                                        ) {
                                            target = "_blank"
                                            +label
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            script {
                unsafe {
                    +"document.addEventListener('DOMContentLoaded', function() {"
                    +"var elems = document.querySelectorAll('.modal');"
                    +"var instances = M.Modal.init(elems);"
                    +"});"
                }
            }

            shynet.attachToBody(this)
        }

    }
}