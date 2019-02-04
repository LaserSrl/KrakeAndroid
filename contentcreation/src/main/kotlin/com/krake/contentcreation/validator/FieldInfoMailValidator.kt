package com.krake.contentcreation.validator

import android.content.Context
import com.krake.contentcreation.ContentCreationTabInfo
import com.krake.contentcreation.R

/**
 * Created by giuliettif on 08/05/17.
 * Validator usato sui fieldInfo per controllare se la stringa inserita è formattata correttamente come mail.
 * Dev'essere usato con un FieldInfo di tipo [ContentCreationTabInfo.FIELD_TYPE_TEXT]
 */
class FieldInfoMailValidator : ContentCreationTabInfo.FieldInfoValidator<String> {
    override fun validate(context: Context, field : String): ContentCreationTabInfo.FieldInfoValidator.Result {
        val result = ContentCreationTabInfo.FieldInfoValidator.Result()
        if (!field.matches(context.getString(R.string.mail_regex_validation).toRegex())) {
            result.errorMessage = context.getString(R.string.error_invalid_mail)
        }
        return result
    }
}