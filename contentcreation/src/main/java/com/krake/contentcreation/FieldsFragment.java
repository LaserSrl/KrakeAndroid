package com.krake.contentcreation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.krake.core.ClassUtils;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.model.RecordWithStringIdentifier;
import com.krake.core.widget.EditTextExtensionTextInputLayoutKt;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Fragment per caricare dati in input di tipo diverso per i nuovi contenuti.
 * Attualmente sono supportati i tipi di campo indicati in {@link ContentCreationTabInfo.FieldType}
 * L'activity che contiene questo fragment deve estendere la classe {@link ContentCreationActivity}
 */
public class FieldsFragment extends Fragment implements
        ContentCreationFragment, EnumOrTermSpinnerManager.SelectionListener, MultiEnumOrTermManager.MultiSelectionListener, KeyDatePickerFragment.OnKeyDatePickerListener {

    public static final String ENUM_INFO_SETTING = "Setting";
    public static final String ENUM_INFO_VALUES = "Values";
    public static final String ENUM_INFO_SINGLE_CHOICE = "SingleChoice";

    private ArrayMap<String, Object> mKeyRowMap = new ArrayMap<>();
    private ArrayMap<String, DateFormat> mDateFormatForKey = new ArrayMap<>();
    private ContentCreationActivity mActivity;

    private FieldInfos mSaveFieldInfos;

    @SuppressLint("SimpleDateFormat")
    private DateFormat mOrchardDateFormat = new SimpleDateFormat(ContentCreationUtils.CONTENT_CREATION_DATE_PATTERN);

    public FieldsFragment() {
        // Required empty public constructor
    }

    public static FieldsFragment newInstance() {
        return new FieldsFragment();
    }

    /**
     * Verifica che l'inputType sia di tipo uguale a uno dei tre tipi numerici o a una loro combinazione
     *
     * @param inputType inputType passato come extra
     * @return true se l'inputType è di tipo numerico
     */
    private static boolean numberInputType(int inputType) {

        return inputType == InputType.TYPE_CLASS_NUMBER ||
                inputType == InputType.TYPE_NUMBER_FLAG_SIGNED ||
                inputType == InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }

    /**
     * Verifica che un Object sia un Integer
     *
     * @param object oggetto da verificare
     * @return true se è un intero
     */
    private static boolean objectIsInteger(@NonNull Object object) {
        // il check sul resto ritorna true se un double può essere considerato come intero
        return object instanceof Number && ((Number) object).doubleValue() % 1 == 0;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mActivity = (ContentCreationActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ContentCreationTabInfo.ContentFieldsInfos fieldInfos = (ContentCreationTabInfo.ContentFieldsInfos) mActivity.getFragmentCreationInfo(this);
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup) inflater.inflate(fieldInfos.getFields().size() > 1 ? R.layout.fragment_fields : R.layout.fragment_fields_single, container, false);

        LinearLayout linearContainer = (LinearLayout) view.findViewById(R.id.fieldsCreationContainer);
        if (linearContainer == null)
            linearContainer = (LinearLayout) view;
        mSaveFieldInfos = (FieldInfos) mActivity.getFragmentData(this);
        if (mSaveFieldInfos == null) {
            mSaveFieldInfos = new FieldInfos();
            Object originalObject;
            if ((originalObject = mActivity.getOriginalObject()) != null) {
                for (ContentCreationTabInfo.FieldInfo fieldInfo : fieldInfos.getFields()) {
                    final String dataKey = fieldInfo.getDataKey();
                    final String orchardKey = fieldInfo.getOrchardKey();

                    if (!TextUtils.isEmpty(dataKey)) {
                        Object originalValue = ClassUtils.getValueForKeyPath(dataKey, originalObject);

                        if (originalValue != null) {
                            if (fieldInfo.getType() == ContentCreationTabInfo.FIELD_TYPE_DATE) {
                                //Magico check per evitare la data iniziale impostata da Orchard al posto di null
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(1900, 1, 1);

                                if ((originalValue instanceof Date && ((Date) originalValue).compareTo(calendar.getTime()) >= 0) ||
                                        (originalObject instanceof String)) {
                                    mSaveFieldInfos.mFieldValues.put(orchardKey, originalValue);
                                }
                            } else if (fieldInfo.getType() != ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION && fieldInfo.getType() != ContentCreationTabInfo.FIELD_TYPE_CONTENT_PICKER)
                                mSaveFieldInfos.mFieldValues.put(orchardKey, originalValue);
                            else {
                                JsonArray selectedValues = new JsonArray();
                                if (originalValue instanceof String) {
                                    StringTokenizer tokenizer = new StringTokenizer(originalValue.toString(), ",");
                                    while (tokenizer.hasMoreTokens())
                                        selectedValues.add(new JsonPrimitive(tokenizer.nextToken()));
                                } else {
                                    List elements = (List) originalValue;
                                    for (Object term : elements) {
                                        if (term instanceof RecordWithIdentifier)
                                            selectedValues.add(((RecordWithIdentifier) term).getIdentifier());
                                        else if (term instanceof RecordWithStringIdentifier)
                                            selectedValues.add(((RecordWithStringIdentifier) term).getStringIdentifier());
                                    }
                                }
                                mSaveFieldInfos.mFieldValues.put(orchardKey, selectedValues);
                            }
                        }
                    }
                }
            }
            mActivity.updateFragmentData(this, mSaveFieldInfos);
        }

        for (ContentCreationTabInfo.FieldInfo fieldInfo : fieldInfos.getFields()) {
            final SparseArray extras = fieldInfo.getExtras();
            final Object defaultValue = fieldInfo.getDefaultValue();
            final String orchardKey = fieldInfo.getOrchardKey();

            switch (fieldInfo.getType()) {
                case ContentCreationTabInfo.FIELD_TYPE_TEXT: {
                    TextInputLayout textFieldContainer = (TextInputLayout) inflater.inflate(R.layout.creation_text_field, null);
                    textFieldContainer.setHint(getString(fieldInfo.getName()));

                    EditText editText = textFieldContainer.getEditText();
                    if (editText != null) {
                        boolean addMultiline = false;
                        Object lineNumber = extras == null ? 1 : extras.get(FieldExtras.Text.KEY_MAX_LINES, 1);
                        if (objectIsInteger(lineNumber)) {
                            int convertedLineNumber = ((Number) lineNumber).intValue();
                            if (convertedLineNumber == 1) {
                                editText.setHorizontallyScrolling(true);
                                editText.setSingleLine(true);
                            } else {
                                editText.setHorizontallyScrolling(false);
                                editText.setSingleLine(false);
                                editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
                                addMultiline = true;
                            }
                            editText.setMaxLines(convertedLineNumber);
                        } else {
                            throw new ClassCastException("The attribute FieldExtras.Text.KEY_MAX_LINES must be an int");
                        }

                        int inputType = InputType.TYPE_CLASS_TEXT;
                        if (addMultiline) {
                            inputType |= InputType.TYPE_TEXT_FLAG_MULTI_LINE;
                        }
                        Object extraVal;
                        if (extras != null && (extraVal = extras.get(FieldExtras.Text.KEY_INPUT_TYPE)) != null) {
                            if (objectIsInteger(extraVal)) {
                                inputType |= ((Number) extraVal).intValue();
                            } else {
                                throw new ClassCastException("The attribute FieldExtras.Text.KEY_INPUT_TYPE must be an int, check values from: " + InputType.class.getName());
                            }
                        }

                        editText.setInputType(inputType);

                        linearContainer.addView(textFieldContainer);
                        mKeyRowMap.put(orchardKey, textFieldContainer);

                        String value = (String) mSaveFieldInfos.mFieldValues.get(orchardKey);

                        if (!TextUtils.isEmpty(value)) {
                            editText.setText(value);
                        }

                        editText.addTextChangedListener(new TextWatcher(orchardKey));
                        if (TextUtils.isEmpty(value) && defaultValue != null && defaultValue instanceof String && !TextUtils.isEmpty((String) defaultValue)) {
                            textFieldContainer.getEditText().setText((String) defaultValue);
                        }

                        editText.setEnabled(fieldInfo.isEditingEnabled());
                        String errorsEmpty = (String) mSaveFieldInfos.mFieldsErrors.get(orchardKey);
                        if (!TextUtils.isEmpty(errorsEmpty)) {
                            EditTextExtensionTextInputLayoutKt.setErrorInThisOrInputLayout(editText, errorsEmpty);
                        }
                    }
                }
                break;

                case ContentCreationTabInfo.FIELD_TYPE_CONTENT_PICKER:
                case ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION:

                    boolean single;
                    JsonArray elements = null;

                    if (fieldInfo.getType() == ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION) {

                        JsonObject enumTermInfos = mActivity.getObjectEnumInfos().getAsJsonObject(orchardKey);
                        single = enumTermInfos.getAsJsonObject(ENUM_INFO_SETTING).get(ENUM_INFO_SINGLE_CHOICE).getAsBoolean();
                        elements = enumTermInfos.getAsJsonArray(ENUM_INFO_VALUES);
                    } else {
                        single = !fieldInfo.isMultipleSelection();
                    }
                    if (single) {
                        View textFieldContainer = inflater.inflate(R.layout.category_selection, null);

                        JsonArray values = (JsonArray) mSaveFieldInfos.mFieldValues.get(orchardKey);

                        EnumOrTermSpinnerManager spinnerManager = new EnumOrTermSpinnerManager(getActivity(),
                                this,
                                fieldInfo,
                                textFieldContainer,
                                this,
                                elements,
                                values);

                        linearContainer.addView(textFieldContainer);

                        mKeyRowMap.put(orchardKey, spinnerManager);
                    } else {
                        View textFieldContainer = inflater.inflate(R.layout.creation_multi_list_terms, null);

                        JsonArray values = (JsonArray) mSaveFieldInfos.mFieldValues.get(orchardKey);

                        MultiEnumOrTermManager spinnerManager = new MultiEnumOrTermManager(getActivity(),
                                this,
                                fieldInfo,
                                textFieldContainer,
                                this,
                                linearContainer != view,
                                elements,
                                values);

                        linearContainer.addView(textFieldContainer);

                        mKeyRowMap.put(orchardKey, spinnerManager);
                    }

                    break;

                case ContentCreationTabInfo.FIELD_TYPE_BOOLEAN:
                    SwitchCompat switchCompat = (SwitchCompat) inflater.inflate(R.layout.creation_boolean_field, null);
                    switchCompat.setText(fieldInfo.getName());
                    Boolean checked = (Boolean) mSaveFieldInfos.mFieldValues.get(orchardKey);
                    if (checked != null) {
                        switchCompat.setChecked(checked);
                    } else if (defaultValue != null && defaultValue instanceof Boolean) {
                        switchCompat.setChecked((Boolean) defaultValue);
                    }

                    switchCompat.setOnCheckedChangeListener(new OnFieldCheckedListener(orchardKey));

                    switchCompat.setEnabled(fieldInfo.isEditingEnabled());
                    linearContainer.addView(switchCompat);
                    mKeyRowMap.put(orchardKey, switchCompat);
                    break;

                case ContentCreationTabInfo.FIELD_TYPE_DATE:
                    View dateContainer = inflater.inflate(R.layout.creation_date_field, null);
                    TextView dateLabel = dateContainer.findViewById(R.id.creation_date_field_label);
                    TextView dateTextVal = dateContainer.findViewById(R.id.creation_date_field_value);
                    dateLabel.setText(fieldInfo.getName());

                    final String defaultFormat = "dd/MM/yyyy";
                    boolean enableTime;
                    DateFormat visualDateFormat;
                    if (extras != null) {
                        Object enableTimeExtra = extras.get(FieldExtras.Date.KEY_ENABLE_TIME, false);
                        if (enableTimeExtra instanceof Boolean) {
                            enableTime = (boolean) enableTimeExtra;
                        } else {
                            throw new ClassCastException("The attribute FieldExtras.Date.KEY_ENABLE_TIME must be a boolean");
                        }

                        Object dateFormat = extras.get(FieldExtras.Date.KEY_DATE_FORMAT, enableTime ? defaultFormat + " - HH:mm" : defaultFormat);
                        if (dateFormat instanceof String) {
                            visualDateFormat = new SimpleDateFormat((String) dateFormat, Locale.US);
                        } else {
                            throw new ClassCastException("The attribute FieldExtras.Date.KEY_DATE_FORMAT must be a String representing the date format e.g. dd/MM/yyyy");
                        }
                    } else {
                        visualDateFormat = new SimpleDateFormat(defaultFormat, Locale.US);
                        enableTime = false;
                    }

                    Date currentDate = null;

                    Object oldDate = mSaveFieldInfos.mFieldValues.get(orchardKey);
                    if (oldDate != null) {
                        if (oldDate instanceof String) {
                            try {
                                mSaveFieldInfos.mFieldValues.put(orchardKey, mOrchardDateFormat.parse((String) oldDate));
                                oldDate = mSaveFieldInfos.mFieldValues.get(orchardKey);
                            } catch (ParseException ignored) {
                                // parse ignored
                            }
                        }
                    }

                    if (oldDate instanceof Date) {
                        currentDate = (Date) oldDate;
                    } else if (defaultValue instanceof Date) {
                        currentDate = (Date) defaultValue;
                    }

                    if (currentDate != null) {
                        dateTextVal.setText(visualDateFormat.format(currentDate));
                    } else {
                        currentDate = new Date();
                    }

                    String errorsEmpty = (String) mSaveFieldInfos.mFieldsErrors.get(orchardKey);
                    if (!TextUtils.isEmpty(errorsEmpty)) {
                        dateTextVal.setTextColor(ContextCompat.getColor(mActivity, R.color.field_text_color_error));
                        dateTextVal.setText(errorsEmpty);
                    }

                    if (fieldInfo.isEditingEnabled())
                        dateContainer.findViewById(R.id.creation_date_field_button).setOnClickListener(new OnDateClickListener(orchardKey, currentDate, enableTime));
                    else
                        dateContainer.findViewById(R.id.creation_date_field_button).setEnabled(false);

                    linearContainer.addView(dateContainer);
                    mKeyRowMap.put(orchardKey, dateTextVal);
                    mDateFormatForKey.put(orchardKey, visualDateFormat);
                    break;
            }
        }

        return view;
    }

    @Override
    public void onSingleTermOrEnumSelected(EnumOrTermSpinnerManager spinnerManager, Object selectedValue) {
        mSaveFieldInfos.mFieldValues.put(spinnerManager.getFieldKey(), spinnerManager.getServerObjectToCreate());
        mActivity.updateFragmentData(this, mSaveFieldInfos);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SwitchIntDef")
    @Override
    public boolean validateDataAndSaveError(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @Nullable Object savedInfos) {
        boolean valid = true;

        FieldInfos fieldInfos = savedInfos != null ? (FieldInfos) savedInfos : new FieldInfos();
        ContentCreationTabInfo.ContentFieldsInfos mContentFields = (ContentCreationTabInfo.ContentFieldsInfos) creationInfo;

        for (ContentCreationTabInfo.FieldInfo fieldInfo : mContentFields.getFields()) {
            final String orchardKey = fieldInfo.getOrchardKey();
            final Object rowObj = mKeyRowMap.get(orchardKey);

            String error = null;

            switch (fieldInfo.getType()) {
                case ContentCreationTabInfo.FIELD_TYPE_CONTENT_PICKER:
                case ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION:
                    JsonObject enumTermInfos = activity.getObjectEnumInfos().getAsJsonObject(orchardKey);

                    JsonArray values = (JsonArray) fieldInfos.mFieldValues.get(orchardKey);
                    if (fieldInfo.isRequired() || (fieldInfo.getType() == ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION &&
                            enumTermInfos.getAsJsonObject(ENUM_INFO_SETTING).get("Required").getAsBoolean())) {
                        if (values == null || values.size() == 0) {
                            valid = false;
                            error = activity.getString(com.krake.contentcreation.R.string.error_missing_required_field);
                        }
                    }

                    if (valid && values != null && values.size() > 0) {
                        ContentCreationTabInfo.FieldInfoValidator.Result fieldInfoValidatorResult = validateField(activity, fieldInfo, values);
                        if (fieldInfoValidatorResult != null) {
                            valid = false;
                            error = fieldInfoValidatorResult.getErrorMessage();
                        }
                    }

                    if (TextUtils.isEmpty(error))
                        fieldInfos.mFieldsErrors.remove(orchardKey);
                    else
                        fieldInfos.mFieldsErrors.put(orchardKey, error);

                    break;

                case ContentCreationTabInfo.FIELD_TYPE_TEXT:
                    TextInputLayout textInputLayout = (TextInputLayout) rowObj;

                    String text = (String) fieldInfos.mFieldValues.get(orchardKey);
                    final SparseArray extras = fieldInfo.getExtras();
                    Object inputType;
                    if (TextUtils.isEmpty(text)) {
                        if (fieldInfo.isRequired()) {
                            error = activity.getString(R.string.error_missing_required_field);
                            valid = false;
                        }
                    } else if (extras != null
                            && objectIsInteger(inputType = extras.get(FieldExtras.Text.KEY_INPUT_TYPE, InputType.TYPE_NULL))
                            && numberInputType(((Number) inputType).intValue())) {
                        try {
                            NumberFormat.getNumberInstance(Locale.US).parse(text);
                        } catch (ParseException e) {
                            error = activity.getString(R.string.error_invalid_number);
                            valid = false;
                        }
                    }

                    if (valid && !TextUtils.isEmpty(text)) {
                        ContentCreationTabInfo.FieldInfoValidator.Result fieldInfoValidatorResult = validateField(activity, fieldInfo, text);
                        if (fieldInfoValidatorResult != null) {
                            valid = false;
                            error = fieldInfoValidatorResult.getErrorMessage();
                        }
                    }

                    if (TextUtils.isEmpty(error))
                        fieldInfos.mFieldsErrors.remove(orchardKey);
                    else
                        fieldInfos.mFieldsErrors.put(orchardKey, error);

                    if (textInputLayout != null)
                        textInputLayout.setError(error);

                    break;

                case ContentCreationTabInfo.FIELD_TYPE_DATE:
                    TextView dateTextVal = (TextView) rowObj;

                    Date date = (Date) fieldInfos.mFieldValues.get(orchardKey);
                    if (date == null) {
                        if (fieldInfo.isRequired()) {
                            error = activity.getString(R.string.error_missing_required_field_short);
                            valid = false;
                        }
                    }

                    if (valid && date != null) {
                        ContentCreationTabInfo.FieldInfoValidator.Result fieldInfoValidatorResult = validateField(activity, fieldInfo, date);
                        if (fieldInfoValidatorResult != null) {
                            valid = false;
                            error = fieldInfoValidatorResult.getErrorMessage();
                        }
                    }

                    if (TextUtils.isEmpty(error)) {
                        fieldInfos.mFieldsErrors.remove(orchardKey);
                        if (dateTextVal != null) {
                            dateTextVal.setTextColor(ContextCompat.getColor(activity, R.color.field_text_color));
                        }
                    } else {
                        fieldInfos.mFieldsErrors.put(orchardKey, error);
                        if (dateTextVal != null) {
                            dateTextVal.setTextColor(ContextCompat.getColor(activity, R.color.field_text_color_error));
                            dateTextVal.setText(error);
                        }
                    }

                    break;
                case ContentCreationTabInfo.FIELD_TYPE_BOOLEAN:
                    Boolean oBool = (Boolean) fieldInfos.mFieldValues.get(orchardKey);

                    if (oBool != null) {
                        ContentCreationTabInfo.FieldInfoValidator.Result fieldInfoValidatorResult = validateField(activity, fieldInfo, oBool);
                        if (fieldInfoValidatorResult != null) {
                            valid = false;
                            error = fieldInfoValidatorResult.getErrorMessage();
                        }
                    }

                    //TODO visualizzare errori per boolean
                    if (TextUtils.isEmpty(error)) {
                        fieldInfos.mFieldsErrors.remove(orchardKey);
                    } else {
                        fieldInfos.mFieldsErrors.put(orchardKey, error);
                    }

                    break;
            }

        }

        activity.updateFragmentData(this, savedInfos);
        return valid;
    }

    private ContentCreationTabInfo.FieldInfoValidator.Result validateField(Activity activity, ContentCreationTabInfo.FieldInfo fieldInfo, Object objectToValidate) {
        for (ContentCreationTabInfo.FieldInfoValidator validator : fieldInfo.getFieldInfoValidators()) {
            ContentCreationTabInfo.FieldInfoValidator.Result fieldInfoValidatorResult = validator.validate(activity, objectToValidate);
            if (fieldInfoValidatorResult.getError()) {
                return fieldInfoValidatorResult;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean insertDataToUpload(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, Object savedInfos, @NonNull JsonObject parameters) {

        FieldInfos fieldInfos = savedInfos != null ? (FieldInfos) savedInfos : new FieldInfos();
        ContentCreationTabInfo.ContentFieldsInfos mContentFields = (ContentCreationTabInfo.ContentFieldsInfos) creationInfo;

        for (ContentCreationTabInfo.FieldInfo fieldInfo : mContentFields.getFields()) {
            String orchardKey = fieldInfo.getOrchardKey();

            switch (fieldInfo.getType()) {
                case ContentCreationTabInfo.FIELD_TYPE_TEXT:
                    String text = (String) fieldInfos.mFieldValues.get(orchardKey);

                    if (!TextUtils.isEmpty(text)) {
                        parameters.addProperty(orchardKey, text);
                    }
                    break;

                case ContentCreationTabInfo.FIELD_TYPE_CONTENT_PICKER:
                case ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION:
                    Object value = fieldInfos.mFieldValues.get(orchardKey);
                    if (value instanceof JsonElement)
                        parameters.add(orchardKey, (JsonElement) value);
                    break;

                case ContentCreationTabInfo.FIELD_TYPE_BOOLEAN:
                    Boolean checked = (Boolean) fieldInfos.mFieldValues.get(orchardKey);
                    parameters.addProperty(orchardKey, checked != null && checked ? 1 : 0);
                    break;

                case ContentCreationTabInfo.FIELD_TYPE_DATE:
                    Date pickedDate = (Date) fieldInfos.mFieldValues.get(orchardKey);
                    if (pickedDate != null) {
                        String formattedDate = mOrchardDateFormat.format(pickedDate);
                        parameters.addProperty(orchardKey, formattedDate);
                    } else {
                        parameters.add(orchardKey, JsonNull.INSTANCE);
                    }
                    break;
            }
        }

        return true;
    }

    @Override
    public Object deserializeSavedInstanceState(@NonNull ContentCreationActivity activity, @NonNull ContentCreationTabInfo.ContentCreationInfo creationInfo, @NonNull Gson gson, @Nullable String serializedInfos) {
        FieldInfos savedInfos = gson.fromJson(serializedInfos, FieldInfos.class);

        if (savedInfos.mFieldsErrors == null)
            savedInfos.mFieldsErrors = new ArrayMap<>();
        if (savedInfos.mFieldValues == null)
            savedInfos.mFieldValues = new ArrayMap<>();

        ContentCreationTabInfo.ContentFieldsInfos fieldsInfos = (ContentCreationTabInfo.ContentFieldsInfos) creationInfo;

        for (ContentCreationTabInfo.FieldInfo fieldInfo : fieldsInfos.getFields()) {
            if (fieldInfo.getType() == ContentCreationTabInfo.FIELD_TYPE_ENUM_OR_TERM_SELECTION || fieldInfo.getType() == ContentCreationTabInfo.FIELD_TYPE_CONTENT_PICKER) {
                final String orchardKey = fieldInfo.getOrchardKey();

                Object value = savedInfos.mFieldValues.get(orchardKey);
                if (value != null && !(value instanceof JsonElement)) {
                    String s = gson.toJson(value);
                    JsonElement element = new JsonParser().parse(s);
                    savedInfos.mFieldValues.put(orchardKey, element);
                }
            }
        }
        return savedInfos;
    }

    @Override
    public void onTermOrEnumSelected(MultiEnumOrTermManager spinnerManager, JsonArray selectedValues) {
        mSaveFieldInfos.mFieldValues.put(spinnerManager.getFieldKey(), selectedValues);
        mActivity.updateFragmentData(this, mSaveFieldInfos);
    }

    @Override
    public void onDatePicked(@NonNull String key, @NonNull Date date) {
        mSaveFieldInfos.mFieldValues.put(key, date);
        mActivity.updateFragmentData(FieldsFragment.this, mSaveFieldInfos);
        TextView dateTextVal = (TextView) mKeyRowMap.get(key);
        if (dateTextVal != null) {
            dateTextVal.setTextColor(ContextCompat.getColor(mActivity, R.color.field_text_color));
            dateTextVal.setText(mDateFormatForKey.get(key).format(date));
        }
    }

    class OnFieldCheckedListener implements CompoundButton.OnCheckedChangeListener {
        private final String key;

        OnFieldCheckedListener(String key) {
            this.key = key;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mSaveFieldInfos.mFieldValues.put(key, isChecked);
            mActivity.updateFragmentData(FieldsFragment.this, mSaveFieldInfos);
        }
    }

    /**
     * Listener con costruttore custom per il bottone che apre il date picker
     */
    class OnDateClickListener implements View.OnClickListener {
        private final String key;
        private final Date defaultDate;
        private final boolean enableTime;

        /**
         * Costruttore che istanzia un nuovo listener settando i parametri che verranno usati nell'onClick(View)
         *
         * @param key         orchardKey abbinata al campo
         * @param defaultDate data di default con la quale aprire il picker se nessuna data è stata inserita
         * @param enableTime  true nel caso in cui si voglia dare al picker la possibilità di selezionare anche ore e minuti
         */
        OnDateClickListener(@NonNull String key, @NonNull Date defaultDate, boolean enableTime) {
            this.key = key;
            this.defaultDate = defaultDate;
            this.enableTime = enableTime;
        }

        @Override
        public void onClick(View v) {
            Date currentDate = (Date) mSaveFieldInfos.mFieldValues.get(key);
            // nel caso in cui la data non sia stata inserita almeno una volta, viene usata quella di default
            if (currentDate == null) {
                currentDate = defaultDate;
            }
            KeyDatePickerFragment fragment = KeyDatePickerFragment.newInstance(key, currentDate, enableTime);
            fragment.show(getChildFragmentManager(), "Date");
            // setta il listener al Fragment
            fragment.setListener(FieldsFragment.this);
        }
    }

    class TextWatcher implements android.text.TextWatcher {
        private final String key;

        TextWatcher(String key) {
            this.key = key;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            mSaveFieldInfos.mFieldValues.put(key, editable.toString());
            mActivity.updateFragmentData(FieldsFragment.this, mSaveFieldInfos);
        }
    }

    class FieldInfos {
        private ArrayMap<String, Object> mFieldValues = new ArrayMap<>();

        private ArrayMap<String, Object> mFieldsErrors = new ArrayMap<>();
    }
}