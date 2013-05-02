package nz.gen.mi6.celticknot;


final class ArcStripCalculator implements StripCalculator {
	private final float cy;
	private final ZCalculator zcalc;
	private final float endAngle;
	private final float cx;
	private final float startAngle;
	final float r1;
	final float r2;

	ArcStripCalculator(
		final float cx,
		final float cy,
		final float radius,
		final float startAngle,
		final float endAngle,
		final float width,
		final ZCalculator zcalc)
	{
		this.cy = cy;
		this.zcalc = zcalc;
		this.endAngle = endAngle;
		this.cx = cx;
		this.startAngle = startAngle;
		this.r1 = radius - width / 2.f;
		this.r2 = this.r1 + width;
	}

	@Override
	public void edges(final float p, final float[] e1, final float[] e2)
	{
		final float angle = this.startAngle + p * (this.endAngle - this.startAngle);
		final float cos = (float) Math.cos(angle);
		final float sin = (float) Math.sin(angle);
		e1[0] = this.cx + this.r1 * cos;
		e2[0] = this.cx + this.r2 * cos;
		e1[1] = this.cy + this.r1 * sin;
		e2[1] = this.cy + this.r2 * sin;
		e1[2] = this.zcalc.z(p);
		e2[2] = e1[2];
	}
}