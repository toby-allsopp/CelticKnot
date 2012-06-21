package nz.gen.mi6.celticknot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

public class DrawingView extends View {

	private class ScaleGestureListener implements OnScaleGestureListener {

		@Override
		public boolean onScaleBegin(final ScaleGestureDetector detector)
		{
			return true;
		}

		@Override
		public boolean onScale(final ScaleGestureDetector detector)
		{
			final float worldFocusX = screenToWorldX(detector.getFocusX());
			final float worldFocusY = screenToWorldY(detector.getFocusY());
			DrawingView.this.worldToScreen *= detector.getScaleFactor();
			DrawingView.this.xScroll -= screenToWorldX(detector.getFocusX()) - worldFocusX;
			DrawingView.this.yScroll -= screenToWorldY(detector.getFocusY()) - worldFocusY;
			invalidate();
			return true;
		}

		@Override
		public void onScaleEnd(final ScaleGestureDetector detector)
		{
		}
	}

	private final class GestureListener implements GestureDetector.OnGestureListener {

		@Override
		public boolean onSingleTapUp(final MotionEvent e)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void onShowPress(final MotionEvent e)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY)
		{
			final float worldDX = distanceX / DrawingView.this.worldToScreen;
			final float worldDY = distanceY / DrawingView.this.worldToScreen;
			DrawingView.this.xScroll += worldDX;
			DrawingView.this.yScroll += worldDY;
			invalidate();
			return true;
		}

