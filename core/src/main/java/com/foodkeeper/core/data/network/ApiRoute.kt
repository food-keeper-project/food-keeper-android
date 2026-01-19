package com.foodkeeper.core.data.network

import com.foodkeeper.core.data.mapper.request.AccountRequestDTO
import com.foodkeeper.core.data.mapper.request.FoodCreateRequestDTO
import com.foodkeeper.core.data.mapper.request.RecipeCreateRequest
import com.foodkeeper.core.data.mapper.request.SignUpRequestDTO
import io.ktor.client.request.forms.FormBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod // Ktor의 HttpMethod 사용
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


sealed class ApiRoute {
    // ✅ 추가: 기본값은 false로 두고, RefreshToken 클래스에서만 true로 재정의합니다.
    open val isRefreshTokenRequest: Boolean = false
    open val isLoginRequest: Boolean = false

    // 기본값을 true로 설정하고, 인증이 필요 없는 경우에만 false로 오버라이드
    //open val requiresAuth: Boolean = true
    // ========== Auth APIs ==========
    // 2. Route 정의는 데이터만 전달하는 역할만 수행
    data class LocalLogin(
        val account: String, // 카카오에서 받은 토큰
        val pw: String,
        val fcmToken:String?// FCM 토큰
    ) : ApiRoute()
    data class RefreshToken(
        val curAccessToken:String,
        val curRefreshToken:String
    ) : ApiRoute()
    data class RecommendRecipe(
        val ingredients:List<String>,
        val excludeMenus:List<String>
    ): ApiRoute()
    data class PostRecipe(
        val request: RecipeCreateRequest
    ) : ApiRoute()
    data class GetFavoriteRecipe(val cursor: Long?,
                             val limit: Int?
        ) : ApiRoute()
    data class GetDetailedRecipe(val recipeId: Long) : ApiRoute()
    data class DeleteFavoriteRecipe(val recipeId: Long) : ApiRoute()

    // ✅ MyProfile 수정: GET 요청이므로 별도의 파라미터가 필요 없습니다.
    // 인증은 requiresAuth = true를 통해 자동으로 처리됩니다.
    object MyProfile : ApiRoute()
    object Logout: ApiRoute()
    object WithdrawAccount: ApiRoute()

    object GetMyRecipeCount: ApiRoute()

    data class PostCheckAccount(val request: AccountRequestDTO) : ApiRoute()
    data class PostSignUp(val request: SignUpRequestDTO): ApiRoute()
    data class PostVerifyEmail(val email: String): ApiRoute()
    data class PostVerifyEmailCode(val email: String, val code: String): ApiRoute()

    data class PostAccountVerify(val email: String) : ApiRoute()
    data class PostAccountCodeVerify(val email: String,val code:String) : ApiRoute()
    data class PostPwVerify(val email:String,val account:String) : ApiRoute()
    data class PostPwCodeVerify(val email:String,val account:String,val code:String) : ApiRoute()
    data class PostPwReset(val email:String,val account:String,val password:String) : ApiRoute()
    // ========== 식자재 관련 정의 ==========
    data class AllFoodList(
        val categoryId: Long?,
        val cursor: Long?,
        val limit: Int?
    ) : ApiRoute()
    object ImminentFoodList: ApiRoute()
    data class AddFood(
        val request: FoodCreateRequestDTO,
        val imageBytes: ByteArray?,              // 이미지 선택 안 하면 null
    ) : ApiRoute()

    data class UpdateFood(
        val foodId: Long,
        val request: FoodCreateRequestDTO,
        val imageBytes: ByteArray?,              // 이미지 선택 안 하면 null
    ) : ApiRoute()

    data class ConsumptionFood(
        val foodId: Long
    ) : ApiRoute()
    object GetMyFoodCount: ApiRoute()
    // ========== 카테고리 관련 정의 ==========
    object Categories: ApiRoute()

