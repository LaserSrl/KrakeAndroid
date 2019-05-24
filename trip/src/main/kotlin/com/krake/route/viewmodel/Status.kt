package com.krake.route.viewmodel

sealed class Status
object Idle : Status()
object Loading : Status()
object Error : Status()