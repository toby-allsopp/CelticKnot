package nz.gen.mi6.celticknot;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

class LineSegment {

	private final FloatBuffer vertexBuffer;

	// number of coordinates per vertex in this array
	private static final int COORDS_PER_VERTEX = 3;
	private final float[] coords;

	private final static int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes
																	// per
																	// vertex

	// Set color with red, green, blue and alpha (opacity) values
	private final float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

	private final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"void main() {" +
					// the matrix must be included as a modifier of gl_Position
					"  gl_Position = vPosition * uMVPMatrix;"
					+
					"}";

	private final String fragmentShaderCode =
			"precision mediump float;" +
					"uniform vec4 vColor;" +
					"void main() {" +
					"  gl_FragColor = vColor;" +
					"}";

	private final int program;
	private int positionHandle;
	private int colorHandle;
	private int MVPMatrixHandle;

	LineSegment(final float width)
	{
		this.coords = new float[] {
				0.5f, -width, 0.f, // 3
				0.5f, width, 0.f, // 4
				0.f, -width, -1.f, // 2
				0.f, width, -1.f, // 5
				-0.5f, -width, 0.f, // 1
				-0.5f, width, 0.f, // 0
		};
		final ByteBuffer bb = ByteBuffer.allocateDirect(this.coords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		this.vertexBuffer = bb.asFloatBuffer();
		this.vertexBuffer.put(this.coords);
		this.vertexBuffer.position(0);

		final int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCode);
		final int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCode);

		this.program = GLES20.glCreateProgram(); // create empty OpenGL ES
													// Program
		GLES20.glAttachShader(this.program, vertexShader); // add the vertex
															// shader to program
		GLES20.glAttachShader(this.program, fragmentShader); // add the
																// fragment
																// shader to
																// program
		GLES20.glLinkProgram(this.program); // creates OpenGL ES program
	}

	public void draw(final float[] mvpMatrix)
	{
		// Add program to OpenGL environment
		GLES20.glUseProgram(this.program);

		this.positionHandle = GLES20.glGetAttribLocation(this.program, "vPosition");
		MyGLRenderer.checkGlError("glGetAttribLocation");
		this.colorHandle = GLES20.glGetUniformLocation(this.program, "vColor");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		this.MVPMatrixHandle = GLES20.glGetUniformLocation(this.program, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(this.positionHandle);

		// Prepare the triangle coordinate data
		GLES20.glVertexAttribPointer(this.positionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false,
				vertexStride, this.vertexBuffer);

		// Set color for drawing the triangle
		GLES20.glUniform4fv(this.colorHandle, 1, this.color, 0);

		// Apply the projection and view transformation
		GLES20.glUniformMatrix4fv(this.MVPMatrixHandle, 1, false, mvpMatrix, 0);
		MyGLRenderer.checkGlError("glUniformMatrix4fv");

		// Draw the square
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, this.coords.length / COORDS_PER_VERTEX);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(this.positionHandle);
	}
}
