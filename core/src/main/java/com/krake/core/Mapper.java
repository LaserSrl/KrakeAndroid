package com.krake.core;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.krake.core.cache.CacheManager;
import com.krake.core.data.DataMapper;
import com.krake.core.login.PrivacyException;
import com.krake.core.model.PolicyText;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.model.RecordWithStringIdentifier;
import com.krake.core.model.RequestCache;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmModel;
import io.realm.RealmObject;

import java.util.*;

/**
 * Classe per caricare i dati da Orchard.
 * Created by joel on 23/07/14.
 */
public class Mapper implements DataMapper {

    public static final String IDENTIFIER_ORCHARD_FIELD = "Id";
    public static final String STRING_IDENTIFIER_ORCHARD_FIELD = "Sid";

    private Configurations mConfigurations;

    /**
     * Istanzia un mapper sincrono con le configurazioni indicate
     *
     * @param context contesto
     */
    public Mapper(Context context) {
        mConfigurations = new Configurations(context);
    }

    public Configurations getConfigurations() {
        return mConfigurations;
    }

    /**
     * @param result     json da parsificare
     * @param e          eccezione generata
     * @param parameters parametri della chiamata
     * @return cacheName della RequestCache
     * @throws OrchardError errore legato ad Orchard
     */
    @Override
    final public String parseContentFromResult(final JsonObject result,
                                                  @Nullable Exception e,
                                                  boolean requestedPrivacy,
                                                  @Nullable Map<String, String> parameters)
            throws OrchardError {

        OrchardError resultException;
        Object parsedResult = null;
        RequestCache resultCache = null;
        Realm mRealm = Realm.getDefaultInstance();
        try {
            if ((resultException = OrchardError.createErrorFromResult(result)) == null) {

                mRealm.beginTransaction();
                parsedResult = parsedObject(result, null, null, mRealm);

                if (!objectContainsPrivacy(parsedResult, requestedPrivacy))
                    resultCache = saveObjectInCache(parsedResult, parameters, mRealm);

                mRealm.commitTransaction();


            }
        } catch (Exception e1) {
            mRealm.cancelTransaction();
            resultException = new OrchardError(e1);
        }

        if (parsedResult != null && objectContainsPrivacy(parsedResult, requestedPrivacy)) {
            List<PolicyText> policies = new ArrayList<>(((List) parsedResult).size());

            for (RealmObject policy : ((List<RealmObject>) parsedResult)) {
                policies.add((PolicyText) Realm.getDefaultInstance().copyFromRealm(policy));
            }

            resultException = new OrchardError(new PrivacyException(policies));
        }

        if (resultCache != null) {
            return resultCache.getCacheName();
        } else
            throw resultException;
    }

    private @Nullable
    RequestCache saveObjectInCache(Object parsedResult,
                                   Map<String, String> parameters,
                                   Realm mRealm) {
        String cacheName = CacheManager.Companion.getShared().getCacheKey(parameters.get(Constants.REQUEST_DISPLAY_PATH_KEY), parameters);

        if ((parsedResult instanceof List || parsedResult instanceof RealmModel)) {
            Class requestClass = ClassUtils.dataClassForName(RequestCache.class.getSimpleName());

            RequestCache cacheEntry = RequestCache.Companion.findCacheWith(cacheName);

            if (cacheEntry == null) {
                cacheEntry = (RequestCache) ClassUtils.instantiateObjectOfClass(requestClass);
                cacheEntry.setCacheName(cacheName);
                cacheEntry = mRealm.copyToRealmOrUpdate(cacheEntry);
            } else {
                if (!parametersContainsFollowingPage(parameters))
                    cacheEntry.clearAllLists();
            }

            cacheEntry.setExtras(new HashMap<>(parameters));
            cacheEntry.setDateExecuted(new Date());

            if (parsedResult instanceof List) {
                cacheEntry.addAll((List<RealmModel>) parsedResult);
            } else {
                cacheEntry.add((RealmModel) parsedResult);
            }

            return cacheEntry;
        }
        return null;
    }

