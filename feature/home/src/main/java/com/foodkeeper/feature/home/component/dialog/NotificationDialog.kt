package com.foodkeeper.feature.home.component.dialog

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
fun showNotificationRationaleDialog(
    context: Context,
    onConfirm: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle("알림 권한 필요")
        .setMessage("유통기한 알림을 받기 위해 알림 권한이 필요합니다.")
        .setPositiveButton("허용") { _, _ -> onConfirm() }
        .setNegativeButton("취소", null)
        .show()
}

fun showGoToSettingsDialog(context: Context) {
    AlertDialog.Builder(context)
        .setTitle("알림 권한이 꺼져 있습니다")
        .setMessage("설정에서 알림 권한을 직접 허용해주세요.")
        .setPositiveButton("설정으로 이동") { _, _ ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            context.startActivity(intent)
        }
        .setNegativeButton("취소", null)
        .show()
}
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}