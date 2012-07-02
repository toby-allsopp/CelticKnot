package nz.gen.mi6.celticknot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.FloatMath;
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
			setUpPaints();
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

	/** World height of the grid */
	private final float gridWidth = 20.f;

	/** World width of the grid */
	private final float gridHeight = 20.f;

	private Handle mStartHandle;

	private Handle mEndHandle;

	private static final float[] handlePropX = { 1 / 3.f, 2 / 3.f, 3 / 3.f, 3 / 3.f, 2 / 3.f, 1 / 3.f, 0 / 3.f, 0 / 3.f };

	private static final float[] handlePropY = { 0 / 3.f, 0 / 3.f, 1 / 3.f, 2 / 3.f, 3 / 3.f, 3 / 3.f, 2 / 3.f, 1 / 3.f };

	private final Paint frameratePaint;

	private final Paint knotPaint;

	private final Paint proposedSegmentPaint;

	private final Paint selectedHandlePaint;

	private final Paint gridPaint;

	private final Paint knotBackgroundPaint;

	public DrawingView(final Context context, final AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public DrawingView(final Context context, final AttributeSet attrs, final int defStyle)
	{
		super(context, attrs, defStyle);
		this.gestureDetector = new GestureDetector(context, new GestureListener());
		this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());

		this.knotBackgroundPaint = new Paint();
		this.frameratePaint = new Paint();
		this.knotPaint = new Paint();
		this.proposedSegmentPaint = new Paint();
		this.selectedHandlePaint = new Paint();
		this.gridPaint = new Paint();

		setUpPaints();
	}

	private void setUpPaints()
	{
		this.frameratePaint.setARGB(127, 0, 0, 0);
		this.frameratePaint.setStyle(Paint.Style.STROKE);
		this.frameratePaint.setTypeface(Typeface.DEFAULT);
		this.frameratePaint.setTextAlign(Align.LEFT);

		this.knotPaint.setARGB(0xFF, 0xFF, 0x00, 0x00);
		this.knotPaint.setStyle(Paint.Style.STROKE);
		this.knotPaint.setStrokeWidth(4 * this.worldToScreen);

		this.knotBackgroundPaint.setARGB(0xFF, 0x00, 0x00, 0x00);
		this.knotBackgroundPaint.setStyle(Paint.Style.STROKE);
		this.knotBackgroundPaint.setStrokeWidth(5 * this.worldToScreen);

		this.proposedSegmentPaint.setARGB(0xFF, 0xFF, 0x00, 0xFF);
		this.proposedSegmentPaint.setStyle(Paint.Style.STROKE);
		this.proposedSegmentPaint.setAntiAlias(true);
		this.proposedSegmentPaint.setStrokeWidth(5 * this.worldToScreen);

		this.selectedHandlePaint.setARGB(0xFF, 0xFF, 0x00, 0xFF);
		this.selectedHandlePaint.setStyle(Paint.Style.STROKE);
		this.selectedHandlePaint.setAntiAlias(true);

		this.gridPaint.setARGB(0xFF, 0, 0, 0);
		this.gridPaint.setStyle(Paint.Style.STROKE);
		this.gridPaint.setAntiAlias(false);

	}

	public void setModel(final DrawingModel model)
	{
		this.model = model;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas)
	{
		final long startNanos = System.nanoTime();
		int saveCount;
		canvas.drawColor(0xFFFFFFFF);
		if (this.drawGrid) {
			saveCount = canvas.save();
			drawGrid(canvas);
			canvas.restoreToCount(saveCount);
		}
		saveCount = canvas.save();
		drawKnot(canvas);
		canvas.restoreToCount(saveCount);
		if (this.mStartHandle != null) {
			drawSelectedHandle(canvas, this.mStartHandle);
			if (this.mEndHandle != null) {
				drawSelectedHandle(canvas, this.mEndHandle);
				drawProposedSegment(canvas);
			}
		}
		final long frameNanos = System.nanoTime() - startNanos;
		canvas.drawText(String.format("%fms/frame", frameNanos / 1000. / 1000.), 10, 10, this.frameratePaint);
	}

	private void drawKnot(final Canvas canvas)
	{
		final Path path = new Path();
		for (int column = 0; column < this.model.getNumColumns() + 2; ++column) {
			for (int row = 0; row < this.model.getNumRows() + 2; ++row) {
				final Cell cell = this.model.getCell(column, row);
				final float cellOriginWorldX = column * this.gridWidth;
				final float cellOriginWorldY = row * this.gridHeight;
				final int[] froms = { 0, 2, 4, 6, 1, 3, 5, 7 };
				for (int i = 0; i < 8; ++i) {
					final int from = froms[i];
					final int to = cell.getConnectionFrom(from);
					if (to != -1) {
						final float startWorldX = cellOriginWorldX + handlePropX[from] * this.gridWidth;
						final float stopWorldX = cellOriginWorldX + handlePropX[to] * this.gridWidth;
						final float startWorldY = cellOriginWorldY + handlePropY[from] * this.gridHeight;
						final float stopWorldY = cellOriginWorldY + handlePropY[to] * this.gridHeight;
						final boolean startHorizontal = ((from / 2) & 1) == 1;
						final boolean stopHorizontal = ((to / 2) & 1) == 1;
						final float worldX1;
						final float worldY1;
						if (startHorizontal) {
							worldX1 = cellOriginWorldX + (from / 4 == 0 ? 2 / 3.f : 1 / 3.f) * this.gridWidth;
							worldY1 = startWorldY;
						} else {
							worldX1 = startWorldX;
							worldY1 = cellOriginWorldY + (from / 4 == 0 ? 1 / 3.f : 2 / 3.f) * this.gridHeight;
						}
						final float worldX2;
						final float worldY2;
						if (stopHorizontal) {
							worldX2 = cellOriginWorldX + (to / 4 == 0 ? 2 / 3.f : 1 / 3.f) * this.gridWidth;
							worldY2 = stopWorldY;
						} else {
							worldX2 = stopWorldX;
							worldY2 = cellOriginWorldY + (to / 4 == 0 ? 1 / 3.f : 2 / 3.f) * this.gridHeight;
						}
						path.reset();
						path.moveTo(worldToScreenX(startWorldX), worldToScreenY(startWorldY));
						path.cubicTo(
								worldToScreenX(worldX1),
								worldToScreenY(worldY1),
								worldToScreenX(worldX2),
								worldToScreenY(worldY2),
								worldToScreenX(stopWorldX),
								worldToScreenY(stopWorldY));
						if (startHorizontal == stopHorizontal || true) {
							final float clipPropX1, clipPropX2, clipPropY1, clipPropY2;
							if (startHorizontal) {
								clipPropY1 = 0.f;
								clipPropY2 = 1.f;
								if (from < 4) {
									clipPropX1 = 0.5f;
									clipPropX2 = 1.f;
								} else {
									clipPropX1 = 0.f;
									clipPropX2 = .5f;
								}
							} else {
								clipPropX1 = 0.f;
								clipPropX2 = 1.f;
								if (from < 4) {
									clipPropY1 = 0.f;
									clipPropY2 = .5f;
								} else {
									clipPropY1 = 0.5f;
									clipPropY2 = 1.f;
								}
							}
							canvas.clipRect(
									worldToScreenX(cellOriginWorldX + clipPropX1 * this.gridWidth),
									worldToScreenY(cellOriginWorldY + clipPropY1 * this.gridHeight),
									worldToScreenX(cellOriginWorldX + clipPropX2 * this.gridWidth),
									worldToScreenY(cellOriginWorldY + clipPropY2 * this.gridHeight),
									Region.Op.REPLACE);
						}
						canvas.drawPath(path, this.knotBackgroundPaint);
						canvas.drawPath(path, this.knotPaint);
						final boolean drawTangents = false;
						if (drawTangents) {
							canvas.drawLine(
									worldToScreenX(startWorldX),
									worldToScreenY(startWorldY),
									worldToScreenX(worldX1),
									worldToScreenY(worldY1),
									this.knotPaint);
							canvas.drawLine(
									worldToScreenX(worldX2),
									worldToScreenY(worldY2),
									worldToScreenX(stopWorldX),
									worldToScreenY(stopWorldY),
									this.knotPaint);
						}
					}
				}
			}
		}
	}

	private void drawProposedSegment(final Canvas canvas)
	{
		canvas.drawLine(
				worldToScreenX(this.mStartHandle.worldX),
				worldToScreenY(this.mStartHandle.worldY),
				worldToScreenX(this.mEndHandle.worldX),
				worldToScreenY(this.mEndHandle.worldY),
				this.proposedSegmentPaint);
	}

	private void drawSelectedHandle(final Canvas canvas, final Handle handle)
	{
		final float screenX = worldToScreenX(handle.worldX);
		final float screenY = worldToScreenY(handle.worldY);
		canvas.drawCircle(screenX, screenY, 20, this.selectedHandlePaint);
	}

	private void drawGrid(final Canvas canvas)
	{
		final int height = canvas.getHeight();
		final int width = canvas.getWidth();
		final float minScreenX = Math.max(worldToScreenX(this.gridWidth / 2), 0);
		final float maxScreenX = Math.min(worldToScreenX((this.model.getNumColumns() + 1)
				* this.gridWidth
				+ this.gridWidth
				/ 2), width);
		final float minScreenY = Math.max(worldToScreenY(this.gridHeight / 2), 0);
		final float maxScreenY = Math.min(worldToScreenY((this.model.getNumRows() + 1)
				* this.gridHeight
				+ this.gridHeight
				/ 2), height);
		final int firstHorizGridIndex = clamp(
				1,
				this.model.getNumRows() + 2,
				(int) FloatMath.floor(this.yScroll / this.gridHeight));
		final int lastHorizGridIndex = clamp(
				1,
				this.model.getNumRows() + 2,
				(int) FloatMath.floor(screenToWorldY(height)));
		final int firstVertGridIndex = clamp(
				1,
				this.model.getNumColumns() + 2,
				(int) FloatMath.floor(this.xScroll / this.gridWidth));
		final int lastVertGridIndex = clamp(
				1,
				this.model.getNumColumns() + 2,
				(int) FloatMath.floor(screenToWorldX(width)));
		final float handleLength = Math.min(this.gridWidth, this.gridHeight) / 10 * this.worldToScreen;
		for (int yi = firstHorizGridIndex; yi < lastHorizGridIndex; ++yi) {
			final float y = worldToScreenY(yi * this.gridHeight);
			canvas.drawLine(minScreenX, y, maxScreenX, y, this.gridPaint);
			for (int xi = firstVertGridIndex; xi < lastVertGridIndex; ++xi) {
				final float x1 = worldToScreenX(xi * this.gridWidth - this.gridWidth / 3);
				final float x2 = worldToScreenX(xi * this.gridWidth + this.gridWidth / 3);
				canvas.drawLine(x1, y - handleLength, x1, y + handleLength, this.gridPaint);
				canvas.drawLine(x2, y - handleLength, x2, y + handleLength, this.gridPaint);
			}
		}
		for (int xi = firstVertGridIndex; xi < lastVertGridIndex; ++xi) {
			final float x = worldToScreenX(xi * this.gridWidth);
			canvas.drawLine(x, minScreenY, x, maxScreenY, this.gridPaint);
			for (int yi = firstHorizGridIndex; yi < lastHorizGridIndex; ++yi) {
				final float y1 = worldToScreenY(yi * this.gridHeight - this.gridHeight / 3);
				final float y2 = worldToScreenY(yi * this.gridHeight + this.gridHeight / 3);
				canvas.drawLine(x - handleLength, y1, x + handleLength, y1, this.gridPaint);
				canvas.drawLine(x - handleLength, y2, x + handleLength, y2, this.gridPaint);
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
						final int column = this.mEndHandle.column;
						final int row = this.mEndHandle.row;
						final int from;
						if (this.mStartHandle.column != column) {
							// 2 -> 7
							// 3 -> 6
							// 7 -> 2
							// 6 -> 3
							from = 9 - this.mStartHandle.handleIndex;
						} else if (this.mStartHandle.row != row) {
							// 0 -> 5
							// 1 -> 4
							// 5 -> 0
							// 4 -> 1
							from = 5 - this.mStartHandle.handleIndex;
						} else {
							from = this.mStartHandle.handleIndex;
						}
						final int to = this.mEndHandle.handleIndex;
						this.model.getCell(column, row).connect(from, to);
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

	private int clamp(final int min, final int max, final int i)
	{
		return i < min ? min : i > max ? max : i;
	}

	private Handle getNearestHandle(final float worldX, final float worldY, final Handle adjacentHandle)
	{
		// Find the cell in which the touch occurred.
		final int minColumn;
		final int maxColumn;
		final int minRow;
		final int maxRow;
		if (adjacentHandle == null) {
			minColumn = 0;
			maxColumn = this.model.getNumColumns() + 1;
			minRow = 0;
			maxRow = this.model.getNumRows() + 1;
		} else {
			switch (adjacentHandle.handleIndex) {
				case 0:
				case 1:
				case 4:
				case 5:
					minColumn = adjacentHandle.column;
					maxColumn = adjacentHandle.column;
					break;
				case 2:
				case 3:
					minColumn = adjacentHandle.column;
					maxColumn = adjacentHandle.column + 1;
					break;
				case 6:
				case 7:
					minColumn = adjacentHandle.column - 1;
					maxColumn = adjacentHandle.column;
					break;
				default:
					minColumn = adjacentHandle.column;
					maxColumn = adjacentHandle.column;
			}
			switch (adjacentHandle.handleIndex) {
				case 0:
				case 1:
					minRow = adjacentHandle.row - 1;
					maxRow = adjacentHandle.row;
					break;
				case 2:
				case 3:
				case 6:
				case 7:
					minRow = adjacentHandle.row;
					maxRow = adjacentHandle.row;
					break;
				case 4:
				case 5:
					minRow = adjacentHandle.row;
					maxRow = adjacentHandle.row + 1;
					break;
				default:
					minRow = adjacentHandle.row;
					maxRow = adjacentHandle.row;
			}
		}
		final int column = clamp(minColumn, maxColumn, (int) FloatMath.floor(worldX / this.gridWidth));
		final int row = clamp(minRow, maxRow, (int) FloatMath.floor(worldY / this.gridHeight));

		// Calculate the cell-relative coordinates of the touch, from 0 to 1.
		final float cellPropX = (worldX - column * this.gridWidth) / this.gridWidth;
		final float cellPropY = (worldY - row * this.gridHeight) / this.gridHeight;

		// Figure out the nearest handle in the cell
		final int handleIndex;
		if (cellPropX < 0.5 && column != 0 || column == this.model.getNumColumns() + 1) {
			// Handle index 0, 5, 6, 7
			if (cellPropY < 0.5 && row != 0 || row == this.model.getNumRows() + 1) {
				// Handle index 0, 7
				if (cellPropX < cellPropY) {
					handleIndex = 7;
				} else {
					handleIndex = 0;
				}
			} else {
				// Handle index 5, 6
				if (cellPropX > 1 - cellPropY) {
					handleIndex = 5;
				} else {
					handleIndex = 6;
				}
			}
		} else {
			// Handle index 1, 2, 3, 4
			if (cellPropY < 0.5 && row != 0 || row == this.model.getNumRows() + 1) {
				// Handle index 1, 2
				if (1 - cellPropX > cellPropY) {
					handleIndex = 1;
				} else {
					handleIndex = 2;
				}
			} else {
				// Handle index 3, 4
				if (cellPropX < cellPropY) {
					handleIndex = 4;
				} else {
					handleIndex = 3;
				}
			}
		}

		final Handle handle = new Handle();
		handle.worldX = column * this.gridWidth + handlePropX[handleIndex] * this.gridWidth;
		handle.worldY = row * this.gridHeight + handlePropY[handleIndex] * this.gridHeight;
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
