package nz.gen.mi6.celticknot;

public class DrawingModel {

	private final Cell[][] cells;

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

	public void cutVertical(final int column, final int row)
	{
		final Cell tl = getCell(column, row);
		final Cell tr = column < getNumColumns() + 1 ? getCell(column + 1, row) : null;
		final Cell bl = row < getNumRows() + 1 ? getCell(column, row + 1) : null;
		final Cell br = column < getNumColumns() + 1 && row < getNumRows() + 1 ? getCell(column + 1, row + 1) : null;
		tl.moveConnection(4, 5);
		if (tr != null) {
			tr.moveConnection(6, 5);
		}
		if (bl != null) {
			bl.moveConnection(2, 1);
		}
		if (br != null) {
			br.moveConnection(0, 1);
		}
	}
}
