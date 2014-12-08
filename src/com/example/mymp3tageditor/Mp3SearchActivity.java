package com.example.mymp3tageditor;



import java.io.File;
import java.io.IOException;
import java.io.OutputStream;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.mymp3tageditor.R;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
import helper.CrashLog;
import helper.Mp3Utility;


import static android.content.Intent.ACTION_SEND;
import static android.content.Intent.EXTRA_EMAIL;
import static android.content.Intent.EXTRA_STREAM;
import static android.content.Intent.EXTRA_SUBJECT;
import static android.content.Intent.EXTRA_TEXT;

public class Mp3SearchActivity extends Activity {

    private  Mp3EditorApplication app;
    public static String[] mFilenameList;
    public static File[] mFileListfiles;
    private MediaPlayer mp;
//    public static File defaultRootFile = Environment.getExternalStorageDirectory();
    public static File defaultRootFile = new File("/mnt");
    public static File mPath = defaultRootFile;

	public static final String TAG = Mp3SearchActivity.class.getSimpleName();


	protected void loadFileList() {

        try {
			mPath.mkdirs();
		} catch (SecurityException e) {
			Log.e(TAG, "unable to write on the sd card " + e.toString());
		}
		if (mPath.exists()) {
			mFilenameList = mPath.list(Mp3Utility.getFileNameFilter(true));
			mFileListfiles = mPath.listFiles(Mp3Utility.getFileNameFilter(true));

		} else {
			mFilenameList = new String[0];
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        app = (Mp3EditorApplication) getApplicationContext();
        maybeSendCrashLog(CrashLog.instance(app).previousCrashLog());
        mp = new MediaPlayer();
//		setContentView(R.layout.activity_main);



        /****
         * The other way is this, query MediaProvider for audio files and load them into cursorloader and listen for changes
         *
            mFilenameList = new String[0];
            Cursor cursor = null;
            try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {MediaStore.Audio.Media.DATA},
                                        null, null, null);
                if(cursor!=null && cursor.getCount() >0) {
                    mFilenameList = new String[cursor.getCount()];
                    int index = 0;
                    while(cursor.moveToNext()) {
                        mFilenameList[index++] = cursor.getString(0);
                    }
                }
            } finally {
                if(cursor!=null) cursor.close();
            }
        */
		showDetails(false);
	}

    private static final String ZIPFile = "attach.gz";
    private static final String CRPromptString =
            "Mp3Editor closed unexpectedly. " +
                    "Send error report?";
    private static final String DefaultBody =
            "Please tell us what you were doing when Mp3Editor crashed.";

    private void maybeSendCrashLog(byte[] log)
    {
        if (log == null) return;

        Resources res = getResources();
        String[] addrs = res.getStringArray(R.array.default_support_email);

        if (addrs == null || addrs.length == 0)
            return;

        Intent intent = new Intent(ACTION_SEND);
        intent.setType("application/gzip");
        intent.putExtra(EXTRA_SUBJECT,
                String.format("CrashReport: Mp3Editor %s",
                        app.buildVersion()));
        intent.putExtra(EXTRA_EMAIL, addrs);
        intent.putExtra(EXTRA_TEXT, DefaultBody);

        try {
            OutputStream ous = openFileOutput(ZIPFile, MODE_WORLD_READABLE);
            ous.write(log);
            ous.close();
            intent.putExtra(EXTRA_STREAM,
                    Uri.parse("file://" +
                            getFileStreamPath(ZIPFile)));
            startActivity(Intent.createChooser(intent, CRPromptString));
        } catch (IOException ioe) {
            Log.e(TAG, "Couldn't store zip attachment.", ioe);
        }
    }
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.i(TAG,"Mp3SearchActivity - onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.i(TAG,"Mp3SearchActivity - onResume");
		super.onResume();
	}

	@Override
	protected void onStart() {
		Log.i(TAG,"Mp3SearchActivity - onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.i(TAG,"Mp3SearchActivity - onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG,"Mp3SearchActivity - On Destroy");
		mPath = defaultRootFile;
		super.onDestroy();
	}

	void showDetails(boolean reset) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Log.d(TAG, "showDetails()... Creating ArrayListFragment");

        Fragment currentFrg = getFragmentManager().findFragmentById(android.R.id.content);
        Log.d(TAG, "showDetails()..." + currentFrg);
        if(reset) {
            ft.detach(currentFrg);
            ft.attach(currentFrg).commit();
            return;
        }

        FileListFragment list = new FileListFragment();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.add(android.R.id.content, list).addToBackStack(null).commit();
	
	}

