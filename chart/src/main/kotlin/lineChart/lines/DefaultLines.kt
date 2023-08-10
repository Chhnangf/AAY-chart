package lineChart.lines

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import drawPathLineWrapper
import lineChart.model.LineParameters
import lineChart.model.LineShadow


fun DrawScope.drawDefaultLineWithShadow(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    xAxisSize: Int,
    spacingX:Dp,
    spacingY:Dp,
) {
    val spaceBetweenXes = (size.width.toDp() - spacingX) / xAxisSize
    val strokePathOfDefaultLine = drawLineAsDefault(
        lineParameter = line,
        lowerValue = lowerValue,
        upperValue = upperValue,
        spaceBetweenXes = spaceBetweenXes,
        animatedProgress = animatedProgress,
        xAxisSize = xAxisSize,
        spacingX =spacingX,
        spacingY=spacingY,
    )

    if (line.lineShadow == LineShadow.SHADOW) {
        val fillPath = strokePathOfDefaultLine.apply {
            lineTo((size.width.toDp() - spaceBetweenXes).toPx(), size.height - spacingY.toPx())
            lineTo(spacingX.toPx(), (size.height.toDp() - spacingY).toPx())
            close()
        }
        clipRect(right = size.width * animatedProgress.value) {
            drawPath(
                path = fillPath, brush = Brush.verticalGradient(
                    colors = listOf(
                        line.lineColor.copy(alpha = .3f), Color.Transparent
                    ), endY = (size.height.toDp() - spacingY).toPx()
                )
            )
        }
    }
}


private fun DrawScope.drawLineAsDefault(
    lineParameter: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    spaceBetweenXes: Dp,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    xAxisSize: Int,
    spacingX: Dp,
    spacingY: Dp,
) = Path().apply {

    val height = size.height.toDp()
    drawPathLineWrapper(
        lineParameter = lineParameter,
        strokePath = this,
        xAxisSize = xAxisSize,
        animatedProgress = animatedProgress,
        spacingX = spacingX,
        spacingY = spacingY,
    ) { lineParameter, index, maxX, maxY ->

        val info = lineParameter.data[index]
        val ratio = (info-lowerValue ) / (upperValue - lowerValue)
        val startXPoint = (spacingX.toPx()) +index * spaceBetweenXes.toPx()
        val startYPoint = (height.toPx() - spacingY.toPx() - (ratio * (height.toPx()-spacingY.toPx())))

//       // Adjust the coordinates to stay within boundaries
//        val xAdjusted = startXPoint.coerceAtMost(maxX.toPx() - spacingX.toPx()).coerceAtLeast(spacingX.toPx())
//        val yAdjusted = startYPoint.coerceAtMost( (maxY.plus(50.0))).coerceAtLeast( ( spacingY.toPx().toDouble()))


        if (index == 0) {
            moveTo(startXPoint, startYPoint.toFloat())
        } else {
            lineTo(startXPoint, startYPoint.toFloat())
        }
    }

}