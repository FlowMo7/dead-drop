package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.Localization
import dev.moetz.deaddrop.Shynet
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.b
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.hidden
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.onClick
import kotlinx.html.pre
import kotlinx.html.textInput
import kotlinx.html.unsafe

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
            id = "container_get_drop"

            div("row") {
                div("col s12") {
                    +localization["pickup_kicker"]
                }
            }
            div("row") {
                div("col s12") {
                    val text = localization["pickup_description_before_button_text"]
                    val parts = text.split(localization["pickup_description_once_word_to_make_bold"])
                    +parts[0]
                    b { +localization["pickup_description_once_word_to_make_bold"] }
                    +parts[1]
                    i { +localization["pickup_btn_get_the_drop"] }
                    +localization["pickup_description_after_button_text"]
                }
            }

            div("row") {
                div("input-field col s12") {
                    textInput(classes = "validate white-text") {
                        id = "drop_password"
                        placeholder = localization["pickup_password_placeholder"]
                    }
                }
                div(classes = "col s12") {
                    a(classes = "waves-effect waves-light btn orange") {
                        onClick = "getDrop(" +
                                "'$dropId', " +
                                "document.getElementById('drop_password').value" +
                                ")"
                        +localization["pickup_btn_get_the_drop"]
                    }
                }
            }
        }

        div(classes = "section") {
            id = "drop_content_section"
            hidden = true

            h3 {
                +localization["pickup_you_drop_title"]
            }

            div(classes = "divider") {

            }

            div(classes = "row") {
                div(classes = "col s12") {
                    pre {
                        id = "drop_content"
                    }
                }
            }
        }


        div(classes = "section") {
            id = "error_text"
            hidden = true

            div(classes = "row") {
                div(classes = "col s12") {
                    h3(classes = "red-text") {
                        +localization["pickup_error_title"]
                    }
                }
            }
            div(classes = "row") {
                div(classes = "col s12") {
                    +localization["pickup_error_description_kicker"]
                    br()
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["pickup_error_description_bullet_point_1"]

                    br()
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["pickup_error_description_bullet_point_2"]
                }
                div(classes = "col s12") {
                    br()
                    br()
                    b { +localization["pickup_error_description_bottom_line"] }
                }
            }
        }
    }

}