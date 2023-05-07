package dev.moetz.deaddrop.template

import dev.moetz.deaddrop.combinePartsToPathPrefix
import kotlinx.html.*

class IndexTemplate(
    pathPrefix: String?,
    showGithubLinkInFooter: Boolean,
    colorCode: String,
    showLinkToInfoPage: Boolean = true,
    siteTitle: String,
    siteTitleShort: String,
    private val keepFilesTimeInHours: Int,
) : SiteTemplate(
    pathPrefix = pathPrefix,
    showGithubLinkInFooter = showGithubLinkInFooter,
    colorCode = colorCode,
    showLinkToInfoPage = showLinkToInfoPage,
    siteTitle = siteTitle,
    siteTitleShort = siteTitleShort,
) {

    override fun FlowContent.content() {
        br()

        div(classes = "section") {
            id = "send_div"

            div(classes = "row") {
                div(classes = "col s12") {
                    +"Want to send something private? A password, love-note or something else, no-one else than the recipient should see?"
                    br()
                    br()
                    +"This service will:"
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +"Encrypt your message in your browser with a randomly generated password."
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +"Upload the encrypted message to the server (while not sharing the password with the server)"
                    br()
                    unsafe { +"&bullet;&nbsp;" }
                    +"Display the password as well as a link to get the message back, which you can then share with your recipient."
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    +"Enter your message below:"
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    textArea(cols = "70", rows = "8") {
                        style = "min-height:200px;padding:10px;color:var(--color-text);"
                        name = "message"
                        id = "drop_content"
                        placeholder = "Message to encrypt"
                    }
                }
                div(classes = "col s8") {
                    span(classes = "red-text") {
                        id = "error_message"
                        hidden = true
                        +"There was an error creating your drop. Please try again."
                    }
                }
                div(classes = "col s4") {
                    a(classes = "waves-effect waves-light btn right") {
                        style = "background-color:#$colorCode;"
                        onClick = "sendDrop(document.getElementById('drop_content').value)"
                        +"Make the drop!"
                    }
                }
            }
        }

        div(classes = "section") {
            id = "link_div"
            hidden = true

            div("row") {
                div(classes = "col s12") {
                    h5(classes = "green-text center") {
                        +"Drop made!"
                    }
                }
            }
            div("row") {
                div(classes = "col s12") {
                    +"Your recipient needs the link as well as the password to get the drop."
                }
                div(classes = "col s12") {
                    +"It might be best to just fully copy the message below to your recipient."
                }
                div(classes = "col s12") {
                    +"Note, that for additional security, the link and password may be sent on separate channels (e.g. mail and a messenger)"
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    div(classes = "card") {
                        div(classes = "card-content black-text grey lighten-3") {
                            span(classes = "card-title") { +"Hi," }
                            p {
                                id = "message_to_share_drop"

                                +"I'm sending you some secure information."
                                br()
                                +"Location: "
                                b {
                                    span {
                                        id = "drop_share_link"
                                    }
                                }
                                br()
                                +"Password: "

                                b {
                                    span {
                                        id = "drop_share_password"
                                    }
                                }
                                br()
                                br()

                                b { +"Warning!" }
                                br()

                                +"This drop will only work "
                                b { +"once" }
                                +", so be careful with the password, and make sure to copy the data immediately."
                                br()

                                +"After you pick it up (either successfully or with e.g. a wrong password), the data will self-destruct and won't be available anymore."
                                br()

                                +"This link will only work for $keepFilesTimeInHours hours, after that, the data will self-destruct as well."
                            }
                        }
                    }
                }
            }

            div(classes = "row") {
                div(classes = "col s12") {
                    a(classes = "waves-effect waves-light btn-small right") {
                        style = "background-color:#$colorCode;"
                        onClick = "window.location.assign('${combinePartsToPathPrefix(pathPrefix)}')"
                        +"Make another drop"
                    }
                }
            }
        }
    }

}