package za.varsitycollege.kronosapp

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.layout.fillMaxSize
import kotlin.random.Random
import androidx.compose.ui.geometry.Offset


class ConfettiEffect {
    private val confettiColors = listOf(
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFEB3B),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFFF5722)
    )

    @Composable
    fun Draw(alpha: Float) {
        val confettiPieces = remember { List(100) { ConfettiPiece(color = confettiColors.random()) } }

        Canvas(modifier = Modifier.fillMaxSize()) {
            confettiPieces.forEach { confetti ->
                rotate(degrees = confetti.rotation, pivot = confetti.position) {
                    translate(left = confetti.position.x, top = confetti.position.y) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(confetti.size, 0f)
                            lineTo(confetti.size / 2, confetti.size)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = confetti.color,
                            style = if (alpha > 0.5f) Fill else Stroke(width = 2f),
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }

    private data class ConfettiPiece(
        val position: Offset = Offset(
            x = (Random.nextFloat() * 800).toFloat(),
            y = (Random.nextFloat() * 1200).toFloat()
        ),
        val size: Float = (Random.nextFloat() * 20 + 10).toFloat(),
        val rotation: Float = (Random.nextFloat() * 360).toFloat(),
        val color: Color
    )
}