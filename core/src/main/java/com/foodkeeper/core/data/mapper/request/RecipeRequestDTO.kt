package com.foodkeeper.core.data.mapper.request

import kotlinx.serialization.Serializable

@Serializable
data class RecipeCreateRequest(
    val menuName: String,
    val description: String,
    val cookMinutes: Int,
    val ingredients: List<IngredientRequest>,
    val steps: List<StepRequest>
)

@Serializable
data class IngredientRequest(
    val name: String,
    val quantity: String
)

@Serializable
data class StepRequest(
    val title: String,
    val content: String
)
