package org.cgnetswara.swara;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private int MY_PERMISSIONS_REQUESTS = 0;
    private static int linesInFile=0;
    private static int linesInStorySP=0;
    SharedPreferences sp;
    RequestQueue requestQueue;
    StringRequest stringRequest, stringRequest2;
    public static final String REQUESTTAG = "requesttag";
    public static final String REQUESTTAG2 = "requesttag2";
    static SharedPreferences spStoryShare;
    static SharedPreferences spWalletData;
    public static final String MyPREFERENCES = "MainActivityPrefs";
    public static final String StoryShareInfo = "StoryShareInfo1";
    private static final String WalletData = "WalletData";
    EditText phoneNumber;
    Spinner operator;
    Button op1, op2, op3, op4;
    IntentFilter mFilter;
    Boolean numberOk = false, operatorOk = false;
    Boolean onCreateFlag = true;
    public static final String BULTOO_FILE = "org.cgnetswara.swara.BULTOO_FILE";
    String[] opArray = {"-", "AC", "AR", "B", "ID", "JIO", "MT", "M", "DC", "DG", "RC", "RG", "UN", "VC"};//Caution! Make sure this array is congruent to R.array.operator_array
    private String rechargePhoneNumber="",rechargeOperator="",rechargeAmount="";

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

    private void checkForOptions() {
        Log.d("number/op", "" + numberOk + "/" + operatorOk);
        if (numberOk) {
            switchOptions(true);
            handleHiddenFile();
        } else {
            switchOptions(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actions, menu);
        menu.getItem(0).setVisible(false);
        menu.getItem(2).setTitle("₹ "+spWalletData.getString("Cash","0"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String)item.getTitleCondensed();
        if (title!=null && title.equals("रिचार्ज करे")) {
            buildDialogPhoneNumber();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void buildDialogPhoneNumber(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया 10 अंकों का फोन नंबर दर्ज करें");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechargePhoneNumber = input.getText().toString();
                buildDialogOperator();
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogOperator(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया ऑपरेटर का चयन करें");
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        input.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                rechargeOperator=opArray[position];
                Log.d("option selected", rechargeOperator);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d("option selected", ":");
            }
        });
        builder.setView(input);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildDialogAmount();
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogAmount(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("कृपया रिचार्ज राशि दर्ज करें");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                rechargeAmount = input.getText().toString();
                buildDialogConfirm();
        }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void buildDialogConfirm(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("जाँच के बाद पुष्टि करें");
        final TextView info = new TextView(this);
        info.setGravity(Gravity.CENTER);
        info.setText("\nPhone: "+rechargePhoneNumber+"\n\tOperator: "+rechargeOperator+"\n\tAmount: "+rechargeAmount);
        builder.setView(info);
        builder.setPositiveButton("ठीक", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String walletAmount=spWalletData.getString("Cash","0");
                Log.d("Finally: ",rechargePhoneNumber+rechargeOperator+rechargeAmount);
                if(Integer.parseInt(walletAmount)>Integer.parseInt(rechargeAmount)) {
                    sendTopUpRequestToServer(walletAmount, rechargePhoneNumber, rechargeOperator, rechargeAmount);
                }
            }
        });
        builder.setNegativeButton("रद्द", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void sendTopUpRequestToServer(final String wa, final String rpn, final String ro, final String ra){
        String url = getString(R.string.base_url) + "swaraRecharge";
        stringRequest2 = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String newWalletAmount=response;
                        Log.d("Response is: ",response);
                        if(newWalletAmount.equals(wa) || newWalletAmount.equals( Integer.toString((Integer.parseInt(wa)-Integer.parseInt(ra))) )){
                            setToWallet(newWalletAmount);
                        }
                        else{
                            Log.e("Error!!","this should never happen");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Some","Network Error: ",error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("phone_number", rpn);
                params.put("amount", ra);
                params.put("carrier_code", ro);
                params.put("wallet_amount", wa);
                return params;
            }
        };
        stringRequest2.setTag(REQUESTTAG);
        stringRequest2.setShouldCache(false);
        stringRequest2.setRetryPolicy(new DefaultRetryPolicy(20000,0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest2);
    }


    private void initialiseUI() {
        phoneNumber = findViewById(R.id.editText);
        operator = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.operator_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        operator.setAdapter(adapter);
        op1 = findViewById(R.id.button);
        op2 = findViewById(R.id.button2);
        op3 = findViewById(R.id.button3);
        op4 = findViewById(R.id.button4);//Phone Number and operator info

        if (sp.contains("phone_number") && sp.contains("operator")) {
            phoneNumber.setText(sp.getString("phone_number", "DNE"));
            operator.setSelection(Integer.parseInt(sp.getString("operator", "0")));
            numberOk = true;
            operatorOk = true;
        } else {
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
                if (s.length() != 10) {
                    phoneNumber.setError("कृपया 10 अंकों का फोन नंबर दर्ज करें और ऑपरेटर का चयन करें !");
                    numberOk = false;
                } else {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("phone_number", s.toString());
                    editor.apply();
                    numberOk = true;
                }
                checkForOptions();
            }
        });
        operator.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!onCreateFlag) {
                    if (id != 0) {
                        Log.d("option selected", position + ":" + opArray[position]);
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
                Log.d("option selected", ":");
            }
        });

    }

    public void switchOptions(boolean val) {
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
        sp = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        spStoryShare = getSharedPreferences(StoryShareInfo, Context.MODE_PRIVATE);
        spWalletData = getSharedPreferences(WalletData,Context.MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);
        initialiseUI();
        onCreateFlag = false;

        mFilter = new IntentFilter();
        mFilter.addAction(BULTOO_FILE);
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
                handleSync();
            }
        };
        mHandler.post(runnable);
    }

    private static void handleHiddenFile() {
        linesInFile=0;linesInStorySP=0;
        readFromFile();
        Map<String,?> Keys = spStoryShare.getAll();
        for(Map.Entry<String,?> row : Keys.entrySet()){
            linesInStorySP++;
        }
        Log.d("Lines in SP=",""+linesInStorySP);
        if(linesInStorySP>linesInFile){
            saveToFile();
        }
    }

    public void handleSync(){
        String pn="",rbtmac="",fn="",cc="";
        pn=phoneNumber.getText().toString();
        cc=opArray[Integer.parseInt(sp.getString("operator","0"))];
        if (pn.length() == 10) {
            Map<String,?> Keys = spStoryShare.getAll();

            for(Map.Entry<String,?> row : Keys.entrySet()){
                try {
                    rbtmac = row.getKey().split(",")[0];
                    fn = row.getKey().split(",")[1];
                    //Log.d("Separately: ",rbtmac+fn+cc);
                    if (row.getValue().toString().equals("0")) {
                        syncToServer(row.getKey(), pn, rbtmac, fn, cc);
                    }
                }catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void syncToServer(final String key, final String pn, final String rbtmac, final String fn, final String cc) {
        String url = getString(R.string.base_url) + "newswaratoken";
        Log.d("Syncing File:",fn);

        stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("Done!")){
                            Log.d("Synced File:",fn);
                            SharedPreferences.Editor editor=spStoryShare.edit();
                            editor.putString(key,"1");
                            editor.apply();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Synced File:","Failed");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("senderBTMAC", pn);
                params.put("receiverBTMAC", rbtmac);
                params.put("filename", fn);
                params.put("appName", "Surajpur Bultoo Radio");
                params.put("phoneNumber", pn);
                params.put("carrierCode", cc);
                return params;
            }
        };
        stringRequest.setTag(REQUESTTAG);
        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
    }

    public void option1Recording(View view) {
        Intent goToRecordingScreen=new Intent(this, RecordingScreenActivity.class);
        goToRecordingScreen.putExtra("phone_number",phoneNumber.getText().toString());
        startActivity(goToRecordingScreen);
    }

    public void option4ShareApp(View view) {
        Intent sendName = new Intent();
        sendName.setAction(BULTOO_FILE);
        sendName.putExtra("problem_id", "Apk");
        sendName.putExtra("type","apk");
        sendBroadcast(sendName);
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

    public void option3ListenStories(View view) {
        Intent storyList=new Intent(this, StoryListViewActivity.class);
        storyList.putExtra("option","3");
        startActivity(storyList);
    }

    public static final BroadcastReceiver bultooReceiver = new BroadcastReceiver() {
        private Long timeStartWhenConnected = 0L, timeWhenDisconnected=0L;
        private String mDeviceAddress = "";
        private String problemId = "";
        private String type="";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BULTOO_FILE)){
                problemId=intent.getStringExtra("problem_id");
                type=intent.getStringExtra("type");
                Log.d("Type: ",type);
            }
            if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                timeStartWhenConnected = System.currentTimeMillis()/1000;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceAddress = device.getAddress(); // MAC address

            } else if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                timeWhenDisconnected = System.currentTimeMillis() / 1000;
                Log.d("Time: ",""+(timeWhenDisconnected - timeStartWhenConnected));
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("Disconnected: ", "" + device.getAddress());

                if (device.getAddress().equals(mDeviceAddress)  && (timeWhenDisconnected - timeStartWhenConnected) > 10) {
                    String key=mDeviceAddress+","+problemId;
                    Log.d("Key: ",key);
                    switch(spStoryShare.getString(key,"-1")){
                        case "-1":
                            SharedPreferences.Editor editor=spStoryShare.edit();
                            editor.putString(key,"0");
                            editor.apply();
                            Log.d("Case -1","New Unique File Transfer");
                            if(type.equals("bultoo")) {
                                addInWallet();
                            }
                            break;
                        case "0":
                            Log.d("Case 0","Already Shared But not Synced");
                            break;
                        case "1":
                            Log.d("Case 1","Shared and Synced");
                            break;
                    }
                }

            }
        }
    };

    private static void addInWallet() {
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash",(Integer.parseInt(spWalletData.getString("Cash","0"))+2)+"");
        editor.apply();
        if(linesInFile==0 && linesInStorySP==1){
            editor.putString("Cash",(Integer.parseInt(spWalletData.getString("Cash","0"))+8)+"");
            editor.apply();
        }
        handleHiddenFile();
        Log.d("Cash",spWalletData.getString("Cash","Error"));
    }

    private static void encash(int amount){
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash",(Integer.parseInt(spWalletData.getString("Cash","0"))-amount)+"");
        editor.apply();
        Log.d("Cash",spWalletData.getString("Cash","Error"));
    }

    private static void setToWallet(String amount){
        SharedPreferences.Editor editor = spWalletData.edit();
        editor.putString("Cash",amount);
        editor.apply();
        Log.d("Cash",spWalletData.getString("Cash","Error"));
    }

    public static void saveToFile(){
        Map<String,?> map=spStoryShare.getAll();
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath+"/swararecordings");
        folder.mkdirs();
        String path=folder+"/pref123.prf";
        try {
            FileWriter f=new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(f);
            for(Map.Entry<String,?> row : map.entrySet()){
                bw.write(row.getKey()+","+row.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void readFromFile(){
        String exstPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File folder = new File(exstPath+"/swararecordings");
        folder.mkdirs();
        String path=folder+"/pref123.prf";
        try {
            FileReader f=new FileReader(path);
            BufferedReader br = new BufferedReader(f);
            String line;
            SharedPreferences.Editor editor = spStoryShare.edit();
            while((line=br.readLine())!=null) {
                linesInFile++;
                try {
                    editor.putString((line.split(",")[0] + "," + line.split(",")[1]), line.split(",")[2]);
                    editor.apply();
                }catch(ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                Log.d("Key,Value", line);
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        Log.d("NumLine=",""+linesInFile);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(bultooReceiver);
    }
}
