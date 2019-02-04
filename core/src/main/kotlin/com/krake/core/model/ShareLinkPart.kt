package com.krake.core.model

/**
 * Created by joel on 28/02/17.
 */

interface ShareLinkPart {
    val sharedText: String?
    val sharedLink: String?
    val sharedImage: String?

    val isShareValid: Boolean
        get() {
            return !sharedText.isNullOrEmpty() || !sharedLink.isNullOrEmpty() || !sharedImage.isNullOrEmpty()
        }
}
