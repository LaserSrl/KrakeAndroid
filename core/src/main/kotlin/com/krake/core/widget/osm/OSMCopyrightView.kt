package com.krake.core.widget.osm

/**
 * Used to define a view that will show the Open Street Map copyright.
 * The view must be shown only when the copyright is shown, so, avoid the configurations
 * that are permanently visible independently from the visibility of the OSM copyright.
 */
interface OSMCopyrightView {

    /**
     * Show this view with the loaded Open Street Map copyright.
     * The view must be visible to the user only when this method is called, not before.
     */
    fun showOSMCopyright()
}