		@Override
		public void onLongPress(final MotionEvent e)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY)
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onDown(final MotionEvent e)
		{
			// TODO Auto-generated method stub
			return false;
		}
	}

	private static final String LOG_TAG = DrawingView.class.getName();

	private static final int DEFAULT_HEIGHT = 100;
	private static final int DEFAULT_WIDTH = DEFAULT_HEIGHT;

	private final boolean drawGrid = true;

	private float xScroll;
	private float yScroll;

	private float worldToScreen = 10;

	// private DrawingModel model;

	private final GestureDetector gestureDetector;
	private final ScaleGestureDetector scaleGestureDetector;

	/** World height of the grid */
	private final float gridWidth = 20.f;

	/** World width of the grid */
	private final float gridHeight = 20.f;

	private final int numRows = 10;

	private final int numColumns = 10;

	private Handle mStartHandle;

	private Handle mEndHandle;

	public DrawingView(final Context context, final AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public DrawingView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		this.gestureDetector = new GestureDetector(context, new GestureListener());
		this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		if (this.drawGrid) {
			drawGrid(canvas);
		}
		if (this.mStartHandle != null) {
			drawSelectedHandle(canvas, this.mStartHandle);
			if (this.mEndHandle != null) {
				drawSelectedHandle(canvas, this.mEndHandle);
				drawProposedSegment(canvas);
			}
		}
	}

	private void drawProposedSegment(final Canvas canvas)
	{
		// TODO Auto-generated method stub

	}

	private void drawSelectedHandle(final Canvas canvas, final Handle handle)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0x00, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		final float screenX = worldToScreenX(handle.worldX);
		final float screenY = worldToScreenY(handle.worldY);
		canvas.drawCircle(screenX, screenY, 20, paint);
	}

	private void drawGrid(final Canvas canvas)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		final int height = canvas.getHeight();
		final int width = canvas.getWidth();
		final float minScreenX = Math.max(worldToScreenX(0), 0);
		final float maxScreenX = Math.min(worldToScreenX((this.numColumns - 1) * this.gridWidth), width);
		final float minScreenY = Math.max(worldToScreenY(0), 0);
		final float maxScreenY = Math.min(worldToScreenY((this.numRows - 1) * this.gridHeight), height);
		{
			final int firstHorizGridIndex = clamp(1, this.numRows - 1, (int) Math.ceil(this.yScroll / this.gridHeight));
			final int lastHorizGridIndex = clamp(1, this.numRows - 1, (int) Math.floor(screenToWorldY(height)));
			for (int yi = firstHorizGridIndex; yi < lastHorizGridIndex; ++yi) {
				final float y = worldToScreenY(yi * this.gridHeight);
				canvas.drawLine(minScreenX, y, maxScreenX, y, paint);
			}
		}
		{
			final int firstVertGridIndex = clamp(1, this.numColumns - 1, (int) Math.ceil(this.xScroll / this.gridWidth));
			final int lastVertGridIndex = clamp(1, this.numColumns - 1, (int) Math.floor(screenToWorldX(width)));
			for (int xi = firstVertGridIndex; xi < lastVertGridIndex; ++xi) {
				final float x = worldToScreenX(xi * this.gridWidth);
				canvas.drawLine(x, minScreenY, x, maxScreenY, paint);
			}
		}
	}

	private float worldToScreenX(final float x)
	{
		return (x - this.xScroll) * this.worldToScreen;
	}

	private float screenToWorldX(final float x)
	{
		return x / this.worldToScreen + this.xScroll;
	}

	private float worldToScreenY(final float y)
	{
		return (y - this.yScroll) * this.worldToScreen;
	}

	private float screenToWorldY(final float y)
	{
		return y / this.worldToScreen + this.yScroll;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
	{
		final int measuredWidth;
		final int measuredHeight;
		{
			final int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
			switch (widthMeasureSpecMode) {
				case MeasureSpec.EXACTLY:
				case MeasureSpec.AT_MOST:
					measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
					break;
				case MeasureSpec.UNSPECIFIED:
					measuredWidth = DEFAULT_WIDTH;
					break;
				default:
					Log.e(LOG_TAG, "Unexpected width measure spec mode: " + widthMeasureSpecMode);
					measuredWidth = DEFAULT_WIDTH;
			}
		}
		{
			final int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);
			switch (heightMeasureSpecMode) {
				case MeasureSpec.EXACTLY:
				case MeasureSpec.AT_MOST:
					measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
					break;
				case MeasureSpec.UNSPECIFIED:
					measuredHeight = DEFAULT_HEIGHT;
					break;
				default:
					Log.e(LOG_TAG, "Unexpected height measure spec mode: " + heightMeasureSpecMode);
					measuredHeight = DEFAULT_HEIGHT;
			}
		}
		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		this.scaleGestureDetector.onTouchEvent(event);
		if (this.mStartHandle == null) {
			this.gestureDetector.onTouchEvent(event);
		}
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				final float worldX = screenToWorldX(event.getX());
				final float worldY = screenToWorldY(event.getY());
				this.mStartHandle = getNearestHandle(worldX, worldY);
				invalidate();
				break;
			}
			case MotionEvent.ACTION_UP:
				if (this.mStartHandle != null) {
					if (this.mEndHandle != null) {

					}
					this.mStartHandle = null;
					this.mEndHandle = null;
					invalidate();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				this.mStartHandle = null;
				this.mEndHandle = null;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (this.mStartHandle != null) {
					final float worldX = screenToWorldX(event.getX());
					final float worldY = screenToWorldY(event.getY());
					this.mEndHandle = getNearestHandle(worldX, worldY);
					invalidate();
				}
				break;
		}
		return true;
	}

	private float clamp(final float min, final float max, final float f)
	{
		return f < min ? min : f > max ? max : f;
	}

	private int clamp(final int min, final int max, final int i)
	{
		return i < min ? min : i > max ? max : i;
	}

	private Handle getNearestHandle(final float worldX, final float worldY)
	{
		// Find the grid line this is closest to.
		final float horizGridY = clamp(
				this.gridHeight,
				this.gridHeight * (this.numRows - 1),
				Math.round(worldY / this.gridHeight) * this.gridHeight);
		final float vertGridX = clamp(
				this.gridWidth,
				this.gridWidth * (this.numColumns - 1),
				Math.round(worldX / this.gridWidth) * this.gridWidth);

		// Handles are at the same interval as the grid lines but offset by grid
		// size * 1/3 and * 2/3.
		final float horizHandleOffset1 = this.gridWidth / 3;
		final float horizHandleOffset2 = this.gridWidth * 2 / 3;
		final float vertHandleOffset1 = this.gridHeight / 3;
		final float vertHandleOffset2 = this.gridHeight * 2 / 3;
		final float horizHandleMinX = horizHandleOffset2;
		final float horizHandleMaxX = horizHandleOffset1 + this.gridWidth * (this.numColumns - 1);
		final float vertHandleMinY = vertHandleOffset2;
		final float vertHandleMaxY = vertHandleOffset1 + this.gridHeight * (this.numRows - 1);
		final float horizHandleX1 = clamp(
				horizHandleMinX,
				horizHandleMaxX,
				Math.round((worldX - horizHandleOffset1) / this.gridWidth) * this.gridWidth + horizHandleOffset1);
		final float horizHandleX2 = clamp(
				horizHandleMinX,
				horizHandleMaxX,
				Math.round((worldX - horizHandleOffset2) / this.gridWidth) * this.gridWidth + horizHandleOffset2);
		final float vertHandleY1 = clamp(
				vertHandleMinY,
				vertHandleMaxY,
				Math.round((worldY - vertHandleOffset1) / this.gridHeight) * this.gridHeight + vertHandleOffset1);
		final float vertHandleY2 = clamp(
				vertHandleMinY,
				vertHandleMaxY,
				Math.round((worldY - vertHandleOffset2) / this.gridHeight) * this.gridHeight + vertHandleOffset2);
		final float distToHorizHandle1 = Math.abs(worldX - horizHandleX1);
		final float distToHorizHandle2 = Math.abs(worldX - horizHandleX2);
		final float horizHandleX = distToHorizHandle2 < distToHorizHandle1 ? horizHandleX2 : horizHandleX1;
		final float distToVertHandle1 = Math.abs(worldY - vertHandleY1);
		final float distToVertHandle2 = Math.abs(worldY - vertHandleY2);
		final float vertHandleY = distToVertHandle2 < distToVertHandle1 ? vertHandleY2 : vertHandleY1;
		final float horizDX = horizHandleX - worldX;
		final float horizDY = horizGridY - worldY;
		final float sqrdistToHorizHandle = horizDX * horizDX + horizDY * horizDY;
		final float vertDY = vertHandleY - worldY;
		final float vertDX = vertGridX - worldX;
		final float sqrdistToVertHandle = vertDX * vertDX + vertDY * vertDY;
		final float handleX;
		final float handleY;
		final float handleSqrdist;
		if (sqrdistToHorizHandle < sqrdistToVertHandle) {
			handleX = horizHandleX;
			handleY = horizGridY;
			handleSqrdist = sqrdistToHorizHandle;
		} else {
			handleY = vertHandleY;
			handleX = vertGridX;
			handleSqrdist = sqrdistToVertHandle;
		}
		if (handleSqrdist * this.worldToScreen * this.worldToScreen < 20 * 20) {
			final Handle handle = new Handle();
			handle.worldX = handleX;
			handle.worldY = handleY;
			return handle;
		} else {
			return null;
		}
	}
}
