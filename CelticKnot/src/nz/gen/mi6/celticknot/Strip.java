package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

class Strip implements GLDrawable {

	private final FloatBuffer vertexBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	private FloatBuffer texCoordBuffer;

	// Set color with red, green, blue and alpha (opacity) values
	private final float outsideColor[] = { 0.f, 0.f, 0.f, 1.0f };
	private final float insideColor[] = { 1.f, 0.f, 0.f, 1.0f };

	Strip(
			final float[] matrix,
			final StripCalculator stripCalc)
	{
		final int numEdges = 50;
		final int numVertices = numEdges * 2 + 2;
		this.coords = new float[numVertices * COORDS_PER_VERTEX];
		final float[] texcoords = new float[numVertices * 2];
		int v = 0;
		final float[] vec1 = new float[8];
		final float[] vec2 = new float[8];
		for (int e = 0; e <= numEdges; ++e) {
			final float p = (float) e / numEdges;
			stripCalc.edges(p, vec1, vec2);
			vec1[3] = 1;
			Matrix.multiplyMV(vec1, 4, matrix, 0, vec1, 0);
			this.coords[v * COORDS_PER_VERTEX + 0] = vec1[4];
			this.coords[v * COORDS_PER_VERTEX + 1] = vec1[5];
			this.coords[v * COORDS_PER_VERTEX + 2] = vec1[6];
			texcoords[v * 2 + 0] = 0.f;
			texcoords[v * 2 + 1] = 0.f;
			++v;
			vec2[3] = 1;
			Matrix.multiplyMV(vec2, 4, matrix, 0, vec2, 0);
			this.coords[v * COORDS_PER_VERTEX + 0] = vec2[4];
			this.coords[v * COORDS_PER_VERTEX + 1] = vec2[5];
			this.coords[v * COORDS_PER_VERTEX + 2] = vec2[6];
			texcoords[v * 2 + 0] = 1.f;
			texcoords[v * 2 + 1] = 0.f;
			++v;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see nz.gen.mi6.celticknot.GLDrawable#draw(float[])
	 */
	@Override
	public void draw(final Shaders shaders, final float[] mvpMatrix)
	{
		final KnotShaders arcShaders = shaders.knotShaders;

		// Add program to OpenGL environment
		GLES20.glUseProgram(arcShaders.program);

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(arcShaders.positionHandle);
		GLES20.glEnableVertexAttribArray(arcShaders.texCoordHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(
				arcShaders.positionHandle,
				COORDS_PER_VERTEX,
				GLES20.GL_FLOAT,
				false,
				COORDS_PER_VERTEX * 4,
				this.vertexBuffer);

		// Prepare the texture data
		GLES20.glVertexAttribPointer(arcShaders.texCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, this.texCoordBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(arcShaders.outsideColorHandle, 1, this.outsideColor, 0);
		GLES20.glUniform4fv(arcShaders.insideColorHandle, 1, this.insideColor, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(arcShaders.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(arcShaders.positionHandle);
		GLES20.glDisableVertexAttribArray(arcShaders.texCoordHandle);
	}
}
