package nz.gen.mi6.celticknot;

import android.opengl.GLES20;

class GridShaders {

	private final String vertexShaderCode = ""
			+ "attribute vec4 position;"
			+ "attribute vec2 texcoord;"
			+ "uniform mat4 uMVPMatrix;"
			+ "void main() {"
			+ "  gl_Position = uMVPMatrix * position;"
			+ "}";
	private final String fragmentShaderCode = ""
			+ "precision mediump float;"
			+ "uniform vec4 uColor;"
			+ "void main() {"
			+ "  gl_FragColor = uColor;"
			+ "}";

	int positionHandle;
	int colorHandle;
	final int program;
	int MVPMatrixHandle;

	GridShaders()
	{
		final int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, this.vertexShaderCode);
		final int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, this.fragmentShaderCode);

		this.program = GLES20.glCreateProgram();
		GLES20.glAttachShader(this.program, vertexShader);
		MyGLRenderer.checkGlError("glGetAttribLocation");
		GLES20.glAttachShader(this.program, fragmentShader);
		MyGLRenderer.checkGlError("glGetAttribLocation");
		GLES20.glLinkProgram(this.program);
		MyGLRenderer.checkGlError("glGetAttribLocation");

		this.positionHandle = GLES20.glGetAttribLocation(this.program, "position");
		MyGLRenderer.checkGlError("glGetAttribLocation");
		this.colorHandle = GLES20.glGetUniformLocation(this.program, "uColor");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		this.MVPMatrixHandle = GLES20.glGetUniformLocation(this.program, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");
	}
}
