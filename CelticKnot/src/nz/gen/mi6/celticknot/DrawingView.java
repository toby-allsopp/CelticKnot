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

	private DrawingModel model;

	private final GestureDetector gestureDetector;
	private final ScaleGestureDetector scaleGestureDetector;

	private final int gridWidth = 20;

	private final int gridHeight = 20;

	private final int numRows = 10;

	private final int numColumns = 10;

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
	}

	private void drawGrid(final Canvas canvas)
	{
		final Paint paint = new Paint();
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		paint.setStyle(Paint.Style.STROKE);
		final int height = canvas.getHeight();
		final int width = canvas.getWidth();
		final float minScreenX = Math.max(worldToScreenX(0), 0);
		final float maxScreenX = Math.min(worldToScreenX(this.numColumns * this.gridWidth), width);
		final float minScreenY = Math.max(worldToScreenY(0), 0);
		final float maxScreenY = Math.min(worldToScreenY(this.numRows * this.gridHeight), height);
		final float screenGridHeight = this.gridHeight * this.worldToScreen;
		final float firstGridScreenY = worldToScreenY(Math.max(1, (float) Math.ceil(this.yScroll / this.gridHeight))
				* this.gridHeight);
		for (float y = firstGridScreenY; y < maxScreenY; y += screenGridHeight) {
			canvas.drawLine(minScreenX, y, maxScreenX, y, paint);
		}
		final float firstGridScreenX = worldToScreenX(Math.max(1, (float) Math.ceil(this.xScroll / this.gridWidth))
				* this.gridWidth);
		final float screenGridWidth = this.gridWidth * this.worldToScreen;
		for (float x = firstGridScreenX; x < maxScreenX; x += screenGridWidth) {
			canvas.drawLine(x, minScreenY, x, maxScreenY, paint);
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
		this.gestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_CANCEL:
				break;
			case MotionEvent.ACTION_MOVE:
				for (int i = 0; i < event.getHistorySize(); ++i) {
				}
				break;
		}
		return true;
	}
}
