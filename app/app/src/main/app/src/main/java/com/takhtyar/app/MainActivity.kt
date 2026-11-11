
package com.takhtyar.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.Gravity
import android.widget.*
import android.graphics.Typeface

private enum class DrawTool {
    PEN, MARKER, ERASER
}

class MainActivity : Activity() {

    private lateinit var board: DrawingBoard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        board = DrawingBoard(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(242, 244, 248))
            setPadding(8, 8, 8, 8)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }

        val title = TextView(this).apply {
            text = "تخته یار | تخته نقاشی مجازی"
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(30, 40, 60))
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
        }

        root.addView(title)

        // ابزارهای نقاشی
        val toolRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addButton(toolRow, "قلم") {
            board.setTool(DrawTool.PEN)
        }

        addButton(toolRow, "ماژیک") {
            board.setTool(DrawTool.MARKER)
        }

        addButton(toolRow, "پاک‌کن") {
            board.setTool(DrawTool.ERASER)
        }

        root.addView(toolRow)

        // انتخاب رنگ
        val colorLabel = TextView(this).apply {
            text = "انتخاب رنگ قلم:"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(6, 10, 6, 4)
        }

        root.addView(colorLabel)

        val colorScroll = HorizontalScrollView(this)

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val colors = listOf(
            Color.BLACK,
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.CYAN,
            Color.rgb(255, 140, 0),
            Color.rgb(128, 0, 128),
            Color.GRAY
        )

        colors.forEach { selectedColor ->

            val colorButton = TextView(this).apply {
                text = "●"
                textSize = 30f
                gravity = Gravity.CENTER
                setTextColor(selectedColor)
                setPadding(7, 5, 7, 5)

                setOnClickListener {
                    board.setColor(selectedColor)
                }
            }

            colorRow.addView(colorButton)
        }

        colorScroll.addView(colorRow)
        root.addView(colorScroll)

        // ضخامت قلم
        val sizeLabel = TextView(this).apply {
            text = "ضخامت قلم:"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(6, 8, 6, 2)
        }

        root.addView(sizeLabel)

        val sizeSeekBar = SeekBar(this).apply {
            max = 48
            progress = 6

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        board.setBrushSize((progress + 2).toFloat())
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {}

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {}
                }
            )
        }

        root.addView(sizeSeekBar)

        // برگشت، جلو و پاک کردن تخته
        val actionRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addButton(actionRow, "برگشت") {
            board.undo()
        }

        addButton(actionRow, "جلو") {
            board.redo()
        }

        addButton(actionRow, "پاک کردن") {
            board.clearBoard()
        }

        root.addView(actionRow)

        // فضای اصلی نقاشی
        root.addView(
            board,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply {
                setMargins(0, 10, 0, 5)
            }
        )

        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setOnClickListener {
                action()
            }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
    }
}

// کلاس اصلی تخته نقاشی
class DrawingBoard(context: android.content.Context) : View(context) {

    private data class Stroke(
        val path: Path,
        val color: Int,
        val width: Float,
        val alpha: Int
    )

    private val strokes = mutableListOf<Stroke>()
    private val redoStrokes = mutableListOf<Stroke>()

    private var currentColor = Color.BLACK
    private var currentWidth = 8f
    private var currentTool = DrawTool.PEN

    private var activePath: Path? = null

    init {
        setBackgroundColor(Color.WHITE)
        isClickable = true
    }

    fun setTool(tool: DrawTool) {
        currentTool = tool
    }

    fun setColor(color: Int) {
        currentColor = color
        currentTool = DrawTool.PEN
    }

    fun setBrushSize(size: Float) {
        currentWidth = size
    }

    fun undo() {
        if (strokes.isNotEmpty()) {
            redoStrokes.add(strokes.removeAt(strokes.lastIndex))
            invalidate()
        }
    }

    fun redo() {
        if (redoStrokes.isNotEmpty()) {
            strokes.add(redoStrokes.removeAt(redoStrokes.lastIndex))
            invalidate()
        }
    }

    fun clearBoard() {
        strokes.clear()
        redoStrokes.clear()
        activePath = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        strokes.forEach { stroke ->

            val paint = Paint().apply {
                color = stroke.color
                style = Paint.Style.STROKE
                strokeWidth = stroke.width
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                alpha = stroke.alpha
                isAntiAlias = true
            }

            canvas.drawPath(stroke.path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {

                val path = Path()
                path.moveTo(event.x, event.y)

                val color: Int
                val width: Float
                val alpha: Int

                when (currentTool) {

                    DrawTool.PEN -> {
                        color = currentColor
                        width = currentWidth
                        alpha = 255
                    }

                    DrawTool.MARKER -> {
                        color = currentColor
                        width = currentWidth * 2.5f
                        alpha = 100
                    }

                    DrawTool.ERASER -> {
                        color = Color.WHITE
                        width = currentWidth * 3f
                        alpha = 255
                    }
                }

                redoStrokes.clear()

                strokes.add(
                    Stroke(path, color, width, alpha)
                )

                activePath = path
                invalidate()

                return true
            }

            MotionEvent.ACTION_MOVE -> {

                activePath?.lineTo(event.x, event.y)
                invalidate()

                return true
            }

            MotionEvent.ACTION_UP -> {

                activePath?.lineTo(event.x, event.y)
                activePath = null

                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {

                activePath = null
                invalidate()

                return true
            }
        }

        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
