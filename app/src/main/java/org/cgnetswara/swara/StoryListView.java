package org.cgnetswara.swara;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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

public class StoryListView extends AppCompatActivity {
    private List<StoryModel> storyList=new ArrayList<>();
    private RecyclerView recyclerView;
    private StoryAdapter storyAdapter;
    public static final String REQUESTTAG = "requesttag";
    RequestQueue requestQueue;
    StringRequest stringRequest;
    private String phoneNumber;
    private int start;
    private int end;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list_view);
        Intent data=getIntent();
        phoneNumber=data.getStringExtra("phone_number");
        recyclerView = (RecyclerView) findViewById(R.id.rView);
        //creating adapter object and setting it to recyclerview
        storyAdapter = new StoryAdapter(this,storyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(storyAdapter);
        start=0;
        end=start+10;
        loadStorys();
    }

    public void loadStorys(){
        String url = getString(R.string.base_url)+"pblock/"+start+"/"+end;

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
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_LONG).show();
                    }
                });
        stringRequest.setTag(REQUESTTAG);
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
            loadStorys();
        }
    }

    public void showOlder(View view) {
        start=start+10;
        end=start+10;
        storyList.clear();
        loadStorys();
    }

    public void reload(View view) {
        loadStorys();
    }

    public void search(View view) {
    }
}