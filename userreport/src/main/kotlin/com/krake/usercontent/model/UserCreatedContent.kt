package com.krake.usercontent.model

import com.krake.core.model.ContentItemWithGallery
import com.krake.core.model.EnumerationField

/**
 * Created by joel on 08/03/17.
 */

interface UserCreatedContent : ContentItemWithGallery {

    val sottotitoloValue: String?

    val publishExtensionPartPublishExtensionStatus: EnumerationField?
}