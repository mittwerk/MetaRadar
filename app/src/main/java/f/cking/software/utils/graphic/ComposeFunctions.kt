package f.cking.software.utils.graphic

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.flowlayout.FlowRow
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogButtons
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.DatePickerColors
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerColors
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import f.cking.software.R
import f.cking.software.domain.model.DeviceData
import f.cking.software.domain.model.ExtendedAddressInfo
import f.cking.software.dpToPx
import f.cking.software.pxToDp
import f.cking.software.toHexString
import f.cking.software.ui.GlobalUiState
import f.cking.software.ui.devicelist.DeviceListScreen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun rememberDateDialog(
    initialDate: LocalDate = LocalDate.now(),
    datePickerColors: DatePickerColors = DatePickerDefaults.colors(
        headerBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
        headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        calendarHeaderTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        dateActiveBackgroundColor = MaterialTheme.colorScheme.primary,
        dateActiveTextColor = MaterialTheme.colorScheme.onPrimary,
        dateInactiveBackgroundColor = Color.Transparent,
        dateInactiveTextColor = MaterialTheme.colorScheme.onSurface,
    ),
    dateResult: (date: LocalDate) -> Unit,
): MaterialDialogState {
    val dialogState = rememberMaterialDialogState()
    ThemedDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(
                stringResource(R.string.ok),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ) { dialogState.hide() }
            negativeButton(
                stringResource(R.string.cancel),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ) { dialogState.hide() }
        },
    ) {
        datepicker(initialDate = initialDate, colors = datePickerColors) { localDate ->
            dateResult.invoke(localDate)
        }
    }
    return dialogState
}

@Composable
fun rememberTimeDialog(
    initialTime: LocalTime = LocalTime.now(),
    timePickerColors: TimePickerColors = TimePickerDefaults.colors(
        activeBackgroundColor = MaterialTheme.colorScheme.primary,
        activeTextColor = MaterialTheme.colorScheme.onPrimary,
        inactiveBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
        inactiveTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        inactivePeriodBackground = Color.Transparent,
        selectorColor = MaterialTheme.colorScheme.primary,
        selectorTextColor = MaterialTheme.colorScheme.onPrimary,
        headerTextColor = MaterialTheme.colorScheme.onSurface,
        borderColor = Color.Transparent,
    ),
    dateResult: (date: LocalTime) -> Unit,
): MaterialDialogState {
    val dialogState = rememberMaterialDialogState()
    ThemedDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(
                stringResource(R.string.ok),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ) { dialogState.hide() }
            negativeButton(
                stringResource(R.string.cancel),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ) { dialogState.hide() }
        },
    ) {
        timepicker(is24HourClock = true, initialTime = initialTime, colors = timePickerColors) { localDate ->
            dateResult.invoke(localDate)
        }
    }
    return dialogState
}

@Composable
fun rememberProgressDialog(
    text: String,
): MaterialDialogState {
    val dialogState = rememberMaterialDialogState()
    ThemedDialog(
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        dialogState = dialogState,
        autoDismiss = false,
        buttons = {},
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = text, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            CircularProgressIndicator()
        }
    }
    return dialogState
}

@Composable
fun infoDialog(
    title: String,
    content: String?,
    buttons: ((state: MaterialDialogState) -> (@Composable MaterialDialogButtons.() -> Unit))? = null,
): MaterialDialogState {
    val dialogState = rememberMaterialDialogState()
    ThemedDialog(
        dialogState = dialogState,
        buttons = buttons?.invoke(dialogState) ?: {
            positiveButton(
                stringResource(R.string.ok),
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface)
            ) { dialogState.hide() }
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            if (!content.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = content)
            }
        }
    }
    return dialogState
}

