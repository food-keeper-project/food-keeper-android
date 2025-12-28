package com.foodkeeper.core.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FoodKeeperMessagingService : FirebaseMessagingService() {

    // NotificationHelper를 초기화 (필요 시 Hilt 주입 가능)
    private val notificationHelper by lazy { NotificationHelper(applicationContext) }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. 서버에서 보낸 메시지의 제목과 본문 추출
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "알림"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "유통기한을 확인하세요!"

        // 2. 알림 띄우기
        notificationHelper.showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 로그인 시 서버에 등록하신다고 했으니 여기서는 별도 처리 불필요
    }
}
