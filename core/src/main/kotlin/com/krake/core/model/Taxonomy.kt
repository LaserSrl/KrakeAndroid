package com.krake.core.model


/**
 * Created by joel on 28/02/17.
 */
@Suppress("UNCHECKED_CAST")
interface Taxonomy : ContentItem, RecordWithIdentifier {
    val taxonomyPartTermTypeName: String?
    val taxonomyPartName: String?
    val taxonomyPartTerms: List<*>
    val terms get() = taxonomyPartTerms as List<com.krake.core.model.TermPart>
}