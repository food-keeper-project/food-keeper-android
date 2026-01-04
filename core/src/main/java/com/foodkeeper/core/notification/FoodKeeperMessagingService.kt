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

        // 1. 서버 data 페이로드에서 값 추출
        val foodName = remoteMessage.data["foodName"]
        val remainingDays = remoteMessage.data["remainingDays"]

        // 2. 알림 제목 결정
        val title = remoteMessage.data["title"] ?: "유통기한 알림"

        // 3. 알림 본문 조립 (분기 간소화)
        val body = if (foodName != null && remainingDays != null) {
            // "우유의 유통기한이 3일 남았습니다." (0일이어도 "0일 남았습니다"로 출력됨)
            "${foodName}의 유통기한이 ${remainingDays}일 남았습니다."
        } else {
            // 데이터가 부족할 경우 서버가 보낸 body를 쓰거나, 기본 문구 사용
            remoteMessage.data["body"] ?: "냉장고의 식자재를 확인해주세요."
        }
        // 2. 알림 띄우기
        notificationHelper.showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 로그인 시 서버에 등록하신다고 했으니 여기서는 별도 처리 불필요
    }
}