    private boolean objectContainsPrivacy(Object parsedResult, boolean requestedPrivacy) {

        if (!requestedPrivacy) {
            if (parsedResult instanceof List && ((List) parsedResult).size() > 0) {
                Object object = ((List) parsedResult).get(0);
                return object instanceof PolicyText;
            }
        }
        return false;
    }

    private Object parsedObject(JsonObject sourceObject, Object destination, String baseKeyPath, Realm mRealm) {
        String keyName = sourceObject.get(Constants.RESPONSE_NAME_KEY).getAsString();

        if (!getConfigurations().getGarbageParts().contains(keyName)) {
            Object value = StringUtils.convertValue(sourceObject.get(Constants.RESPONSE_VALUE_KEY));

            JsonArray model = sourceObject.getAsJsonArray(Constants.RESPONSE_MODEL_KEY);

            JsonArray lists = sourceObject.getAsJsonArray(Constants.RESPONSE_LIST_KEY);

            JsonObject usefullList = null;

            if (lists != null) {
                usefullList = getJsonObject(lists);
            }

            String contentType = (String) getObjectInModel(model, Constants.RESPONSE_CONTENT_TYPE_KEY);

            if (contentType == null && value != null) {
                contentType = value.toString();
            }

            if (model != null && model.size() > 0) {

                if (contentType == null || (usefullList == null && !StringUtils.matchesAnyOfPatterns(contentType, getConfigurations().getMultipleValuesKeyRegex()))) {

                    if (existsClassForName(contentType)) {
                        contentType = StringUtils.stringWithFirstLetterUppercase(contentType);
                        RealmModel newDestination = null;

                        Class<RealmModel> destinationClass = ClassUtils.dataClassForName(contentType);

                        Object parsedIdentifier = getObjectInModel(model, IDENTIFIER_ORCHARD_FIELD);

                        String parsedStringId = (String) getObjectInModel(model, STRING_IDENTIFIER_ORCHARD_FIELD);

                        Long identifier = null;

                        if (parsedIdentifier != null) {
                            if (parsedIdentifier instanceof Long)
                                identifier = (Long) parsedIdentifier;
                            else //if(parsedValue instanceof Double)
                                identifier = ((Double) parsedIdentifier).longValue();
                        }


                        if (parsedStringId != null && RecordWithStringIdentifier.class.isAssignableFrom(destinationClass)) {
                            newDestination = mRealm.where(destinationClass).equalTo(RecordWithStringIdentifier.StringIdentifierFieldName, parsedStringId).findFirst();
                        } else if (identifier != null && RecordWithIdentifier.class.isAssignableFrom(destinationClass)) {
                            newDestination = mRealm.where(destinationClass).equalTo(RecordWithIdentifier.IdentifierFieldName, identifier).findFirst();
                        }

                        if (newDestination == null) {
                            newDestination = (RealmModel) ClassUtils.instantiateObjectOfClass(destinationClass);
                            if (identifier != null && TextUtils.isEmpty(parsedStringId)) {
                                ClassUtils.setValueInDestination(StringUtils.methodName(null, RecordWithIdentifier.IdentifierFieldName, getConfigurations(), StringUtils.MethodType.SETTER), identifier, newDestination);
                            }
                            if (parsedStringId != null) {
                                ClassUtils.setValueInDestination(StringUtils.methodName(null, RecordWithStringIdentifier.StringIdentifierFieldName, getConfigurations(), StringUtils.MethodType.SETTER), parsedStringId, newDestination);
                            }

                            if (identifier == null && RecordWithIdentifier.class.isAssignableFrom(destinationClass)) {
                                ClassUtils.setValueInDestination(StringUtils.methodName(null, RecordWithIdentifier.IdentifierFieldName, getConfigurations(), StringUtils.MethodType.SETTER), new Date().getTime(), newDestination);
                            }

                            if (newDestination instanceof RecordWithIdentifier || newDestination instanceof RecordWithStringIdentifier)
                                newDestination = mRealm.copyToRealmOrUpdate(newDestination);
                            else
                                newDestination = mRealm.copyToRealm(newDestination);
                        }

                        importAllValuesInModel(model, newDestination, null, mRealm);

                        if (destination != null) {
                            String setterName = StringUtils.methodName(baseKeyPath, keyName, getConfigurations(), StringUtils.MethodType.SETTER);
                            ClassUtils.setValueInDestination(setterName, newDestination, destination);
                        }
                        return newDestination;
                    } else {
                        if (destination != null) {
                            String newBaseKeyPath;

                            if (value.equals(Constants.CONTENT_TYPE_CONTENT_PART)) {
                                newBaseKeyPath = baseKeyPath;
                            } else {
                                newBaseKeyPath = StringUtils.methodName(baseKeyPath, keyName, getConfigurations(), StringUtils.MethodType.NAME);
                            }

                            importAllValuesInModel(model, destination, newBaseKeyPath, mRealm);
                        } else
                            return null;
                    }

                } else {
                    if (usefullList == null) {
                        if (destination != null) {
                            importAllSubItemsFromModel(model, destination, baseKeyPath, keyName, mRealm);
                        }
                    } else {
                        return extractProjectionData(usefullList, mRealm);
                    }
                }
            } else {
                if (contentType == null || !StringUtils.matchesAnyOfPatterns(contentType, getConfigurations().getMultipleValuesKeyRegex())) {
                    if (!keyName.equals(IDENTIFIER_ORCHARD_FIELD) || !(destination instanceof RecordWithStringIdentifier) || !TextUtils.isEmpty(baseKeyPath)) {
                        ClassUtils.setValueInDestination(StringUtils.methodName(baseKeyPath,
                                keyName, getConfigurations(),
                                StringUtils.MethodType.SETTER),
                                value,
                                destination);
                    }

                } else if (StringUtils.matchesAnyOfPatterns(contentType, getConfigurations().getMultipleValuesKeyRegex())) {
                    String listMethodName = StringUtils.methodName(baseKeyPath, keyName, getConfigurations(), StringUtils.MethodType.NAME);

                    if (getConfigurations().getGeneratedClassFields(destination.getClass().getSimpleName()).contains(listMethodName)) {

                        List<RealmObject> elements = (List<RealmObject>) ClassUtils.getValueInDestination(StringUtils.methodName(baseKeyPath, keyName, getConfigurations(), StringUtils.MethodType.GETTER), destination);

                        deleteAndResetList(elements);
                    }
                }
            }
        }
        return null;
    }

