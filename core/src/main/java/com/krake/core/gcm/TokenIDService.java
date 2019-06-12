package com.krake.core.gcm;

import android.content.Context;
import com.krake.core.messaging.MessagingService;


@Deprecated
public class TokenIDService {

    static public String getUUID(Context mContext) {
        return MessagingService.getUUID(mContext);
    }
}
