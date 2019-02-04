package com.krake.cards

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import com.krake.core.app.ContentItemListMapActivity

class CardsActivity : ContentItemListMapActivity() {
    override fun changeContentVisibility(visible: Boolean) {
        super.changeContentVisibility(visible)

        if (visible) {
            if (!displayedWelcome && !TextUtils.isEmpty(getString(R.string.cards_welcome_message_text))) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.welcome)
                        .setMessage(R.string.cards_welcome_message_text)
                        .setNeutralButton(android.R.string.ok, null)
                        .show()
                setDisplayedWelcome()
            }
        }
    }

    private fun openPreferences(): SharedPreferences {
        return getSharedPreferences("CardsWelcomePref", Context.MODE_PRIVATE)
    }

    private val displayedWelcome: Boolean
        get() = openPreferences().getBoolean("Displayed", false)

    @SuppressLint("CommitPrefEdits")
    private fun setDisplayedWelcome() {
        openPreferences().edit().putBoolean("Displayed", true).apply()
    }
}