    private JsonObject getJsonObject(JsonArray lists) {
        for (int listIndex = 0; listIndex < lists.size(); ++listIndex) {
            JsonObject listObject = lists.get(listIndex).getAsJsonObject();

            if (!listObject.get(Constants.RESPONSE_NAME_KEY).getAsString().equals(Constants.CONTENT_TYPE_WIDGET_LIST)) {
                return listObject;
            }
        }
        return null;
    }

    private Object extractProjectionData(JsonObject usefullList, Realm mRealm) {
        List<Object> projectionList = new LinkedList<>();

        JsonArray listContents = usefullList.getAsJsonArray(Constants.RESPONSE_MODEL_KEY);

        if (listContents != null) {
            for (int i = 0; i < listContents.size(); ++i) {
                projectionList.add(parsedObject(listContents.get(i).getAsJsonObject(), null, null, mRealm));
            }
        }
        return projectionList;
    }

    private boolean existsClassForName(String contentType) {
        return contentType != null && getConfigurations().getContainsGeneratedClass(StringUtils.stringWithFirstLetterUppercase(contentType));
    }

    private Object getObjectInModel(JsonArray model, String key) {
        if (model != null) {
            for (int i = 0; i < model.size(); ++i) {
                JsonObject object = model.get(i).getAsJsonObject();

                if (object.get(Constants.RESPONSE_NAME_KEY).getAsString().equals(key))
                    return StringUtils.convertValue(object.get(Constants.RESPONSE_VALUE_KEY));
            }
        }
        return null;
    }

    private void importAllValuesInModel(JsonArray model, Object destination, String baseKeypath, Realm mRealm) {
        for (int i = 0; i < model.size(); ++i) {
            parsedObject(model.get(i).getAsJsonObject(), destination, baseKeypath, mRealm);
        }
    }

