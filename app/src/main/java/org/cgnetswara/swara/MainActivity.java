package org.cgnetswara.swara;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import net.vidageek.mirror.dsl.Mirror;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private int MY_PERMISSIONS_REQUESTS = 0;
    SharedPreferences sp;
    SharedPreferences sp2;
    public static final String MyPREFERENCES = "MainActivityPrefs" ;
    public static final String RecordingScreenPrefs = "RecordingScreenPrefs" ;
    EditText phoneNumber;
    Spinner operator;
    Button op1,op2,op3,op4;
    IntentFilter mFilter;
    Boolean numberOk=false, operatorOk=false;
    Boolean onCreateFlag=true;

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
    }

    private void givePermissions() {
        final List<String> permissionsList = new ArrayList<String>();
        addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
        addPermission(permissionsList, Manifest.permission.RECORD_AUDIO);
        if (permissionsList.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        MY_PERMISSIONS_REQUESTS);
            }
            return;
        }
    }

    private void checkForOptions(){
        Log.d("number/op",""+numberOk+"/"+operatorOk);
        if(numberOk&&operatorOk){
            switchOptions(true);
        }
        else{
            switchOptions(false);
        }
    }

    private void initialiseUI(){
        phoneNumber=findViewById(R.id.editText);
        operator=findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operator.setAdapter(adapter);
        op1=findViewById(R.id.button);
        op2=findViewById(R.id.button2);
        op3=findViewById(R.id.button3);
        op4=findViewById(R.id.button4);//Phone Number and operator info
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if(sp.contains("phone_number")&&sp.contains("operator")){
            phoneNumber.setText(sp.getString("phone_number","DNE"));
            operator.setSelection(Integer.parseInt(sp.getString("operator","0")));
            numberOk=true;
            operatorOk=true;
        }
        else{
            phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
        }
        checkForOptions();
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()!=10){
                    phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
                    numberOk=false;
                }
                else{
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("phone_number",s.toString());
                    editor.apply();
                    numberOk=true;
                }
                checkForOptions();
            }
        });
        operator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!onCreateFlag) {
                    String[] temp = {"-", "BSNL", "JIO", "AIRTEL", "VODAFONE", "RC", "RG", "AIRCEL", "IDEA"};//Caution! Make sure this array is congruent to R.array.operator_array
                    if (id != 0) {
                        Log.d("option selected", position + ":" + temp[position]);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("operator", "" + position);
                        editor.apply();
                        operatorOk = true;
                    } else {
                        operatorOk = false;
                    }
                    checkForOptions();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
                Log.d("option selected",":");
            }
        });

    }

    public void switchOptions(boolean val){
        op1.setEnabled(val);
        op2.setEnabled(val);
        op3.setEnabled(val);
        op4.setEnabled(val);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //asking for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            givePermissions();
        }
        initialiseUI();
        onCreateFlag=false;

        mFilter = new IntentFilter();
        mFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bultooReceiver, mFilter);

        //Runnable
        final Handler mHandler=new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Email asyncTask = new Email(getApplicationContext());
                asyncTask.execute();
                mHandler.postDelayed(this, 5000);
                //Log.d("Noting instance ","of runnable");
            }
        };
        mHandler.post(runnable);
    }

    public void option1Recording(View view) {
        Intent goToRecordingScreen=new Intent(this, RecordingScreenActivity.class);
        goToRecordingScreen.putExtra("phone_number",phoneNumber.getText().toString());
        startActivity(goToRecordingScreen);
    }

    public void option4ShareApp(View view) {
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;
        Intent intent = new Intent(Intent.ACTION_SEND);
        // MIME of .apk is "application/vnd.android.package-archive".
        // but Bluetooth does not accept this. Let's use "*/*" instead.
        intent.setType("*/*");
        // Append file and send Intent
        File originalApk = new File(filePath);
        //Make new directory in new location
        File tempFile = new File(getExternalCacheDir() + "/ExtractedApk");
        //If directory doesn't exists create new
        if (!tempFile.isDirectory())
            if (!tempFile.mkdirs())
                return;
        //Get application's name and convert to lowercase
        tempFile = new File(tempFile.getPath() + "/" + getString(app.labelRes).replace(" ","").toLowerCase() + ".apk");
        //Copy file to new location
        InputStream in;
        OutputStream out;
        try {
            in = new FileInputStream(originalApk);
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e){
            return;
        }

        byte[] buf = new byte[1024];
        int len;
        try {
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e){
        }

        Uri shareUri;
        if (Build.VERSION.SDK_INT >= 24) {
            shareUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile);
        }
        else {
            shareUri = Uri.fromFile(tempFile);
        }
        intent.setClassName("com.android.bluetooth", "com.android.bluetooth.opp.BluetoothOppLauncherActivity");
        intent.putExtra(Intent.EXTRA_STREAM, shareUri);
        startActivity(intent);

    }


    public void option2ListenSpecific(View view) {
        Intent storyList=new Intent(this, StoryListViewActivity.class);
        storyList.putExtra("phone_number",phoneNumber.getText().toString());
        storyList.putExtra("option","2");
        startActivity(storyList);
    }

    @Override
    protected void onResume(){
        super.onResume();

    }

    public void option3ListenStories(View view) {
        Intent storyList=new Intent(this, StoryListViewActivity.class);
        storyList.putExtra("option","3");
        startActivity(storyList);
    }

    public static final BroadcastReceiver bultooReceiver = new BroadcastReceiver() {

        private boolean mConnected = false;

        private Long timeStartWhenConnected = 0L;

        private String mDeviceAddress = "";

        private String mDeviceName = "";

        private String mFileName = "";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                Log.e("MainActivity.Java", "Device connected");
                mConnected = true;
                timeStartWhenConnected = System.currentTimeMillis()/1000;
                Log.e("MainActivity.Java", "Time when started "+timeStartWhenConnected);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceAddress = device.getAddress(); // MAC address
                mDeviceName = device.getName();
                Log.e("MainActivity.Java", mDeviceAddress + " and " + mDeviceName);

            } else if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                mConnected = false;
                Long clientEpochTime = System.currentTimeMillis();
                Long timeWhenDisconnected = clientEpochTime / 1000;
                Log.e("MainActivity.Java", "Time when disconnected " + timeWhenDisconnected);
                Log.e("MainActivity.Java", "TIME " + (timeWhenDisconnected - timeStartWhenConnected));
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("MainActivity.Java", "Disconnected " + device.getAddress());
                String macAddress = getBtAddressViaReflection();
                Log.e("MainActivity.Java", "My id " + macAddress);


                if (device.getAddress().equals(mDeviceAddress) || (timeWhenDisconnected - timeStartWhenConnected) > 10) {

                    Log.e(mDeviceAddress + " + " + mDeviceName, mFileName);
                    String senderDeviceAddress = "";
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        try {
                            Field mServiceField = bluetoothAdapter.getClass().getDeclaredField("mService");
                            mServiceField.setAccessible(true);

                            Object btManagerService = mServiceField.get(bluetoothAdapter);

                            if (btManagerService != null) {
                                Log.e("MainActivity.Java", "Difficult MAC address access");
                                senderDeviceAddress = (String) btManagerService.getClass().getMethod("getAddress").invoke(btManagerService);

                            } else {
                                Log.e("MainActivity.Java", "Bluetooth manager is null");
                            }
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                            //throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                            //throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            //throw new RuntimeException(e);
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                            //throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("MainActivity.Java", "Easily accessible MAC address");
                        try {
                            senderDeviceAddress = BluetoothAdapter.getDefaultAdapter().getAddress();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("senderBTMAC", senderDeviceAddress);
                        obj.put("receiverBTMAC", mDeviceAddress);
                        obj.put("filename", mFileName);
                        //obj.put("appName", context.getString(R.string.app_name_for_topup_server));
//                        obj.put("clientEpoch", clientEpochTime );

                        for (int i = 0; i < obj.names().length(); i++) {
                            Log.e("MainActivity.Java", "key = " + obj.names().getString(i) + " value = " + obj.get(obj.names().getString(i)));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

//                    SharedPreferences sharedPref = context.getSharedPreferences(
//                            context.getString(R.string.first_file_transfer), Context.MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPref.edit();
//                    editor.putBoolean(context.getString(R.string.first_file_transfer), true);
//                    editor.apply();

                    mDeviceName = "";
                    mDeviceAddress = "";

                }

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(bultooReceiver);
    }
    private static String getBtAddressViaReflection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
        if (bluetoothManagerService == null) {
            Log.w("ma", "couldn't find bluetoothManagerService");
            return null;
        }
        Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
        if (address != null && address instanceof String) {
            Log.w("ma", "using reflection to get the BT MAC address: " + address);
            return (String) address;
        } else {
            return null;
        }
    }

}
