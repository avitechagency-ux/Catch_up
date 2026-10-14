package com.example.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Modifier for a hand-drawn "pencil sketch" border effect.
 */
fun Modifier.pencilSketchBorder(
    color: Color = Color.Gray,
    strokeWidth: Dp = 2.dp,
    jitter: Dp = 1.dp
): Modifier = this.drawBehind {
    val path = Path()
    val strokeWidthPx = strokeWidth.toPx()
    val jitterPx = jitter.toPx()
    
    // Simple jittered rectangle path
    fun addJitteredLine(start: Offset, end: Offset) {
        val steps = 10
        for (i in 0 until steps) {
            val t1 = i.toFloat() / steps
            val t2 = (i + 1).toFloat() / steps
            
            val p1 = Offset(
                start.x + (end.x - start.x) * t1 + Random.nextFloat() * jitterPx,
                start.y + (end.y - start.y) * t1 + Random.nextFloat() * jitterPx
            )
            val p2 = Offset(
                start.x + (end.x - start.x) * t2 + Random.nextFloat() * jitterPx,
                start.y + (end.y - start.y) * t2 + Random.nextFloat() * jitterPx
            )
            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
        }
    }

    addJitteredLine(Offset(0f, 0f), Offset(size.width, 0f))
    addJitteredLine(Offset(size.width, 0f), Offset(size.width, size.height))
    addJitteredLine(Offset(size.width, size.height), Offset(0f, size.height))
    addJitteredLine(Offset(0f, size.height), Offset(0f, 0f))

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )
}
