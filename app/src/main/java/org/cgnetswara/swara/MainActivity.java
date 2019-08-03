package org.cgnetswara.swara;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private int MY_PERMISSIONS_REQUESTS = 0;
    SharedPreferences sp;
    SharedPreferences sp2;
    public static final String MyPREFERENCES = "MainActivityPrefs" ;
    public static final String RecordingScreenPrefs = "RecordingScreenPrefs" ;
    EditText phoneNumber;
    Spinner operator;
    Button op1,op2,op3,op4;

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
        if(sp.contains("phone_number")){
            phoneNumber.setText(sp.getString("phone_number","DNE"));
        }
        else{
            switchOptions(false);
            phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
        }
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
                    switchOptions(false);
                    phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
                }
                else{
                    switchOptions(true);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("phone_number",s.toString());
                    editor.apply();
                }
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
    }

    public void option1Recording(View view) {
        Intent goToRecordingScreen=new Intent(this,RecordingScreen.class);
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
        Email asyncTask = new Email(getApplicationContext());
        asyncTask.execute();
    }
}
