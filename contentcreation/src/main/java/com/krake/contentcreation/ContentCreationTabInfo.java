package com.krake.contentcreation;

import android.content.Context;
import android.util.SparseArray;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.krake.contentcreation.validator.FieldInfoBoolValidator;
import com.krake.core.media.MediaType;
import com.krake.core.media.watermark.Watermark;
import com.krake.core.model.TermPart;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Informazioni per rappresentare un tab che permette di creare un nuovo contenuto su Orchard.
 * I tab sono di 3 tipologia diverse:
 * <ol>
 * <li>{@link FieldsFragment} per permette di modificare 1 o + campi del contenuto</li>
 * <li>{@link MediaPickerFragment} per aggiungere una foto o un video</li>
 * <li>{@link LocationSelectionFragment} per selezionare un punto su mappa</li>
 * </ol>
 * <p/>
 * Alcune note su come identificare il nome dei campi in Orchard:
 * <ol>
 * <li>Se sono delle Part, allora è necessario specificare il nome della parte seguito da un punto e dal nome della proprietaà della parte
 * che si vuole completare. Ad esempio per riempire il Titolo di un record occore indicare TitlePart.Title, la prima metà del nome rappresenta il nome della parte in orchard,
 * se seconda il nome della proprietà della parte. <strong>Eccezione</strong> per la MapPart occorre solo specificare il nome della parte, i singoli componenti della parte
 * saranno gestiti  direttamente dalla libreria</li>
 * <li>Per i field invece occorre solo specificare il nome del field ad esempio Sottotitolo.</li>
 * </ol>
 */
public class ContentCreationTabInfo {
    public static final int FIELD_TYPE_TEXT = 1;
    public static final int FIELD_TYPE_ENUM_OR_TERM_SELECTION = 1 << 2;
    public static final int FIELD_TYPE_BOOLEAN = 1 << 3;
    public static final int FIELD_TYPE_DATE = 1 << 4;
    public static final int FIELD_TYPE_CONTENT_PICKER = 1 << 5;

    private ContentCreationInfo mInfo;

    private ContentCreationTabInfo(@NonNull ContentCreationInfo info) {
        mInfo = info;
    }

    /**
     * Crea un nuovo tab di tipo {@link MediaPickerFragment} per aggiungere una foto al contenuto.
     * Sarà necessario fare l'upload della foto prima di procedere all'effettiva creazione del contenuto in Orchard.
     * Per i dettagli vedere {@link ContentCreationActivity}.
     *
     * @param title             titolo del tab
     * @param orchardKey        chiave del content picker field per i media part impostata in Orchard (normalmente Gallery)
     * @param required          indicazione se è necessario selezionare una foto per caricare il contenuto
     * @param mediaType         tipi di media selezionabili
     * @param maxNumberOfMedias se e' possibile selezionare piu' di un media
     * @return le info per la creazione del tab
     * @throws IllegalArgumentException se mediaType = 0 e se maxNumberOfMedias < 0
     */
    public static ContentCreationTabInfo createMediaInfo(@StringRes int title, @NonNull String orchardKey, @Nullable String dataKey, boolean required, @MediaType int mediaType, int maxNumberOfMedias) {
        return new ContentCreationTabInfo(new MediaInfo(title, orchardKey, dataKey, mediaType, maxNumberOfMedias, required, true));
    }

