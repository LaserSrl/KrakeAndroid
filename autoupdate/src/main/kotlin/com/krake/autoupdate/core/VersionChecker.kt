package com.krake.autoupdate.core

/**
 * Used for provide a [ApkVersion]
 * that will be used for check if the new apk as to be downloaded
 */
interface VersionChecker
{

    fun loadApkVersion(): ApkVersion
}