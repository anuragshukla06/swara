package org.cgnetswara.swara;

import android.os.AsyncTask;

public class Email extends AsyncTask<String,Void,Void> {
    @Override
    protected Void doInBackground(String... parts) {
        return null;
    }
    /*

            //Code to retrieve Metadata of audio
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();

            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(path);
                metaRetriever.setDataSource(inputStream.getFD());
                inputStream.close();

                Long durationms = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                audioDuration = durationms / 1000;

            } catch (Exception e) {
                Log.e("Metadata error", e.toString());
                audioDuration = 0;
            }
            */

    //********************************************************************************
    /*code to iterate over sp entries
    Map<String,?> paths = sp.getAll();

        for(Map.Entry<String,?> row : paths.entrySet()){
            Log.d("map values",row.getKey() + ": " + row.getValue().toString());
            //*****************************************
            //Code to remove sp entries
            editor.remove(row.getKey());
            editor.commit();
            //*****************************************
        }
    */
    //********************************************************************************
}
