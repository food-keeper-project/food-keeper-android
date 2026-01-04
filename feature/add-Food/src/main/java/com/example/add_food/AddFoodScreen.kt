package com.example.add_food

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.foodkeeper.core.domain.model.Category
import com.foodkeeper.core.domain.model.ExpiryAlarm
import com.foodkeeper.core.domain.model.StorageMethod
import com.foodkeeper.core.ui.base.BaseUiState
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.foodkeeper.core.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val view = LocalView.current // 시스템 UI 제어용

    val uiState by viewModel.uiState.collectAsState()
    val foodInput by viewModel.foodInput.collectAsState()
    val categories by viewModel.foodCategories.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateFoodImage(uri)
    }
    // --------------------
    // 시스템 하단바 숨기기 설정
    // --------------------
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        if (window != null) {
            val controller = WindowCompat.getInsetsController(window, view)
            // 하단바(Navigation Bars) 숨기기
            controller.hide(WindowInsetsCompat.Type.navigationBars())
            // 사용자가 화면 끝을 쓸어올릴 때만 잠깐 나타나도록 설정 (선택 사항)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // 화면을 나갈 때 다시 나타나게 설정
            val window = (context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }
    LaunchedEffect(Unit) {
        viewModel.onScreenEnter()
        launch {
            viewModel.toastMessage.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        launch {
            viewModel.dissmissEvent.collect { success ->
                onBackClick()
            }
        }
    }

    Scaffold(
        containerColor = AppColors.white,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "직접 등록하기", style = AppFonts.size22Title2) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                // 빈 배경 클릭 시 키보드 닫기
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusManager.clearFocus() },
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { // 24dp 대신 8dp 적용
                FoodNameField(
                    value = foodInput.name,
                    onValueChange = { if (it.length <= 10) viewModel.updateFoodName(it) }
                )
                // 여기서 별도의 Spacer를 추가하지 않아도 spacedBy(8.dp)가 작동합니다.
                ImageUploadSection(
                    imageUri = foodInput.imageUri,
                    onUploadClick = {
                        focusManager.clearFocus()
                        galleryLauncher.launch("image/*")
                    }
                )
            }

            // 3. 카테고리 (포커스 해제)
            CategorySection(
                selected = foodInput.categorys.firstOrNull(),
                categoryList = categories,
                onSelect = {
                    focusManager.clearFocus()
                    viewModel.updateCategory(it)
                }
            )

            // 4. 보관 방식 (포커스 해제)
            StorageSection(
                selected = foodInput.storageMethod,
                StorageMethodList = StorageMethod.values().toList(),
                onSelect = {
                    focusManager.clearFocus()
                    viewModel.updateStorageMethod(it)
                }
            )

            // 5. 유통기한 (포커스 해제)
            ExpiryDateSection(
                date = foodInput.expiryDate,
                onCalendarClick = {
                    focusManager.clearFocus()
                    showDatePicker = true
                }
            )

            // 6. 알림 (포커스 해제)
            AlarmSection(
                selected = foodInput.expiryAlarm,
                onClick = {
                    focusManager.clearFocus()
                    showBottomSheet = true
                }
            )

            // 7. 메모 (포커싱 가능)
            MemoSection(
                value = foodInput.memo,
                onValueChange = { if (it.length <= 100) viewModel.updateMemo(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 하단 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // 중앙 정렬
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                Button(
//                    onClick = { focusManager.clearFocus() },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.main),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text("품목 즐겨찾기 등록")
//                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.submitFood()
                    },
                    modifier = Modifier
                        .width(200.dp)  // 가로 길이
                        .height(56.dp), // 세로 길이
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.main),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("식재료 등록", style = AppFonts.size16Body1B)
                }
            }
        }

        // --------------------
        // Processing 오버레이
        // --------------------
        if (uiState is BaseUiState.Processing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF9500)
                )
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = {
                    viewModel.updateExpiryDate(it)
                    showDatePicker = false
                }
            )
        }

        // Alarm Picker Dialog
        if (showBottomSheet) {
            AlarmPickerBottomSheet(
                selected = foodInput.expiryAlarm,
                onDismiss = { showBottomSheet = false },
                onSelect = {
                    viewModel.updateExpiryAlarm(it)
                    showBottomSheet = false
                }
            )
        }


    }
}

// ========================================
// 텍스트 입력 필드 스타일 (식재료명, 메모 전용)
// ========================================
@Composable
fun foodFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.point,
    unfocusedBorderColor = AppColors.main,
    cursorColor = AppColors.point
)

// ========================================
// 식재료명 입력 필드
// ========================================
@Composable
fun FoodNameField(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = "식재료명", style = AppFonts.size16Body1B, color = AppColors.black)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("이름을 입력하세요") },
            colors = foodFieldColors(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        Text(
            text = "${value.length}/10",
            style = AppFonts.size14Body2,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth() // 가로 전체를 채워야 우측 정렬 기준이 생깁니다.
                .padding(top = 4.dp),
            textAlign = TextAlign.End // ⭐ 텍스트를 오른쪽 끝으로 정렬
        )
    }
}

