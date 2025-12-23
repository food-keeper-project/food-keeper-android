package com.foodkeeper.core.ui.util

import java.util.Date
import java.util.concurrent.TimeUnit

// Date 공통 유틸 정리


private const val EXPIRING_SOON_DAYS = 3

//유통기한 임박 여부
fun Date.isExpiringSoon(): Boolean {
    val diffMillis = time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return diffDays in 0..EXPIRING_SOON_DAYS
}