    public void saveInfoClick(View v) {
	    RelativeLayout parentRow = (RelativeLayout) v.getParent();
        final EditText child = (EditText) parentRow.findViewById(R.id.editalbuminfo);
        final String filePath = ((ContentAdapter.ViewHolder)parentRow.getTag()).filePath;
        final EditText newTitle = (EditText) parentRow.findViewById(R.id.titletext);
        
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Mp3Utility.setMP3FileInfo2(filePath, new Mp3Info(child.getText().toString(), newTitle.getText().toString()));
		        Mp3Utility.scanSDCardFile(getApplicationContext(), new String[] {filePath});
		        runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Toast.makeText(Mp3SearchActivity.this, filePath, Toast.LENGTH_SHORT).show();
					}
				});
			}
		}).start();
        
        
    }

    public void playOrStopClick(View v) throws IOException {
        RelativeLayout parentRow = (RelativeLayout) v.getParent();
        String filePath = ((ContentAdapter.ViewHolder)parentRow.getTag()).filePath;
        Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
        int position = ((ContentAdapter.ViewHolder)parentRow.getTag()).position;
        if(position == FileListFragment.PREV_PLAYED_POSITION &&
                FileListFragment.PREV_PLAYED_POSITION_STATE) {
            playMusicFrom(null, false);
            ((ImageView)v).setImageResource(R.drawable.play_icon);
            FileListFragment.PREV_PLAYED_POSITION_STATE = false;
        } else {
            playMusicFrom(filePath, false);
            ((ImageView)v).setImageResource(R.drawable.stop_icon);
            FileListFragment.PREV_PLAYED_POSITION_STATE = true;
        }
        FileListFragment.PREV_PLAYED_POSITION = position;

        FileListFragment frg = (FileListFragment) getFragmentManager().findFragmentById(android.R.id.content);
        if(frg!=null) frg.refresh();
    }

    //This method is Needed because - when pressing back button, Android pops up the fragment
    //from the back stack and when the last fragment is poped out, there remains only a blank
    //activity. So when there is a back key pressed and the backstack count is 1, just finish
    //the activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

            if (keyCode == KeyEvent.KEYCODE_BACK)
            {
//                if (getFragmentManager().getBackStackEntryCount() == 1)
//                    this.finish();
//                else
//                    getFragmentManager().popBackStackImmediate();
//                    //removeCurrentFragment();
//                return true;
                FileListFragment frg = (FileListFragment)getFragmentManager().findFragmentById(android.R.id.content);
                if(frg!=null) {
                    File parent = mPath.getParentFile();
                    if(parent!=null && parent.getAbsolutePath().equalsIgnoreCase("/")) {
                        this.finish();

                    } else if(parent!=null && parent.exists()) {
                        mPath = parent;
                        frg.resetAdapter();

                    }
                    try {
                        playMusicFrom(null, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }

            return super.onKeyDown(keyCode, event);
    }

    public void playMusicFrom(String audioFilePath, boolean hardStop) throws IOException {
        if(hardStop || mp.isPlaying()) {
            mp.stop();
            mp.reset();
        }

        if(!TextUtils.isEmpty(audioFilePath)) {
            mp.setDataSource(audioFilePath);
            mp.prepare();
            mp.start();
        }
    }

}