@Composable
fun ThemedDialog(
    dialogState: MaterialDialogState = rememberMaterialDialogState(),
    properties: DialogProperties = DialogProperties(),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke? = null,
    elevation: Dp = 24.dp,
    autoDismiss: Boolean = true,
    onCloseRequest: (MaterialDialogState) -> Unit = { it.hide() },
    buttons: @Composable MaterialDialogButtons.() -> Unit = {},
    content: @Composable MaterialDialogScope.() -> Unit
) {
    MaterialDialog(
        dialogState = dialogState,
        properties = properties,
        backgroundColor = backgroundColor,
        shape = shape,
        border = border,
        elevation = elevation,
        autoDismiss = autoDismiss,
        onCloseRequest = onCloseRequest,
        buttons = buttons,
        content = content
    )
}

@Composable
fun ClickableField(
    modifier: Modifier = Modifier,
    text: String?,
    placeholder: String?,
    label: String?,
    onClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val unfocuse = remember { mutableStateOf(false) }
    if (unfocuse.value) {
        focusManager.clearFocus(true)
        unfocuse.value = false
    }
    TextField(
        modifier = modifier
            .onFocusChanged {
                if (it.isFocused) {
                    unfocuse.value = true
                    onClick.invoke()
                }
            },
        value = text ?: "",
        onValueChange = {},
        readOnly = true,
        label = label?.let { { Text(text = it) } },
        placeholder = placeholder?.let { { Text(text = it) } },
    )
}

@Composable
fun DeviceListItem(
    modifier: Modifier = Modifier,
    device: DeviceData,
    showSignalData: Boolean = false,
    showLastUpdate: Boolean = true,
    onTagSelected: (tag: String) -> Unit = {},
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick.invoke() },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            DeviceTypeIcon(
                modifier = Modifier.size(64.dp),
                device = device,
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Row(verticalAlignment = Alignment.Top) {
                    if (device.favorite) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = stringResource(R.string.is_favorite),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if (device.isPaired) {
                        DevicePairedIcon(true)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = device.resolvedName ?: stringResource(R.string.not_applicable),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (showSignalData) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SignalData(rssi = device.rssi, distance = device.distance())
                    }
                }
                device.tags.takeIf { it.isNotEmpty() }?.let { tags ->
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth(),
                        mainAxisSpacing = 4.dp,
                    ) {
                        tags.forEachIndexed { index, tag ->
                            TagChip(tagName = tag, onClick = { onTagSelected.invoke(tag) })
                        }
                    }
                }
                device.resolvedManufacturerName?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it)
                }
                device.manufacturerInfo?.airdrop?.let { airdrop ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = airdrop.contacts.joinToString { "0x${it.sha256.toHexString().uppercase()}" })
                }
                Spacer(modifier = Modifier.height(4.dp))
                ExtendedAddressView(device.extendedAddressInfo())
                Spacer(modifier = Modifier.height(4.dp))

                val updateStr = if (showLastUpdate) {
                    stringResource(
                        R.string.lifetime_data_last_update,
                        device.firstDetectionPeriod(LocalContext.current),
                        device.lastDetectionPeriod(LocalContext.current),
                    )
                } else {
                    stringResource(
                        R.string.lifetime_data,
                        device.firstDetectionPeriod(LocalContext.current),
                    )
                }
                Text(
                    text = updateStr,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
fun DevicePairedIcon(isPaired: Boolean, extended: Boolean = false) {
    if (isPaired) {
        val color = colorResource(R.color.blue_600)
        val infoDialog = infoDialog(
            title = stringResource(id = R.string.bluetooth_status_paired_description),
            content = null,
        )
        Row(
            modifier = Modifier.background(color.copy(0.2f), RoundedCornerShape(20.dp))
                .clickable { infoDialog.show() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_ble_paired),
                contentDescription = stringResource(R.string.bluetooth_status_paired),
                tint = color
            )
            if (extended) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.bluetooth_status_paired),
                    color = color,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
fun DeviceTypeIcon(
    modifier: Modifier = Modifier.size(64.dp),
    device: DeviceData,
    paddingDp: Dp = 16.dp
) {
    val icon = remember(device) { GetIconForDeviceClass.getIcon(device) }
    val color = colorByHash(device.address.hashCode())
    Icon(
        modifier = modifier.background(color.copy(0.2f), CircleShape)
            .padding(paddingDp),
        painter = painterResource(icon),
        contentDescription = stringResource(R.string.device_type),
        tint = color
    )
}

@Composable
fun ExtendedAddressView(
    extendedAddressInfo: ExtendedAddressInfo,
) {

    Row {
        Text(
            text = extendedAddressInfo.address,
            fontWeight = FontWeight.Light,
        )
        val chip = extendedAddressInfo.type.toChip()
        if (chip != null) {

            val dialog = infoDialog(
                title = stringResource(id = chip.descriptionRes),
                content = stringResource(id = R.string.address_private_disclamer)
            )

            Spacer(modifier = Modifier.width(8.dp))
            val color = chip.color.invoke()
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.2f))
                    .clickable { dialog.show() }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = chip.titleRes),
                    color = color,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    modifier = Modifier
                        .size(12.dp),
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.app_info_title),
                    tint = color
                )
            }
        }
    }
}

