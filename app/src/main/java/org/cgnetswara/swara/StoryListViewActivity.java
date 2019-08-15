package org.cgnetswara.swara;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StoryListViewActivity extends AppCompatActivity {
    private List<StoryModel> storyList=new ArrayList<>();
    private RecyclerView recyclerView;
    private StoryAdapter storyAdapter;
    public static final String REQUESTTAG = "requesttag";
    RequestQueue requestQueue;
    StringRequest stringRequest;
    EditText searchInput;
    ImageButton searchButton;
    private String phoneNumber="";
    private String option="";
    private int start;
    private int end;
    String url;
    public static final String StoryListPrefs = "StoryListPrefs" ;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list_view);
        start=0;
        end=start+10;
        intentManager();
        //creating adapter object and setting it to recyclerview
        storyAdapter = new StoryAdapter(this,storyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(storyAdapter);
        loadStorys();
    }

    public void intentManager(){
        sp = getSharedPreferences(StoryListPrefs,Context.MODE_PRIVATE);
        try {
            Intent data = getIntent();
            option = data.getStringExtra("option");
            switch (option) {
                case "3":
                    break;
                case "2":
                    SharedPreferences.Editor editor = sp.edit();
                    phoneNumber = data.getStringExtra("phone_number");
                    editor.putString("phone_number", phoneNumber);
                    editor.apply();
                    break;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("option_chosen", option);
            editor.apply();
        }catch (NullPointerException e){
            Log.d("Looks like:", "The intent is missing");
            option = sp.getString("option_chosen", "3");
            phoneNumber = sp.getString("phone_number", "9");
        }
        Map<String, ?> paths = sp.getAll();
        for (Map.Entry<String, ?> row : paths.entrySet()) {
            //Iterating over each sp entry and emailing
            String values = row.getKey() + "," + row.getValue().toString();
            Log.d("values",values);
        }
        layoutManager();
    }

    public void layoutManager(){
        recyclerView = (RecyclerView) findViewById(R.id.rView);
        searchInput = findViewById(R.id.editTextSearchInput);
        searchButton = findViewById(R.id.imageButtonSearch);
        switch (option) {
            case "3":
                searchButton.setVisibility(View.GONE);
                searchInput.setVisibility(View.GONE);
                url = getString(R.string.base_url)+"pblockswara/BULTOO/"+start+"/"+end;
                break;
            case "2":
                searchInput.setText(phoneNumber);
                url = getString(R.string.base_url)+"pblockswara2/"+phoneNumber+"/"+start+"/"+end;
                break;
        }
    }

    public void loadStorys(){
        Log.d("Fetching from URL:",url);
        stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {


                            //converting the string to json array object
                            JSONArray array = new JSONArray(response);

                            //traversing through all the object
                            for (int i = 0; i < array.length(); i++) {

                                //getting product object from json array
                                JSONObject jsonStory = array.getJSONObject(i);
                                StoryModel story=new StoryModel();
                                story.setId(jsonStory.getString("problem_id"));
                                story.setDesc(jsonStory.getString("problem_desc"));
                                story.setText(jsonStory.getString("problem_text"));
                                story.setCount(jsonStory.getString("duration"));
                                story.setDatetime(jsonStory.getString("datetime"));

                                story.setAccessingUser(phoneNumber);

                                storyList.add(story);
                                storyAdapter.notifyDataSetChanged();

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Story fetch volley", error.toString());
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                    }
                });
        stringRequest.setTag(REQUESTTAG);
        stringRequest.setShouldCache(false);
        requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


        //sample storys
        /*
        StoryModel story1=new StoryModel();
        story1.setId("123");
        String desc="\u0917";
        story1.setDesc(desc);
        story1.setText("story text");
        story1.setCount("0/2");
        story1.setDatetime("29th Aug");
        story1.setAccessingUser(username);
        storyList.add(story1);
        StoryModel story2=new StoryModel();
        story2.setId("123");
        story2.setDesc("story alok");
        story2.setText("story text the other thing to see is this now.long type list");
        story2.setCount("1/2");
        story2.setDatetime("1st Sep");
        story2.setAccessingUser(username);
        storyList.add(story2);
        */

        storyAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUESTTAG);
        }
    }


    public void showNewer(View view) {
        if (start-10 >= 0){
            start=start-10;
            end=start+10;
            storyList.clear();
            layoutManager();
            loadStorys();
        }
    }

    public void showOlder(View view) {
        start=start+10;
        end=start+10;
        storyList.clear();
        layoutManager();
        loadStorys();
    }

    public void reload(View view) {
        loadStorys();
    }

    public void search(View view) {
        if(searchInput.length()>0) {
            phoneNumber = searchInput.getText().toString();
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("phone_number", phoneNumber);
            editor.apply();
            storyList.clear();
            layoutManager();
            loadStorys();
        }else{
            searchInput.setError("कृपया सही फ़ोन नंबर दर्ज करें !");
        }
    }
}