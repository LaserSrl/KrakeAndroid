package com.krake.core.media;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation utilizzata per distinguere i vari tipi di MediaPart
 */
@IntDef(flag = true, value = {MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO})
@Retention(RetentionPolicy.SOURCE)
public @interface MediaType {
    int IMAGE = 1;
    int VIDEO = 1 << 1;
    int AUDIO = 1 << 2;
}