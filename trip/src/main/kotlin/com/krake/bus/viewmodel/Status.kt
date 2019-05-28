package com.krake.bus.viewmodel

sealed class Status
object Idle : Status()
object Loading : Status()
object Error : Status()