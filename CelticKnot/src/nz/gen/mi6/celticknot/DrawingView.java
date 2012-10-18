package nz.gen.mi6.celticknot;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class DrawingView extends GLSurfaceView {

	private class ScaleGestureListener implements OnScaleGestureListener {

		@Override
		public boolean onScaleBegin(final ScaleGestureDetector detector)
		{
			return true;
		}

		@Override
		public boolean onScale(final ScaleGestureDetector detector)
		{
			DrawingView.this.renderer.zoom(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
			requestRender();
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
			return false;
		}

		@Override
		public void onShowPress(final MotionEvent e)
		{
		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY)
		{
			DrawingView.this.renderer.scroll(distanceX, distanceY);
			requestRender();
			return true;
		}

		@Override
		public void onLongPress(final MotionEvent e)
		{
		}

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY)
		{
			return false;
		}

		@Override
		public boolean onDown(final MotionEvent e)
		{
			return false;
		}
	}

	private final GestureDetector gestureDetector;
	private final ScaleGestureDetector scaleGestureDetector;

	private final MyGLRenderer renderer = new MyGLRenderer();

	public DrawingView(final Context context, final AttributeSet attrs)
	{
		super(context, attrs);
		this.gestureDetector = new GestureDetector(context, new GestureListener());
		this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());

		// Create an OpenGL ES 2.0 context.
		setEGLContextClientVersion(2);

		setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
		setRenderer(this.renderer);

		// Render the view only when there is a change in the drawing data
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	public void setModel(final DrawingModel model)
	{
		this.renderer.setModel(model);
		invalidate();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event)
	{
		requestRender();
		this.scaleGestureDetector.onTouchEvent(event);
		this.gestureDetector.onTouchEvent(event);
		return true;
	}
}
