package nz.gen.mi6.celticknot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
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

	private final int numRows = 10;

	private final int numColumns = 10;

	// private final DrawingModel model = new DrawingModel(this.numColumns,
	// this.numRows);

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
		final long startNanos = System.nanoTime();
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
		final long frameNanos = System.nanoTime() - startNanos;
		final Paint paint = new Paint();
		paint.setARGB(127, 255, 255, 255);
		paint.setStyle(Paint.Style.STROKE);
		paint.setTypeface(Typeface.DEFAULT);
		paint.setTextAlign(Align.LEFT);
		canvas.drawText(String.format("%fms/frame", frameNanos / 1000. / 1000.), 10, 10, paint);
	}

	private void drawProposedSegment(final Canvas canvas)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0x00, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(5 * this.worldToScreen);
		canvas.drawLine(
				worldToScreenX(this.mStartHandle.worldX),
				worldToScreenY(this.mStartHandle.worldY),
				worldToScreenX(this.mEndHandle.worldX),
				worldToScreenY(this.mEndHandle.worldY),
				paint);
	}

	private void drawSelectedHandle(final Canvas canvas, final Handle handle)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0x00, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		final float screenX = worldToScreenX(handle.worldX);
		final float screenY = worldToScreenY(handle.worldY);
		canvas.drawCircle(screenX, screenY, 20, paint);
	}

	private void drawGrid(final Canvas canvas)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(false);
		final int height = canvas.getHeight();
		final int width = canvas.getWidth();
		final float minScreenX = Math.max(worldToScreenX(this.gridWidth / 2), 0);
		final float maxScreenX = Math.min(
				worldToScreenX((this.numColumns + 1) * this.gridWidth + this.gridWidth / 2),
				width);
		final float minScreenY = Math.max(worldToScreenY(this.gridHeight / 2), 0);
		final float maxScreenY = Math.min(
				worldToScreenY((this.numRows + 1) * this.gridHeight + this.gridHeight / 2),
				height);
		final int firstHorizGridIndex = clamp(1, this.numRows + 2, (int) Math.floor(this.yScroll / this.gridHeight));
		final int lastHorizGridIndex = clamp(1, this.numRows + 2, (int) Math.floor(screenToWorldY(height)));
		final int firstVertGridIndex = clamp(1, this.numColumns + 2, (int) Math.floor(this.xScroll / this.gridWidth));
		final int lastVertGridIndex = clamp(1, this.numColumns + 2, (int) Math.floor(screenToWorldX(width)));
		final float handleLength = Math.min(this.gridWidth, this.gridHeight) / 10 * this.worldToScreen;
		for (int yi = firstHorizGridIndex; yi < lastHorizGridIndex; ++yi) {
			final float y = worldToScreenY(yi * this.gridHeight);
			canvas.drawLine(minScreenX, y, maxScreenX, y, paint);
			for (int xi = firstVertGridIndex; xi < lastVertGridIndex; ++xi) {
				final float x1 = worldToScreenX(xi * this.gridWidth - this.gridWidth / 3);
				final float x2 = worldToScreenX(xi * this.gridWidth + this.gridWidth / 3);
				canvas.drawLine(x1, y - handleLength, x1, y + handleLength, paint);
				canvas.drawLine(x2, y - handleLength, x2, y + handleLength, paint);
			}
		}
		for (int xi = firstVertGridIndex; xi < lastVertGridIndex; ++xi) {
			final float x = worldToScreenX(xi * this.gridWidth);
			canvas.drawLine(x, minScreenY, x, maxScreenY, paint);
			for (int yi = firstHorizGridIndex; yi < lastHorizGridIndex; ++yi) {
				final float y1 = worldToScreenY(yi * this.gridHeight - this.gridHeight / 3);
				final float y2 = worldToScreenY(yi * this.gridHeight + this.gridHeight / 3);
				canvas.drawLine(x - handleLength, y1, x + handleLength, y1, paint);
				canvas.drawLine(x - handleLength, y2, x + handleLength, y2, paint);
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
				this.mStartHandle = getNearestHandle(worldX, worldY, null);
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
					this.mEndHandle = getNearestHandle(worldX, worldY, this.mStartHandle);
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

	private Handle getNearestHandle(final float worldX, final float worldY, final Handle adjacentHandle)
	{
		// Find the cell in which the touch occurred.
		final int column = clamp(0, this.numColumns + 1, (int) Math.floor(worldX / this.gridWidth));
		final int row = clamp(0, this.numRows + 1, (int) Math.floor(worldY / this.gridHeight));

		// Calculate the cell-relative coordinates of the touch, from 0 to 1.
		final float cellPropX = (worldX - column * this.gridWidth) / this.gridWidth;
		final float cellPropY = (worldY - row * this.gridHeight) / this.gridHeight;

		// Figure out the nearest handle in the cell
		final int handleIndex;
		final float handlePropX;
		final float handlePropY;
		if (cellPropX < 0.5) {
			// Handle index 0, 5, 6, 7
			if (cellPropY < 0.5) {
				// Handle index 0, 7
				if (cellPropX < cellPropY) {
					handleIndex = 7;
					handlePropX = 0.f;
					handlePropY = 1 / 3.f;
				} else {
					handleIndex = 0;
					handlePropX = 1 / 3.f;
					handlePropY = 0.f;
				}
			} else {
				// Handle index 5, 6
				if (cellPropX > 1 - cellPropY) {
					handleIndex = 5;
					handlePropX = 1 / 3.f;
					handlePropY = 1.f;
				} else {
					handleIndex = 6;
					handlePropX = 0.f;
					handlePropY = 2 / 3.f;
				}
			}
		} else {
			// Handle index 1, 2, 3, 4
			if (cellPropY < 0.5) {
				// Handle index 1, 2
				if (1 - cellPropX > cellPropY) {
					handleIndex = 1;
					handlePropX = 2 / 3.f;
					handlePropY = 0.f;
				} else {
					handleIndex = 2;
					handlePropX = 1.f;
					handlePropY = 1 / 3.f;
				}
			} else {
				// Handle index 3, 4
				if (cellPropX < cellPropY) {
					handleIndex = 4;
					handlePropX = 2 / 3.f;
					handlePropY = 1.f;
				} else {
					handleIndex = 3;
					handlePropX = 1.f;
					handlePropY = 2 / 3.f;
				}
			}
		}

		final Handle handle = new Handle();
		handle.worldX = column * this.gridWidth + handlePropX * this.gridWidth;
		handle.worldY = row * this.gridHeight + handlePropY * this.gridHeight;
		handle.column = column;
		handle.row = row;
		handle.handleIndex = handleIndex;

		final float dx = worldX - handle.worldX;
		final float dy = worldY - handle.worldY;
		final float sqrdist = (dx * dx) + (dy * dy);
		if (sqrdist < this.gridWidth * this.gridHeight / 9
				&& (adjacentHandle == null || adjacentHandle.adjacentTo(handle))) {
			return handle;
		} else {
			return null;
		}
	}
}
