package com.krake.core.social;

import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;

import com.krake.core.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Parcelable model used to create an item in the detail toolbar
 */
@Deprecated
public class DetailToolbarItem {
    public static final int FACEBOOK = 1;
    public static final int TWITTER = 2;
    public static final int PINTEREST = 3;
    public static final int INSTANGRAM = 4;
    public static final int WEBSITE = 5;
    public static final int MAIL = 6;
    public static final int CALL = 7;

    private static final int CUSTOM = 0;
    final private String value;
    final private int type;
    @DrawableRes
    private int image;
    private int name;

    /**
     * @param image drawable resource of the icon
     * @param value   tag to handle click
     */
    public DetailToolbarItem(@StringRes int name, @DrawableRes int image, String value) {
        this.name = name;
        this.image = image;
        this.value = value;
        this.type = CUSTOM;
    }

    /**
     * @param type static final type with default implementation
     */
    public DetailToolbarItem(@Type int type, String value) {

        this.type = type;
        this.value = value;
        switch (type) {
            case FACEBOOK:
                this.image = R.drawable.ic_facebook;
                name = R.string.faceboook;
                break;
            case TWITTER:
                this.image = R.drawable.ic_twitter;
                name = R.string.twitter;
                break;
            case PINTEREST:
                this.image = R.drawable.ic_pinterest;
                name = R.string.pinterest;
                break;
            case INSTANGRAM:
                this.image = R.drawable.ic_instagram;
                name = R.string.instagram;
                break;
            case MAIL:
                this.image = R.drawable.ic_email;
                name = R.string.mail;
                break;
            case WEBSITE:
                this.image = R.drawable.ic_public;
                name = R.string.website;
                break;
            case CALL:
                this.image = R.drawable.ic_call;
                this.name = R.string.call;
                break;
        }


    }

    public int getImage() {
        return image;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }

    public int getName() {
        return name;
    }

    @IntDef({FACEBOOK, TWITTER, PINTEREST, INSTANGRAM, WEBSITE, MAIL, CALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }
}