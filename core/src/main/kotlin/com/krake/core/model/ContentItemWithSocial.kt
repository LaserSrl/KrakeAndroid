package com.krake.core.model

/**
 * Created by joel on 01/03/17.
 */

interface ContentItemWithSocial : ContentItem {
    val facebookValue: String?
    val pinterestValue: String?
    val instagramValue: String?
    val twitterValue: String?
    val youtubeValue: String?
}