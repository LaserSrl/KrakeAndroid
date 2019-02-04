package com.krake.autoupdate

import com.krake.autoupdate.core.ApkVersion
import com.krake.autoupdate.core.VersionChecker
import com.krake.core.network.RemoteClient
import com.krake.core.network.RemoteRequest

/**
 * Default [VersionChecker] for krake apps,
 * this download a json from a apk_version_url and create the ApkVersion that will be used for check the version.
 * json format:
 * {
 *      "v":"012340102",
 *      "u":"https://test.com/test/test.apk"
 * }
 */
open class KrakeVersionChecker(private val versionUrl: String) : VersionChecker
{
    override fun loadApkVersion(): ApkVersion
    {
        try
        {
            val request = RemoteRequest(versionUrl)
                    .setMethod(RemoteRequest.Method.GET)

            val response = RemoteClient.client(RemoteClient.Mode.DEFAULT)
                    .execute(request).jsonObject()!!

            return ApkVersion(response["v"].asLong,
                              response["u"].asString, null, null)
        }
        catch (e: Exception)
        {
            throw e
        }
    }
}