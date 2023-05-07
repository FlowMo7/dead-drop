package dev.moetz.deaddrop.template

import kotlinx.html.*

class PickupTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    colorCode: String,
    showLinkToInfoPage: Boolean = true,
    siteTitle: String,
    siteTitleShort: String,
    private val dropId: String?,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    colorCode = colorCode,
    showLinkToInfoPage = showLinkToInfoPage,
    siteTitle = siteTitle,
    siteTitleShort = siteTitleShort,
) {

    override fun FlowContent.content() {
        div(classes = "section") {
            id = "container_get_drop"

            div("row") {
                div("col s12") {
                    +"Enter the password you got provided with this link."
                }
            }
            div("row") {
                div("col s12") {
                    +"This will only work "
                    b { +"once" }
                    +"! Clicking "
                    i { +"Get the drop" }
                    +" will delete the message permanently, regardless of whether the password was correct or not."
                }
            }

            div("row") {
                div("input-field col s12") {
                    textInput(classes = "validate") {
                        id = "drop_password"
                        placeholder = "Enter the password here"
                    }
                }
                div(classes = "col s12") {
                    a(classes = "waves-effect waves-light btn") {
                        style = "background-color:#$colorCode;"
                        onClick = "getDrop(" +
                                "'$dropId', " +
                                "document.getElementById('drop_password').value" +
                                ")"
                        +"Get the drop"
                    }
                }
            }
        }

        div(classes = "section") {
            id = "drop_content_section"
            hidden = true

            h3 {
                +"Your drop:"
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
                h3(classes = "red-text") {
                    +"Error"
                }
            }
            div(classes = "row") {
                div(classes = "col s12") {
                    +"There was an error getting your drop. This can either be:"
                    br()
                    br()

                    +"The drop has already been fetched. You can only open / get a drop once. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."

                    br()
                    br()
                    +"The entered password was wrong. You can only open / get a drop once, and if you enter the wrong password, the drop is gone forever. If you didn't get the drop yet, it might be that someone else tried (and if they got the password, may have succeeded) to get your drop. If you are unsure, you should consider that the dropped content is no longer safe in this scenario."

                    br()
                    br()
                    b {
                        +"In each case, if neither you nor the creator of the drop took actions that could lead to this scenario, please consider that the content of your drop may be in malicious hands as of now."
                    }
                }
            }
        }
    }

}