    //    data class Logout(val userId: String) : ApiRoute()
    // ========== 경로 정의 ==========
    val baseURL: String
        get() = when (this) {
            else -> ApiKey.BASE_URL
        }
    // ========== 경로 정의 ==========
    val path: String
        // TODO: URL 선언 시 앞에 '/' 제거!!
        get() = when (this) {
            // Auth
            is RefreshToken -> "api/v1/auth/refresh" // 엑세스 토큰 갱신 API
            is Logout -> "api/v1/auth/sign-out" // 로그아웃 api
            is PostCheckAccount -> "api/v1/auth/check/account" // 중복확인 api
            is PostVerifyEmail -> "api/v1/auth/email/verify" // 이메일 인증 api
            is PostVerifyEmailCode -> "api/v1/auth/email-code/verify" // 이메일 인증 코드 확인 api
            is PostSignUp -> "api/v1/auth/sign-up" // 회원가입 api
            is LocalLogin -> "api/v1/auth/sign-in/local" //로컬로그인 API
            is PostAccountVerify -> "api/v1/auth/account/verify" // 이메일 인증 api
            is PostAccountCodeVerify -> "api/v1/auth/account-code/verify" // 이메일 인증 코드 확인 api
            is PostPwVerify -> "api/v1/auth/password/verify"
            is PostPwCodeVerify -> "api/v1/auth/password-code/verify"
            is PostPwReset -> "api/v1/auth/password/change"
            // User
            is MyProfile -> "api/v1/members/me" // 내 카톡 프로필 사진,이름을 가져오는 API
            is WithdrawAccount -> "api/v1/members/me/withdraw" // 회원탈퇴 api

            // Food
            is AllFoodList -> "api/v1/foods"
            is ImminentFoodList -> "api/v1/foods/imminent"
            is AddFood -> "api/v1/foods"
            is ConsumptionFood -> "api/v1/foods/${this.foodId}"
            is GetMyFoodCount -> "api/v1/foods/count/me"
            is UpdateFood -> "api/v1/foods/${this.foodId}"

            // Categorie
            is Categories -> "api/v1/categories"
            // AI-Recipe
            is RecommendRecipe -> "api/v1/recipes/recommend"
            is PostRecipe, is GetFavoriteRecipe -> "api/v1/recipes"
            is GetMyRecipeCount -> "api/v1/recipes/count/me"
            // ApiRoute.kt의 path 부분
            is GetDetailedRecipe -> "api/v1/recipes/${this.recipeId}"
            is DeleteFavoriteRecipe -> "api/v1/recipes/${this.recipeId}"

        }

    // ========== HTTP 메서드 정의 ==========
    val method: HttpMethod
        get() = when (this) {
            is RefreshToken -> HttpMethod.Post
            is Logout -> HttpMethod.Delete
            is AddFood -> HttpMethod.Post
            is ConsumptionFood -> HttpMethod.Delete
            is PostRecipe -> HttpMethod.Post
            is DeleteFavoriteRecipe -> HttpMethod.Delete
            is WithdrawAccount -> HttpMethod.Delete

            is PostCheckAccount->HttpMethod.Post
            is PostSignUp->HttpMethod.Post
            is PostVerifyEmail->HttpMethod.Post
            is PostVerifyEmailCode->HttpMethod.Post
            is PostAccountVerify->HttpMethod.Post
            is PostAccountCodeVerify->HttpMethod.Post
            is PostPwVerify->HttpMethod.Post
            is PostPwCodeVerify->HttpMethod.Post
            is PostPwReset->HttpMethod.Post
            is LocalLogin->HttpMethod.Post
            is UpdateFood-> HttpMethod.Put
            else -> HttpMethod.Get //선언이 없을 경우 디폴트값 GET
//            is Logout -> HttpMethod.GET
        }

    // ========== 인증 필요 여부 ==========
    val requiresAuth: Boolean
        get() = when (this) {
             is PostAccountVerify, is PostAccountCodeVerify,is RefreshToken, is LocalLogin,is PostSignUp , is PostCheckAccount, is PostVerifyEmailCode, is PostVerifyEmail
                ,is PostPwVerify, is PostPwCodeVerify, is PostPwReset -> false
            else -> true
        }

    // ========== MultiPartRequest 여부 ==========
    val multiPartRequest: Boolean
        get() = when (this) {
            is AddFood, is UpdateFood -> true
            else -> false
        }

