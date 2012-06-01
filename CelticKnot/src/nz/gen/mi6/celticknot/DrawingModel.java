package nz.gen.mi6.celticknot;

import android.graphics.Path;

public class DrawingModel {

	private final PersistentList<Path> paths;

	public DrawingModel()
	{
		this.paths = new PersistentLinkedList<Path>();
	}

	public DrawingModel(final PersistentList<Path> paths)
	{
		this.paths = paths;
	}

	public DrawingModel addPath(final Path path)
	{
		return new DrawingModel(this.paths.add(path));
	}

}
