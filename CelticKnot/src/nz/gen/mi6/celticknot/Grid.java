package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

class Grid implements GLDrawable {

	private final FloatBuffer vertexBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	// Set color with red, green, blue and alpha (opacity) values
	private final float outsideColor[] = { 0.f, 0.f, 0.f, 1.0f };

	Grid(
			final float x,
			final float y,
			final float dx,
			final float dy,
			final int nx,
			final int ny)
	{
		this.coords = new float[(nx * 2 + ny * 2) * COORDS_PER_VERTEX];
		int v = 0;
		for (int i = 0; i < nx; ++i) {
			this.coords[v * COORDS_PER_VERTEX + 0] = x + dx * i;
			this.coords[v * COORDS_PER_VERTEX + 1] = y;
			this.coords[v * COORDS_PER_VERTEX + 2] = 0.f;
			++v;
			this.coords[v * COORDS_PER_VERTEX + 0] = x + dx * i;
			this.coords[v * COORDS_PER_VERTEX + 1] = y + dy * (ny - 1);
			this.coords[v * COORDS_PER_VERTEX + 2] = 0.f;
			++v;
		}
		for (int i = 0; i < ny; ++i) {
			this.coords[v * COORDS_PER_VERTEX + 0] = x;
			this.coords[v * COORDS_PER_VERTEX + 1] = y + dy * i;
			this.coords[v * COORDS_PER_VERTEX + 2] = 0.f;
			++v;
			this.coords[v * COORDS_PER_VERTEX + 0] = x + dx * (nx - 1);
			this.coords[v * COORDS_PER_VERTEX + 1] = y + dy * i;
			this.coords[v * COORDS_PER_VERTEX + 2] = 0.f;
			++v;
		}
		{
			final ByteBuffer bb = ByteBuffer.allocateDirect(this.coords.length * 4);
			bb.order(ByteOrder.nativeOrder());
			this.vertexBuffer = bb.asFloatBuffer();
			this.vertexBuffer.put(this.coords);
			this.vertexBuffer.position(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nz.gen.mi6.celticknot.GLDrawable#draw(float[])
	 */
	@Override
	public void draw(final Shaders shaders, final float[] mvpMatrix)
	{
		final GridShaders gridShaders = shaders.gridShaders;

		// Add program to OpenGL environment
		GLES20.glUseProgram(gridShaders.program);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(gridShaders.positionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(
				gridShaders.positionHandle,
				COORDS_PER_VERTEX,
				GLES20.GL_FLOAT,
				false,
				COORDS_PER_VERTEX * 4,
				this.vertexBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(gridShaders.colorHandle, 1, this.outsideColor, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(gridShaders.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_LINES, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(gridShaders.positionHandle);
	}
}
