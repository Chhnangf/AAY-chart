package com.aay.compose.lineChart.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.aay.compose.lineChart.model.LineParameters
import com.aay.compose.utils.clickedOnThisPoint
import com.aay.compose.utils.formatToThousandsMillionsBillions

private var lastClickedPoint: Pair<Float, Float>? = null

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawQuarticLineWithShadow(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    spacingX: Dp,
    spacingY: Dp,
    specialChart: Boolean,
    clickedPoints: MutableList<Pair<Float, Float>>,
    xRegionWidth: Dp,
    textMeasurer: TextMeasurer,
) {
    val strokePathOfQuadraticLine = drawLineAsQuadratic(
        line = line,
        lowerValue = lowerValue,
        upperValue = upperValue,
        animatedProgress = animatedProgress,
        spacingY = spacingY,
        specialChart = specialChart,
        clickedPoints = clickedPoints,
        textMeasurer = textMeasurer,
        xRegionWidth = xRegionWidth
    )

    if (line.lineShadow && !specialChart) {
        val fillPath = strokePathOfQuadraticLine.apply {
            lineTo(size.width - xRegionWidth.toPx() + 40.dp.toPx(), size.height * 40)
            lineTo(spacingX.toPx() * 2, size.height * 40)
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

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawLineAsQuadratic(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    spacingY: Dp,
    specialChart: Boolean,
    clickedPoints: MutableList<Pair<Float, Float>>,
    textMeasurer: TextMeasurer,
    xRegionWidth: Dp
) = Path().apply {
    var medX: Float
    val height = size.height.toDp()

    drawPathLineWrapper(
        lineParameter = line,
        strokePath = this,
        animatedProgress = animatedProgress,
    ) { lineParameter, index ->

        val yTextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(upperValue.formatToThousandsMillionsBillions()),
        ).size.width
        val textSpace = yTextLayoutResult - (yTextLayoutResult/4)

        val info = lineParameter.data[index]
        val nextInfo = lineParameter.data.getOrNull(index + 1) ?: lineParameter.data.last()
        val firstRatio = (info - lowerValue) / (upperValue - lowerValue)
        val secondRatio = (nextInfo - lowerValue) / (upperValue - lowerValue)

        val xFirstPoint = (textSpace * 1.5.toFloat().toDp()) + index * xRegionWidth
        val xSecondPoint =
            (textSpace * 1.5.toFloat().toDp()) + (index + checkLastIndex(
                lineParameter.data,
                index
            )) * xRegionWidth

        val yFirstPoint = (height.toPx()
                + 11.dp.toPx()
                - spacingY.toPx()
                - (firstRatio * (size.height.toDp() - spacingY).toPx())
                )
        val ySecondPoint = (height.toPx()
                + 11.dp.toPx()
                - spacingY.toPx()
                - (secondRatio * (size.height.toDp() - spacingY).toPx())
                )

        val tolerance = 20.dp.toPx()
        val savedClicks =
            clickedOnThisPoint(clickedPoints, xFirstPoint.toPx(), yFirstPoint, tolerance)
        if (savedClicks) {
            if (lastClickedPoint != null) {
                clickedPoints.clear()
                lastClickedPoint = null
            } else {
                lastClickedPoint = Pair(xFirstPoint.toPx(), yFirstPoint.toFloat())
                circleWithRectAndText(
                    x = xFirstPoint,
                    y = yFirstPoint,
                    textMeasure = textMeasurer,
                    info = info,
                    stroke = Stroke(width = 2.dp.toPx()),
                    line = line,
                    animatedProgress = animatedProgress
                )
            }

        }

        if (index == 0) {
            moveTo(xFirstPoint.toPx(), yFirstPoint.toFloat())
            medX = ((xFirstPoint + xSecondPoint) / 2f).toPx()
            cubicTo(
                medX,
                yFirstPoint.toFloat(),
                medX,
                ySecondPoint.toFloat(),
                xSecondPoint.toPx(),
                ySecondPoint.toFloat()
            )
        } else {
            medX = ((xFirstPoint + xSecondPoint) / 2f).toPx()
            cubicTo(
                medX,
                yFirstPoint.toFloat(),
                medX,
                ySecondPoint.toFloat(),
                xSecondPoint.toPx(),
                ySecondPoint.toFloat()
            )
        }

        if (index == 0 && specialChart) {
            chartCircle(
                xFirstPoint.toPx(),
                yFirstPoint.toFloat(),
                color = lineParameter.lineColor,
                animatedProgress = animatedProgress,
            )
        }
    }
}

private fun checkLastIndex(data: List<Double>, index: Int): Int {
    return if (data[index] == data[data.lastIndex])
        0
    else
        1
}



@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawLineAsCatmullRom(
    line: LineParameters,
    lowerValue: Float,
    upperValue: Float,
    animatedProgress: Animatable<Float, AnimationVector1D>,
    spacingX: Dp,
    spacingY: Dp,
    specialChart: Boolean,
    clickedPoints: MutableList<Pair<Float, Float>>,
    textMeasurer: TextMeasurer,
    xRegionWidth: Dp
) = Path().apply {
    val height = size.height.toDp()
    var allPoints = mutableListOf<Pair<Float, Float>>()

    drawPathLineWrapper(
        lineParameter = line,
        strokePath = this,
        animatedProgress = animatedProgress,
    ) { lineParameter, index ->
        // 计算每个点的位置
        val info = lineParameter.data[index]
        val nextInfo = lineParameter.data.getOrNull(index + 1) ?: lineParameter.data.last()
        val firstRatio = (info - lowerValue) / (upperValue - lowerValue)
        val secondRatio = (nextInfo - lowerValue) / (upperValue - lowerValue)

        // 确保 yTextLayoutResult 和 textSpace 的计算是必要的，并且它们影响到 x 轴位置
        val yTextLayoutResult = textMeasurer.measure(
            text = AnnotatedString(upperValue.formatToThousandsMillionsBillions()),
        ).size.width
        val textSpace = yTextLayoutResult - (yTextLayoutResult / 4)

        val xFirstPoint = (textSpace * 1.5f) + index * spacingX.toPx()
        val yFirstPoint = height.toPx() - spacingY.toPx() - (firstRatio * (size.height.toDp() - spacingY).toPx())

        // Add current point to list of all points
        allPoints.add(Pair(xFirstPoint, yFirstPoint.toFloat()))

        // Handle click events and other drawing logic...
        // ...
    }

    // Compute and draw the Catmull-Rom spline after collecting all points
    if (allPoints.size >= 4) { // Catmull-Rom 需要至少四个点才能工作
        val catmullRomPoints = computeCatmullRomPoints(allPoints)

        if (catmullRomPoints.isNotEmpty()) {
            // 解构 Pair 以获取 x 和 y
            val (firstX, firstY) = catmullRomPoints.first()
            moveTo(firstX, firstY)

            catmullRomPoints.forEachIndexed { idx, (x, y) ->
                if (idx > 0) {
                    lineTo(x, y)
                }
            }

        }
    } else {
        // If there are not enough points for a Catmull-Rom spline, draw straight lines instead
        if (allPoints.size >= 2) {
            val (firstX, firstY) = allPoints.first()
            moveTo(firstX, firstY)

            for (i in 1 until allPoints.size) {
                val (x, y) = allPoints[i]
                lineTo(x, y)
            }
        }
    }

    // Ensure the path is drawn on the canvas
    drawPath(
        path = this,
        color = line.lineColor,
        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
    )
}


fun computeCatmullRomPoints(
    points: List<Pair<Float, Float>>,
    tension: Float = 0.5f,
    numSegments: Int = 20
): List<Pair<Float, Float>> {
    val result = mutableListOf<Pair<Float, Float>>()

    // Handle boundary conditions by duplicating the first and last points
    val extendedPoints = listOf(points.first()) + points + listOf(points.last())

    for (i in 1 until extendedPoints.size - 2) {
        val p0 = extendedPoints[i - 1]
        val p1 = extendedPoints[i]
        val p2 = extendedPoints[i + 1]
        val p3 = extendedPoints[i + 2]

        for (j in 0..numSegments) {
            val t = j / numSegments.toFloat()
            val tt = t * t
            val ttt = tt * t

            val x = 0.5f * (
                    (2f * p1.first) +
                            (-p0.first + p2.first) * t +
                            (2f * p0.first - 5f * p1.first + 4f * p2.first - p3.first) * tt +
                            (-p0.first + 3f * p1.first - 3f * p2.first + p3.first) * ttt
                    )

            val y = 0.5f * (
                    (2f * p1.second) +
                            (-p0.second + p2.second) * t +
                            (2f * p0.second - 5f * p1.second + 4f * p2.second - p3.second) * tt +
                            (-p0.second + 3f * p1.second - 3f * p2.second + p3.second) * ttt
                    )

            result.add(Pair(x, y))
        }
    }

    return result
}