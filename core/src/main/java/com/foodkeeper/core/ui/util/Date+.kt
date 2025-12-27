package com.foodkeeper.core.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// Date 공통 유틸 정리


private const val EXPIRING_SOON_DAYS = 3

//Date → "yy. MM. dd" 형식 문자열 변환
fun Date.toyyMMddString(): String {
    val formatter = SimpleDateFormat("yy.MM.dd", Locale.getDefault())
    return formatter.format(this)
}
//유통기한 임박 여부
fun Date.isExpiringSoon(): Boolean {
    val diffMillis = time - System.currentTimeMillis()
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return diffDays in 0..EXPIRING_SOON_DAYS
}

// 디데이 계산
fun Date.getDDay(from: Date = Date()): Int {
    val diffMillis = this.time - from.time
    return TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
}