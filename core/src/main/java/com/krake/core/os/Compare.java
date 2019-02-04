package com.krake.core.os;

import android.content.Intent;
import android.os.Bundle;

import com.krake.core.extension.BundleExtensionsKt;
import com.krake.core.extension.IntentExtensionsKt;

/**
 * Deprecated v. 8.x
 */
@Deprecated
public class Compare {
    /**
     * Verifica dell'uguaglianza di due bundle.
     * Due bundle sono ugualie se hanno le stesse chiavi e se per ogni chiave
     * hanno valori che ritornano true se confrontati con equals().
     * I Bundle interni sono esplorati in modo ricorsivo.
     *
     * @param one
     * @param two
     * @return true se bundle contengono dati uguali
     */
    @Deprecated
    public static boolean equalBundles(Bundle one, Bundle two) {
        return BundleExtensionsKt.equalToBundle(one, two);
    }

    /**
     * Verifica dell'uguaglianza di due Intent.
     * Due Intent sono uguali se hanno lo stesso component, le stesse chiavi e se per ogni chiave
     * hanno valori che ritornano true se confrontati con equals().
     * I Bundle interni sono esplorati in modo ricorsivo.
     * @param one
     * @param two
     * @return true se bundle contengono dati uguali
     */
    @Deprecated
    public static boolean equalsIntent(Intent one, Intent two) {

        return IntentExtensionsKt.equalsToIntent(one, two);
    }
}
