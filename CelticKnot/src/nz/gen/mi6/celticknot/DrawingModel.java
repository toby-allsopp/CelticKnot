package nz.gen.mi6.celticknot;

public class DrawingModel {

	private final Cell[][] cells;

	public DrawingModel(final int columns, final int rows)
	{
		if (columns < 0 || rows < 0) {
			throw new IllegalArgumentException("columns and rows mst be >= 0");
		}
		this.cells = new Cell[columns + 2][rows + 2];
	}

	public int getNumColumns()
	{
		return this.cells[0].length - 2;
	}

	public int getNumRows()
	{
		return this.cells.length - 2;
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
}
