package com.foodkeeper.core.domain.model

import java.util.Date


enum class FoodCategory(val displayName: String) {
    VEGETABLE("야채류"),
    MEAT("육류"),
    SEAFOOD("해산물"),
    DAIRY("유제품"),
    FRUIT("과일류");

    companion object {
        fun fromDisplayName(name: String): FoodCategory =
            values().find { it.displayName == name }
                ?: throw IllegalArgumentException("Unknown category: $name")
    }
}


data class Food(
    val id: Int, //고유 ID
    val name: String, //식품명
    val imageURL: String, //이미지 URL
    val expiryDate: Date, //유통기한 날짜
    val category: FoodCategory,
    val isExpiringSoon: Boolean = false //유통기한 임박 여부???
)
