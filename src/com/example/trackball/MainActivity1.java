package com.example.trackball;

import java.util.ArrayList;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity1 extends Activity {

	private ImageButton trackBall;
	private TextView console;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		console = (TextView) findViewById(R.id.Console);
		
		trackBall = (ImageButton) findViewById(R.id.TrackBall);
		trackBall.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, final MotionEvent event) {
				// TODO Auto-generated method stub
				final float centerX = ((View) v.getParent()).getWidth() / 2;
				final float centerY = ((View) v.getParent()).getHeight() / 2;
				MoveStrategy ms = new MoveStrategy(centerX, centerY);
				ArrayList<Move> moves = ms.getMoves(event.getRawX(),
						event.getRawY());
				String text = "";
				for (int i = 0; i < moves.size(); i++){
					
					text += moves.get(i).toString();
					}
				final String text1=text;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						console.setText(text1);
						trackBall.setX(event.getRawX() - trackBall.getWidth()
								/ 2);
						trackBall.setY(event.getRawY() - trackBall.getHeight()
								/ 2);
					}
				});
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
