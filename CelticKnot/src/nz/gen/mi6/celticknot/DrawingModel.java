package nz.gen.mi6.celticknot;

import java.util.ArrayList;

import android.graphics.Path;

public class DrawingModel {

	private final ArrayList<Path> paths;

	public DrawingModel()
	{
		this.paths = new ArrayList<Path>();
	}

	public DrawingModel(final ArrayList<Path> paths)
	{
		this.paths = paths;
	}

	public DrawingModel addPath(final Path path)
	{
		final ArrayList<Path> paths = new ArrayList<Path>(this.paths);
		paths.add(path);
		return new DrawingModel(paths);
	}

}
