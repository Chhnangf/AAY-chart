package com.aay.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aay.compose.baseComponents.model.GridOrientation
import com.aay.compose.baseComponents.model.LegendPosition
import com.aay.compose.lineChart.LineChart
import com.aay.compose.lineChart.model.LineParameters
import com.aay.compose.lineChart.model.LineType
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LineChartSample() {
    val items = (8800..8830).map { (sin((2 * PI * it) / 28) * 100).toInt() }
    val testLineParameters: List<LineParameters> = listOf(
        LineParameters(
            label = "revenue",
            data = items.map { it.toDouble() },
            lineColor = Color.Gray,
            lineType = LineType.CURVED_LINE,
            lineShadow = true,
        ),
    )

    Box(Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp)) {
        LineChart(
            modifier = Modifier.fillMaxSize(),
            linesParameters = testLineParameters,
            isGrid = true,
            gridColor = Color.Gray,
            xAxisData = items.mapIndexed { index, d -> index.toString() },
            animateChart = true,
            showGridWithSpacer = false,
            yAxisStyle = TextStyle(
                fontSize = 14.sp,
                color = Color.Gray,
            ),
            xAxisStyle = TextStyle(
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.W400
            ),
            yAxisRange = items.size,
            oneLineChart = false,
            gridOrientation = GridOrientation.GRID,
            legendPosition = LegendPosition.TOP
        )
    }
}