    private void importAllSubItemsFromModel(JsonArray model, Object destination, String baseKeyPath, String name, Realm mRealm) {
        String listMethodName = StringUtils.methodName(baseKeyPath, name, getConfigurations(), StringUtils.MethodType.NAME);

        if (getConfigurations().getGeneratedClassFields(destination.getClass().getSimpleName()).contains(listMethodName)) {

            List<RealmObject> elements = (List<RealmObject>) ClassUtils.getValueInDestination(
                    StringUtils.methodName(baseKeyPath, name, getConfigurations(), StringUtils.MethodType.GETTER),
                    destination);

            deleteAndResetList(elements);

            for (int i = 0; i < model.size(); ++i) {
                Object parsed = parsedObject(model.get(i).getAsJsonObject(), null, null, mRealm);

                if (parsed != null) {
                    addParsedObjectInList(destination, baseKeyPath, name, parsed);
                }
            }
        } else {
            loadSubItemsOfOptimizedArrayFromModel(model, destination, baseKeyPath, name, mRealm);
        }
    }

    private void loadSubItemsOfOptimizedArrayFromModel(JsonArray model, Object destination, String baseKeyPath, String name, Realm mRealm) {
        String methodName = StringUtils.methodName(baseKeyPath, name, getConfigurations(), StringUtils.MethodType.NAME);
        List<String> listsReseted = new LinkedList<>();

        for (int modelIndex = 0; modelIndex < model.size(); ++modelIndex) {
            JsonObject subItem = model.get(modelIndex).getAsJsonObject();

            JsonArray subItemModel = subItem.getAsJsonArray(Constants.RESPONSE_MODEL_KEY);

            if (subItemModel != null) {
                for (int subItemModelIndex = 0; subItemModelIndex < subItemModel.size(); ++subItemModelIndex) {
                    JsonObject subSubItem = subItemModel.get(subItemModelIndex).getAsJsonObject();

                    if (subSubItem.get(Constants.RESPONSE_NAME_KEY) != null) {
                        String newName = subSubItem.get(Constants.RESPONSE_NAME_KEY).getAsString();

                        String subListGetter = StringUtils.methodName(methodName, newName, getConfigurations(), StringUtils.MethodType.GETTER);

                        if (getConfigurations().getGeneratedClassFields(
                                destination.getClass().getSimpleName()).contains(StringUtils.methodName(methodName, newName, getConfigurations(),
                                StringUtils.MethodType.NAME))) {

                            List<RealmObject> elements = (List<RealmObject>) ClassUtils.getValueInDestination(subListGetter, destination);

                            if (!listsReseted.contains(subListGetter)) {
                                deleteAndResetList(elements);
                                listsReseted.add(subListGetter);
                            }

                            Object importedValue = parsedObject(subSubItem, null, null, mRealm);

                            if (importedValue != null) {
                                addParsedObjectInList(destination, methodName, newName, importedValue);
                            }
                        }
                    }
                }
            }
        }
    }

    private void addParsedObjectInList(Object destination, String baseKeyPath, String name, Object parsed) {

        RealmList<RealmModel> elements = (RealmList<RealmModel>) ClassUtils.getValueInDestination(StringUtils.methodName(baseKeyPath,
                name, getConfigurations(),
                StringUtils.MethodType.GETTER), destination);
        elements.add((RealmModel) parsed);
    }

    private void deleteAndResetList(List<RealmObject> elements) {

        if (elements != null) {
            List<RealmObject> objects = new ArrayList<>(elements);
            for (RealmObject element : objects) {
                if (!(element instanceof RecordWithIdentifier) && !(element instanceof RecordWithStringIdentifier))
                    element.deleteFromRealm();
            }

            elements.clear();
        }
    }

    private boolean parametersContainsFollowingPage(Map<String, String> parameters) {

        if (parameters.containsKey(Constants.REQUEST_PAGE_KEY)) {
            Integer page = Integer.valueOf(parameters.get(Constants.REQUEST_PAGE_KEY));
            return page > 1;
        }

        return false;
    }
}
