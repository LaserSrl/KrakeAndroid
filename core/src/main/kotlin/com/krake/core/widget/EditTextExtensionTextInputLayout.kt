package com.krake.core.widget

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Created by joel on 21/02/17.
 */

private fun EditText.textInputLayoutParent(): TextInputLayout? {
    var parent = this.parent
    while (parent != null) {
        if (parent is TextInputLayout)
            return parent

        parent = parent.parent
    }
    return null
}

fun EditText.setErrorInThisOrInputLayout(error: String?) {
    val parent = this.textInputLayoutParent()

    if (parent != null)
        parent.error = error
    else
        this.error = error
}

fun EditText.setHintInThisOrInputLayout(hint: String?) {
    val parent = this.textInputLayoutParent()

    if (parent != null) {
        parent.hint = hint
    } else
        this.hint = hint
}