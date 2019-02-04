package com.krake.core;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.krake.core.login.PrivacyException;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by joel on 19/05/15.
 */
public class OrchardError extends IOException {
    private static final long serialVersionUID = -5594775017637063778L;

    public static final int REACTION_LOGIN = 1000;
    public static final int REACTION_PRIVACY = 3000;

    /**
     * Resolution action legata al processo di upload. Indica di continuare l'upload da dove è stato interrotto.
     */
    public static final int REACTION_CONTINUE_UPLOAD = 4001;

    /**
     * Resolution action legata al processo di upload. Indica di iniziare nuovamente l'upload.
     */
    public static final int REACTION_UPLOAD_NEVER_STARTED = 4002;

    /**
     * Resolution action legata al processo di upload. Indica che l'upload è finito anche se Orchard ritorna un errore.
     */
    public static final int REACTION_UPLOAD_THEORETICALLY_FINISHED = 4003;

    public static final int ERROR_NO_USER = 1000;
    public static final int ERROR_INVALID_TOKEN = 1001;
    public static final int ERROR_INVALID_API_KEY = 1002;
    private final Exception originalException;
    private String message;
    private int reactionCode;
    private int errorCode;
    private final Date dateCreated;

    public OrchardError(Exception e) {
        super("", null);
        message = e.getLocalizedMessage();
        if (TextUtils.isEmpty(message))
            message = e.getMessage();
        originalException = e;

        if (originalException instanceof PrivacyException) {
            reactionCode = REACTION_PRIVACY;
        }
        dateCreated = new Date();
    }

    private OrchardError(JsonObject orchardResult) {
        super("", null);
        if (orchardResult.has("Message"))
            message = orchardResult.get("Message").getAsString();
        else if (orchardResult.has("Error"))
            message = orchardResult.get("Error").getAsString();
        if (orchardResult.has("message"))
            message = orchardResult.get("message").getAsString();

        if (orchardResult.has("ErrorCode")) {
            errorCode = orchardResult.get("ErrorCode").getAsInt();
        }

        if (orchardResult.has("ResolutionAction")) {
            reactionCode = orchardResult.get("ResolutionAction").getAsInt();
        }

        if (reactionCode == 0 && !TextUtils.isEmpty(message)) {
            String lowerMessage = message.toLowerCase(Locale.US);
            if (/*lowerMessage.contains("user") ||*/ lowerMessage.contains("token")
                    || lowerMessage.contains("cookie")) {
                reactionCode = REACTION_LOGIN;
            }
        }
        Exception originalException1;

        if (reactionCode == REACTION_PRIVACY) {
            try {
                Mapper.getSharedInstance().parseContentFromResult(orchardResult.getAsJsonObject("Data"),
                        null,
                        false,
                        null);
                originalException1 = null;
            } catch (OrchardError e) {
                originalException1 = e.getOriginalException();
            }
        } else
            originalException1 = null;
        originalException = originalException1;
        dateCreated = new Date();
    }

    public OrchardError(String message) {
        this(message, 0);
    }

    public OrchardError(String message, int reactionCode) {
        this(message, reactionCode, 0);
    }

    public OrchardError(String message, int reactionCode, int errorCode) {
        this(message, reactionCode, errorCode, null);
    }

    public OrchardError(String message, int reactionCode, int errorCode, Exception originalException) {
        super("", null);
        this.message = message;
        this.reactionCode = reactionCode;
        this.errorCode = errorCode;
        this.originalException = originalException;
        dateCreated = new Date();
    }

    public static
    @Nullable
    OrchardError createErrorFromResult(@NonNull JsonObject jResult) {

        boolean success = true;
        boolean hasSuccessKey = false;

        if (jResult.has("Success")) {
            hasSuccessKey = true;
            success = jResult.get("Success").getAsBoolean();
        } else if (jResult.has("success")) {
            hasSuccessKey = true;
            success = jResult.get("success").getAsBoolean();
        }

        if (!success || (!hasSuccessKey && jResult.has("Message"))) {
            return new OrchardError(jResult);
        }

        return null;
    }

    public String getOriginalMessage() {
        return message;
    }

    public String getUserFriendlyMessage(Context context) {
        if (originalException == null)
            return message;
        else
            return context.getString(R.string.error_generic_failure);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getReactionCode() {
        return reactionCode;
    }

    public boolean isExceptionMessage() {
        return originalException != null;
    }

    public Exception getOriginalException() {
        return originalException;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Error - code: ");
        sb.append(errorCode);
        sb.append(", reaction code: ");
        sb.append(reactionCode);
        if (message != null) {
            sb.append(", message: ");
            sb.append(message);
        }
        return sb.toString();
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
