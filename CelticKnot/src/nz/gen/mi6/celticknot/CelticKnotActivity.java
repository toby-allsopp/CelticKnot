package nz.gen.mi6.celticknot;

import android.app.Activity;
import android.os.Bundle;

public class CelticKnotActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final DrawingView view = (DrawingView) findViewById(R.id.drawingView);
		view.setModel(new DrawingModel(4, 2));
	}
}