package nz.gen.mi6.celticknot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawingModel {

	private final Cell[][] cells;
	private static final int[] neighbourFroms = { 4, 5, 6, 7, 0, 1, 2, 3 };

	public DrawingModel(final int columns, final int rows)
	{
		if (columns < 0 || rows < 0) {
			throw new IllegalArgumentException("columns and rows mst be >= 0");
		}
		this.cells = new Cell[columns + 2][rows + 2];
		for (int column = 0; column < columns + 2; ++column) {
			for (int row = 0; row < rows + 2; ++row) {
				final Cell cell = new Cell();
				if (column == 0) {
					if (row == 0) {
						cell.connect(3, 4);
					} else if (row == rows + 1) {
						cell.connect(2, 3);
					} else {
						cell.connect(2, 4);
					}
				} else if (column == columns + 1) {
					if (row == 0) {
						cell.connect(6, 7);
					} else if (row == rows + 1) {
						cell.connect(0, 7);
					} else {
						cell.connect(0, 6);
					}
				} else {
					if (row == 0) {
						cell.connect(4, 7);
						cell.connect(3, 6);
					} else if (row == rows + 1) {
						cell.connect(0, 3);
						cell.connect(2, 7);
					} else {
						cell.connect(0, 4);
						cell.connect(2, 6);
					}
				}
				this.cells[column][row] = cell;
			}
		}
	}

	public int getNumColumns()
	{
		return this.cells.length - 2;
	}

	public int getNumRows()
	{
		return this.cells[0].length - 2;
	}

	/**
	 * 0 is the first half-cell, 1 is the first full cell, numColumns is the
	 * last full cell, numColumns + 1 is the last half-cell
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	public Cell getCell(final int column, final int row)
	{
		return this.cells[column][row];
	}

	public Set<Integer> getPossibleAlternativeTos(
		final int column,
		final int row,
		final int from)
	{
		final Set<Integer> alternatives = new HashSet<Integer>();
		final boolean firstCol = column == 0;
		final boolean firstRow = row == 0;
		final boolean finalCol = column == this.cells.length - 1;
		final boolean finalRow = row == this.cells[0].length - 1;
		final boolean anyOldCol = true;
		final boolean anyOldRow = true;
		final Cell[] neighbours = {
			!firstCol && !firstRow ? getCell(column - 1, row - 1) : null,
			anyOldCol && !firstRow ? getCell(column + 0, row - 1) : null,
			!finalCol && !firstRow ? getCell(column + 1, row - 1) : null,
			!finalCol && anyOldRow ? getCell(column + 1, row + 0) : null,
			!finalCol && !finalRow ? getCell(column + 1, row + 1) : null,
			anyOldCol && !finalRow ? getCell(column + 0, row + 1) : null,
			!firstCol && !finalRow ? getCell(column - 1, row + 1) : null,
			!firstCol && anyOldRow ? getCell(column - 1, row + 0) : null,
		};
		final Cell cell = getCell(column, row);
		final int to = cell.getConnectionFrom(from);
		if (to != -1) {
			for (int altTo = 0; altTo < neighbours.length; ++altTo) {
				if (altTo == from || altTo == to) {
					continue;
				}
				if (neighbours[altTo] != null &&
					neighbours[altTo].getConnectionFrom(neighbourFroms[altTo]) != -1)
				{
					alternatives.add(altTo);
				}
			}
		}
		return alternatives;
	}

	public static class Path
	{
		public final List<PathCoord> coords = new ArrayList<PathCoord>();
	}

	public static class PathCoord
	{
		public final int column;
		public final int row;
		public final int from;
		public final int to;

		public PathCoord(final int column, final int row, final int from, final int to)
		{
			this.column = column;
			this.row = row;
			this.from = from;
			this.to = to;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + this.column;
			result = prime * result + this.row;
			result = prime * result + Math.min(this.from, this.to);
			result = prime * result + Math.max(this.to, this.from);
			return result;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final PathCoord other = (PathCoord) obj;
			if (this.column != other.column) {
				return false;
			}
			if (this.row != other.row) {
				return false;
			}
			return this.from == other.from && this.to == other.to || this.from == other.to && this.to == other.from;
		}
	}

	public List<Path> calcPaths()
	{
		final List<Path> paths = new ArrayList<Path>();
		final Set<PathCoord> done = new HashSet<PathCoord>();
		for (int column = 0; column < this.cells.length; ++column) {
			for (int row = 0; row < this.cells[column].length; ++row) {
				final Cell cell = this.cells[column][row];
				for (int from = 0; from < 8; ++from) {
					final int to = cell.getConnectionFrom(from);
					if (to != -1) {
						final PathCoord coord = new PathCoord(column, row, from, to);
						if (!done.contains(coord)) {
							final Path path = calcPathFrom(coord);
							paths.add(path);
							done.addAll(path.coords);
						}
					}
				}
			}
		}
		return paths;
	}

	private Path calcPathFrom(final PathCoord firstCoord)
	{
		final Path path = new Path();
		path.coords.add(firstCoord);
		PathCoord prevCoord = firstCoord;
		while (true) {
			final int[] columnOffset = { -1, 0, 1, 1, 1, 0, -1, -1 };
			final int[] rowOffset = { -1, -1, -1, 0, 1, 1, 1, 0 };
			final int column = prevCoord.column + columnOffset[prevCoord.to];
			final int row = prevCoord.row + rowOffset[prevCoord.to];
			final int from = neighbourFroms[prevCoord.to];
			final int to = this.cells[column][row].getConnectionFrom(from);
			final PathCoord nextCoord = new PathCoord(column, row, from, to);
			if (nextCoord.equals(firstCoord)) {
				break;
			}
			path.coords.add(nextCoord);
			if (nextCoord.to == -1) {
				break;
			}
			prevCoord = nextCoord;
		}
		return path;
	}
}
