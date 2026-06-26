package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.hidden
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.onClick
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.textArea

class IndexTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    sitePrivacyPolicyLink: String?,
    localization: Localization,
    shynet: Shynet,
    private val keepFilesTimeInHours: Int,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    privacyPolicyLink = sitePrivacyPolicyLink,
    localization = localization,
    shynet = shynet,
) {

    override fun FlowContent.content() {
        br()

        div(classes = "section") {
            id = "send_div"
            div(classes = "row") {
                div(classes = "col s12 center-align") {
                    h4 { +"\uD83D\uDD12 ${localization["index_headline"]} \uD83D\uDCE7" }
                }
                div(classes = "col s12 center-align") {
                    i { +localization["index_subtitle"] }
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    div(classes = "card-panel grey darken-3") {
                        textArea(cols = "70", rows = "8", classes = "white-text") {
                            style = "min-height:200px;padding:10px;"
                            name = "message"
                            id = "drop_content"
                            placeholder = localization["index_enter_message_placeholder"]
                        }
                    }
                }
                div(classes = "col s8 offset-s2 center-align") {
                    span(classes = "red-text") {
                        id = "error_message"
                        hidden = true
                        +localization["index_error_creating_drop"]
                    }
                }
            }

            div(classes = "row") {
                div(classes = "col s12 right-align") {
                    a(classes = "waves-effect waves-light btn hoverable right orange black-text") {
                        onClick = "sendDrop(document.getElementById('drop_content').value)"
                        i(classes = "material-icons right") {
                            +"send"
                        }
                        +localization["index_btn_make_the_drop"]
                    }
                }
            }
        }

        div(classes = "section") {
            id = "link_div"
            hidden = true

            div("row") {
                div(classes = "col s12") {
                    h3(classes = "green-text center") {
                        +"✅ ${localization["index_drop_made_success"]}"
                    }
                }
            }
            div("row") {
                div(classes = "col s12") {
                    +localization["index_drop_made_description"]
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    div(classes = "card-panel black-text grey lighten-3") {
                        id = "share-card"
                        span(classes = "card-title") {
                            +localization["drop_message_1_greeting"]
                        }
                        p {
                            id = "message_to_share_drop"

                            +localization["drop_message_2_kicker"]
                            br()
                            +localization["drop_message_3_link"]
                            b {
                                span {
                                    id = "drop_share_link"
                                }
                            }
                            br()
                            +localization["drop_message_4_password"]

                            b {
                                span {
                                    id = "drop_share_password"
                                }
                            }
                            br()
                            br()

                            b { +localization["drop_message_5_warning"] }
                            br()

                            +localization["drop_message_6_description_1"]
                            br()

                            +localization["drop_message_7_description_2"]
                            br()

                            +localization.get("drop_message_8_description_3_time", keepFilesTimeInHours)
                        }
                    }
                }
            }

            val message = buildString {
                append(localization["drop_message_1_greeting"])
                appendLine()
                append(localization["drop_message_2_kicker"])
                appendLine()
                append(localization["drop_message_3_link"])
                append("\" + document.getElementById('drop_share_link').innerHTML + \"")
                appendLine()
                append(localization["drop_message_4_password"])
                append("\" + document.getElementById('drop_share_password').innerHTML + \"")
                appendLine()
                appendLine()
                append(localization["drop_message_5_warning"])
                appendLine()
                append(localization["drop_message_6_description_1"])
                appendLine()
                append(localization["drop_message_7_description_2"])
                appendLine()
                append(localization.get("drop_message_8_description_3_time", keepFilesTimeInHours))
            }.replace("\n", "\\n")

            div(classes = "row") {
                div(classes = "col s12 m6 l6 left-align") {
                    a(classes = "waves-effect waves-light btn orange black-text") {
                        onClick = "window.location.reload();"
                        i(classes = "material-icons right") {
                            +"refresh"
                        }
                        +localization["index_btn_make_another_drop"]
                    }
                }

                div(classes = "col s12 m6 l6 right-align") {
                    a(classes = "waves-effect waves-light btn blue white-text") {
                        style = "margin-bottom: 12px"
                        onClick = "copyToClipboard(\"$message\");"
                        i(classes = "material-icons left") {
                            +"content_copy"
                        }
                        +localization["index_btn_copy_message"]
                    }
                }
            }
        }



        div(classes = "section") {
            div(classes = "row") {
                div(classes = "col s12") {
                    div(classes = "divider") {
                    }
                }
            }
            div(classes = "row") {
                div(classes = "col s12 center-align") {
                    h4 { +localization["index_how_it_works_section_title"] }
                }
            }

            div(classes = "row") {
                div(classes = "col s12 m6 l3") {
                    div(classes = "card-panel green darken-1") {
                        div(classes = "row") {
                            div(classes = "col s12 center-align") {
                                img {
                                    style = "height: 8em;aspect-ratio:1;"
                                    src = "/icon/encrypted-browser.svg"
                                }
                            }
                            div(classes = "col s12 center-align") {
                                style = "height:4em;"
                                +localization["index_how_it_works_step_1"]
                            }
                        }
                    }
                }

                div(classes = "col s12 m6 l3") {
                    div(classes = "card-panel green darken-1") {
                        div(classes = "row") {
                            div(classes = "col s12 center-align") {
                                img {
                                    style = "height: 8em;aspect-ratio:1;"
                                    src = "/icon/encrypted-upload.svg"
                                }
                            }
                            div(classes = "col s12 center-align") {
                                style = "height:4em;"
                                +localization["index_how_it_works_step_2"]
                            }
                        }
                    }
                }

                div(classes = "col s12 m6 l3") {
                    div(classes = "card-panel green darken-1") {
                        div(classes = "row") {
                            div(classes = "col s12 center-align") {
                                img {
                                    style = "height: 8em;aspect-ratio:1;"
                                    src = "/icon/pass-key.svg"
                                }
                            }
                            div(classes = "col s12 center-align") {
                                style = "height:4em;"
                                +localization["index_how_it_works_step_3"]
                            }
                        }
                    }
                }

                div(classes = "col s12 m6 l3") {
                    div(classes = "card-panel green darken-1") {
                        div(classes = "row") {
                            div(classes = "col s12 center-align") {
                                img {
                                    style = "height: 8em;aspect-ratio:1;"
                                    src = "/icon/decrypt.svg"
                                }
                            }
                            div(classes = "col s12 center-align") {
                                style = "height:4em;"
                                +localization["index_how_it_works_step_4"]
                            }
                        }
                    }
                }
            }
        }
    }

}