package com.example.foodkeeper // 패키지 이름을 프로젝트에 맞게 통일합니다.

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.foodkeeper.feature.kakaologin.LoginScreen      // 모듈 이름 'kakao-login'에 맞게 수정
import com.foodkeeper.feature.kakaologin.LoginViewModel   // 모듈 이름 'kakao-login'에 맞게 수정
import com.example.foodkeeper.ui.theme.FoodKeeperTheme     // 패키지 이름에 맞게 수정
import com.foodkeeper.feature.home.HomeScreen
import com.foodkeeper.feature.home.HomeViewModel
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hilt를 사용하기 위한 어노테이션
class MainActivity : ComponentActivity() {

    // Hilt를 통해 LoginViewModel의 인스턴스를 주입받습니다.
    private val loginViewModel: LoginViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FoodKeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- 이 코드를 추가합니다 ---
                    val keyHash = Utility.getKeyHash(this)
                    Log.d("KakaoKeyHash", "keyHash: $keyHash")
                    // :feature:kakao-login 모듈의 LoginScreen을 호출하고,
                    // Hilt로부터 주입받은 ViewModel을 파라미터로 전달합니다.
//                    LoginScreen(viewModel = loginViewModel)
                    HomeScreen()
                }
            }
        }
    }
}
