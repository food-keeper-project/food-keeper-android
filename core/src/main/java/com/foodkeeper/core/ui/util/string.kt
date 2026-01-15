package com.foodkeeper.core.ui.util

import android.util.Patterns
import java.util.regex.Pattern

object AppString {
    val appName = "푸드키퍼"

}

fun String.isEmailValid(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}
/**
 * 비밀번호 유효성 검사 함수 (영문, 숫자 포함 8~20자)
 */
fun String.isPasswordValid(): Boolean {
    val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$"
    return Pattern.matches(passwordPattern, this)
}