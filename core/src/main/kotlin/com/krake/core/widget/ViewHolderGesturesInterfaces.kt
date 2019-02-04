package com.krake.core.widget

import android.view.View
import com.krake.core.R

/**
 * Created by giuliettif on 15/03/17.
 * Interfacce che i [ViewHolder] devono implementare se vogliono che una sua view abbia un [Gesture]
 * [Interceptor] gestirà l'aggiunta dei listener in queste view, che a loro volta comunicheranno ai receiver aggiunti all'Interceptor
 */

interface ViewHolderAccess {
    val itemView: View
}

/**
 * Interfaccia per il gesture [Gesture.CLICK]
 */
interface ViewHolderWithClickGesture : ViewHolderAccess {

    /**
     * resituisce la view su cui verrà aggiunto il OnClickListener
     */
    fun viewWithClick(): View {
        val tagView: View? = itemView.findViewWithTag(itemView.context.getString(R.string.cell_selection_view_tag))
        return tagView ?: itemView
    }
}

/**
 * Interfaccia per il gesture [Gesture.LONG_CLICK]
 */
interface ViewHolderWithLongClickGesture {

    /**
     * resituisce la view su cui verrà aggiunto il OnLongClickListener
     */
    fun viewWithLongClick() : View
}

/**
 * Interfaccia per il gesture [Gesture.TOUCH]
 */
interface ViewHolderWithTouchGesture {

    /**
     * resituisce la view su cui verrà aggiunto il OnTouchListener
     */
    fun viewWithTouch() : View
}