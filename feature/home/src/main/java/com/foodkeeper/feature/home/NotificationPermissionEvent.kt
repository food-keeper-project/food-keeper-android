package com.foodkeeper.feature.home

sealed class NotificationPermissionEvent {
    object RequestPermission : NotificationPermissionEvent()
    object ShowRationale : NotificationPermissionEvent()
    object GoToSettings : NotificationPermissionEvent()
}