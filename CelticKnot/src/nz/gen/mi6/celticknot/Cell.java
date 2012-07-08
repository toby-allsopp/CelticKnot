package nz.gen.mi6.celticknot;

import junit.framework.Assert;

public class Cell {
	private final int[] connections = { -1, -1, -1, -1, -1, -1, -1, -1 };

	public Cell()
	{
		Assert.assertTrue(invariant());
	}

	public void connect(final int from, final int to)
	{
		disconnect(from);
		disconnect(to);

		this.connections[from] = to;
		this.connections[to] = from;
		Assert.assertTrue(invariant());
	}

	public void disconnect(final int node)
	{
		final int to = this.connections[node];
		this.connections[node] = -1;
		if (to != -1) {
			this.connections[to] = -1;
		}
		Assert.assertTrue(invariant());
	}

	public void moveConnection(final int from, final int to)
	{
		if (this.connections[from] != -1) {
			this.connections[this.connections[from]] = to;
			this.connections[to] = from;
		}
		Assert.assertTrue(invariant());
	}

	public int getConnectionFrom(final int from)
	{
		return this.connections[from];
	}

	private boolean invariant()
	{
		if (this.connections.length != 8) {
			return false;
		}
		for (int from = 0; from < 8; ++from) {
			final int to = this.connections[from];
			if (to != -1 && this.connections[to] != from) {
				return false;
			}
		}
		return true;
	}
}
