package com.example.mymp3tageditor;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Handler mHandler = new Handler();

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            startActivity(new Intent(MainActivity.this, Mp3SearchActivity.class));
            MainActivity.this.finish();
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.app_title);
//        textView.setText(//#ff5ea2ca
//                Html.fromHtml("<font color='#8A0868'><b>â– â–‚ â–„ â–… â–† â–‡ â–ˆ </b></font>" +
//                              "<font color='#8A0868' size='45'><b>Ð¼Ï3 Ñ‚Î±g Ñ”âˆ‚Î¹Ñ‚ÏƒÑ </b></font>" +
//                              "<font color='#8A0868'><b> â–ˆ â–‡ â–† â–… â–„ â–‚ â–</b></font>"
//                             )
//        );
        textView.setText(
                Html.fromHtml("<font color='#8A0868'><b>ℳρ3 ✞α❡ €ⅾїт◎ґ</b></font>")
                );
        mHandler.postDelayed(mUpdateTimeTask, 1000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
