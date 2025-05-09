package com.example.noman.myvoicerecorder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int MINIMAL_REQUEST_CODE = 2123;
    private static final int GENERAL_REQUEST_CODE = 1231;
    private ProgressDialog pDialog;
    public static int iscolor = 0;

    ViewGroup transitionsContainer ;

    DBHealper dbHealper = null;
    CustomAdapter myCustomAdapter = null;
    ArrayList<emailData> emailDatalist = null;
    Context mContext;
    LinearLayout txt_nocontact;
    ImageButton btn_settings, btn_play, btn_delete, btn_Record;

    private static final String LOG_TAG = "AudioRecord";
//    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    public String mFileName = null;

    private MediaRecorder mRecorder = null;

    private MediaPlayer   mPlayer = null;

    // Requesting permission to RECORD_AUDIO
//    private boolean permissionToRecordAccepted = false;
    private ListView listView;
    private Chronometer myChronometer;
    private RelativeLayout layout;
    public static int CURRENT_COUNT = 0;
    boolean mStartPlaying = true;
//    private long current_base = 0;
    private TextView txtcount;
    private static String fileName;
    private boolean IS_EMAIL_SENT = false;
    private View top_container;

    public static int cnt;
    public static boolean IS_ORIENTATION_CHANGE = false;
    public static boolean IS_DELETED = true;

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying(btn_play);
        } else {
            stopPlaying();
        }
    }

    public void startPlaying(final View view) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(Helper.mp3File.getPath());
            mPlayer.prepare();
            mPlayer.setLooping(false);
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
                int icon = R.drawable.ic_play_circle_outline_black_24dp;
                ((ImageButton)view).setImageDrawable(mContext.getResources().getDrawable(icon));
                mStartPlaying = !mStartPlaying;
                if(btn_Record != null)
                    btn_Record.setEnabled(true);
            }
        });
    }

    public  void stopPlaying() {
        if(mPlayer != null)
            mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mFileName = getExternalStorage();
        mFileName += "/audiorecord"+CURRENT_COUNT+".wav";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        IS_DELETED = false;
        try {
            mRecorder.prepare();
            mRecorder.start();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "prepare() failed");
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    private String getExternalStorage() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private void stopRecording() {
        try {
            if(mRecorder == null)
                return;
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    protected void stopAndDelete(){
        stopPlaying();
        cnt = 0;
//        if(CURRENT_COUNT < 1) {
//            Toast.makeText(this, R.string.no_recording_found, Toast.LENGTH_SHORT).show();
//            return;
//        }
        String storage = getExternalStorage();
        for (int i = 1; i <= CURRENT_COUNT; i++){
            File file = new File(storage + "/audiorecord"+i+".wav");
            if(file.exists())
                file.delete();
        }
        Helper.mp3File.delete();
        Helper.mp3File = new File(storage + "/"+Helper.default_file_name+".wav");
        CURRENT_COUNT = 0;
        txtcount.setText("00:00");
        int icon = R.drawable.ic_mic_black_24dp;
        layout.setBackgroundResource(R.drawable.circle);
        btn_Record.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), icon));
        if(!IS_EMAIL_SENT)
            Toast.makeText(this, R.string.recording_deleted, Toast.LENGTH_SHORT).show();
        IS_EMAIL_SENT = false;
        IS_DELETED = true;
        mFileName = null;
    }

    private void softReset(){
        int icon = pauseRecording();
        btn_Record.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), icon));

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(!IS_DELETED){
            outState.putString("IS_RECORDING","2");
            outState.putInt("CURRENT_COUNT",CURRENT_COUNT);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState.getString("IS_RECORDING") != null) {
            if (savedInstanceState.getString("IS_RECORDING").equals("2")) {
                CURRENT_COUNT = savedInstanceState.getInt("CURRENT_COUNT",0);
                updateTimer();
                layout.setBackgroundResource(R.drawable.circleorange);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.saving_file));
        pDialog.setCancelable(false);
        DataManager mDataManager = new DataManager(this);
        transitionsContainer = (ViewGroup) findViewById(R.id.transition_container);
        top_container =  findViewById(R.id.top_container);



        Helper.CURRENT_LANGUAGE = mDataManager.getString("LANG");

        if(Helper.CURRENT_LANGUAGE.equalsIgnoreCase("English")){
            setLocale("en");
        }else if(Helper.CURRENT_LANGUAGE.equalsIgnoreCase("Deutsch")){
            setLocale("de");
        }
        if(Helper.CURRENT_LANGUAGE.isEmpty()){
            if(getLocale().equalsIgnoreCase("en") || getLocale().contains("English"))
                Helper.CURRENT_LANGUAGE =  "English";
            else if(getLocale().equalsIgnoreCase("de") || getLocale().contains("Deutsch"))
                Helper.CURRENT_LANGUAGE =  "Deutsch";
        }
        CURRENT_COUNT = 0;
        mContext = this;
        cnt = 0;
        txt_nocontact = findViewById(R.id.txt_contect);
        myChronometer = (Chronometer)findViewById(R.id.chronometer);
        txtcount = findViewById(R.id.txt_count);


        initPrefs();
        String storage = getExternalStorage();
        Helper.mp3File = new File(storage + "/"+Helper.default_file_name+".wav");
        dbHealper = new DBHealper(this);
        if(mDataManager.getString("FirstTime").isEmpty()){
            String email = UserEmailFetcher.getEmail(this);
            if(email != null && !email.isEmpty())
                dbHealper.updatePersonInfo("Me",email,1);
            else
                dbHealper.updatePersonInfo("","",1);
            mDataManager.saveString("FirstTime","No");
        }
        emailDatalist = dbHealper.getData();
        myCustomAdapter = new CustomAdapter(this, R.layout.layoutitems, emailDatalist);
        listView = findViewById(R.id.dat_list);
        listView.setAdapter(myCustomAdapter);
        checkItems();
        btn_settings = findViewById(R.id.btn_settings);
        btn_play = findViewById(R.id.btn_play);
        btn_delete = findViewById(R.id.btn_delete);
        btn_Record = findViewById(R.id.btn_Record);
        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, emailSettings.class);
                startActivity(intent);
            }
        });
        btn_play.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                int icon = 0;
                if (mStartPlaying) {
                    if(CURRENT_COUNT < 1) {
                        Toast.makeText(MainActivity.this, R.string.no_recording_found
                                , Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startPlaying(btn_play);
                    btn_Record.setEnabled(false);
                    icon = R.drawable.ic_pause_circle_outline_black_24dp;
                } else {
                    btn_Record.setEnabled(true);
                    stopPlaying();
                    icon = R.drawable.ic_play_circle_outline_black_24dp;
                }
                mStartPlaying = !mStartPlaying;
                btn_play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), icon));

            }
        });
        btn_delete = findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(CURRENT_COUNT < 1) {
                    Toast.makeText(MainActivity.this, R.string.no_recording_found
                            , Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(mContext);
                }
                builder.setTitle(R.string.delete_audio)
                        .setMessage(R.string.are_you_sure_to_delete)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                int icon = R.drawable.ic_play_circle_outline_black_24dp;
                                btn_play.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), icon));
                                stopAndDelete();                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        layout = (RelativeLayout) findViewById(R.id.relativeLayout);




        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                initPrefs();
                String testname = ((TextView) view.findViewById(R.id.txt_name)).getText().toString();
                String testemail = ((TextView) view.findViewById(R.id.txt_email)).getText().toString();
                Helper.userName = testname;
                Helper.email = testemail;
