package nz.gen.mi6.celticknot;

import android.opengl.GLES20;

class ArcShaders {

	private final String vertexShaderCode = ""
			+ "attribute vec4 position;"
			+ "attribute vec2 texcoord;"
			+ "uniform mat4 uMVPMatrix;"
			+ "varying vec2 vTexCoord;"
			+ "void main() {"
			+ "  gl_Position = uMVPMatrix * position;"
			+ "  vTexCoord = texcoord;"
			+ "}";
	private final String fragmentShaderCode = ""
			+ "precision mediump float;"
			+ "uniform vec4 uOutsideColor;"
			+ "uniform vec4 uInsideColor;"
			+ "varying vec2 vTexCoord;"
			+ "void main() {"
			// + "  gl_FragColor = uColor;"
			+ "  if (vTexCoord.s < 0.1 || vTexCoord.s > 0.9) {"
			+ "    gl_FragColor = uOutsideColor;"
			+ "  } else {"
			+ "    gl_FragColor = uInsideColor;"
			+ "  }"
			+ "}";

	int positionHandle;
	int outsideColorHandle;
	int insideColorHandle;
	final int program;
	int MVPMatrixHandle;
	int texCoordHandle;

	ArcShaders()
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
		this.texCoordHandle = GLES20.glGetAttribLocation(this.program, "texcoord");
		MyGLRenderer.checkGlError("glGetAttribLocation");
		this.outsideColorHandle = GLES20.glGetUniformLocation(this.program, "uOutsideColor");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		this.insideColorHandle = GLES20.glGetUniformLocation(this.program, "uInsideColor");
		MyGLRenderer.checkGlError("glGetUniformLocation");
		this.MVPMatrixHandle = GLES20.glGetUniformLocation(this.program, "uMVPMatrix");
		MyGLRenderer.checkGlError("glGetUniformLocation");
	}
}