// ========================================
// 사진 업로드 섹션
// ========================================
@Composable
fun ImageUploadSection(
    imageUri: Uri?,
    onUploadClick: () -> Unit
) {
    Column {
        Text(
            text = "사진",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            // ⭐ 핵심: Row 내부의 요소들을 바닥(Bottom)으로 정렬합니다.
            verticalAlignment = Alignment.Bottom
        ) {
            val imageShape = RoundedCornerShape(8.dp)

            // 이미지 미리보기 (높이 90dp)
            Box(
                modifier = Modifier
                    .size(190.dp, 90.dp)
                    .border(1.dp, AppColors.main, imageShape)
                    .background(Color.White, imageShape),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "선택된 이미지",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(imageShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.camera), // 본인 폴더명에 맞게 수정
                        contentDescription = "카메라",
                        tint = AppColors.main,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 업로드 버튼 (높이 30dp -> 이미지 박스 바닥에 붙음)
            Button(
                onClick = onUploadClick,
                modifier = Modifier
                    .width(74.dp)
                    .height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.main
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "업로드",
                    style = AppFonts.size14Body2,
                    color = Color.White
                )
            }
        }
    }
}

// ========================================
// 카테고리 섹션
// ========================================
@Composable
fun CategorySection(
    selected: Category?,
    categoryList: List<Category>,
    onSelect: (Category) -> Unit
) {

    Column {
        Text(
            text = "카테고리",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryList.take(3).forEach { category ->
                CategoryChip(
                    text = category.name,
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
            categoryList.drop(3).forEach { category ->
                CategoryChip(
                    text = category.name,
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
            containerColor = if (isSelected) AppColors.main else AppColors.white,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) AppColors.main else Color(0xFFE0E0E0) // 선택 안 됐을 때의 선 색상 (예: 연한 회색)
        )
    ) {
        Text(text, style = AppFonts.size14Body2)
    }
}

// ========================================
// 보관 방식 섹션
// ========================================
@Composable
fun StorageSection(
    selected: StorageMethod?,
    StorageMethodList: List<StorageMethod>,
    onSelect: (StorageMethod) -> Unit
) {

    Column {
        Text(
            text = "보관 방식",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StorageMethodList.forEach { storage ->
                CategoryChip(
                    text = storage.displayName,
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
fun ExpiryDateSection(date: Date?, onCalendarClick: () -> Unit) {
    val text = date?.let { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(it) } ?: "날짜를 선택하세요"
    val textColor = if (date != null) Color.Black else Color.Gray

    Column {
        Text(
            text = "유통 기한",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TextField 대신 Box를 사용하여 포커싱 원천 차단
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .border(1.dp, AppColors.main, RoundedCornerShape(8.dp))
                    .clickable { onCalendarClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(text = text, color = textColor, style = AppFonts.size16Body1)
            }

            IconButton(
                onClick = onCalendarClick,
                modifier = Modifier.size(46.dp).background(AppColors.main, RoundedCornerShape(8.dp))
            ) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "달력", tint = Color.White)
            }
        }
    }
}

// ========================================
// 알림 일시 섹션
// ========================================
@Composable
fun AlarmSection(selected: ExpiryAlarm?, onClick: () -> Unit) {
    Column {
        Text(text = "알림 일시",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, AppColors.main, RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = selected?.displayName ?: "알림 선택", style = AppFonts.size16Body1, color = Color.Black)
                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "선택", tint = Color.Gray)
            }
        }
    }
}
// ========================================
// 메모 섹션
// ========================================
@Composable
fun MemoSection(value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = "메모",
            style = AppFonts.size16Body1B,
            color = AppColors.black
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(150.dp),
            placeholder = { Text("메모를 입력하세요") },
            colors = foodFieldColors(),
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
    onDateSelected: (Date) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Date(millis)
//                    val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                    onDateSelected(date)
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
    selected: ExpiryAlarm?,
    onDismiss: () -> Unit,
    onSelect: (ExpiryAlarm?) -> Unit
) {
    val alarms = ExpiryAlarm.values().toList()
    val context = LocalContext.current
    val view = LocalView.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        DisposableEffect(Unit) {
            val window = (context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.hide(WindowInsetsCompat.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            onDispose { }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "알림 설정",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            // 🔥 알림 미선택
            AlarmItem(
                title = "알림 없음",
                isSelected = selected == null,
                onClick = {
                    onSelect(null)
                    onDismiss()
                }
            )

            // 🔥 알림 목록
            alarms.forEach { alarm ->
                AlarmItem(
                    title = alarm.displayName,
                    isSelected = alarm == selected,
                    onClick = {
                        onSelect(alarm)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun AlarmItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = if (isSelected) Color(0xFFFFF3E0) else Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}