private fun ExtendedAddressInfo.BleAddressType.toChip(): ExtendedAddressInfoChip? {
    return when (this) {
        ExtendedAddressInfo.BleAddressType.PUBLIC -> ExtendedAddressInfoChip.PUBLIC
        ExtendedAddressInfo.BleAddressType.STATIC_RANDOM -> ExtendedAddressInfoChip.RANDOM
        ExtendedAddressInfo.BleAddressType.NON_RESOLVABLE_PRIVATE -> ExtendedAddressInfoChip.NON_RESOLVABLE
        ExtendedAddressInfo.BleAddressType.RESOLVABLE_PRIVATE -> ExtendedAddressInfoChip.RESOLVABLE
        ExtendedAddressInfo.BleAddressType.INVALID -> null
    }
}

private enum class ExtendedAddressInfoChip(
    val titleRes: Int,
    val descriptionRes: Int,
    val color: @Composable () -> Color,
) {
    PUBLIC(
        titleRes = R.string.address_type_public_tag,
        descriptionRes = R.string.address_type_public_description,
        color = { colorResource(R.color.address_tag_stp) },
    ),
    RANDOM(
        titleRes = R.string.address_type_random_static_tag,
        descriptionRes = R.string.address_type_random_static_description,
        color = { colorResource(R.color.address_tag_rst) },
    ),
    NON_RESOLVABLE(
        titleRes = R.string.address_type_non_resolvable_tag,
        descriptionRes = R.string.address_type_non_resolvable_description,
        color = { colorResource(R.color.address_tag_nrp) },
    ),
    RESOLVABLE(
        titleRes = R.string.address_type_resolvable_private_tag,
        descriptionRes = R.string.address_type_resolvable_private_description,
        color = { colorResource(R.color.address_tag_rpa) },
    ),
}

