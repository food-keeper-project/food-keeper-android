package com.foodkeeper.core.domain.usecase

import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.FoodCategory
import java.util.Calendar
import java.util.Date

object FoodMockData {

    private const val IMAGE_URL = "https://picsum.photos/200/300"

    private fun daysFromNow(days: Int): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
        }.time
    }

    val foodList = listOf(
        Food(
            id = 1,
            name = "양파",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(2),
            category = FoodCategory.VEGETABLE
        ),
        Food(
            id = 2,
            name = "소고기",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(1),
            category = FoodCategory.MEAT
        ),
        Food(
            id = 3,
            name = "우유",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(5),
            category = FoodCategory.DAIRY
        ),
        Food(
            id = 4,
            name = "사과",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(7),
            category = FoodCategory.FRUIT
        ),
        Food(
            id = 5,
            name = "고등어",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(0),
            category = FoodCategory.SEAFOOD
        ),
        Food(
            id = 6,
            name = "사과1",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(7),
            category = FoodCategory.FRUIT
        ),
        Food(
            id = 7,
            name = "사과2",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(7),
            category = FoodCategory.FRUIT
        ),
        Food(
            id = 8,
            name = "사과3",
            imageURL = IMAGE_URL,
            expiryDate = daysFromNow(7),
            category = FoodCategory.FRUIT
        )

    )
}