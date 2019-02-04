package com.krake.core;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Created by joel on 24/07/14.
 */
public class StringUtils {
    @SuppressLint("SimpleDateFormat")
    private static DateFormat mDateFormatter = new SimpleDateFormat("'#'MM/dd/yyyy HH:mm:ss'#'");
    private static NumberFormat mNumberFormat = NumberFormat.getInstance(Locale.US);
    private StringUtils() {
        // avoid instantiation
    }

    protected static Object convertValue(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = (JsonPrimitive) jsonElement;

            if (primitive.isBoolean())
                return primitive.getAsBoolean();
            else if (primitive.isNumber())
                try {
                    return mNumberFormat.parse(primitive.getAsString());

                } catch (Exception ignored) {
                }

            String string = primitive.getAsString();

            if (stringBeginsAndEndsWithCharacter(string,'#')) {
                try {
                    return mDateFormatter.parse(string);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return string;
        } else //if(jsonElement.isJsonNull())
            return null;
    }

    private static boolean stringBeginsAndEndsWithCharacter(String string, char character) {
        if (string.length() > 2) {
            return string.charAt(0) == character && string.charAt(string.length() - 1) == character;
        }
        return false;
    }

    static protected boolean matchesAnyOfPatterns(String string, List<Pattern> patternList) {
        for (Pattern pattern : patternList) {
            if (pattern.matcher(string).matches())
                return true;
        }
        return false;
    }

    static public String methodName(String basePath, String fieldName, Configurations configurations, MethodType method) {
        String mappedName = null;
        if (configurations != null && configurations.getSpecialNamesMapping().containsKey(fieldName))
            fieldName = configurations.getSpecialNamesMapping().get(fieldName);

        if (!TextUtils.isEmpty(basePath)) {
            mappedName = basePath + stringWithFirstLetterUppercase(fieldName);
        } else {
            mappedName = fieldName;
        }

        mappedName = stringWithFirstLetterUppercase(mappedName);

        String prefix;
        switch (method) {
            case GETTER:
                prefix = "get";
                break;

            case SETTER:
                prefix = "set";
                break;

            default:
                prefix = "";

        }

        return prefix + mappedName;
    }

    public static String stringWithFirstLetterUppercase(String original) {
        return original.toUpperCase(Locale.US).substring(0, 1) + original.substring(1);
    }

    public static String stringWithFirstLetterLowercase(String original) {
        return original.toLowerCase(Locale.US).substring(0, 1) + original.substring(1);
    }

    public enum MethodType {
        GETTER,
        SETTER,
        NAME
    }
}
