package nz.gen.mi6.celticknot;

final class LineSegmentStripCalculator implements StripCalculator {
	private final ZCalculator zcalc;
	private final float x1mx2;
	private final float y1my2;
	private final float cornerX1;
	private final float cornerX2;
	private final float cornerY1;
	private final float cornerY2;

	LineSegmentStripCalculator(
			final float x1,
			final float y1,
			final float x2,
			final float y2,
			final float width,
			final ZCalculator zcalc)
	{
		this.zcalc = zcalc;
		final double a = Math.atan2(y2 - y1, x2 - x1);
		final float dx1 = (float) (width / 2 * Math.cos(a + Math.PI / 2));
		final float dy1 = (float) (width / 2 * Math.sin(a + Math.PI / 2));
		this.x1mx2 = x1 - x2;
		this.y1my2 = y1 - y2;
		this.cornerX1 = x2 + dx1;
		this.cornerX2 = x2 - dx1;
		this.cornerY1 = y2 + dy1;
		this.cornerY2 = y2 - dy1;
	}

	@Override
	public void edges(final float p, final float[] e1, final float[] e2)
	{
		final float px = this.x1mx2 * p;
		e1[0] = this.cornerX1 + px;
		e2[0] = this.cornerX2 + px;
		final float py = this.y1my2 * p;
		e1[1] = this.cornerY1 + py;
		e2[1] = this.cornerY2 + py;
		final float z = this.zcalc.z(p);
		e1[2] = z;
		e2[2] = z;
	}
}