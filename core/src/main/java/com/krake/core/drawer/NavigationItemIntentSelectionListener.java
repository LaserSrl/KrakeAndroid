package com.krake.core.drawer;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interfaccia da implementare per caricare i contenuti dal drawer usando la navigation view.
 * La classe che implementa quest'interfaccia deve includere un contruttore pubblico con parametro Context
 */
public interface NavigationItemIntentSelectionListener {
    /**
     * Indica che l'utente ha selezionato uno degli elementi del menu,
     * Il gestire deve preparare e restituire l'intent da utilizzare per avviare l'activity corrispondente.
     *
     * @param item
     * @return intent da avviare, oppure null
     */
    @Nullable
    Intent createIntentForNavigationItemSelected(@NonNull android.view.MenuItem item);
}
