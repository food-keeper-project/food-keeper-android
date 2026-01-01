package com.example.add_food

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onBackClick: () -> Unit = {},
    onFoodAdded: () -> Unit = {}
) {
    // State
    var foodName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("야채류") }
    var selectedStorage by remember { mutableStateOf("냉장") }
    var expiryDate by remember { mutableStateOf("") }
    var selectedAlarm by remember { mutableStateOf("3일 전") }
    var memo by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showAlarmPicker by remember { mutableStateOf(false) }

    // 갤러리 런처
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "직접 등록하기",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 식재료명
            FoodNameField(
                value = foodName,
                onValueChange = { if (it.length <= 10) foodName = it }
            )

            // 사진
            ImageUploadSection(
                imageUri = selectedImageUri,
                onUploadClick = {
                    galleryLauncher.launch("image/*")
                }
            )

            // 카테고리
            CategorySection(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )

            // 보관 방식
            StorageSection(
                selected = selectedStorage,
                onSelect = { selectedStorage = it }
            )

            // 유통 기한
            ExpiryDateSection(
                date = expiryDate,
                onCalendarClick = { showDatePicker = true }
            )

            // 알림 일시
            AlarmSection(
                selected = selectedAlarm,
                onClick = { showAlarmPicker = true }
            )

            // 메모
            MemoSection(
                value = memo,
                onValueChange = { memo = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 하단 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* 품목 즐겨찾기 등록 */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("품목 즐겨찾기 등록")
                }

                Button(
                    onClick = onFoodAdded,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("식재료 등록")
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { date ->
                    expiryDate = date
                    showDatePicker = false
                }
            )
        }

        // Alarm Picker Dialog
        if (showAlarmPicker) {
            AlarmPickerBottomSheet(
                selected = selectedAlarm,
                onDismiss = { showAlarmPicker = false },
                onSelect = { alarm ->
                    selectedAlarm = alarm
                }
            )
        }
    }
}

// ========================================
// 식재료명 입력 필드
// ========================================
@Composable
fun FoodNameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = "식재료명",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("이름을 입력하세요") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF9500),
                unfocusedBorderColor = Color(0xFFFF9500)
            ),
            shape = RoundedCornerShape(8.dp)
        )
        Text(
            text = "${value.length}/10",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

// ========================================
// 이미지 업로드 섹션
// ========================================
@Composable
fun ImageUploadSection(
    imageUri: Uri?,
    onUploadClick: () -> Unit
) {
    Column {
        Text(
            text = "사진",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 이미지 미리보기
            Box(
                modifier = Modifier
                    .size(120.dp, 150.dp)
                    .border(2.dp, Color(0xFFFF9500), RoundedCornerShape(8.dp))
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "선택된 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "카메라",
                        tint = Color(0xFFFF9500),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // 업로드 버튼
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .width(100.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9500)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("업로드")
            }
        }
    }
}

// ========================================
// 카테고리 섹션
// ========================================
@Composable
fun CategorySection(
    selected: String,
    onSelect: (String) -> Unit
) {
    val categories = listOf("야채류", "육류", "해산물", "유제품", "과일류", "기타")

    Column {
        Text(
            text = "카테고리",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(3).forEach { category ->
                CategoryChip(
                    text = category,
                    isSelected = selected == category,
                    onClick = { onSelect(category) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.drop(3).forEach { category ->
                CategoryChip(
                    text = category,
                    isSelected = selected == category,
                    onClick = { onSelect(category) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFF9500) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(text, fontSize = 14.sp)
    }
}

// ========================================
// 보관 방식 섹션
// ========================================
@Composable
fun StorageSection(
    selected: String,
    onSelect: (String) -> Unit
) {
    val storages = listOf("실온", "냉장", "냉동")

    Column {
        Text(
            text = "보관 방식",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            storages.forEach { storage ->
                CategoryChip(
                    text = storage,
                    isSelected = selected == storage,
                    onClick = { onSelect(storage) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ========================================
// 유통기한 섹션
// ========================================
@Composable
fun ExpiryDateSection(
    date: String,
    onCalendarClick: () -> Unit
) {
    Column {
        Text(
            text = "유통 기한",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = date,
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = { Text("YYYY/MM/DD") },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF9500),
                    unfocusedBorderColor = Color(0xFFFF9500)
                ),
                shape = RoundedCornerShape(8.dp)
            )

            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFF9500), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "달력",
                    tint = Color.White
                )
            }
        }
    }
}

// ========================================
// 알림 일시 섹션
// ========================================
@Composable
fun AlarmSection(
    selected: String,
    onClick: () -> Unit
) {
    Column {
        Text(
            text = "알림 일시",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(2.dp, Color(0xFFFF9500), RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "선택",
                    tint = Color.Gray
                )
            }
        }
    }
}

// ========================================
// 메모 섹션
// ========================================
@Composable
fun MemoSection(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = "메모",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            placeholder = { Text("메모를 입력하세요") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFF9500),
                unfocusedBorderColor = Color(0xFFFF9500)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ========================================
// DatePicker Dialog
// ========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
                    val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    onDateSelected(format.format(date))
                }
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// ========================================
// Alarm Picker Dialog
// ========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmPickerBottomSheet(
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val alarms = listOf(
        "당일 알림",
        "하루 전 알림",
        "2일 전 알림",
        "3일 전 알림",
        "1주일 전 알림",
        "2주일 전 알림"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            // 상단 핸들바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, RoundedCornerShape(2.dp))
                )
            }

            alarms.forEach { alarm ->
                Surface(
                    onClick = {
                        onSelect(alarm)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    color = if (alarm == selected)
                        Color(0xFFFFF3E0)
                    else
                        Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = alarm,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
