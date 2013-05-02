package nz.gen.mi6.celticknot;

interface GLDrawable {

	void draw(Shaders shaders, float[] mvpMatrix);

	/**
	 * Get the acceleration caused by this object on a particular centre of
	 * mass.
	 * 
	 * @param x
	 *            centre of mass x
	 * @param y
	 *            centre of mass y
	 * @param z
	 *            centre of mass z
	 * @param out
	 *            the first 3 elements are set to the acceleration vector
	 */
	void getGravityAcceleration(float x, float y, float z, float[/*3*/] out);

}