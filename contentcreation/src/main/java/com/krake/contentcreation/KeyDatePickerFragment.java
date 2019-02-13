package com.krake.contentcreation;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.krake.core.app.DateTimePickerFragment;

import java.util.Date;

/**
 * Fragment utilizzato per scegliere una data con un picker a partire da un {@link ContentCreationTabInfo.FieldInfo} e mandare la callback ad un listener
 */
public final class KeyDatePickerFragment extends DateTimePickerFragment {
    private static final String ARG_KEY = "argKey";

    private OnKeyDatePickerListener mListener;
    private String mKey;

    /**
     * Crea un nuovo fragment di tipo {@link KeyDatePickerFragment} e aggiunge i suoi arguments
     *
     * @param key              orchardKey abbinata al field dal quale è stata fatta partire l'azione di apertura del picker
     * @param startDate        data d'inizio usata per centrare il picker su una data differente da quella corrente
     * @param enableTimePicker true nel caso in cui si voglia abilitare il picker per le ore e minuti
     * @return nuova istanza di un {@link KeyDatePickerFragment}
     */
    public static KeyDatePickerFragment newInstance(@NonNull String key, @NonNull Date startDate, boolean enableTimePicker) {
        KeyDatePickerFragment fragment = new KeyDatePickerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        args.putLong(ARG_START_DATE, startDate.getTime());
        args.putBoolean(ARG_ENABLE_TIME_PICKER, enableTimePicker);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onAttachCommon(Context context) {
        // onAttachCommon(Context) senza il super per evitare che venga settato il listener di DateTimePickerFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_KEY)) {
            mKey = args.getString(ARG_KEY);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        super.onClick(dialogInterface, i);
        if (mListener != null) {
            mListener.onDatePicked(mKey, mStartDate);
        }
    }

    /**
     * Setta il listener sul quale verranno ricevute le callbacks di OnKeyDatePickerListener
     *
     * @param listener listener per ricevere le callbacks
     */
    public void setListener(@Nullable OnKeyDatePickerListener listener) {
        mListener = listener;
    }

    /**
     * Listener che riceve le callbacks delle azioni dell'utente sul Fragment
     */
    public interface OnKeyDatePickerListener {
        /**
         * Notifica il listener dopo che una data è stata scelta con successo
         *
         * @param key  orchardKey abbinata al field che ha causato l'apertura di questo Fragment
         * @param date data scelta dall'utente
         */
        void onDatePicked(@NonNull String key, @NonNull Date date);
    }
}