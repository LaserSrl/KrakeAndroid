package com.krake.core.social;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.krake.core.model.ShareLinkPart;


/**
 * Interfaccia Interceptor per tutti gli Intent di condivisione
 */
public interface DetailIntentSharingInterceptor {
    /**
     * Definisce la possibilità di utilizzare un comportamento custom filtrato in base al package dell'app che si vuole aprire
     *
     * @param fragment  Fragment dal quale parte l'Intent di condivisione
     * @param shareLink ShareLinkPart abbinata al ContentItem mostrato nel Fragment
     * @param mediaUri  Uri dell'immagine da condividere, se presente
     * @param intent    Intent valorizzato con il package dell'app scelta per la condivisione
     * @return true se l'Intent è gestito in modo custom
     */
    boolean handleSharingDetail(@NonNull Fragment fragment, @NonNull ShareLinkPart shareLink, @Nullable Uri mediaUri, @NonNull Intent intent);
}