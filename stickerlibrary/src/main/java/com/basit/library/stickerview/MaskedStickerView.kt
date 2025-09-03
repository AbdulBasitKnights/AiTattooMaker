package com.basit.library.stickerview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.*
import androidx.core.graphics.scale

class MaskedStickerView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

	// Base and mask
	private var baseBitmap: Bitmap? = null
	private var maskBitmap: Bitmap? = null

	// Sticker
	private var stickerBitmap: Bitmap? = null
	private val stickerMatrix = Matrix()
	private val stickerMatrixInverse = Matrix()
	private var stickerInitPlaced = false

	// Rects
	private val contentRect = RectF()
	private val tmpRect = Rect()
	private val maskMatrix = Matrix()
	private val stickerBounds = RectF()
	private val maskBounds = RectF()

	// Paints
	private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
		isDither = true
	}
	private val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
	}
	private val dstAtopPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
	}

	// Flags for behavior without removing older code paths
	private val useLayerMask: Boolean = true
	private val restrictToMaskBounds: Boolean = true

	// Gestures
	private var lastX = 0f
	private var lastY = 0f
	private var activePointerId = -1
	private var isDraggingSticker = false
	private val stickerTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

	private val scaleDetector =
		ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
			override fun onScale(detector: ScaleGestureDetector): Boolean {
				if (stickerBitmap == null) return false
				val focusX = detector.focusX
				val focusY = detector.focusY

				// Calculate new scale
				val newMatrix = Matrix(stickerMatrix)
				newMatrix.postTranslate(-focusX, -focusY)
				newMatrix.postScale(detector.scaleFactor, detector.scaleFactor)
				newMatrix.postTranslate(focusX, focusY)

				// Check if scaled sticker still fits within mask
				val sticker = stickerBitmap ?: return false
				val newBounds = calculateStickerBounds(sticker, newMatrix)
				if (!restrictToMaskBounds || RectF.intersects(newBounds, maskBounds)) {
					stickerMatrix.set(newMatrix)
					invalidate()
				}
				return true
			}
		})

	// ---------- Public API ----------

	fun setImageAndMask(image: Bitmap, mask: Bitmap) {
		Log.d("BITMAP", "Base config: ${image.config}, Mask config: ${mask.config}")
		Log.d("BITMAP", "Base hasAlpha: ${image.hasAlpha()}, Mask hasAlpha: ${mask.hasAlpha()}")

		baseBitmap = image
		maskBitmap = if (mask.width == image.width && mask.height == image.height) mask
		else mask.scale(image.width, image.height).apply {
			Log.d("BITMAP", "Scaled mask config: $config")
		}
		invalidate()
	}
	fun setSticker(sticker: Bitmap) {
		stickerBitmap = sticker
		stickerInitPlaced = false
		if (contentRect.width() > 0f && contentRect.height() > 0f) {
			initStickerPlacement()
		}
		invalidate()
	}

	fun exportResult(): Bitmap? {
		if (width == 0 || height == 0) return null
		val out = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val c = Canvas(out)
		draw(c)
		return out
	}

	// ---------- Layout helpers ----------

//	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//		super.onSizeChanged(w, h, oldw, oldh)
//		computeContentRect()
//		if (!stickerInitPlaced) initStickerPlacement()
//		computeMaskMatrix()
//	}
	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		maskBounds.set(0f, 0f, w.toFloat(), h.toFloat())  // used for clamping
		if (!stickerInitPlaced) initStickerPlacement()
	}

	private fun computeContentRect() {
		val img = baseBitmap ?: return
		val vw = width.toFloat()
		val vh = height.toFloat()
		val iw = img.width.toFloat()
		val ih = img.height.toFloat()

		if (vw == 0f || vh == 0f || iw == 0f || ih == 0f) {
			contentRect.setEmpty()
			return
		}

		val scale = min(vw / iw, vh / ih)
		val dw = iw * scale
		val dh = ih * scale
		val left = (vw - dw) / 2f
		val top = (vh - dh) / 2f
		contentRect.set(left, top, left + dw, top + dh)
	}

	private fun computeMaskMatrix() {
		val mask = maskBitmap ?: return
		if (contentRect.isEmpty) return
		maskMatrix.reset()
		val sx = contentRect.width() / mask.width
		val sy = contentRect.height() / mask.height
		maskMatrix.postScale(sx, sy)
		maskMatrix.postTranslate(contentRect.left, contentRect.top)

		// Calculate mask bounds in view coordinates
		maskBounds.set(contentRect)
	}