//                if(Helper.IS_FOR_RECIP_SELECT){
//                    selectItem();
//                    Helper.IS_FOR_RECIP_SELECT = false;
//                    return;
//                }else
                    if(CURRENT_COUNT == 0) {
                    Toast.makeText(MainActivity.this, R.string.record_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(Helper.email.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.addrecipients_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(Helper.mp3File == null) {
                    Toast.makeText(MainActivity.this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                    stopAndDelete();
                    return;
                }


                /*
                *
                *  Scenarios
                    1: subject default
                     */
                if(!Helper.default_subject.isEmpty()) {
                    Helper.subject = Helper.default_subject;
                }

                    /*
                    2: content default
                    */


//                if(!Helper.default_content.isEmpty()){
//
//                }
                    /*
                    3: subject page
                     */
                if(Helper.cb_subject_default){
                    Intent myIntent = new Intent(view.getContext(), SelectSubject.class);
                    if(!Helper.cb_email_default )
                        startActivityForResult(myIntent,MINIMAL_REQUEST_CODE);
                    else
                        startActivityForResult(myIntent,GENERAL_REQUEST_CODE);
                }
                /*
                *
                4: content page
                */
                else if(Helper.cb_content_default) {
                    Intent myIntent = new Intent(view.getContext(), SendEmail.class);
                    if(!Helper.default_subject.isEmpty())
                        Helper.subject = Helper.default_subject;
                    if(!Helper.cb_email_default )
                        startActivityForResult(myIntent,MINIMAL_REQUEST_CODE);
                    else
                        startActivityForResult(myIntent,GENERAL_REQUEST_CODE);
                }
                    /*
                    *
                    6: Our server email
                    * */
                else if(!Helper.cb_email_default) {
                    if(!Helper.default_subject.isEmpty()) {

                        updateFileAndSend();
                    }
                    else{
                        if(Helper.default_subject.isEmpty())
                            Toast.makeText(mContext, getResources().getString(R.string.empty_subject_2), Toast.LENGTH_SHORT).show();
//                        if(Helper.default_content.isEmpty())
//                            Toast.makeText(mContext, getResources().getString(R.string.empty_content), Toast.LENGTH_SHORT).show();
                    }
                }
                    /*
                    *
                    5: default email
                    */
                else {
                    Intent myIntent = new Intent(view.getContext(), SendEmail.class);
                    myIntent.putExtra("BODY", Helper.default_content);
                    startActivityForResult(myIntent, GENERAL_REQUEST_CODE);
                }
            }
        });

    }

    private void initPrefs() {
        if(getSharedPreferences(getPackageName(),MODE_PRIVATE).getBoolean(Helper.PREFS_FIRST_TIME,true)){
            getSharedPreferences(getPackageName(),MODE_PRIVATE).edit().putBoolean(Helper.PREFS_FIRST_TIME,false).apply();
            /*
             * APP IS FIRST TIME RUNNING
             *
             * */
            Helper.default_file_name = getResources().getString(R.string.file_name_default);
            Helper.cb_content_default = true;
            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(Helper.PREFS_CB_CONTENT, true).apply();
            Helper.cb_subject_default = true;
            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(Helper.PREFS_CB_SUBJECT, true).apply();
            Helper.cb_email_default = true;
            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(Helper.PREFS_DEFAULT_EMAIL, true).apply();
            getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putString(Helper.PREFS_CB_FILE_NAME, Helper.default_file_name).apply();
            Helper.default_subject = getResources().getString(R.string.default_subject);
            Helper.default_content = getResources().getString(R.string.default_content);
        }else{
            Helper.default_subject = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Helper.PREFS_DEFAULT_SUBJECT, getResources().getString(R.string.default_subject));
            Helper.default_content = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Helper.PREFS_DEFAULT_CONTENT, getResources().getString(R.string.default_content));
            Helper.default_file_name = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Helper.PREFS_CB_FILE_NAME, "");
            Helper.cb_content_default = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(Helper.PREFS_CB_CONTENT, false);
            Helper.cb_subject_default = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(Helper.PREFS_CB_SUBJECT, false);
            Helper.cb_email_default = getSharedPreferences(getPackageName(), MODE_PRIVATE).getBoolean(Helper.PREFS_DEFAULT_EMAIL, false);
        }
    }


    private String[] getAllFilesPaths() {

        String storage = getExternalStorage();
        String[] files = new String[CURRENT_COUNT];
        for (int i = 1; i <= CURRENT_COUNT; i++){
            File file = new File(storage + "/audiorecord"+i+".wav");
            files[i-1] = file.getPath();
        }

        return files;
    }
    public void selectItem() {
        if(getCallingActivity() != null) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", "OK");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }
    private void checkItems(){
        boolean recipientsTest = false;
        for (emailData obj : emailDatalist){
            if(obj.getEmailAddress() == null){
            }else if(obj.getEmailAddress().isEmpty()) {
            }
            else{
                recipientsTest = true;
                break;
            }
        }


//        if (recipientsTest){
//            txt_nocontact.setVisibility(View.GONE);
//            listView.setVisibility(View.VISIBLE);
//        }else{
//            txt_nocontact.setVisibility(View.VISIBLE);
//            listView.setVisibility(View.GONE);
//        }
    }


    public void updateListview(){
        dbHealper = new DBHealper(MainActivity.this);
        emailDatalist = dbHealper.getData();

        myCustomAdapter= new CustomAdapter(MainActivity.this,R.layout.layoutitems,emailDatalist);
        listView = (ListView) findViewById(R.id.dat_list);

        listView.setAdapter(myCustomAdapter);


    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateListview();
        checkItems();
//        if(Helper.RESET){
//            stopAndDelete();
//            Helper.RESET = false;
//        }else{
//
//        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(t != null && Helper.mp3File != null && mFileName != null && !IS_ORIENTATION_CHANGE)
            softReset();
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
    public void setLocale(final String lang) {
        Locale myLocale;
        myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public String getLocale() {

        return getResources().getConfiguration().locale.getDisplayName();


    }
    static CountDownTimerWithPause t;

    private void initTimer(){

        t = new CountDownTimerWithPause( Long.MAX_VALUE , 1000,true) {
            @Override
            public void onTick(long millisUntilFinished) {

                cnt++;
                updateTimer();
            }

            @Override
            public void onFinish() {            }
        };
        t.create();
    }

    private void updateTimer() {
//        String time = Integer.valueOf(cnt).toString();
        long millis = cnt;
        int seconds = (int) (millis / 60);
        int minutes = seconds / 60;
        seconds  = seconds % 60;
        millis  = millis % 60;
        txtcount.setText(String.format("%02d:%02d", seconds,millis));
    }

    public void startRecording(View view) {
        int icon = 0;
        iscolor++;
        Helper.IS_RECORDING = true;

        if (iscolor == 1) {
            btn_play.setEnabled(false);
            btn_delete.setEnabled(false);
            btn_settings.setEnabled(false);
            if(CURRENT_COUNT > 1)
                t.resume();
            else
                initTimer();
            myChronometer.start();
            CURRENT_COUNT++;
            startRecording();
            layout.setBackgroundResource(R.drawable.circlered);
            icon = R.drawable.ic_pause_black_24dp;
        } else if (iscolor == 2){
           icon = pauseRecording();
        }



        btn_Record.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), icon));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        IS_ORIENTATION_CHANGE = true;
    }

    private int pauseRecording() {
        t.pause();
        stopRecording();
        btn_play.setEnabled(true);
        btn_delete.setEnabled(true);
        btn_settings.setEnabled(true);
//            myChronometer.setBase(SystemClock.elapsedRealtime());

        layout.setBackgroundResource(R.drawable.circleorange);
        mergeMediaFiles(true, getAllFilesPaths(),Helper.mp3File.getPath());
        iscolor = 0;
        return R.drawable.ic_mic_black_24dp;
    }

    public  void mergeMediaFiles(final boolean isAudio, final String sourceFiles[], final String targetFile) {
        pDialog.show();
        new Thread(){
            @Override
            public void run() {
                try {
                    String mediaKey = isAudio ? "soun" : "vide";
                    List<Movie> listMovies = new ArrayList<>();
                    for (String filename : sourceFiles) {
                        listMovies.add(MovieCreator.build(filename));
                    }
                    List<Track> listTracks = new LinkedList<>();
                    for (Movie movie : listMovies) {
                        for (Track track : movie.getTracks()) {
                            if (track.getHandler().equals(mediaKey)) {
                                listTracks.add(track);
                            }
                        }
                    }
                    Movie outputMovie = new Movie();
                    if (!listTracks.isEmpty()) {
                        outputMovie.addTrack(new AppendTrack(listTracks.toArray(new Track[listTracks.size()])));
                    }
                    Container container = new DefaultMp4Builder().build(outputMovie);
                    FileChannel fileChannel = new RandomAccessFile(String.format(targetFile), "rw").getChannel();
                    container.writeContainer(fileChannel);
                    fileChannel.close();
                }
                catch (IOException e) {
                    Log.e(LOG_TAG, "Error merging media files. exception: "+e.getMessage());
                }finally {
                    pDialog.cancel();
                    this.interrupt();

                }
            }
        }.start();
    }
    private File getSingleFile() {
        String storage = getExternalStorage();
        return new File(storage + "/audiorecord"+CURRENT_COUNT+".wav");

    }


    private void updateFileAndSend() {
        pDialog.setMessage("Sending Email");
        pDialog.show();
        fileName = UUID.randomUUID().toString().concat(".wav");


        UploadNotificationConfig config = new UploadNotificationConfig();
        config.setClearOnActionForAllStatuses(true);
        UploadNotificationStatusConfig statusProgressConfig = new UploadNotificationStatusConfig();
        UploadNotificationStatusConfig statusCompletedConfig = new UploadNotificationStatusConfig();
        UploadNotificationStatusConfig statusErrorConfig = new UploadNotificationStatusConfig();
        UploadNotificationStatusConfig statusCancelledConfig = new UploadNotificationStatusConfig();
        config.setTitleForAllStatuses(getString(R.string.app_name));

        config.getProgress().message = getString(R.string.uploading);
        config.getCompleted().message = getString(R.string.upload_success);
        config.getError().message = getString(R.string.upload_error);
        config.getCancelled().message = getString(R.string.upload_cancelled);

        try {
            new MultipartUploadRequest(this, fileName, "http://talk2memo.de/upload_file.php")
                    .addFileToUpload(Helper.mp3File.getPath(), "file") //Adding file
                    .addParameter("name", fileName) //Adding text parameter to the request
                    .addParameter("language", getLanguageKey())
                    .addParameter("file_name", Helper.default_file_name.concat(".wav"))
                    .setMethod("POST")
                    .setNotificationConfig(config)
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) {

                        }

                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, net.gotev.uploadservice.ServerResponse serverResponse, Exception exception) {
                            pDialog.dismiss();
                            pDialog.setMessage(getString(R.string.saving_file));

                        }

                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, net.gotev.uploadservice.ServerResponse serverResponse) {
                            Log.d("Completed","OK");
                            String res = serverResponse.getBodyAsString();
                            try {
                                fileName = new JSONObject(res).get("message").toString();
                                sendEmailFromServer();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) {
                            pDialog.dismiss();
                            pDialog.setMessage(getString(R.string.saving_file));

                        }
                    })
                    .startUpload(); //Starting the upload
        } catch (FileNotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }




    }


    private String getLanguageKey(){
        String language;
        // if file name is given in the minimimalistic mode

        if(getLocale().equalsIgnoreCase("en")
                || getLocale().equalsIgnoreCase("English")){
            language = "1";
        }else
            language = "0";

        return language;
    }

    private void sendEmailFromServer(){



//        if(Helper.default_content.isEmpty())
//            Helper.default_content = getResources().getString(R.string.email_body);
//        if(Helper.default_subject.isEmpty())
//            Helper.default_subject = getResources().getString(R.string.email_body);
        HashMap<String,String> json = new HashMap<>();
        json.put("to", Helper.email);
        json.put("subject", Helper.subject);
        json.put("body", Helper.default_content.isEmpty() ? " " : Helper.default_content);
        json.put("file_name", Helper.default_file_name.concat(".wav"));
        json.put("folder_name", fileName);
        json.put("language", "-1");
//        if(!Helper.default_file_name.isEmpty()) {
//            json.put("custom_file_name", Helper.default_file_name);
//            json.put("language", "-1");
//        }else{
//            json.put("language", getLanguageKey());
//        }


        ServerPostRequests.SendEmail("http://talk2memo.de/sendemail.php", json, new ServerResponse() {
            @Override
            public void onResponse(String response) {
                try {
                    response = new JSONObject(response).getString("message");
                    if(response.equalsIgnoreCase("Message was Sent")){
                        Toast.makeText(MainActivity.this, R.string.email_sent_successfully, Toast.LENGTH_SHORT).show();
                        IS_EMAIL_SENT = true;
                        stopAndDelete();
//                        Helper.RESET = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                pDialog.dismiss();
                pDialog.setMessage(getString(R.string.saving_file));

            }

            @Override
            public void onError(VolleyError error) {
                pDialog.dismiss();
                pDialog.setMessage(getString(R.string.saving_file));
                Toast.makeText(MainActivity.this, R.string.email_not_sent_successfully, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(MINIMAL_REQUEST_CODE == requestCode)
            if(resultCode == RESULT_OK){
                if(data != null && data.getExtras() != null
                        && data.getExtras().containsKey("RESULT")){
                    if(data.getExtras().getString("RESULT").equalsIgnoreCase("SENT")){
                        // send email
                        updateFileAndSend();
                    }else if(data.getExtras().getString("RESULT").equalsIgnoreCase("DELETE_ALL")){
                        updateListview();
                        checkItems();
                        stopAndDelete();
                    }
                }
        }
        if(GENERAL_REQUEST_CODE == requestCode)
            if(resultCode == RESULT_OK){
                if(data != null && data.getExtras() != null
                        && data.getExtras().containsKey("RESULT")){
                    if(data.getExtras().getString("RESULT").equalsIgnoreCase("SENT")){
                        updateListview();
                        checkItems();
                        stopAndDelete();
                    }else if(data.getExtras().getString("RESULT").equalsIgnoreCase("DELETE_ALL")){
                        updateListview();
                        checkItems();
                        stopAndDelete();
                    }
                }
            }


    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void showRecipients(View view) {
        TransitionManager.beginDelayedTransition(transitionsContainer);
        top_container.setVisibility(View.GONE);
    }

    public void showTopContainer(View view) {
        TransitionManager.beginDelayedTransition(transitionsContainer);
        top_container.setVisibility(View.VISIBLE);
    }
}
