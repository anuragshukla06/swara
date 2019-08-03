package org.cgnetswara.swara;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Map;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

public class Email extends AsyncTask<Void, Void, Boolean> {

    Mail mail;
    String audioLocation,phoneNumber,photoLocation;
    SharedPreferences sp;
    public static final String RecordingScreenPrefs = "RecordingScreenPrefs" ;

    public Email(Context context){
        sp=context.getSharedPreferences(RecordingScreenPrefs,Context.MODE_PRIVATE);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        SharedPreferences.Editor editor = sp.edit();
        Map<String, ?> paths = sp.getAll();
        for (Map.Entry<String, ?> row : paths.entrySet()) {
            //Iterating over each sp entry and emailing
            String values = row.getKey() + "," + row.getValue().toString();
            audioLocation = values.split(",")[0];
            phoneNumber = values.split(",")[1];
            if(values.split(",").length>2) {
                photoLocation = values.split(",")[2];
            }
            else{
                photoLocation="";
            }

            mail = new Mail("cgnetmail2019@gmail.com", "QWERTYCGTECH123");
            String[] toArr = {"rulebreakerdude@gmail.com"}; // multiple email addresses can be added here
            mail.setTo(toArr);
            mail.setFrom("cgnetmail2019@gmail.com");
            mail.setSubject("Test Mail's Subject: " + phoneNumber);
            mail.setBody("Test Mail's Body");

            //Attachment section
            try {
                mail.addAttachment(audioLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if(!photoLocation.equals("")){
                    mail.addAttachment(photoLocation);
                    Log.d("Location: ",photoLocation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Sending Mail section
            try {
                if (mail.send()) {
                    Log.d("Email.java", "Mail has been sent");
                    editor.remove(audioLocation);
                    editor.apply();
                }
            } catch (AuthenticationFailedException e) {
                e.printStackTrace();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

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
