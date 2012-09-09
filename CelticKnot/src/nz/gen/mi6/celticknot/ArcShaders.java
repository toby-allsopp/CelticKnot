package nz.gen.mi6.celticknot;

import android.opengl.GLES20;

class ArcShaders {

	private final String fragmentShaderCode =
			"precision mediump float;" +
					"uniform vec4 vColor;" +
					"void main() {" +
					"  gl_FragColor = vColor;" +
					"}";
	private final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"void main() {" +
					// the matrix must be included as a modifier of gl_Position
					"  gl_Position = uMVPMatrix * vPosition;"
					+
					"}";
	int positionHandle;
	int colorHandle;
	final int program;
	int MVPMatrixHandle;

	ArcShaders()
	{
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

		this.positionHandle = GLES20.glGetAttribLocation(this.program, "vPosition");
		MyGLRenderer.checkGlError("glGetAttribLocation");
		this.colorHandle = GLES20.glGetUniformLocation(this.program, "vColor");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		this.MVPMatrixHandle = GLES20.glGetUniformLocation(this.program, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");
	}
}
