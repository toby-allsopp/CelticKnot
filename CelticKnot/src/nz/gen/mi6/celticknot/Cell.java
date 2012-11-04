package nz.gen.mi6.celticknot;

import junit.framework.Assert;

public class Cell {
	private final int[] connections = { -1, -1, -1, -1, -1, -1, -1, -1 };

	public Cell()
	{
		assertInvariant();
	}

	public void connect(final int from, final int to)
	{
		disconnect(from);
		disconnect(to);

		this.connections[from] = to;
		this.connections[to] = from;
		assertInvariant();
	}

	public void disconnect(final int node)
	{
		final int to = this.connections[node];
		this.connections[node] = -1;
		if (to != -1) {
			this.connections[to] = -1;
		}
		assertInvariant();
	}

	public void swap(final int from, final int newTo)
	{
		final int oldTo = this.connections[from];
		final int otherFrom = this.connections[newTo];
		if (otherFrom != -1) {
			this.connections[otherFrom] = oldTo;
		}
		this.connections[oldTo] = otherFrom;
		this.connections[from] = newTo;
		this.connections[newTo] = from;
		assertInvariant();
	}

	public int getConnectionFrom(final int from)
	{
		return this.connections[from];
	}

	private void assertInvariant()
	{
		Assert.assertEquals(8, this.connections.length);
		for (int from = 0; from < 8; ++from) {
			final int to = this.connections[from];
			Assert.assertTrue(to == -1 || this.connections[to] == from);
		}
	}
}
