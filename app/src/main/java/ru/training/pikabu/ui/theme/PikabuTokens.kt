package ru.training.pikabu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object PikabuDimensions {
    val paddingSmall = 4.dp
    val paddingMedium = 8.dp
    val paddingLarge = 16.dp
    val paddingExtraLarge = 24.dp

    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
    val iconSizeExtraLarge = 40.dp
}

object PikabuShapes {
    private val cornerRadiusSmall = 4.dp
    private val cornerRadiusMedium = 8.dp
    private val cornerRadiusLarge = 16.dp

    val Shapes = Shapes(
        small = RoundedCornerShape(cornerRadiusSmall),
        medium = RoundedCornerShape(cornerRadiusMedium),
        large = RoundedCornerShape(cornerRadiusLarge)
    )
}