    /**
     * Crea un nuovo tab di tipo {@link MediaPickerFragment} per aggiungere una foto al contenuto.
     * Sarà necessario fare l'upload della foto prima di procedere all'effettiva creazione del contenuto in Orchard.
     * Per i dettagli vedere {@link ContentCreationActivity}.
     *
     * @param title             titolo del tab
     * @param orchardKey        chiave del content picker field per i media part impostata in Orchard (normalmente Gallery)
     * @param required          indicazione se è necessario selezionare una foto per caricare il contenuto
     * @param mediaType         tipi di media selezionabili
     * @param maxNumberOfMedias se e' possibile selezionare piu' di un media
     * @return le info per la creazione del tab
     * @throws IllegalArgumentException se mediaType = 0 e se maxNumberOfMedias < 0
     */
    public static ContentCreationTabInfo createMediaInfo(@StringRes int title,
                                                         @NonNull String orchardKey,
                                                         @Nullable String dataKey, boolean required, boolean editingEnabled, @MediaType int mediaType, int maxNumberOfMedias) {
        return new ContentCreationTabInfo(new MediaInfo(title, orchardKey, dataKey, mediaType, maxNumberOfMedias, required, editingEnabled));
    }

    /**
     * Crea un nuovo tab di tipo {@link MediaPickerFragment} per aggiungere una foto al contenuto.
     * Sarà necessario fare l'upload della foto prima di procedere all'effettiva creazione del contenuto in Orchard.
     * Per i dettagli vedere {@link ContentCreationActivity}.
     *
     * @param title             titolo del tab
     * @param orchardKey        chiave del content picker field per i media part impostata in Orchard (normalmente Gallery)
     * @param required          indicazione se è necessario selezionare una foto per caricare il contenuto
     * @param watermark         watermark da applicare sull'immagine
     * @param maxNumberOfMedias se e' possibile selezionare piu' di un media
     * @return le info per la creazione del tab
     * @throws IllegalArgumentException se mediaType = 0 e se maxNumberOfMedias < 0
     */
    public static ContentCreationTabInfo createMediaInfo(@StringRes int title, @NonNull String orchardKey, @Nullable String dataKey, boolean required, Watermark watermark, int maxNumberOfMedias) {
        return new ContentCreationTabInfo(new MediaInfo(title, orchardKey, dataKey, watermark, maxNumberOfMedias, required));
    }

    /**
     * Crea un nuovo tab di tipo {@link FieldsFragment} per caricare diversi campi di un nuovo contenuto in orchard.
     *
     * @param title  titolo del tab
     * @param fields i campi da aggiungere al contenuto
     * @return le info per la creazione del tab
     */
    public static ContentCreationTabInfo createFieldsInfo(@StringRes int title, @NonNull List<FieldInfo> fields) {
        return new ContentCreationTabInfo(new ContentFieldsInfos(title, fields));
    }

    /**
     * Creazione di un nuovo tab {@link LocationSelectionFragment} per indicare una posizione da assegnare al contenuto.
     *
     * @param title      titolo del tab
     * @param orchardKey chiave della MapPart impostata in Orchard (normalmente MapPart)
     * @param dataKey
     * @param required   indicazione se è necessario indicare la posizione per caricare il contenuto
     * @return le info per la creazione del tab
     */
    public static ContentCreationTabInfo createMapInfo(@StringRes int title, @NonNull String orchardKey, @Nullable String dataKey, boolean required) {
        return new ContentCreationTabInfo(new MapInfo(title, orchardKey, dataKey, required));
    }

    public static ContentCreationTabInfo createCustomInfo(@NonNull ContentCreationInfo info) {
        return new ContentCreationTabInfo(info);
    }

    public static ContentCreationTabInfo createAllPoliciesInfo(Context context, @StringRes int title, String orchardKey, String dataKey) {
        return createPoliciesInfo(title, orchardKey, dataKey, context.getString(R.string.policy_all_key));
    }

    public static ContentCreationTabInfo createRegistrationPoliciesInfo(Context context, @StringRes int title, String orchardKey, String dataKey) {
        return createPoliciesInfo(title, orchardKey, dataKey, context.getString(R.string.policy_register_key));
    }

    public static ContentCreationTabInfo createPoliciesInfo(@StringRes int title, String orchardKey, String dataKey, String type) {
        return new ContentCreationTabInfo(new PolicyInfo(title, orchardKey, dataKey, type));
    }

