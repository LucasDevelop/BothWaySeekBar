package com.lmkj.bgseekbar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * @package    com.lmkj.bgseekbar
 * @anthor     luan
 * @date       2019/4/26
 * @des        双向指示器
 */
class BothWaySeekBar : View {
    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(context)
    }

    //图片资源
    val bg = R.mipmap.seekbar_no_check
    val progress = R.mipmap.seekbar_check
    val leftTrack = R.mipmap.seekbar_bg
    val rightTrack = R.mipmap.seekbar_bg

    //位图
    lateinit var bgBitmap: Bitmap
    lateinit var progressBitmap: Bitmap
    lateinit var leftTrackBitmap: Bitmap
    lateinit var rightTrackBitmap: Bitmap

    var leftProgress = .0f//左侧进度
    var rightProgress = 1f//右侧进度

    var min = 0f//最小值
    var max = 1000f//最大值
    var paint = Paint()

    //事件回调
    var onProgressChange: (min: Int, max: Int) -> Unit = { min, max -> }
    //滑块开始接收事件
    var onTrackStartEvent: () -> Unit = {}
    //滑块结束事件
    var onTrackStopEvent: () -> Unit = {}

    private fun initView(context: Context) {
        //获取背景图片大小
        bgBitmap = BitmapFactory.decodeResource(resources, bg)

        progressBitmap = BitmapFactory.decodeResource(resources, progress)

        leftTrackBitmap = BitmapFactory.decodeResource(resources, leftTrack)

        rightTrackBitmap = BitmapFactory.decodeResource(resources, rightTrack)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //设置控件大小
        setMeasuredDimension(bgBitmap.width + leftTrackBitmap.width, bgBitmap.height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制背景
        canvas.drawBitmap(bgBitmap, leftTrackBitmap.width / 2f, 0f, paint)

        //绘制进度
        canvas.save()
        //计算右侧偏移距离
        val rightDiff = progressBitmap.width * (1 - rightProgress)
        //计算左侧偏移
        val leftDiff = progressBitmap.width * leftProgress
        //设置裁剪区域
        canvas.clipRect(
            leftTrackBitmap.width / 2f + leftDiff,
            0f,
            (progressBitmap.width.toFloat() + leftTrackBitmap.width / 2f) - rightDiff,
            progressBitmap.height.toFloat()
        )
        canvas.drawBitmap(progressBitmap, leftTrackBitmap.width / 2f, 0f, paint)
        canvas.restore()

        //绘制左边滑块
        canvas.save()
        canvas.drawBitmap(leftTrackBitmap, leftDiff, (height - leftTrackBitmap.height) / 2f, paint)
        canvas.restore()

        //绘制右边滑块
        canvas.save()
        canvas.drawBitmap(
            rightTrackBitmap,
            (width.toFloat() - rightTrackBitmap.width) * rightProgress,
            (height - rightTrackBitmap.height) / 2f,
            paint
        )
        canvas.restore()

//        Log.d("lucas", "progressBitmap.width:${progressBitmap.width},rightDiff${rightDiff}")
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        //获取相对位置
        val x = event.x
        val y = event.y
        //超出控件范围的事件不处理
        if (x < 0 || y < 0 || x > width || y > height) return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (event.action == MotionEvent.ACTION_DOWN)
                    onTrackStartEvent()
                if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL)
                    onTrackStopEvent()
                //计算百分比
                val clickProgress = (x - leftTrackBitmap.width / 2) / progressBitmap.width.toFloat()
                //判断临界
                if (clickProgress < 0) {
                    leftProgress = 0f
                } else if (clickProgress > 1) {
                    rightProgress = 1f
                } else {
                    //判断是左移动还是右移动
                    if (Math.abs(clickProgress - leftProgress) > Math.abs(clickProgress - rightProgress)) {//右移动
                        rightProgress = clickProgress
                    } else {//左移动
                        leftProgress = clickProgress
                    }
                }
                onProgressChange((leftProgress * max).toInt(), (rightProgress * max).toInt())
            }
            else -> {
                return super.onTouchEvent(event)
            }
        }
        //刷新界面
        invalidate()
        return true
    }

    fun setMinProgress(progress: Int) {
        leftProgress = progress / max
        postInvalidate()
    }

    fun setMaxProgress(progress: Int) {
        rightProgress = progress / max
        postInvalidate()
    }

    fun getMinProgress() = (leftProgress * max).toInt()
    fun getMaxProgress() = (rightProgress * max).toInt()
}