package nz.gen.mi6.celticknot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {

	private static final String LOG_TAG = DrawingView.class.getName();

	private static final int DEFAULT_HEIGHT = 100;
	private static final int DEFAULT_WIDTH = DEFAULT_HEIGHT;

	private Path newPath;

	private DrawingModel model;

	public DrawingView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
	}

	public DrawingView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		if (this.newPath != null) {
			final Paint paint = new Paint();
			paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawPath(this.newPath, paint);
		}
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
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				this.newPath = new Path();
				this.newPath.moveTo(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_UP:
				if (this.newPath != null) {
					// this.model = this.model.addPath(this.newPath);
					this.newPath = null;
					invalidate();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				this.newPath = null;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (this.newPath != null) {
					for (int i = 0; i < event.getHistorySize(); ++i) {
						this.newPath.lineTo(event.getHistoricalX(i), event.getHistoricalY(i));
					}
					this.newPath.lineTo(event.getX(), event.getY());
					invalidate();
				}
				break;
		}
		return true;
	}
}