    public ContentCreationInfo getInfo() {
        return mInfo;
    }

    @IntDef(flag = true, value = {
            FIELD_TYPE_TEXT,
            /**
             * Utilizzato anche per i Content Picker Field
             */
            FIELD_TYPE_ENUM_OR_TERM_SELECTION,
            FIELD_TYPE_BOOLEAN,
            FIELD_TYPE_DATE,
            FIELD_TYPE_CONTENT_PICKER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FieldType {
    }

    /**
     * Validator con cui si ha la possibilità di aggiungere dei validator per ogni {@link FieldInfo} al momento della dichiarazione
     * Ogni tipo di validator deve estendere da questa classe astratta e il metodo [FieldInfoValidator.validate] verrà richiamato al momento del check degli errori
     * Occorre creare i validator tenendo a mente i vari tipi di fieldInfo: per esempio posso creare un validator<Object> e posso gestire tutti i tipi di FieldInfo ma se vogliamo creare
     * dei validator specifici per ogni singolo tipo allora:
     * {@value FIELD_TYPE_TEXT} -> {@link FieldInfoValidator<String>}
     * {@value FIELD_TYPE_ENUM_OR_TERM_SELECTION} -> {@link FieldInfoValidator<org.json.JSONArray>}
     * {@value FIELD_TYPE_BOOLEAN} -> {@link FieldInfoValidator<Boolean>}
     * {@value FIELD_TYPE_DATE} -> {@link FieldInfoValidator<java.util.Date>}
     * il metodo deve restituire un {@link Result} dove di default [Result.error] sarà false e quindi il controllo passerà, ma se la validazione non va a buon fine occorre impostare
     * [Result.error] a true e [Result.errorMessage] con il messaggio che vogliamo far visualizzare
     * Viene passato anche un {@link Context} nel costruttore così da ottenere facilmente le [res]
     */
    public interface FieldInfoValidator<Object> {
        Result validate(Context context, Object field);

        class Result {
            private boolean mError = false;
            private String mErrorMessage;

            public boolean getError() {
                return mError;
            }

            public void setError(boolean error) {
                this.mError = error;
            }

            public String getErrorMessage() {
                return mErrorMessage;
            }

            public void setErrorMessage(String errorMessage) {
                this.mErrorMessage = errorMessage;
                setError(true);
            }
        }
    }

    public static abstract class ContentCreationInfo {

        protected int mTitle;

        protected ContentCreationInfo(@StringRes int title) {
            mTitle = title;
        }

        int getTabTitle() {
            return mTitle;
        }
    }

    public static class MediaInfo extends ContentCreationInfo {

        private final boolean required;
        private final boolean editingEnabled;
        private final
        @MediaType
        int mediaType;
        private final int maxNumberOfMedias;
        private final String orchardKey;
        private final String dataKey;
        private final Watermark watermark;

        public MediaInfo(int titleStringRef,
                         String orchardKey, String dataKey, @MediaType int mediaType, int maxNumberOfMedias, boolean required, boolean editingEnabled) throws IllegalArgumentException {
            super(titleStringRef);
            this.dataKey = dataKey;
            this.required = required;
            this.orchardKey = orchardKey;
            this.mediaType = mediaType;
            this.editingEnabled = editingEnabled;
            if (mediaType == 0)
                throw new IllegalArgumentException("You can't disable all media types");

            if (maxNumberOfMedias < 0)
                throw new IllegalArgumentException("Max number of media can't be less then zero");

            this.maxNumberOfMedias = maxNumberOfMedias;
            watermark = null;
        }

        public MediaInfo(int titleStringRef,
                         String orchardKey, String dataKey, Watermark watermark, int maxNumberOfMedias, boolean required) throws IllegalArgumentException {
            super(titleStringRef);
            this.dataKey = dataKey;
            this.required = required;
            this.orchardKey = orchardKey;
            this.mediaType = MediaType.IMAGE;
            this.editingEnabled = true;

            if (maxNumberOfMedias < 0)
                throw new IllegalArgumentException("Max number of media can't be less then zero");

            this.maxNumberOfMedias = maxNumberOfMedias;
            this.watermark = watermark;
        }

        public boolean isRequired() {
            return required;
        }

        public String getOrchardKey() {
            return orchardKey;
        }

        public int getMaxNumberOfMedias() {
            return maxNumberOfMedias;
        }

        public
        @MediaType
        int getMediaType() {
            return mediaType;
        }

        public Watermark getWatermark() {
            return watermark;
        }

        public String getDataKey() {
            return dataKey;
        }

        public boolean isEditingEnabled() {
            return editingEnabled;
        }
    }

    public static class PolicyInfo extends ContentCreationInfo {
        private final String orchardKey;
        private final String dataKey;
        private final String type;

        public PolicyInfo(@StringRes int title, String orchardKey, String dataKey, String type) {
            super(title);

            this.orchardKey = orchardKey;
            this.dataKey = dataKey;
            this.type = type;
        }

        public String getOrchardKey() {
            return orchardKey;
        }

        public String getDataKey() {
            return dataKey;
        }

        public String getType() {
            return type;
        }
    }

    public static class MapInfo extends ContentCreationInfo {

        private final boolean required;
        private final String orchardKey;
        private final String dataKey;

        public MapInfo(int titleStringRef, String orchardKey, String dataKey, boolean required) {
            super(titleStringRef);
            this.dataKey = dataKey;
            this.required = required;
            this.orchardKey = orchardKey;
        }

        public boolean isRequired() {
            return required;
        }

        public String getOrchardKey() {
            return orchardKey;
        }

        public String getDataKey() {
            return dataKey;
        }
    }

    public static class ContentFieldsInfos extends ContentCreationInfo {

        private List<FieldInfo> fields = new ArrayList<>();

        public ContentFieldsInfos(int title, List<FieldInfo> fields) {
            super(title);
            this.fields = fields;
        }

        public List<FieldInfo> getFields() {
            return fields;
        }
    }

    /**
     * Informazioni su un campo da inserire nel contenuto.
     * Ci sono principalmente due categorie di campi disponibili:
     * i campi di testo e i campi per la selezione di una {@link TermPart}
     */
    public static class FieldInfo {
        private final int name;
        private final String orchardKey;
        private final String dataKey;
        @FieldType
        private final int type;
        private final boolean required;
        private final Object defaultValue;
        private final boolean multipleSelection;
        private final SparseArray extras;
        private final ArrayList<FieldInfoValidator> fieldInfoValidators = new ArrayList<>();
        private final boolean editingEnabled;
        private String orchardComponentModule;
        private boolean isLoginEnabled;

        /**
         * @param name       nome del campo
         * @param orchardKey chiave del campo assegnata in orchard
         * @param dataKey    nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                   I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                   Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type       tipologia del campo.
         */
        public FieldInfo(@StringRes int name, @NonNull String orchardKey, @Nullable String dataKey, @FieldType int type) {
            this(name, orchardKey, dataKey, type, false);
        }

        /**
         * @param name       nome del campo
         * @param orchardKey chiave del campo assegnata in orchard
         * @param dataKey    nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                   I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                   Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type       tipologia del campo.
         * @param required   indicazione se è necessario compilare il campo prima di procedere (valido solo per campi diveri da FIELD_TYPE_BOOLEAN), di default a false
         */
        public FieldInfo(@StringRes int name, @NonNull String orchardKey, @Nullable String dataKey, @FieldType int type, boolean required) {
            this(name, orchardKey, dataKey, type, required, null, null);
        }

        /**
         * @param name         nome del campo
         * @param orchardKey   chiave del campo assegnata in orchard
         * @param dataKey      nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                     I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                     Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type         tipologia del campo. Non può essere utilizzata
         * @param required     indicazione se è necessario compilare il campo prima di procedere
         * @param defaultValue valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                     di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                     <br>
         *                     (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         */
        public FieldInfo(@StringRes int name, @NonNull String orchardKey, @Nullable String dataKey, @FieldType int type, boolean required, @Nullable SparseArray extras, @Nullable Object defaultValue) {
            this(name, orchardKey, dataKey, type, required, false, true, defaultValue, null, null, false, extras);
        }

        /**
         * @param name               nome del campo
         * @param orchardKey         chiave del campo assegnata in orchard
         * @param dataKey            nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                           I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                           Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type               tipologia del campo. Non può essere utilizzata
         * @param required           indicazione se è necessario compilare il campo prima di procedere
         * @param defaultValue       valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                           di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                           <br>
         *                           (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         * @param fieldInfoValidator validator da applicare al field al momento del check
         */
        public FieldInfo(@StringRes int name, @NonNull String orchardKey, @Nullable String dataKey, @FieldType int type, boolean required, @Nullable SparseArray extras,
                         @Nullable Object defaultValue, @Nullable FieldInfoValidator fieldInfoValidator) {
            this(name, orchardKey, dataKey, type, required, false, true, defaultValue, fieldInfoValidator, null, false, extras);
        }

        /**
         * @param name               nome del campo
         * @param orchardKey         chiave del campo assegnata in orchard
         * @param dataKey            nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                           I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                           Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type               tipologia del campo. Non può essere utilizzata
         * @param required           indicazione se è necessario compilare il campo prima di procedere
         * @param defaultValue       valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                           di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                           <br>
         *                           (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         * @param fieldInfoValidator validator da applicare al field al momento del check
         */
        public FieldInfo(@StringRes int name, @NonNull String orchardKey, @Nullable String dataKey, @FieldType int type, boolean required, boolean editingEnabled, @Nullable SparseArray extras,
                         @Nullable Object defaultValue, @Nullable FieldInfoValidator fieldInfoValidator) {
            this(name, orchardKey, dataKey, type, required, false, editingEnabled, defaultValue, fieldInfoValidator, null, false, extras);
        }

        /**
         * Per creare una reference ad un ContentPickerField
         *
         * @param name                   nome del campo
         * @param orchardKey             chiave del campo assegnata in orchard
         * @param dataKey                nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                               I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                               Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param required               indicazione se è necessario compilare il campo prima di procedere
         * @param multipleValue          indicazione se il content picker field permette di prendere uno o più valori
         * @param editingEnabled
         * @param defaultValue           valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                               di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                               <br>
         *                               (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         * @param orchardComponentModule informazioni per caricare i dati da Orchard, utile solo per gli i content Picker filed
         */
        public FieldInfo(
                @StringRes int name,
                @NonNull String orchardKey,
                @Nullable String dataKey,
                boolean required,
                boolean multipleValue,
                boolean editingEnabled,
                @Nullable Object defaultValue,
                @NonNull String orchardComponentModule,
                @Nullable SparseArray extras) {
            this(name, orchardKey, dataKey, FIELD_TYPE_CONTENT_PICKER, required, multipleValue, editingEnabled, defaultValue, null, orchardComponentModule, false, extras);
        }

        /**
         * Per creare una reference ad un ContentPickerField
         *
         * @param name                   nome del campo
         * @param orchardKey             chiave del campo assegnata in orchard
         * @param dataKey                nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                               I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                               Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param required               indicazione se è necessario compilare il campo prima di procedere
         * @param multipleValue          indicazione se il content picker field permette di prendere uno o più valori
         * @param editingEnabled
         * @param defaultValue           valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                               di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                               <br>
         *                               (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         * @param orchardComponentModule informazioni per caricare i dati da Orchard, utile solo per gli i content Picker filed
         * @param isLoginEnabled         true if the content of the picker can be available only for logged users.
         */
        public FieldInfo(
                @StringRes int name,
                @NonNull String orchardKey,
                @Nullable String dataKey,
                boolean required,
                boolean multipleValue,
                boolean editingEnabled,
                @Nullable Object defaultValue,
                @NonNull String orchardComponentModule,
                boolean isLoginEnabled,
                @Nullable SparseArray extras) {
            this(name, orchardKey, dataKey, FIELD_TYPE_CONTENT_PICKER, required, multipleValue, editingEnabled, defaultValue, null, orchardComponentModule, isLoginEnabled, extras);
        }

        /**
         * @param name                            nome del campo
         * @param orchardKey                      chiave del campo assegnata in orchard
         * @param dataKey                         nome del campo da prendere dal database. Per prendere il nome del campo bisogna utilizzare i nomi dei metodi e non delle colonne.
         *                                        I nomi dei metodi devono essere privati del get iniziale. Se e' necessario accedere ad un campo di un sotto oggetto inserire il carattere . nel data key.
         *                                        Ad esempio sesso.value (accedera al campo sesso dell'oggetto, e poi prendera il campo value dell oggetto restituito da sesso)
         * @param type                            tipologia del campo. Non può essere utilizzata
         * @param required                        indicazione se è necessario compilare il campo prima di procedere
         * @param editingEnabled
         * @param defaultValue                    valore di default del campo (di tipo String per esempio nel caso in cui il campo sia di tipo "text",
         *                                        di tipo Boolean nel caso in cui il campo sia di tipo "boolean"
         *                                        <br>
         *                                        (valido solo per campi diversi da FIELD_TYPE_ENUM_OR_TERM_SELECTION)
         * @param fieldInfoValidator              validator da applicare al field al momento del check
         * @param bundleForOrchardComponentModule bundle informazioni per caricare i dati da Orchard, utile solo per gli i content Picker filed
         */
        private FieldInfo(@StringRes int name,
                          @NonNull String orchardKey,
                          @Nullable String dataKey,
                          @FieldType int type,
                          boolean required,
                          boolean multipleSelection,
                          boolean editingEnabled,
                          @Nullable Object defaultValue,
                          @Nullable FieldInfoValidator fieldInfoValidator,
                          @Nullable String bundleForOrchardComponentModule,
                          boolean isLoginEnabled,
                          @Nullable SparseArray extras) {

            this.name = name;
            this.orchardKey = orchardKey;
            this.dataKey = dataKey;
            this.type = type;
            this.required = required;
            this.editingEnabled = editingEnabled;
            this.extras = extras;
            this.defaultValue = defaultValue;
            this.multipleSelection = multipleSelection;
            this.orchardComponentModule = bundleForOrchardComponentModule;
            this.isLoginEnabled = isLoginEnabled;

            if (fieldInfoValidator != null)
                addFieldInfoValidator(fieldInfoValidator);

            if (type == FIELD_TYPE_BOOLEAN && required) {
                addFieldInfoValidator(new FieldInfoBoolValidator());
            }
        }

        public int getName() {
            return name;
        }

        public String getOrchardKey() {
            return orchardKey;
        }

        public
        @FieldType
        int getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public String getDataKey() {
            return dataKey;
        }

        public SparseArray getExtras() {
            return extras;
        }

        public ArrayList<FieldInfoValidator> getFieldInfoValidators() {
            return fieldInfoValidators;
        }

        public void addFieldInfoValidator(FieldInfoValidator fieldInfoValidator) {
            if (fieldInfoValidator == null) {
                throw new RuntimeException("FieldValidator must not be null");
            }
            fieldInfoValidators.add(fieldInfoValidator);
        }

        public String getOrchardComponentModule() {
            return orchardComponentModule;
        }

        public boolean isLoginEnabled() {
            return isLoginEnabled;
        }

        public boolean isMultipleSelection() {
            return multipleSelection;
        }

        public boolean isEditingEnabled() {
            return editingEnabled;
        }
    }
}
