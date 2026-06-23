package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.Localization
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.i
import kotlinx.html.unsafe

class InfoTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    colorCode: String,
    showLinkToInfoPage: Boolean = true,
    siteTitle: String,
    siteTitleShort: String,
    sitePrivacyPolicyLink: String?,
    localization: Localization,
    hl: String?,
    private val keepFilesTimeInHours: Int,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    colorCode = colorCode,
    showLinkToInfoPage = showLinkToInfoPage,
    siteTitle = siteTitle,
    siteTitleShort = siteTitleShort,
    privacyPolicyLink = sitePrivacyPolicyLink,
    localization = localization,
    hl = hl,
) {

    override fun FlowContent.content() {
        div(classes = "section") {
            div(classes = "row") {
                div("col s12") {
                    h3 { +localization["info_title"] }
                }
            }
            div(classes = "row") {
                div("col s12") {
                    +localization["info_kicker"]
                }
            }
            div(classes = "row") {
                div("col s12") {
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["info_bullet_point_1_before_link"]
                    i { +localization["index_btn_make_the_drop"] }
                    +localization["info_bullet_point_1_after_link"]
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["info_bullet_point_2"]
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization.get("info_bullet_point_3", keepFilesTimeInHours)
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["info_bullet_point_4"]
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +localization["info_bullet_point_5"]
                }

                div("col s12") {
                    +localization["info_encryption_before_link"]
                    a(href = "https://github.com/bitwiseshiftleft/sjcl") {
                        target = "_blank"
                        +"github.com/bitwiseshiftleft/sjcl"
                    }
                    +localization["info_encryption_after_link"]
                    br()
                    +localization["info_open_source_before_link"]
                    a(href = "https://github.com/FlowMo7/dead-drop") {
                        target = "_blank"
                        +localization["info_open_source_link_text"]
                    }
                    +localization["info_open_source_after_link"]
                    br()
                    +localization["info_self_host_before_link"]
                    a(href = "https://github.com/FlowMo7/dead-drop") {
                        target = "_blank"
                        +localization["info_self_host_link_text"]
                    }
                    +localization["info_self_host_after_link"]
                }
            }
        }
    }

}