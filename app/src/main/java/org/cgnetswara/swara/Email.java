package org.cgnetswara.swara;

import android.os.AsyncTask;
import android.util.Log;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class Email extends AsyncTask<String, Void, Boolean> {

    Mail mail;

    @Override
    protected Boolean doInBackground(String... parts) {
        mail = new Mail("cgnetmail2019@gmail.com","QWERTYCGTECH123");
        String[] toArr = {"rulebreakerdude@gmail.com"}; // multiple email addresses can be added here
        mail.setTo(toArr);
        mail.setFrom("cgnetmail2019@gmail.com");
        mail.setSubject("Test Mail's Subject");
        mail.setBody("Test Mail's Body");
        try {
            if (mail.send()) {
                return true;
            }
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
