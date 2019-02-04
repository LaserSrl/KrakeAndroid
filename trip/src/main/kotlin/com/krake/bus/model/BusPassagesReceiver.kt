package com.krake.bus.model

/**
 * Created by antoniolig on 27/04/2017.
 */
interface BusPassagesReceiver {

    fun onPassageChosen(passage: BusPassage)
}