@Composable
fun SignalData(rssi: Int?, distance: Float?) {
    Column(horizontalAlignment = Alignment.End) {
        distance?.let { distance ->
            val distanceStr = if (distance < 2) "%.1f".format(distance) else distance.toInt().toString()
            val infoDialog = infoDialog(
                title = stringResource(id = R.string.disclaimer),
                content = stringResource(id = R.string.device_distance_disclaimer)
            )
            Row(modifier = Modifier.clickable { infoDialog.show() }, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.distance_to_device, distanceStr),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .alpha(0.5f),
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.is_favorite),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        rssi?.let { rssi ->
            Text(text = stringResource(id = R.string.rssi_value, rssi), fontSize = 14.sp, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        Box(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun ContentPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(R.drawable.ic_ghost),
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier.size(100.dp),
                painter = icon,
                contentDescription = text,
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RoundedBox(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
    internalPaddings: Dp = 16.dp,
    boxContent: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier) {
        val shape = RoundedCornerShape(corner = CornerSize(8.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = shape)
                .clip(shape = shape)
                .padding(internalPaddings)
        ) { boxContent(this) }
    }
}

private val colorsLight = listOf(
    Color(0xFFE57373),
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF9575CD),
    Color(0xFF7986CB),
    Color(0xFF64B5F6),
    Color(0xFF4FC3F7),
    Color(0xFF4DD0E1),
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFAED581),
    Color(0xFFFF8A65),
    Color(0xFFD4E157),
    Color(0xFFFFD54F),
    Color(0xFFFFB74D),
    Color(0xFFA1887F),
    Color(0xFF90A4AE),
)

private val colorsDark = listOf(
    Color(0xFF813535),
    Color(0xFF742A43),
    Color(0xFF5E2F66),
    Color(0xFF443066),
    Color(0xFF363E69),
    Color(0xFF2F5574),
    Color(0xFF275A70),
    Color(0xFF2D6A72),
    Color(0xFF235E58),
    Color(0xFF457047),
    Color(0xFF546D37),
    Color(0xFF885241),
    Color(0xFF6A7030),
    Color(0xFF776426),
    Color(0xFF7C643F),
    Color(0xFF7A5446),
    Color(0xFF3D545F),
)

@Composable
fun colorByHash(hash: Int): Color {
    val colors = if (isSystemInDarkTheme()) colorsDark else colorsLight
    return colors[abs(Random(hash).nextInt() % colors.size)]
}

@Composable
fun TagChip(
    tagName: String,
    tagIcon: ImageVector? = null,
    onClick: () -> Unit = {},
) {
    AssistChip(
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colorByHash(tagName.hashCode()),
            labelColor = Color.Black,
            leadingIconContentColor = Color.Black,
        ),
        border = null,
        onClick = onClick,
        leadingIcon = { tagIcon?.let { Icon(imageVector = it, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) } },
        label = {
            Text(text = tagName, color = MaterialTheme.colorScheme.onSurface)
        }
    )
}

@Composable
fun dpToPx(dp: Float): Float {
    return LocalContext.current.dpToPx(dp).toFloat()
}

@Composable
fun pxToDp(px: Float): Float {
    return LocalContext.current.pxToDp(px)
}

@Composable
fun BottomNavigationSpacer() {
    val bottomOffset = remember { GlobalUiState.navbarOffsetPx }
    Column {
        Spacer(modifier = Modifier.height(pxToDp(bottomOffset.value).dp))
    }
}

@Composable
fun FABSpacer() {
    val bottomOffset = remember { GlobalUiState.totalOffset }
    Column {
        Spacer(modifier = Modifier.height(pxToDp(bottomOffset.value).dp))
        SystemNavbarSpacer()
    }
}

@Composable
fun SystemNavbarSpacer() {
    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
}

fun ColorScheme.surfaceEvaluated(evaluation: Dp = 3.dp): Color {
    return this.surfaceColorAtElevation(evaluation)
}

@Composable
fun Switcher(
    modifier: Modifier = Modifier,
    value: Boolean,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
) {
    Box(modifier = modifier
        .fillMaxWidth()
        .clickable { onClick.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(text = title)
                subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = it, fontWeight = FontWeight.Light, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = value,
                onCheckedChange = { onClick.invoke() }
            )
        }
    }
}

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun RadarIcon() {
    var atEnd by remember { mutableStateOf(false) }
    val image = AnimatedImageVector.animatedVectorResource(id = R.drawable.radar_animation)
    val animatedPainter = DeviceListScreen.rememberAnimatedVectorPainterCompat(image, atEnd)
    LaunchedEffect(Unit) {
        while (true) {
            delay(image.totalDuration.toLong())
            atEnd = !atEnd
        }
    }
    Image(
        painter = animatedPainter,
        contentDescription = null,
    )
}

@Composable
fun ListItem(icon: Painter, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painter = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = subtitle)
        }
    }
}