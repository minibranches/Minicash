package com.uramos.minicash.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object MinicashShapes {
    val Sharp = RoundedCornerShape(4.dp)
    val Element = RoundedCornerShape(8.dp)
    val Input = RoundedCornerShape(12.dp)
    val Compact = RoundedCornerShape(16.dp)
    val Card = RoundedCornerShape(20.dp)
    val ContainerLarge = RoundedCornerShape(24.dp)
    val Hero = RoundedCornerShape(28.dp)
    val Pill = CircleShape
    val FABShape = RoundedCornerShape(18.dp)
}

val Shapes = Shapes(
    extraSmall = MinicashShapes.Sharp,
    small = MinicashShapes.Element,
    medium = MinicashShapes.Input,
    large = MinicashShapes.Compact,
    extraLarge = MinicashShapes.Card
)
