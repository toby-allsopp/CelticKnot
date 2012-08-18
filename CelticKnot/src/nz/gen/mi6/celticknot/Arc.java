package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.util.FloatMath;

class Arc implements GLDrawable {

	private final FloatBuffer vertexBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	private final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes
																	// per
																	// vertex

	// Set color with red, green, blue and alpha (opacity) values
	private final float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

	Arc(
			final float cx,
			final float cy,
			final float startAngle,
			final float endAngle,
			final float radius,
			final float width)
	{
		final int numEdges = 50;
		final float deltaAngle = (endAngle - startAngle) / numEdges;
		final float r1 = radius - width / 2.f;
		final float r2 = r1 + width;
		this.coords = new float[(numEdges * 2 + 2) * COORDS_PER_VERTEX];
		int v = 0;
		for (int e = 0; e <= numEdges; ++e) {
			final float angle = startAngle + e * deltaAngle;
			final float cos = FloatMath.cos(angle);
			final float sin = FloatMath.sin(angle);
			final float x1 = cx + r1 * cos;
			final float y1 = cy + r1 * sin;
			final float x2 = cx + r2 * cos;
			final float y2 = cy + r2 * sin;
			final float z = (FloatMath.cos((angle - startAngle) / (endAngle - startAngle) * (float) Math.PI * 2) - 1.f) / 2.f;
			this.coords[v * COORDS_PER_VERTEX + 0] = x1;
			this.coords[v * COORDS_PER_VERTEX + 1] = y1;
			this.coords[v * COORDS_PER_VERTEX + 2] = z;
			++v;
			this.coords[v * COORDS_PER_VERTEX + 0] = x2;
			this.coords[v * COORDS_PER_VERTEX + 1] = y2;
			this.coords[v * COORDS_PER_VERTEX + 2] = z;
			++v;
		}
		final ByteBuffer bb = ByteBuffer.allocateDirect(this.coords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		this.vertexBuffer = bb.asFloatBuffer();
		this.vertexBuffer.put(this.coords);
		this.vertexBuffer.position(0);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nz.gen.mi6.celticknot.GLDrawable#draw(float[])
	 */
	@Override
	public void draw(final ArcShaders arcShaders, final float[] mvpMatrix)
	{
		// Add program to OpenGL environment
		GLES20.glUseProgram(arcShaders.program);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(arcShaders.positionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(arcShaders.positionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, this.vertexBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(arcShaders.colorHandle, 1, this.color, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(arcShaders.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(arcShaders.positionHandle);
	}
}
