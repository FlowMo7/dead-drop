package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import kotlinx.html.*

class PickupTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    sitePrivacyPolicyLink: String?,
    localization: Localization,
    shynet: Shynet,
    showLanguageSelectionInFooter: Boolean,
    private val dropId: String?,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    privacyPolicyLink = sitePrivacyPolicyLink,
    localization = localization,
    showLanguageSelectionInFooter = showLanguageSelectionInFooter,
    subSitePath = "pickup/$dropId",
    shynet = shynet,
) {

    override fun FlowContent.content() {
        div(classes = "section") {
            div(classes = "row") {
                div(classes = "col s12 center-align") {
                    h4 { +"\uD83D\uDD12 ${localization["pickup_title"]} \uD83D\uDD12" }
                }
                div(classes = "col s12") {
                    +localization["pickup_kicker_1"]
                    br()
                    br()
                    +"⚠ ${localization["pickup_kicker_2"]}"
                }
            }
        }
        div(classes = "section") {
            id = "container_get_drop"

            div("row valign-wrapper") {
                div("input-field col s12 m8 l8") {
                    textInput(classes = "white-text") {
                        id = "drop_password"
                        placeholder = localization["pickup_password_placeholder"]
                        pattern = ".{1,}"
                        onKeyDown = "onPickupKeyDown('$dropId', event)"
                    }
                }
                div(classes = "col s12 m4 l4 right-align") {
                    a(classes = "waves-effect waves-light btn orange") {
                        onClick = "getDrop('$dropId', document.getElementById('drop_password').value)"
                        +localization["pickup_btn_get_the_drop"]
                    }
                }
            }
        }

        div(classes = "section") {
            id = "drop_content_section"
            hidden = true

            div("row") {
                div("col s12") {
                    h3 { +localization["pickup_your_drop_title"] }
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    div(classes = "card-panel grey darken-3") {
                        pre {
                            id = "drop_content"
                        }
                    }
                }
                div(classes = "col s12 right-align") {
                    a(classes = "waves-effect waves-light btn blue white-text") {
                        style = "margin-bottom: 12px"
                        onClick = "copyToClipboard(document.getElementById('drop_content').innerHTML);"
                        i(classes = "material-icons left") {
                            +"content_copy"
                        }
                        +localization["index_btn_copy_message"]
                    }
                }
            }
        }


        div(classes = "section") {
            id = "error_text"
            hidden = true

            div(classes = "row") {
                div(classes = "col s12 center-align") {
                    h3(classes = "red-text") {
                        i(classes = "small material-icons red-text") {
                            +"error"
                        }
                        unsafe { +"&nbsp;" }
                        +localization["pickup_error_title"]
                        unsafe { +"&nbsp;" }
                        i(classes = "small material-icons red-text") {
                            +"error"
                        }
                    }
                }
                div(classes = "col s12 center-align") {
                    h5 { +localization["pickup_error_subtitle"] }
                }
            }
            div(classes = "row") {
                div(classes = "col s12 m6 l6") {
                    h5 { +localization["pickup_error_possible_reasons"] }
                    br()
                    +"• ${localization["pickup_error_possible_reasons_bullet_point_1"]}"
                    br()
                    +"• ${localization["pickup_error_possible_reasons_bullet_point_2"]}"
                }

                div(classes = "col s12 m6 l6") {
                    h5 { +localization["pickup_error_security_warning"] }
                    br()
                    +localization["pickup_error_security_warning_text"]
                }
            }
        }

        div(classes = "row") {
            div(classes = "col s12 left-align") {
                a(classes = "orange-text") {
                    href = combinedPathPrefix
                    +localization["pickup_hint_encrypt_yourself"]
                }
            }
        }
    }

}
