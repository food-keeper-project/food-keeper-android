package com.foodkeeper.core.data.mapper.external

import kotlinx.serialization.Serializable

@Serializable
data class AiRecipeListResponse(
    val content: List<AiRecipeResponse>, // 레시피 리스트
    val hasNext: Boolean               // 다음 페이지 존재 여부
)

@Serializable
data class AiRecipeResponse(
    val id: Long,
    val title: String,
    val description: String,
    val cookMinutes: Int
)
@Serializable
data class AiRecipe(
    val title: String?,
    val description: String?,
    val cookMinutes: Int?, // ✅ 서버에서 30(숫자)으로 내려오므로 Int가 안전합니다.
    val recipeImage: String? = null, // JSON에 없으므로 기본값 null
    val ingredients: List<Ingredient>?,
    val steps: List<Step>?
)

@Serializable
data class Ingredient(
    val name: String?,
    val quantity: String?
)
@Serializable
data class RecipeCountDTO(
    val recipeCount:Long?
)
@Serializable
data class Step(
    val title: String?,
    val content: String?
)