    // ========== Body 데이터 ==========
    val body: Any?
        get() = when (this) {
            is RefreshToken -> mapOf(
                "refreshToken" to curRefreshToken)
            is AddFood -> buildAddFoodMultipart(
                request = request,
                imageBytes = imageBytes,
                imageFileName = request.expiryDate
            )
            is UpdateFood -> buildAddFoodMultipart(
                request = request,
                imageBytes = imageBytes,
                imageFileName = request.expiryDate
            )
            is LocalLogin -> mapOf(
                "account" to account,
                "password" to pw,
                "fcmToken" to fcmToken)
            is PostSignUp -> request
            is PostRecipe -> request
            is PostCheckAccount -> request
            is PostVerifyEmail -> mapOf("email" to email)
            is PostVerifyEmailCode -> mapOf("email" to email, "code" to code)
            is PostAccountVerify -> mapOf("email" to email)
            is PostAccountCodeVerify -> mapOf("email" to email, "code" to code)
            is PostPwVerify -> mapOf("email" to email, "account" to account)
            is PostPwCodeVerify -> mapOf("email" to email, "account" to account, "code" to code)
            is PostPwReset -> mapOf("email" to email, "account" to account, "newPassword" to password)
            else -> null
        }
    // ✅ 빨간 줄 해결: private 함수들을 companion object 안으로 이동하여 스코프를 명확히 함
    companion object {
        private fun appendImagePart(
            builder: FormBuilder,
            imageBytes: ByteArray,
            fileName: String
        ) {
            builder.append(
                "image",
                imageBytes,
                Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                }
            )
        }

        private fun buildAddFoodMultipart(
            request: FoodCreateRequestDTO,
            imageBytes: ByteArray?,
            imageFileName: String
        ): MultiPartFormDataContent {
            return MultiPartFormDataContent(
                formData {
                    append(
                        "request",
                        Json.encodeToString(request),
                        Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        }
                    )
                    imageBytes?.let {
                        appendImagePart(this, it, imageFileName)
                    }
                }
            )
        }
    }
    // ========== 쿼리 파라미터 ==========
    val queryParameters: Map<String, Any>
        get() = when (this) {
            is AllFoodList -> buildMap {
                categoryId?.let { put("categoryId", it) }
                cursor?.let { put("cursor", it) }
                limit?.let { put("limit", it) }
            }
            is ConsumptionFood -> mapOf("foodId" to foodId)
            // ✅ GET 방식일 때 리스트 데이터를 쿼리 스트링으로 변환
            is RecommendRecipe -> mapOf(
                "ingredients" to ingredients.joinToString(","),
                "excludedMenus" to excludeMenus.joinToString(",")
            )
            is GetFavoriteRecipe -> buildMap {
                cursor?.let { put("cursor", it) }
                limit?.let { put("limit", it) }
            }
            is DeleteFavoriteRecipe -> mapOf("recipeId" to recipeId)
            else -> emptyMap()
        }

    // ========== 커스텀 헤더 (필요시) ==========
    val headers: Map<String, String>
        get() = when (this) {
           is RefreshToken -> mapOf("Authorization" to "Bearer $curRefreshToken")
            else -> emptyMap()
        }

    // ========== 타임아웃 설정 (필요시) ==========
    val timeoutMillis: Long?
        get() = when (this) {
//            is exaple -> 60_000L // 파일 업로드 등 시간이 오래 걸리는 작업
            else -> null // 기본값 사용
        }
}


//// ========== 이미지 파일 JSON 변환 함수 ==========
//private fun appendImagePart(
//    builder: FormBuilder,
//    imageBytes: ByteArray,
//    fileName: String
//) {
//    builder.append(
//        "image",
//        imageBytes,
//        Headers.build {
//            append(HttpHeaders.ContentType, "image/jpeg")
//            append(
//                HttpHeaders.ContentDisposition,
//                "filename=\"$fileName\""
//            )
//        }
//    )
//}
//
//private fun buildAddFoodMultipart(
//    request: FoodCreateRequestDTO,
//    imageBytes: ByteArray?,
//    imageFileName: String
//): MultiPartFormDataContent {
//    return MultiPartFormDataContent(
//        formData {
//
//            // ✅ request JSON
//            append(
//                "request",
//                Json.encodeToString(request),
//                Headers.build {
//                    append(
//                        HttpHeaders.ContentType,
//                        ContentType.Application.Json.toString()
//                    )
//                }
//            )
//
//            // ✅ image (선택)
//            imageBytes?.let {
//                appendImagePart(
//                    builder = this,
//                    imageBytes = it,
//                    fileName = imageFileName
//                )
//            }
//        }
//    )
//}
