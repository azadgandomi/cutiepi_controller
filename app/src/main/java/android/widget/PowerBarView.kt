package android.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import com.sazadgankar.picontroller.PowerChangeListener
import com.sazadgankar.picontroller.R


class PowerBar : SeekBar {
    var powerChangeListener: PowerChangeListener? = null
    private val transparentPaint = Paint()

    init {
        transparentPaint.setARGB(128, 255, 255, 255)
        transparentPaint.strokeWidth = 4f
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
            transparentPaint
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
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumbTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorAccent, null))
            }

            MotionEvent.ACTION_UP -> {
                thumbTintList = ColorStateList.valueOf(Color.WHITE)
                powerChangeListener?.onPowerChange(progress)
            }
        }
        return true
    }


    @Synchronized
    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        onSizeChanged(width, height, 0, 0)
    }
}