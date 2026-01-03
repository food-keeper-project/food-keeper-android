package com.foodkeeper.core.domain.model

import java.util.Date
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class Category(
    val id: Long, //고유 ID
    val name: String, //카테고리명
): Parcelable
