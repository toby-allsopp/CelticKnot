package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

class LineSegment implements GLDrawable {

	private final FloatBuffer vertexBuffer;
	private final FloatBuffer texCoordBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	private final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes
																	// per
																	// vertex

	private static final String LOG_TAG = "LineSegment";

	// Set color with red, green, blue and alpha (opacity) values
	private final float outsideColor[] = { 0.f, 0.f, 0.f, 1.0f };
	private final float insideColor[] = { 1.f, 0.f, 0.f, 1.0f };

	LineSegment(
			final float x1,
			final float y1,
			final float x2,
			final float y2,
			final float width,
			final ZCalculator zcalc,
			final float[] matrix)
	{
		final double a = Math.atan2(y2 - y1, x2 - x1);
		final float dx1 = (float) (width / 2 * Math.cos(a + Math.PI / 2));
		final float dy1 = (float) (width / 2 * Math.sin(a + Math.PI / 2));
		final float xm = (x1 + x2) / 2.f;
		final float ym = (y1 + y2) / 2.f;
		final float zStart = zcalc.z(0.f);
		final float zMiddle = zcalc.z(.5f);
		final float zEnd = zcalc.z(1.f);
		this.coords = new float[] {
				x2 + dx1, y2 + dy1, zStart, // 3
				x2 - dx1, y2 - dy1, zStart, // 4
				xm + dx1, ym + dy1, zMiddle, // 2
				xm - dx1, ym - dy1, zMiddle, // 5
				x1 + dx1, y1 + dy1, zEnd, // 1
				x1 - dx1, y1 - dy1, zEnd, // 0
		};
		final float[] texcoords = {
				0.f, 0.f,
				1.f, 0.f,
				0.f, 0.f,
				1.f, 0.f,
				0.f, 0.f,
				1.f, 0.f,
		};
		if (matrix != null)
		{
			final float[] vec = new float[8];
			for (int i = 0; i < this.coords.length / COORDS_PER_VERTEX; ++i) {
				System.arraycopy(this.coords, i * COORDS_PER_VERTEX, vec, 0, COORDS_PER_VERTEX);
				vec[3] = 1; // because we know that COORDS_PER_VERTEX == 3
				Matrix.multiplyMV(vec, 4, matrix, 0, vec, 0);
				System.arraycopy(vec, 4, this.coords, i * COORDS_PER_VERTEX, COORDS_PER_VERTEX);
			}
		}
		{
			final ByteBuffer bb = ByteBuffer.allocateDirect(this.coords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			this.vertexBuffer = bb.asFloatBuffer();
			this.vertexBuffer.put(this.coords);
			this.vertexBuffer.position(0);
		}
		{
			final ByteBuffer bb = ByteBuffer.allocateDirect(texcoords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			this.texCoordBuffer = bb.asFloatBuffer();
			this.texCoordBuffer.put(texcoords);
			this.texCoordBuffer.position(0);
		}
	}

	@Override
	public void draw(final Shaders shaders, final float[] mvpMatrix)
	{
		final KnotShaders knotShaders = shaders.knotShaders;

		// Add program to OpenGL environment
		GLES20.glUseProgram(knotShaders.program);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(knotShaders.positionHandle);
		GLES20.glEnableVertexAttribArray(knotShaders.texCoordHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(knotShaders.positionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, this.vertexBuffer);

		// Prepare the texture data
		GLES20.glVertexAttribPointer(knotShaders.texCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, this.texCoordBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(knotShaders.outsideColorHandle, 1, this.outsideColor, 0);
		GLES20.glUniform4fv(knotShaders.insideColorHandle, 1, this.insideColor, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(knotShaders.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(knotShaders.positionHandle);
		GLES20.glDisableVertexAttribArray(knotShaders.texCoordHandle);
	}
}
