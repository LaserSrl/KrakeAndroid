package com.krake.core.extension

import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.os.Build

/**
 * Fa il commit di un [Fragment] sincronicamente.
 *
 * @param manager [FragmentManager] usato per fare il commit della [FragmentTransaction].
 */
fun FragmentTransaction.commitSync(manager: FragmentManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        commitNow()
    } else {
        commit()
        manager.executePendingTransactions()
    }
}

/**
 * Fa il commit di un [android.support.v4.app.Fragment] sincronicamente.
 *
 * @param manager [android.support.v4.app.FragmentManager] usato per fare il commit della [android.support.v4.app.FragmentTransaction].
 */
fun android.support.v4.app.FragmentTransaction.commitSync(manager: android.support.v4.app.FragmentManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        commitNow()
    } else {
        commit()
        manager.executePendingTransactions()
    }
}

/**
 * Fa il commit di un [Fragment] sincronicamente permettendo la condizione di state loss.
 *
 * @param manager [FragmentManager] usato per fare il commit della [FragmentTransaction].
 */
fun FragmentTransaction.commitSyncAllowingStateLoss(manager: FragmentManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        commitNowAllowingStateLoss()
    } else {
        commitAllowingStateLoss()
        manager.executePendingTransactions()
    }
}

/**
 * Fa il commit di un [android.support.v4.app.Fragment] sincronicamente permettendo la condizione di state loss.
 *
 * @param manager [android.support.v4.app.FragmentManager] usato per fare il commit della [android.support.v4.app.FragmentTransaction].
 */
fun android.support.v4.app.FragmentTransaction.commitSyncAllowingStateLoss(manager: android.support.v4.app.FragmentManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        commitNowAllowingStateLoss()
    } else {
        commitAllowingStateLoss()
        manager.executePendingTransactions()
    }
}