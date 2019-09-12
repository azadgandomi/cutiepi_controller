package android.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent

class PowerBar : SeekBar {

    private val paint = Paint()

    init {
        paint.setARGB(128, 255, 255, 255)
        paint.strokeWidth = 4f
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
    }

    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }


    override fun onDraw(c: Canvas) {
        c.drawLine(
            width.toFloat() / 2,
            paddingRight.toFloat(),
            width.toFloat() / 2,
            height.toFloat() - paddingLeft,
            paint
        )
        c.rotate(-90f)
        c.translate((-height).toFloat(), 0f)
        super.onDraw(c)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                progressTintList = ColorStateList.valueOf(resources.getColor(R.color.buttonsActiveColor))
            }

            MotionEvent.ACTION_UP -> {
//                progressTintList = ColorStateList.valueOf(white))
                //TODO: Set power
            }
        }
        return true
    }

}