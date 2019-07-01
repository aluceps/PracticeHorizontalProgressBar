package me.aluceps.horizontalprogressbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.util.*

class HorizontalProgressBar @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var borderWidth = 0f
    private var cornerRadius = 0f
    private var colorBase = 0
    private var colorValue = 0

    /**
     * 枠を表現するために枠の太さを加減
     */
    private val innerLeft by lazy { 0 + borderWidth }
    private val innerTop by lazy { 0 + borderWidth }
    private val innerRight by lazy { width - borderWidth }
    private val innerBottom by lazy { height - borderWidth }
    private val innerRadius by lazy { cornerRadius - borderWidth }

    // 左右の枠と目盛りの本数分を考慮した間隔
    private val tickInterval by lazy {
        (width - borderWidth * (2 + 9)) / 10 + borderWidth
    }

    private val progressBase by lazy {
        Paint().apply {
            color = colorBase
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    private val progressInner by lazy {
        Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }
    }

    private val progressValue by lazy {
        Paint().apply {
            color = colorValue
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }
    }

    private val tickBase by lazy {
        Paint().apply {
            color = colorBase
            style = Paint.Style.FILL
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.XOR)
        }
    }

    private val rectBase by lazy { RectF(0f, 0f, width.toFloat(), height.toFloat()) }
    private val rectInner by lazy { RectF(innerLeft, innerTop, innerRight, innerBottom) }
    private val rectValue = RectF()
    private val rectTick by lazy { RectF(0f, innerTop, borderWidth, innerBottom) }

    private var progress = 0f

    init {
        setup(context, attrs, defStyleAttr)
    }

    private fun setup(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.HorizontalProgressBar, defStyleAttr, 0)
        typedArray?.let { t ->
            t.getDimension(R.styleable.HorizontalProgressBar_progress_border_width, 0f)
                .let { borderWidth = it }
            t.getDimension(R.styleable.HorizontalProgressBar_progress_corner_radius, 0f)
                .let { cornerRadius = it }
            t.getColor(R.styleable.HorizontalProgressBar_progress_color_base, Color.WHITE)
                .let { colorBase = it }
            t.getColor(R.styleable.HorizontalProgressBar_progress_color_value, Color.LTGRAY)
                .let { colorValue = it }
        }
        typedArray?.recycle()

        Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    post { invalidate() }
                }
            }, 10, 10)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        canvas.saveLayer(rectBase, progressBase)

        // プログレスバーのベース
        canvas.drawRoundRect(rectBase, cornerRadius, cornerRadius, progressBase)

        // 枠の太さを考慮した Rect で型抜きをする
        canvas.drawRoundRect(rectInner, innerRadius, innerRadius, progressInner)

        // プログレスが増えたときに型抜き領域に色付けする
        // left は枠の太さを考慮しているので right の開始も枠の太さに合わせる
        rectValue.also {
            it.set(innerLeft, innerTop, borderWidth + (innerRight - borderWidth) * progress, innerBottom)
        }.let {
            Log.d("RectValu", "$it")
            canvas.drawRect(it, progressValue)
        }

        for (i in 1..9) {
            val position = tickInterval * i
            rectTick.also {
                it.set(position, innerTop, position + borderWidth, innerBottom)
            }.let {
                canvas.drawRect(it, tickBase)
            }
        }

        canvas.restore()
    }

    fun setProgress(progress: Float) {
        this.progress = progress
    }
}