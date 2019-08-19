package org.cgnetswara.swara;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private List<StoryModel> storyList;
    String fileLocation;
    private Context context_adapter;

    public class StoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        final TextView textViewTitle, textViewDatetime;
        final ImageButton imageButtonShare;
        final StoryAdapter storyAdapter;

        public static final String STORY_ID = "story_id";
        public static final String STORY_DESC = "story_desc";
        public static final String STORY_TEXT = "story_text";
        public static final String STORY_COUNT = "count";
        public static final String STORY_DATETIME = "datetime";
        public static final String ACCESSINGUSER = "accessing_user";


        StoryViewHolder(View itemView, StoryAdapter adapter) {
            super(itemView);
            context_adapter=itemView.getContext();
            textViewTitle = (TextView)itemView.findViewById(R.id.piDesc);
            imageButtonShare=itemView.findViewById(R.id.piShare);
            textViewDatetime = (TextView)itemView.findViewById(R.id.piDatetime);
            itemView.setOnClickListener(this);
            this.storyAdapter=adapter;
        }

        @Override
        public void onClick(View view) {
            int position=getLayoutPosition();
            Intent pv = new Intent(context_adapter, StoryViewActivity.class);

            StoryModel story=storyList.get(position);
            pv.putExtra(STORY_ID,story.getId());
            pv.putExtra(STORY_DESC,story.getDesc());
            pv.putExtra(STORY_TEXT,story.getText());
            pv.putExtra(STORY_COUNT,story.getCount());
            pv.putExtra(STORY_DATETIME,story.getDatetime());
            pv.putExtra(ACCESSINGUSER,story.getAccessingUser());

            context_adapter.startActivity(pv);
        }
    }

    StoryAdapter(Context context, List<StoryModel> storyList) {
        this.storyList = storyList;
    }

    @Override
    public StoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_item, parent,false);
        return new StoryViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(StoryViewHolder holder, int position) {
        final StoryModel story = storyList.get(position);
        String desc, text;

        if(story.getDesc().length()>100){
            desc=story.getDesc().substring(0,100)+"...";
        }
        else desc=story.getDesc();
        holder.textViewTitle.setText(desc);

        if(story.getText().length()>30){
            text=story.getText().substring(0,30)+"...";
        }
        else text=story.getText();
        holder.textViewDatetime.setText(story.getDatetime());
        holder.imageButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareStory(story.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public void shareStory(String problem_id){
        Uri shareUri;
        Intent intent = new Intent(Intent.ACTION_SEND);
        fileLocation= Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_DOWNLOADS+"/CGSwaraStory_"+problem_id+".mp3";
        File f= new File(fileLocation);
        if(f.exists()) {
            if (Build.VERSION.SDK_INT >= 24) {
                shareUri = FileProvider.getUriForFile(context_adapter, BuildConfig.APPLICATION_ID + ".provider", f);
            } else {
                shareUri = Uri.fromFile(f);
            }
            Log.d("share: ", "" + shareUri);
            intent.putExtra(Intent.EXTRA_STREAM, shareUri);
            intent.setType("*/*");
            context_adapter.startActivity(Intent.createChooser(intent, "Share"));
        }
        else{
            Toast.makeText(context_adapter, "भेजने से पेहले सन्देश डाउनलोड करें", Toast.LENGTH_LONG).show();
        }
    }
}

