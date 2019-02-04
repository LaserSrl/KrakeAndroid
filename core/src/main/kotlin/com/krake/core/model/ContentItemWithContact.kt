package com.krake.core.model

/**
 * Created by joel on 01/03/17.
 */

interface ContentItemWithContact : ContentItem {
    val sitoWebValue: String?
    val telefonoValue: String?
    val eMailValue: String?
}