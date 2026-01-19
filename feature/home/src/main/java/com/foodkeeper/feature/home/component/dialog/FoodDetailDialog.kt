import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.foodkeeper.core.domain.model.Food
import com.foodkeeper.core.domain.model.StorageMethod
import com.foodkeeper.core.ui.util.AppColors
import com.foodkeeper.core.ui.util.AppFonts
import com.foodkeeper.core.ui.util.getDDay
import com.foodkeeper.core.ui.util.toyyMMddWithDay
import java.util.Date
import androidx.compose.animation.AnimatedVisibility
import com.foodkeeper.core.domain.model.Category
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.foodkeeper.core.R
import com.foodkeeper.core.domain.model.ExpiryAlarm

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 📁 메인 다이얼로그
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailDialog(
    food: Food,
    categorys: List<Category>,
    onDismiss: () -> Unit,
    onConsumption: (Food) -> Unit,
    onUpdate: ((Uri?, Food) -> Unit)? = null
) {
    var isEditMode by remember { mutableStateOf(false) }
    var editedFood by remember { mutableStateOf(food) }
    var categoryList by remember { mutableStateOf(categorys) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    // 🔥 이미지 선택 런처
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }



    // 🔥 DatePicker 다이얼로그 상태
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // 수정 취소 시 원복
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            editedFood = food
            imageUri = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.white)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                /* ---------- Header ---------- */
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Text(
                        text = if (isEditMode) "식재료 수정하기" else "식재료 상세보기",
                        style = AppFonts.size16Body1,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = AppColors.light3Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- Image ---------- */
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FoodImageSection(
                        isEditMode = isEditMode,
                        imageUrl = editedFood.imageURL,
                        imageUri = imageUri,
                        onImageClick = { imagePickerLauncher.launch("image/*") }
                    )
                }


                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- Fields ---------- */

                // 1. 식재료명
                FoodInfoRow(
                    label = "식재료명",
                    isEditMode = isEditMode,
                    value = editedFood.name,
                    onValueChange = { editedFood = editedFood.copy(name = it) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. 카테고리
                FoodDropdownRow(
                    label = "카테고리",
                    isEditMode = isEditMode,
                    value = editedFood.category,
                    options = categoryList.map { it.name },
                    isBadge = true
                ) {
                    editedFood = editedFood.copy(category = it)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. 보관방식
                val storageMethods = StorageMethod.values()
                FoodDropdownRow(
                    label = "보관방식",
                    isEditMode = isEditMode,
                    value = editedFood.storageMethod.displayName,
                    options = storageMethods.map { it.displayName },
                    isBadge = true
                ) { name ->
                    storageMethods.find { it.displayName == name }?.let {
                        editedFood = editedFood.copy(storageMethod = it)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // 알림일시
                val expiryAlarm = ExpiryAlarm.values().toList()
                FoodDropdownRow(
                    label = "알림일시",
                    isEditMode = isEditMode,
                    value = ExpiryAlarm.fromDaysBefore(editedFood.expiryAlarm)?.displayName ?: "알수없음" ,
                    options = expiryAlarm.map { it.displayName },
                    isBadge = false
                ) { name ->
                    expiryAlarm.find { it.displayName == name }?.let {
                        editedFood = editedFood.copy(expiryAlarm = it.daysBefore)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. 유통기한
                FoodCalendarRow(
                    label = "유통기한",
                    isEditMode = isEditMode,
                    date = editedFood.expiryDate,
                    onCalendarClick = { showDatePickerDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5. 메모
                FoodMemoRow(
                    label = "메모",
                    isEditMode = isEditMode,
                    value = editedFood.memo,
                    onValueChange = { editedFood = editedFood.copy(memo = it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                /* ---------- Actions ---------- */
                ActionButtons(
                    isEditMode = isEditMode,
                    onEditClick = { isEditMode = true },
                    onCancelClick = {
                        editedFood = food
                        isEditMode = false
                    },
                    onSaveClick = {
                        onUpdate?.invoke(imageUri,editedFood)
                    },
                    onConsumptionClick = { onConsumption(food) }
                )
            }
        }
    }

    // 🔥 DatePicker 팝업 다이얼로그
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = editedFood.expiryDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            editedFood = editedFood.copy(expiryDate = Date(millis))
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("확인", color = AppColors.main)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("취소", color = AppColors.dartGray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 📁 개별 필드 컴포넌트
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * 이미지 섹션 (수정 모드에서 클릭 가능)
 */
@Composable
fun FoodImageSection(
    isEditMode: Boolean,
    imageUrl: String?,
    imageUri: Uri?,
    onImageClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(90.dp)
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .border(1.dp, if (!isEditMode) AppColors.main else AppColors.light5Gray, RoundedCornerShape(20.dp))
            .background(AppColors.white, RoundedCornerShape(20.dp))
            .then(
                if (isEditMode) {
                    Modifier.clickable { onImageClick() }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // 이미지 표시 우선순위: imageUri > imageUrl > placeholder
        val displayImage = imageUri ?: imageUrl

        if (displayImage != null) {
            AsyncImage(
                model = displayImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                error = painterResource(id = R.drawable.foodplaceholder),
                placeholder = painterResource(id = R.drawable.foodplaceholder)
            )
        } else {
            // placeholder 이미지
            Image(
                painter = painterResource(id = R.drawable.foodplaceholder),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

// 🔥 수정 모드일 때 중앙에 카메라 아이콘 오버레이
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 🔥 카메라 아이콘 흰색 배경 컨테이너
                Box(
                    modifier = Modifier
                        .size(35.dp) // 👉 아이콘 28dp + 여백
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.vector),
                        contentDescription = "사진 변경",
                        tint = AppColors.light3Gray,
                        modifier = Modifier.size(22.dp) // 🔥 카메라 아이콘 사이즈
                    )
                }
            }
        }
    }
}

/**
 * 식재료명 필드 (텍스트 입력)
 */
@Composable
fun FoodInfoRow(
    label: String,
    isEditMode: Boolean,
    value: String,
    onValueChange: (String) -> Unit = {}
) {
    FoodInfoRowLayout(label = label, isEditMode = isEditMode) {
        if (isEditMode) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = AppFonts.size14Body2,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = value,
                style = AppFonts.size14Body2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 드롭다운 필드 (카테고리, 보관방식)
 */
@Composable
fun FoodDropdownRow(
    label: String,
    isEditMode: Boolean,
    value: String,
    options: List<String>,
    isBadge: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FoodInfoRowLayout(
        label = label,
        isEditMode = isEditMode,
        onClick = { if (isEditMode) expanded = true }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (isEditMode) {
                // 수정 모드
                Text(text = value, style = AppFonts.size14Body2)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                // 읽기 모드
                if (!isBadge) {
                    // 🔥 배지 제거 → 텍스트만
                    Text(
                        text = value,
                        style = AppFonts.size14Body2
                    )
                } else {
                    // 기존 배지 유지
                    Surface(
                        color = AppColors.point,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = value,
                            style = AppFonts.size14Body2,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppColors.white).wrapContentSize(),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = AppFonts.size12Caption1) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    modifier = Modifier.height(32.dp)
                )
            }
        }
    }
}

/**
 * 유통기한 필드 (캘린더)
 */
@Composable
fun FoodCalendarRow(
    label: String,
    isEditMode: Boolean,
    date: Date,
    onCalendarClick: () -> Unit
) {
    FoodInfoRowLayout(
        label = label,
        isEditMode = isEditMode,
        onClick = { if (isEditMode) onCalendarClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = date.toyyMMddWithDay(), style = AppFonts.size14Body2)

            if (!isEditMode) {
                // 읽기 모드: D-Day 표시
                val dDay = date.getDDay()
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = if (dDay >= 0) AppColors.main else AppColors.dartGray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (dDay >= 0) "D-$dDay" else "D+${Math.abs(dDay)}",
                        style = AppFonts.size12Caption1,
                        color = AppColors.white,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else {
                // 수정 모드: 캘린더 아이콘
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.black
                )
            }
        }
    }
}

/**
 * 메모 필드 (3줄 고정)
 */
@Composable
fun FoodMemoRow(
    label: String,
    isEditMode: Boolean,
    value: String,
    onValueChange: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp), // 🔥 3줄 고정 높이 (24dp * 3)
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = AppFonts.size12Caption1,
            color = AppColors.main,
            modifier = Modifier
                .width(58.dp)
                .padding(top = 4.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(
                    if (isEditMode) AppColors.light4Gray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (isEditMode) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = AppFonts.size14Body2,
                    modifier = Modifier.fillMaxSize(),
                    maxLines = 3
                )
            } else {
                Text(
                    text = value,
                    style = AppFonts.size14Body2,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 공통 필드 레이아웃
 */
@Composable
private fun FoodInfoRowLayout(
    label: String,
    isEditMode: Boolean,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = AppFonts.size12Caption1,
            color = AppColors.main,
            modifier = Modifier.width(58.dp)
        )

        Box(
            modifier = Modifier
                .wrapContentWidth() // 🔥 텍스트 길이만큼만 차지
                .height(28.dp)
                .background(
                    if (isEditMode) AppColors.light4Gray else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .then(
                    if (isEditMode && onClick != null) {
                        Modifier.clickable { onClick() }
                    } else Modifier
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// 📁 버튼 영역
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

@Composable
private fun ActionButtons(
    isEditMode: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onConsumptionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val btnMod = Modifier.weight(1f).height(45.dp)

        if (isEditMode) {
            // 수정 모드: [취소] [저장]
            OutlinedButton(
                onClick = onCancelClick,
                modifier = btnMod,
                shape = RoundedCornerShape(23.dp),
                border = BorderStroke(1.dp, AppColors.main)
            ) {
                Text("취소", style = AppFonts.size14Body2, color = AppColors.main)
            }
            Button(
                onClick = onSaveClick,
                modifier = btnMod,
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.main)
            ) {
                Text("저장", style = AppFonts.size14Body2, color = AppColors.white)
            }
        } else {
            // 읽기 모드: [수정하기] [소비완료]
            OutlinedButton(
                onClick = onEditClick,
                modifier = btnMod,
                shape = RoundedCornerShape(23.dp),
                border = BorderStroke(1.dp, AppColors.main)
            ) {
                Text("수정하기", style = AppFonts.size14Body2, color = AppColors.main)
            }
            Button(
                onClick = onConsumptionClick,
                modifier = btnMod,
                shape = RoundedCornerShape(23.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.main)
            ) {
                Text("소비완료", style = AppFonts.size14Body2, color = AppColors.white)
            }
        }
    }
}