package com.foodkeeper.core.ui.util

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.foodkeeper.core.R

object AppFonts {
    val NanumSquareNeo = FontFamily(
        Font(R.font.nanum_square_neo_light, FontWeight.Light),
        Font(R.font.nanum_square_neo_regular, FontWeight.Normal),
        Font(R.font.nanum_square_neo_bold, FontWeight.Bold),
        Font(R.font.nanum_square_neo_extra_bold, FontWeight.ExtraBold),
        Font(R.font.nanum_square_neo_heavy, FontWeight.Black)
    )

    // Title 시리즈
    val size26Title1 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.02).em
    )

    val size22Title2 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.02).em
    )

    val size19Title3 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).em
    )

    // Body 시리즈
    val size16Body1 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em
    )

    val size14Body2 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Caption 시리즈
    val size12Caption1 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    val size10Caption2 = TextStyle(
        fontFamily = NanumSquareNeo,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )
}