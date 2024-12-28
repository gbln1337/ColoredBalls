package com.example.coloredballs

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.widget.FrameLayout


class MainActivity : AppCompatActivity() {

    private lateinit var paint: Paint
    private var circles = mutableListOf<Circle>()
    private var rectangle: Rectangle? = null
    private var remainingCircles = 0
    private lateinit var gameView: GameView
    private var lastRemovedCircle: Circle? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paint = Paint()
        remainingCircles = Colors.resetColors().size

        gameView = GameView(this)
        val frameLayout = findViewById<FrameLayout>(R.id.mainLayout)
        frameLayout.addView(gameView)
        rectangle = createRectangle()
        createCircles()
    }

    private fun createRectangle(): Rectangle {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val rectWidth = 300f
        val rectHeight = 200f
        val left = (screenWidth - rectWidth) / 2f
        val top = (screenHeight - rectHeight) / 2f
        val right = left + rectWidth
        val bottom = top + rectHeight
        val initialColor = Colors.getInitialColor() ?: Color.WHITE
        return Rectangle(left, top, right, bottom, initialColor)
    }

    private fun createCircles() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val rectLeft = rectangle!!.left
        val rectTop = rectangle!!.top
        val rectRight = rectangle!!.right
        val rectBottom = rectangle!!.bottom
        val colors = Colors.resetColors()
        for ((_, color) in colors) {
            var x: Float
            var y: Float
            do {
                x = Random.nextFloat() * (screenWidth - 100f) + 50f
                y = Random.nextFloat() * (screenHeight - 100f) + 50f
            } while (x in rectLeft..rectRight && y in rectTop..rectBottom)
            circles.add(Circle(x, y, 50f, color))
        }
    }

    private fun checkGameOver() {
        if (remainingCircles == 0 && rectangle == null) {
            val intent = Intent(this, WinActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    inner class GameView(context: Context) : View(context) {
        private var movingCircle: Circle? = null
        private var dX = 0f
        private var dY = 0f

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Рисуем прямоугольник
            rectangle?.let {
                paint.color = it.color
                canvas.drawRect(it.left, it.top, it.right, it.bottom, paint)
            }

            // Рисуем все шарики
            for (circle in circles) {
                if (circle != movingCircle) {
                    paint.color = circle.color
                    canvas.drawCircle(circle.x, circle.y, circle.radius, paint)
                }
            }

            // Рисуем движущийся шарик отдельно
            movingCircle?.let {
                paint.color = it.color
                canvas.drawCircle(it.x, it.y, it.radius, paint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    for (circle in circles) {
                        if (event.x in (circle.x - circle.radius)..(circle.x + circle.radius) &&
                            event.y in (circle.y - circle.radius)..(circle.y + circle.radius)) {
                            movingCircle = circle
                            dX = event.x - movingCircle!!.x
                            dY = event.y - movingCircle!!.y
                            return true
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    movingCircle?.let { moving ->
                        // Обновляем позицию движущегося шарика
                        moving.x = event.x - dX
                        moving.y = event.y - dY

                        // Проверяем коллизию с другими шариками
                        for (circle in circles) {
                            if (circle != moving) {
                                val distance = distanceBetween(moving, circle)
                                val overlap = moving.radius + circle.radius - distance
                                if (overlap > 0) {
                                    // Раздвигаем шарики при пересечении
                                    val angle = Math.atan2((circle.y - moving.y).toDouble(), (circle.x - moving.x).toDouble())
                                    val pushX = (overlap * Math.cos(angle)).toFloat()
                                    val pushY = (overlap * Math.sin(angle)).toFloat()

                                    circle.x += pushX / 2  // Половина силы перемещения для обоих шариков
                                    circle.y += pushY / 2
                                    moving.x -= pushX / 2
                                    moving.y -= pushY / 2
                                }
                            }
                        }
                        invalidate()
                    }
                }

                MotionEvent.ACTION_UP -> {
                    movingCircle?.let {
                        if (rectangle?.contains(it.x, it.y) == true && it.color == rectangle?.color) {
                            lastRemovedCircle = it
                            circles.remove(it)
                            remainingCircles--
                            rectangle = if (remainingCircles > 0) createRectangle() else null
                            if (remainingCircles > 0) {
                                val availableColors = circles.map { circle -> circle.color }.distinct()
                                rectangle!!.color = Colors.getRandomColor(availableColors) ?: Color.WHITE
                            }
                            checkGameOver()
                        }
                        movingCircle = null
                    }
                    invalidate()
                }
            }
            return true
        }

        // Вспомогательная функция для вычисления расстояния между двумя шариками
        private fun distanceBetween(c1: Circle, c2: Circle): Float {
            val dx = c1.x - c2.x
            val dy = c1.y - c2.y
            return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
    }

}
