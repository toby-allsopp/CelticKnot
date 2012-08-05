package nz.gen.mi6.celticknot;

import android.app.Activity;
import android.os.Bundle;

public class CelticKnotActivity extends Activity {
	private DrawingView drawingView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.drawingView = (DrawingView) findViewById(R.id.drawingView);
		this.drawingView.setModel(new DrawingModel(4, 2));
	}

	@Override
	protected void onPause()
	{
		this.drawingView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		this.drawingView.onResume();
	}
}