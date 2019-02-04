package com.krake.contentcreation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Interfaccia che devono implementare i fragment da inserire nei tab pe mostrare i vari tipi di contenuti.
 */
public interface ContentCreationFragment {

    /**
     * Questo metodo permette di validare i campi da spedire al WS.
     * Potrebbe essere invocato anche quando il fragment non è collegato all'activity, quindi nella
     * sua implementazione non devono essere utilizzate le variabili di istanza, ma solamente i parametri passati al metodo
     *
     * @param activity     activity da cui è chiamato il metodo
     * @param creationInfo info di creazione del fragment
     * @param savedInfos   informazioni del fragment che sono state memorizzate
     * @return true se i valori sono validi e possono essere spediti
     */
    boolean validateDataAndSaveError(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @Nullable Object savedInfos);

    /**
     * Metodo per inserire i dati da inviare al WS.
     * <p/>
     * Potrebbe essere invocato anche quando il fragment non è collegato all'activity, quindi nella
     * sua implementazione non devono essere utilizzate le variabili di istanza, ma solamente i parametri passati al metodo
     *
     * @param activity     activity da cui è chiamato il metodo
     * @param creationInfo info di creazione del fragment
     * @param savedInfos   informazioni del fragment che sono state memorizzate
     * @param parameters   parametri da inviare al WS
     * @return true se i parametri possono essere spediti, false altrimenti
     * (il false viene utilizzato solo da {@link MediaPickerFragment} quando è necessario fare l'upload della foto)
     */
    boolean insertDataToUpload(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @Nullable Object savedInfos, @NonNull JsonObject parameters);

    /**
     * Metodo per inserire i dati da inviare al WS.
     * <p/>
     * Potrebbe essere invocato anche quando il fragment non è collegato all'activity, quindi nella
     * sua implementazione non devono essere utilizzate le variabili di istanza, ma solamente i parametri passati al metodo
     *
     * @param activity        activity da cui è chiamato il metodo
     * @param creationInfo    info di creazione del fragment
     * @param gson            istanza di gson
     * @param serializedInfos informazioni serializzate  @return informazioni deserializzate
     */
    Object deserializeSavedInstanceState(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @NonNull Gson gson, @Nullable String serializedInfos);
}