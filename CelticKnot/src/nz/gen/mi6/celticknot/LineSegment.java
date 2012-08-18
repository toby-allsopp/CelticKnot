package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

class LineSegment implements GLDrawable {

	private final FloatBuffer vertexBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	private final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes
																	// per
																	// vertex

	// Set color with red, green, blue and alpha (opacity) values
	private final float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

	LineSegment(final float x1, final float y1, final float x2, final float y2, final float width)
	{
		final double a = Math.atan2(y2 - y1, x2 - x1);
		final float dx1 = (float) (width / 2 * Math.cos(a + Math.PI / 2));
		final float dy1 = (float) (width / 2 * Math.sin(a + Math.PI / 2));
		final float xm = (x1 + x2) / 2.f;
		final float ym = (y1 + y2) / 2.f;
		this.coords = new float[] {
				x2 + dx1, y2 + dy1, 0.f, // 3
				x2 - dx1, y2 - dy1, 0.f, // 4
				xm + dx1, ym + dy1, -1.f, // 2
				xm - dx1, ym - dy1, -1.f, // 5
				x1 + dx1, y1 + dy1, 0.f, // 1
				x1 - dx1, y1 - dy1, 0.f, // 0
		};
		final ByteBuffer bb = ByteBuffer.allocateDirect(this.coords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		this.vertexBuffer = bb.asFloatBuffer();
		this.vertexBuffer.put(this.coords);
		this.vertexBuffer.position(0);
	}

	@Override
	public void draw(final ArcShaders shaders, final float[] mvpMatrix)
	{
		// Add program to OpenGL environment
		GLES20.glUseProgram(shaders.program);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(shaders.positionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(shaders.positionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, this.vertexBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(shaders.colorHandle, 1, this.color, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(shaders.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(shaders.positionHandle);
	}
}
