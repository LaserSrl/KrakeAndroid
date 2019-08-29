package com.krake.contentcreation.validator

import android.content.Context
import com.krake.contentcreation.ContentCreationTabInfo
import com.krake.contentcreation.R

class FieldInfoBoolValidator : ContentCreationTabInfo.FieldInfoValidator<Boolean> {
    override fun validate(
        context: Context,
        field: Boolean
    ): ContentCreationTabInfo.FieldInfoValidator.Result {
        val result = ContentCreationTabInfo.FieldInfoValidator.Result()
        if (field == false)
            result.errorMessage = context.getString(R.string.error_invalid_bool)

        return result
    }

}