/*	private fun initStickerPlacement() {
		val sticker = stickerBitmap ?: return
		val mask = maskBitmap ?: return
		if (contentRect.isEmpty) return

		val boundsBitmap = findMaskBounds(mask)
		if (boundsBitmap.isEmpty) {
			centerStickerIn(contentRect)
		} else {
			val mapped = mapBitmapRectToView(boundsBitmap, mask.width, mask.height, contentRect)
			val targetW = mapped.width() * 0.25f
			val aspect = sticker.width.toFloat() / sticker.height
			val targetH = targetW / aspect

			val cx = mapped.centerX()
			val cy = mapped.centerY()
			stickerMatrix.reset()
			stickerMatrix.postTranslate(-sticker.width / 2f, -sticker.height / 2f)
			stickerMatrix.postScale(targetW / sticker.width, targetH / sticker.height)
			stickerMatrix.postTranslate(cx, cy)
		}
		stickerInitPlaced = true
	}*/
	private fun initStickerPlacement() {
		val s = stickerBitmap ?: return
		if (width == 0 || height == 0) return

		val targetW = width * 0.25f
		val aspect = s.width.toFloat() / s.height
		val targetH = targetW / aspect

		val cx = width / 2f
		val cy = height / 2f

		stickerMatrix.reset()
		stickerMatrix.postTranslate(-s.width / 2f, -s.height / 2f)
		stickerMatrix.postScale(targetW / s.width, targetH / s.height)
		stickerMatrix.postTranslate(cx, cy)

		stickerInitPlaced = true
	}

	private fun centerStickerIn(rect: RectF) {
		val sticker = stickerBitmap ?: return
		val targetW = rect.width() * 0.25f
		val aspect = sticker.width.toFloat() / sticker.height
		val targetH = targetW / aspect

		val cx = rect.centerX()
		val cy = rect.centerY()

		stickerMatrix.reset()
		stickerMatrix.postTranslate(-sticker.width / 2f, -sticker.height / 2f)
		stickerMatrix.postScale(targetW / sticker.width, targetH / sticker.height)
		stickerMatrix.postTranslate(cx, cy)
	}

	private fun findMaskBounds(mask: Bitmap): RectF {
		val w = mask.width
		val h = mask.height
		var minX = w
		var minY = h
		var maxX = -1
		var maxY = -1

		val pixels = IntArray(w)
		for (y in 0 until h step max(1, h / 400)) {
			mask.getPixels(pixels, 0, w, 0, y, w, 1)
			for (x in 0 until w step max(1, w / 400)) {
				val a = (pixels[x] ushr 24) and 0xFF
				if (a > 8) {
					if (x < minX) minX = x
					if (y < minY) minY = y
					if (x > maxX) maxX = x
					if (y > maxY) maxY = y
				}
			}
		}
		return if (maxX >= minX && maxY >= minY)
			RectF(minX.toFloat(), minY.toFloat(), maxX.toFloat(), maxY.toFloat())
		else RectF()
	}

	private fun mapBitmapRectToView(srcRect: RectF, srcW: Int, srcH: Int, dst: RectF): RectF {
		val scale = min(dst.width() / srcW, dst.height() / srcH)
		val dx = dst.left + (dst.width() - srcW * scale) / 2f
		val dy = dst.top + (dst.height() - srcH * scale) / 2f
		return RectF(
			dx + srcRect.left * scale,
			dy + srcRect.top * scale,
			dx + srcRect.right * scale,
			dy + srcRect.bottom * scale
		)
	}
	/*override fun onDraw(canvas: Canvas) {
		// Draw base image into its fitted rect
		val base = baseBitmap ?: return
		canvas.drawBitmap(base, null, contentRect, drawPaint)

        baseBitmap?.let {
            canvas.drawBitmap(it, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), drawPaint)
        } ?: return

        val sticker = stickerBitmap ?: return
        val mask = maskBitmap ?: return

        // 2. Mask position (we assume you draw the mask at a specific Rect — not using a matrix)
        // If you really use a maskMatrix, you must also *apply it* here.

        // Let's assume you are drawing the mask at center of view:
        val maskLeft = (width - mask.width) / 2f
        val maskTop = (height - mask.height) / 2f
        val maskRect = RectF(maskLeft, maskTop, maskLeft + mask.width, maskTop + mask.height)

        // 3. Center the sticker inside that maskRect
//        val stickerMatrix = Matrix()
        val stickerMatrix = stickerMatrix // <- already maintained by touch events

// Draw sticker with the current matrix

        val scale = minOf(
            maskRect.width() / sticker.width.toFloat(),
            maskRect.height() / sticker.height.toFloat()
        )
        stickerMatrix.postScale(scale, scale)

        val scaledWidth = sticker.width * scale
        val scaledHeight = sticker.height * scale

        val tx = maskRect.centerX() - (scaledWidth / 2f)
        val ty = maskRect.centerY() - (scaledHeight / 2f)
        stickerMatrix.postTranslate(tx, ty)

        // 4. Create temporary bitmap to mask the sticker
        val maskedSticker = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(maskedSticker)

        // Draw sticker
        tempCanvas.drawBitmap(sticker, stickerMatrix, drawPaint)

        // Draw mask using DST_IN to clip sticker
        val maskPaint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        tempCanvas.drawBitmap(mask, maskLeft, maskTop, maskPaint)
        // 5. Draw final result to canvas
        canvas.drawBitmap(maskedSticker, 0f, 0f, drawPaint)
        // 6. Clean up
        maskedSticker.recycle()
	}*/
	override fun onDraw(canvas: Canvas) {
		val base = baseBitmap ?: return
		// draw base to full view bounds; no stretch because container ratio == image ratio
		canvas.drawBitmap(base, null, Rect(0, 0, width, height), drawPaint)

		val mask = maskBitmap ?: return
		val save = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

		// draw sticker with its matrix (set once on placement + gestures)
		stickerBitmap?.let { canvas.drawBitmap(it, stickerMatrix, drawPaint) }

		// clip by mask (also drawn to full bounds)
		canvas.drawBitmap(mask, null, Rect(0, 0, width, height), maskPaint)

		canvas.restoreToCount(save)
	}


	// ---------- Touch (drag + pinch) ----------

	override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isPointOnSticker(event.x, event.y, stickerBitmap ?: return false)) {
            return false // Ignore touches outside sticker
        }

		val sticker = stickerBitmap ?: return false

		scaleDetector.onTouchEvent(event)

		when (event.actionMasked) {
			MotionEvent.ACTION_DOWN -> {
				activePointerId = event.getPointerId(0)
				lastX = event.x
				lastY = event.y
				isDraggingSticker = isPointOnSticker(event.x, event.y, sticker)
			}
			MotionEvent.ACTION_MOVE -> {
				if (isDraggingSticker && !scaleDetector.isInProgress) {
					val idx = event.findPointerIndex(activePointerId)
					val x = event.getX(idx)
					val y = event.getY(idx)
					val dx = x - lastX
					val dy = y - lastY

					// Calculate new position
					val newMatrix = Matrix(stickerMatrix)
					newMatrix.postTranslate(dx, dy)

					// Check if new position keeps sticker within mask bounds
					val newBounds = calculateStickerBounds(sticker, newMatrix)
					if (!restrictToMaskBounds || RectF.intersects(newBounds, maskBounds)) {
						stickerMatrix.set(newMatrix)
						invalidate()
					}

					lastX = x
					lastY = y
				}
			}
			MotionEvent.ACTION_POINTER_UP -> {
				val pointerIndex = event.actionIndex
				val pointerId = event.getPointerId(pointerIndex)
				if (pointerId == activePointerId) {
					val newIndex = if (pointerIndex == 0) 1 else 0
					activePointerId = event.getPointerId(newIndex)
					lastX = event.getX(newIndex)
					lastY = event.getY(newIndex)
				}
			}
			MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
				activePointerId = -1
				isDraggingSticker = false
			}
		}
		return true
	}

	private fun isPointOnSticker(x: Float, y: Float, sticker: Bitmap): Boolean {
		if (!stickerMatrix.invert(stickerMatrixInverse)) return false
		val pts = floatArrayOf(x, y)
		stickerMatrixInverse.mapPoints(pts)
		return pts[0] in 0f..sticker.width.toFloat() && pts[1] in 0f..sticker.height.toFloat()
	}

	// ---------- New helper methods ----------

	private fun calculateStickerBounds(sticker: Bitmap): RectF {
		val bounds = RectF(0f, 0f, sticker.width.toFloat(), sticker.height.toFloat())
		stickerMatrix.mapRect(bounds)
		return bounds
	}

	private fun calculateStickerBounds(sticker: Bitmap, matrix: Matrix): RectF {
		val bounds = RectF(0f, 0f, sticker.width.toFloat(), sticker.height.toFloat())
		matrix.mapRect(bounds)
		return bounds
	}
}