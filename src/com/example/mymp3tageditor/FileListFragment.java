package com.example.mymp3tageditor;



import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
import helper.Mp3Utility;


import helper.Mp3Utility;

import java.io.File;
import java.io.IOException;

import com.example.mymp3tageditor.R;


public class FileListFragment extends ListFragment {

    private static final String TAG = FileListFragment.class.getSimpleName();
    private String[] filenames;
    private File[] files;
    public static int PREV_PLAYED_POSITION = -1;
    public static boolean PREV_PLAYED_POSITION_STATE = false;

    private ContentAdapter adapter;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "ArrayListFragment - In onActivityCreated");
        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                final EditText input = new EditText(getActivity());
                Log.i(TAG, view instanceof EditText ? " EditText "
                        : view instanceof TextView ? " TextView " : view.toString());
                String filePath = ((ContentAdapter.ViewHolder) view.getTag()).filePath;
                final File f = new File(filePath);
                final File[] files = f.listFiles(Mp3Utility.getFileNameFilter(false));
                final ProgressDialog[] progress = new ProgressDialog[1];
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Update Album Name for : " + f.getName())
                        .setMessage(files.length == 0 ? "No music files are found in this directory. You may want to check" +
                                " sub-directories." :
                                "Enter album name below and it will be applied to all files under this directory.")
                        .setView(files.length==0? null : input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final Editable value = input.getText();
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void[] objects) {
                                        Log.d(TAG, "doInBackground()");
                                        if(files.length==0) return null;
                                        String[] paths = new String[files.length];
                                        int index = 0;
                                        for (File file : files) {
                                            if (!file.isDirectory()) {
                                                Mp3Utility.setMP3FileInfo2(file.getAbsolutePath(), new Mp3Info(value.toString(), ""));
                                                paths[index++] = file.getAbsolutePath();
                                                Mp3Utility.scanSDCardFile(getActivity(), new String[] {file.getAbsolutePath()});
                                            }
                                        }
                                        //Mp3Utility.scanSDCardFile(getActivity(), paths);
                                        return null;
                                    }

                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        Log.d(TAG, "onPreExecute()");
                                        progress[0] = ProgressDialog.show(getActivity(), "Updating Album...",
                                                "Please wait while your music files are being updated...", true);
                                    }

                                    @Override
                                    protected void onPostExecute(Void v) {
                                        super.onPostExecute(v);
                                        Log.d(TAG, "onPostExecute()");
                                        progress[0].dismiss();
                                    }
                                }.execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Log.d(TAG, "onClick() on cancel");
                            }
                        }).show();
                return true;
            }
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG,"ArrayListFragment - In onCreateView..");
        ((Mp3SearchActivity)getActivity()).loadFileList();
        filenames = Mp3SearchActivity.mFilenameList;
        files = Mp3SearchActivity.mFileListfiles;
        if(filenames==null) {
            View view = inflater.inflate(R.layout.fragment_nodata, container, false);
            view.setBackgroundColor(0xffffffff);
            return view;
        }
        Log.i(TAG,"ArrayListFragment - In onCreateView - files : " + files.length );
        adapter = new ContentAdapter(getActivity(),R.layout.list_item,filenames,files);
        setListAdapter(adapter);
//        View view = inflater.inflate(android.R.layout.list_content, container, false);
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        view.setBackgroundColor(0xffffffff);

        // Create the adView.
//        AdView adView = (AdView) view.findViewById(R.id.adView);
////        adView.setAdSize(AdSize.SMART_BANNER);
////        adView.setAdUnitId("INSERT_YOUR_AD_UNIT_ID_HERE");
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .build();
//        adView.loadAd(adRequest);
//        Log.i(TAG, "Is this test devicce ?" + adRequest.isTestDevice(getActivity()));

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"ArrayListFragment - In onCreate");
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onPause() {
        Log.i(TAG,"ArrayListFragment - In onPause");
        super.onPause();
    }


    @Override
    public void onResume() {
        Log.i(TAG,"ArrayListFragment - In onResume");
        super.onResume();
    }


    @Override
    public void onStart() {
        Log.i(TAG,"ArrayListFragment - In onStart");
        super.onStart();
    }


    @Override
    public void onStop() {
        Log.i(TAG,"ArrayListFragment - In onStop");
        super.onStop();
        FileListFragment.PREV_PLAYED_POSITION = -1;
        FileListFragment.PREV_PLAYED_POSITION_STATE = false;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(TAG,"ArrayListFragment - In onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG,"ArrayListFragment - onDestroyView");
        super.onDestroy();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG,"ArrayListFragment - On Destroy");
        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(TAG, "Item clicked: " + id + " & "+" position :" + position +" -"+l.getItemAtPosition(position));
        File file = files[position];
        Log.i(TAG, "Path of the selected file :" + file.getAbsolutePath());
        if(file.exists() && file.isDirectory()) {
            Mp3SearchActivity.mPath = file;
            resetAdapter();//((Mp3SearchActivity)getActivity()).showDetails(true);
        }else {

            TextView tv =(TextView)v.findViewById(R.id.filename);
            Log.i(TAG, "It's not going in.." + tv.getText());
        }
    }

    public void refresh() {
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }

    public void resetAdapter() {
        Mp3SearchActivity activity = ((Mp3SearchActivity)getActivity());
        try {
            activity.playMusicFrom(null, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        activity.loadFileList();
        filenames = Mp3SearchActivity.mFilenameList;
        files = Mp3SearchActivity.mFileListfiles;
        adapter = null;
        FileListFragment.PREV_PLAYED_POSITION = -1;
        FileListFragment.PREV_PLAYED_POSITION_STATE = false;
        Log.i(TAG,"ArrayListFragment - In onCreateView - files : " + files.length );
        adapter = new ContentAdapter(getActivity(),R.layout.list_item,filenames,files);
        setListAdapter(adapter);
        refresh();
    }


}
