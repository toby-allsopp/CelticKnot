/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.gen.mi6.celticknot;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.FloatMath;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final class RampZCalculator implements ZCalculator {
		private final float startZ;
		private final float stopZ;

		private RampZCalculator(final float startZ, final float stopZ)
		{
			this.startZ = startZ;
			this.stopZ = stopZ;
		}

		@Override
		public float z(final float p)
		{
			return this.startZ + FloatMath.sin(p * (float) Math.PI / 2.f) * (this.stopZ - this.startZ);
		}
	}

	private static final class HumpZCalculator implements ZCalculator {
		private final float startZ;
		private final float middleZ;

		private HumpZCalculator(final float startZ, final float middleZ)
		{
			this.startZ = startZ;
			this.middleZ = middleZ;
		}

		@Override
		public float z(final float p)
		{
			return this.startZ + FloatMath.sin(p * (float) Math.PI) * (this.middleZ - this.startZ);
		}
	}

	private static final String TAG = "MyGLRenderer";

	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];

	private float cx;
	private float cy;

	private ArrayList<GLDrawable> objects = new ArrayList<GLDrawable>();

	private final Shaders shaders = new Shaders();

	private int width;

	private int height;

	public void setModel(final DrawingModel model)
	{
		final ArrayList<GLDrawable> objects = new ArrayList<GLDrawable>();

		drawGrid(objects, model);
		drawKnot(objects, model);

		synchronized (this) {
			this.objects = objects;
		}
	}

	private void drawGrid(final ArrayList<GLDrawable> objects, final DrawingModel model)
	{
		objects.add(new Grid(0.f, 0.f, 1.f, 1.f, model.getNumColumns() + 3, model.getNumRows() + 3));
	}

	private void drawKnot(final ArrayList<GLDrawable> objects, final DrawingModel model)
	{
		final float[] matrix = new float[16];

		for (int column = 0; column < model.getNumColumns() + 2; ++column) {
			for (int row = 0; row < model.getNumRows() + 2; ++row) {
				final boolean skip = false; /*column != 1 || row != 0;*/
				if (skip) {
					continue;
				}
				final Cell cell = model.getCell(column, row);
				Matrix.setIdentityM(matrix, 0);
				Matrix.translateM(matrix, 0, column, row, 0);
				for (int i = 0; i < 8; ++i) {
					final int from = i;
					final int to = cell.getConnectionFrom(from);
					if (to != -1) {
						drawKnotSegment(objects, from, to, matrix);
					}
				}
			}
		}
	}

	private static final float[] handlePropX = { 0.f, .5f, 1.f, 1.f, 1.f, .5f, 0.f, 0.f };

	private static final float[] handlePropY = { 0.f, 0.f, 0.f, .5f, 1.f, 1.f, 1.f, .5f };

	private static final float[] handlePropZ = { 0.f, 0.f, -1.f, -1.f, 0.f, 0.f, -1.f, -1.f };

	private float w = 5.f;

	private float h = 5.f;

	private float rh;

	private float rw;

	private void drawKnotSegment(final ArrayList<GLDrawable> objects, final int from, final int to, final float[] matrix)
	{
		final float DEG_TO_RAD = (float) (Math.PI / 180.);
		final float width = 0.1f;

		final float startWorldX = handlePropX[from];
		final float stopWorldX = handlePropX[to];
		final float startWorldY = handlePropY[from];
		final float stopWorldY = handlePropY[to];
		final float startWorldZ = handlePropZ[from] * width * 2;
		final float stopWorldZ = handlePropZ[to] * width * 2;
		final float zMiddle = -width * 2 - startWorldZ;
		/*final float sqrt8 = FloatMath.sqrt(2) * 2;
		final float adjustStartX;
		final float adjustStopX;
		final float adjustStartY;
		final float adjustStopY;
		if (from == 2) {
			adjustStartX = -.3f / sqrt8;
			adjustStopX = .3f / sqrt8;
			adjustStartY = .3f / sqrt8;
			adjustStopY = -.3f / sqrt8;
		} else {
			adjustStartX = 0.f;
			adjustStopX = 0.f;
			adjustStartY = 0.f;
			adjustStopY = 0.f;
		}*/
		if ((to - from == 2 || from == 0 && to == 6) && (to & 1) == 0) {
			final float radius = FloatMath.sqrt(2) / 2;
			final float cx, cy;
			final float startAngle;
			final float startZ, stopZ;
			switch (from) {
				case 0:
					if (to == 2) {
						cx = .5f;
						cy = -.5f;
						startAngle = 45.f * DEG_TO_RAD;
						startZ = stopWorldZ;
						stopZ = startWorldZ;
					} else {
						cx = -.5f;
						cy = .5f;
						startAngle = 315.f * DEG_TO_RAD;
						startZ = startWorldZ;
						stopZ = stopWorldZ;
					}
					break;
				case 2:
					cx = 1.5f;
					cy = .5f;
					startAngle = 135.f * DEG_TO_RAD;
					startZ = stopWorldZ;
					stopZ = startWorldZ;
					break;
				case 4:
					cx = .5f;
					cy = 1.5f;
					startAngle = 225.f * DEG_TO_RAD;
					startZ = stopWorldZ;
					stopZ = startWorldZ;
					break;
				default:
					throw new AssertionError();
			}
			final float sweepAngle = (float) (Math.PI / 2);
			objects.add(new Strip(
					matrix,
					new ArcStripCalculator(
							cx,
							cy,
							radius,
							startAngle,
							startAngle + sweepAngle,
							width,
							new RampZCalculator(startZ, stopZ))));
		} else if (from == 0 && to == 3 || from == 2 && to == 7 || from == 3 && to == 6 || from == 4 && to == 7) {
			final float startX, startY;
			final float stopX, stopY;
			final float radius = FloatMath.sqrt(2) / 2;
			final float cx, cy;
			final float startAngle;
			final float sweepAngle = 45.f * DEG_TO_RAD;
			switch (from) {
				case 0:
					startX = 1.f;
					startY = radius;
					cx = 1.f;
					cy = 0.f;
					startAngle = 90.f * DEG_TO_RAD;
					stopX = 0.f;
					stopY = 0.f;
					break;
				case 2:
					startX = 1.f;
					startY = 0.f;
					cx = 0.f;
					cy = 0.f;
					startAngle = 45.f * DEG_TO_RAD;
					stopX = 0.f;
					stopY = radius;
					break;
				case 3:
					startX = 0.f;
					startY = 1.f;
					cx = 1.f;
					cy = 1.f;
					startAngle = 225.f * DEG_TO_RAD;
					stopX = 1.f;
					stopY = 1.f - radius;
					break;
				case 4:
					startX = 0.f;
					startY = 1.f - radius;
					cx = 0.f;
					cy = 1.f;
					startAngle = 270.f * DEG_TO_RAD;
					stopX = 1.f;
					stopY = 1.f;
					break;
				default:
					throw new AssertionError();
			}
			objects.add(new Strip(matrix,
					new LineSegmentStripCalculator(
							startX,
							startY,
							cx + FloatMath.cos(startAngle) * radius,
							cy + FloatMath.sin(startAngle) * radius,
							width,
							new RampZCalculator(startWorldZ, zMiddle))));
			final float endAngle = startAngle + sweepAngle;
			objects.add(new Strip(
					matrix,
					new ArcStripCalculator(
							cx,
							cy,
							radius,
							startAngle,
							endAngle,
							width,
							new RampZCalculator(startWorldZ, stopWorldZ))));
			objects.add(new Strip(matrix,
					new LineSegmentStripCalculator(
							cx + FloatMath.cos(endAngle) * radius,
							cy + FloatMath.sin(endAngle) * radius,
							stopX,
							stopY,
							width,
							new RampZCalculator(startWorldZ, zMiddle))));
		} else if ((from & 1) == 0 && to == from + 4) {
			/*path.moveTo(startWorldX + adjustStartX, startWorldY + adjustStartY);
			path.lineTo(stopWorldX + adjustStopX, stopWorldY + adjustStopY);*/
			objects.add(new Strip(matrix,
					new LineSegmentStripCalculator(
							startWorldX,
							startWorldY,
							stopWorldX,
							stopWorldY,
							width,
							new HumpZCalculator(startWorldZ, zMiddle))));
		} else {
		}
	}

	public void scroll(final float px, final float py)
	{
		this.cx += px / this.width * this.rw * 2;
		this.cy -= py / this.height * this.rh * 2;
	}

	public void zoom(final float focusX, final float focusY, final float scaleFactor)
	{
		this.w /= scaleFactor;
		this.h /= scaleFactor;
		calcProj();
	}

	@Override
	public void onSurfaceCreated(final GL10 unused, final EGLConfig config)
	{

		// Set the background frame color
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LESS);

		this.shaders.knotShaders = new KnotShaders();
		this.shaders.gridShaders = new GridShaders();
	}

	@Override
	public void onSurfaceChanged(final GL10 unused, final int width, final int height)
	{
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);
		this.width = width;
		this.height = height;

		calcProj();

	}

	private void calcProj()
	{
		final float ratio = (float) this.width / this.height;
		if (ratio < 1) {
			this.rw = this.w;
			this.rh = this.h / ratio;
		} else {
			this.rw = this.w * ratio;
			this.rh = this.h;
		}

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(this.mProjMatrix, 0, -this.rw, this.rw, -this.rh, this.rh, 3, 7);
		// Matrix.orthoM(this.mProjMatrix, 0, -f, f, -f, f, 3, 7);
	}

	@Override
	public void onDrawFrame(final GL10 unused)
	{
		final long startNanos = System.nanoTime();

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(this.mVMatrix, 0,
				this.cx - 1, this.cy - 1, 4,
				this.cx, this.cy, 0f,
				0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjMatrix, 0, this.mVMatrix, 0);

		synchronized (this) {
			for (final GLDrawable object : this.objects) {
				object.draw(this.shaders, this.mMVPMatrix);
			}
		}

		final long frameNanos = System.nanoTime() - startNanos;
		Log.i(TAG, String.format("drawFrame took %6.2fms", frameNanos / 1000. / 1000.));
	}

	public static int loadShader(final int type, final String shaderCode)
	{

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		final int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		MyGLRenderer.checkGlError("glGetAttribLocation");
		GLES20.glCompileShader(shader);
		MyGLRenderer.checkGlError("glGetAttribLocation");

		return shader;
	}

	/**
	 * Utility method for debugging OpenGL calls. Provide the name of the call
	 * just after making it:
	 * 
	 * <pre>
	 * mColorHandle = GLES20.glGetUniformLocation(mProgram, &quot;vColor&quot;);
	 * MyGLRenderer.checkGlError(&quot;glGetUniformLocation&quot;);
	 * </pre>
	 * 
	 * If the operation is not successful, the check throws an error.
	 * 
	 * @param glOperation
	 *            - Name of the OpenGL call to check.
	 */
	public static void checkGlError(final String glOperation)
	{
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, glOperation + ": glError " + error);
			throw new RuntimeException(glOperation + ": glError " + error);
		}
	}
}
