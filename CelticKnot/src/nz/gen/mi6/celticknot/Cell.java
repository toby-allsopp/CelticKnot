package nz.gen.mi6.celticknot;

import junit.framework.Assert;

public class Cell {
	private final int[] connections = { -1, -1, -1, -1, -1, -1, -1, -1 };

	public void connect(final int from, final int to)
	{
		this.connections[from] = to;
		this.connections[to] = from;
		for (int i = 0; i < 8; ++i) {
			if (i != from && this.connections[i] == to || i != to && this.connections[i] == from) {
				this.connections[i] = -1;
			}
		}
		Assert.assertTrue(invariant());
	}

	public int getConnectionFrom(final int from)
	{
		return this.connections[from];
	}

	private boolean invariant()
	{
		for (int from = 0; from < 8; ++from) {
			final int to = this.connections[from];
			if (to != -1 && this.connections[to] != from) {
				return false;
			}
		}
		return true;
	}
}
