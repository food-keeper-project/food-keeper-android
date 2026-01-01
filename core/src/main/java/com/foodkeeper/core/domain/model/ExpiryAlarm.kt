package com.foodkeeper.core.domain.model

enum class ExpiryAlarm(val displayName: String, val daysBefore: Int) {
    SAME_DAY("당일 알림", 0),
    ONE_DAY("하루 전 알림", 1),
    TWO_DAYS("2일 전 알림", 2),
    THREE_DAYS("3일 전 알림", 3),
    ONE_WEEK("1주일 전 알림", 7),
    TWO_WEEKS("2주일 전 알림", 14)
}