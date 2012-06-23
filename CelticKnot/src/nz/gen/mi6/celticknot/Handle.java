package nz.gen.mi6.celticknot;

class Handle {
	float worldX;
	float worldY;
	int column;
	int row;
	int handleIndex;

	boolean adjacentTo(final Handle handle)
	{
		if (handle.row == this.row && handle.column == this.column) {
			return true;
		}
		switch (this.handleIndex) {
			case 0:
			case 1:
				return handle.column == this.column && handle.row == this.row - 1;
			case 2:
			case 3:
				return handle.column == this.column + 1 && handle.row == this.row;
			case 4:
			case 5:
				return handle.column == this.column && handle.row == this.row + 1;
			case 6:
			case 7:
				return handle.column == this.column - 1 && handle.row == this.row;
			default:
				return false;
		}
	}
}
