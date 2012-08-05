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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MyGLRenderer";

	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];

	private LineSegment mLineSegment;
	private Arc mArc;

	@Override
	public void onSurfaceCreated(final GL10 unused, final EGLConfig config)
	{

		// Set the background frame color
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		this.mLineSegment = new LineSegment(.3f);
		this.mArc = new Arc(0.f, (float) Math.PI, .5f, .1f);
	}

	@Override
	public void onDrawFrame(final GL10 unused)
	{
		final long startNanos = System.nanoTime();

		// Draw background color
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		// Set the camera position (View matrix)
		Matrix.setLookAtM(this.mVMatrix, 0,
				0, 0, -3,
				0f, 0f, 0f,
				0f, 1.0f, 0.0f);

		// Calculate the projection and view transformation
		Matrix.multiplyMM(this.mMVPMatrix, 0, this.mProjMatrix, 0, this.mVMatrix, 0);

		// Draw square
		// this.mLineSegment.draw(this.mMVPMatrix);
		this.mArc.draw(this.mMVPMatrix);

		final long frameNanos = System.nanoTime() - startNanos;
		Log.i(TAG, "drawFrame took " + frameNanos / 1000. / 1000. + "ms");
	}

	@Override
	public void onSurfaceChanged(final GL10 unused, final int width, final int height)
	{
		// Adjust the viewport based on geometry changes,
		// such as screen rotation
		GLES20.glViewport(0, 0, width, height);

		final float ratio = (float) width / height;

		// this projection matrix is applied to object coordinates
		// in the onDrawFrame() method
		Matrix.frustumM(this.mProjMatrix, 0, ratio, -ratio, -1, 1, 3, 7);

	}

	public static int loadShader(final int type, final String shaderCode)
	{

		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
		final int shader = GLES20.glCreateShader(type);

		// add the source code to the shader and compile it
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

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
