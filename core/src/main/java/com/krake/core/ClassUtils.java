package com.krake.core;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import com.krake.core.model.RecordWithIdentifier;
import com.krake.core.model.RecordWithStringIdentifier;
import com.krake.core.util.StringWithRealmNameKt;
import io.realm.RealmModel;
import io.realm.RealmObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

/**
 * Utility per la gestione delle classi
 * Created by joel on 24/07/14.
 */
public class ClassUtils {
    private static ArrayMap<String, Class> mCachedClasses = new ArrayMap<>();
    private static ArrayMap<String, Method> mCachedMethod = new ArrayMap<>();

    private static String baseDataClassPackage;

    private ClassUtils() {
        // avoid instantiation
    }

    public static void init(Context context) {
        String packageName = context.getPackageName();
        if (!context.getString(R.string.app_model_base_package).isEmpty()) {
            packageName = context.getString(R.string.app_model_base_package);
        }

        baseDataClassPackage = String.format("%s.%s.",
                packageName,
                Constants.OGL_SUB_PACKAGE);
    }

    /**
     * Ottiene una classe dei dati di orchard a partire dal simple name.
     * La classe è cercata nel subpackage di utilizzato da generatore

     * @param simpleName    simple name
     * @return
     */
    static public Class<RealmModel> dataClassForName(String simpleName) {

        simpleName = StringWithRealmNameKt.realmCleanClassName(simpleName);

        if (simpleName.contains("."))
            simpleName = simpleName.substring(simpleName.lastIndexOf(".") + 1);

        //END retro


        Class cachedClass = mCachedClasses.get(simpleName);

        if (cachedClass != null)
            return cachedClass;

        try {
            cachedClass = Class.forName(baseDataClassPackage + simpleName);

            mCachedClasses.put(simpleName, cachedClass);
            return cachedClass;
        } catch (Exception ignored) {
        }
        return null;
    }

    static protected Object instantiateObjectOfClass(Class targetClass) {
        try {
            Constructor constructor = targetClass.getConstructor();

            return constructor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    static public Object instantiateObjectOfDataClass(String simpleName) {
        try {
            return instantiateObjectOfClass(dataClassForName(simpleName));
        } catch (Exception e) {
            return null;
        }
    }

    static public void setValueInDestination(String setterValue, Object value, Object destination) {
        if (destination != null) {
            try {
                String keyName = destination.getClass().getName() + "/" + setterValue;
                Method method = mCachedMethod.get(keyName);

                if (method == null) {
                    if (value != null) {
                        try {
                            method = destination.getClass().getMethod(setterValue, value.getClass());
                        } catch (Exception ignored) {
                        }
                    }

                    if (method == null) {
                        Method[] methods = destination.getClass().getMethods();
                        for (int i = 0; i < methods.length; ++i) {
                            Method loopMethod = methods[i];
                            if (loopMethod.getName().equals(setterValue)) {
                                method = loopMethod;
                            }
                        }
                    }
                    if (method != null)
                        mCachedMethod.put(keyName, method);
                }
                if (method != null) {

                    if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Double.class) && !(value instanceof Double)) {
                        value = Double.valueOf(value.toString());
                    } else if (method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(Long.class) && !(value instanceof Long)) {
                        value = Long.valueOf(value.toString());
                    }
                    if (value instanceof RealmModel && !(value instanceof RecordWithIdentifier) && !(value instanceof RecordWithStringIdentifier))
                        deleteOldValue(setterValue, destination, value);

                    method.invoke(destination, value);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static void deleteOldValue(String setterValue, Object destination, Object newValue) {
        Object old = getValueInDestination(setterValue.replaceFirst("set", "get"), destination);

        if (old != null && old != newValue && old instanceof RealmObject) {
            ((RealmObject) old).deleteFromRealm();
        }
    }

    static public Object getValueInDestination(String getterName, Object destination) {

        try {
            String keyName = destination.getClass().getName() + "/" + getterName;
            Method method = mCachedMethod.get(keyName);

            if (method == null) {
                method = destination.getClass().getMethod(getterName);
                mCachedMethod.put(getterName, method);
            }


            return method.invoke(destination);
        } catch (Exception ignored) {
        }

        return null;
    }

    static public Object getValueForKeyPath(String keyPath, Object destination) {

        try {
            StringTokenizer tokenizer = new StringTokenizer(keyPath, ".");
            Object value = destination;
            while (tokenizer.hasMoreTokens()) {
                String key = tokenizer.nextToken();
                value = getValueInDestination(StringUtils.methodName(null, key, null, StringUtils.MethodType.GETTER), value);

            }

            return value;
        } catch (Exception ignored) {
        }

        return null;
    }

    @Nullable
    public static <T> Class<T> fromString(@NonNull String s) {
        Class<T> cls;
        try {
            //noinspection unchecked
            cls = (Class<T>) Class.forName(s);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            cls = null;
        }
        return cls;
    }
}
