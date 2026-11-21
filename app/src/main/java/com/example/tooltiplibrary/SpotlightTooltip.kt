package com.example.tooltiplibrary

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

// دالة مساعدة: coerceIn آمنة، لو الـ Range اتقلب ترجع min بدل ما تكرّش
private fun Int.safeCoerceIn(min: Int, max: Int): Int =
    coerceIn(min, maxOf(min, max))

@Composable
fun SpotlightTooltip(
    targetBounds: () -> Rect?,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val gapPx = with(density) { 8.dp.roundToPx() }
    val marginPx = with(density) { 16.dp.roundToPx() }
    val cornerPx = with(density) { 12.dp.roundToPx() }
    val minBubbleWidthPx = with(density) { 160.dp.roundToPx() }

    Layout(
        modifier = modifier.fillMaxSize(),   // الـ Layout ياخد حجم الأب كله
        content = {
            // child 0: الصندوق
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(description, color = Color.DarkGray)
            }
            // child 1: السهم
            Canvas(Modifier.size(width = 16.dp, height = 8.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(path, Color.White)
            }
        }
    ) { measurables, constraints ->

        // حماية: لو الـ Constraints مش محددة، ما نرسمش حاجة
        if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
            Log.w("Spotlight", "Unbounded constraints: $constraints")
            return@Layout layout(0, 0) {}
        }

        val containerW = constraints.maxWidth
        val containerH = constraints.maxHeight

        // 1) القياس
        val maxBubbleW = (containerW - 2 * marginPx).coerceAtLeast(0)
        val bubble = measurables[0].measure(
            Constraints(
                minWidth = minBubbleWidthPx.coerceAtMost(maxBubbleW),
                maxWidth = maxBubbleW
            )
        )
        val arrow = measurables[1].measure(Constraints())

        layout(containerW, containerH) {
            val bounds = targetBounds() ?: return@layout

            Log.d(
                "Spotlight",
                "container=${containerW}x$containerH bubble=${bubble.width}x${bubble.height} bounds=$bounds"
            )

            // 2) فوق ولا تحت؟
            val showBelow = bounds.center.y < containerH / 2f

            val arrowY: Int
            val bubbleY: Int
            if (showBelow) {
                arrowY = (bounds.bottom + gapPx).roundToInt()
                bubbleY = arrowY + arrow.height
            } else {
                arrowY = (bounds.top - gapPx - arrow.height).roundToInt()
                bubbleY = arrowY - bubble.height
            }

            // 3) الأفقي: الصندوق جوه الشاشة
            val bubbleX = (bounds.center.x - bubble.width / 2f)
                .roundToInt()
                .safeCoerceIn(marginPx, containerW - marginPx - bubble.width)

            // 4) السهم جوه حدود الصندوق
            val arrowX = (bounds.center.x - arrow.width / 2f)
                .roundToInt()
                .safeCoerceIn(
                    bubbleX + cornerPx,
                    bubbleX + bubble.width - cornerPx - arrow.width
                )

            bubble.place(bubbleX, bubbleY)
            arrow.placeWithLayer(arrowX, arrowY) {
                rotationZ = if (showBelow) 0f else 180f
            }
        }
    }
}