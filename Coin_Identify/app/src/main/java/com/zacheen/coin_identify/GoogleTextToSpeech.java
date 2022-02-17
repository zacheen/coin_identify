package com.zacheen.coin_identify;

import android.app.Activity;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class GoogleTextToSpeech {
  private static String ENCODING = "UTF-8"; //make constants and assign values to them
  //private static String URL_BEGINNING = "http://translate.google.com/translate_tts?ie=";
  private static String URL_BEGINNING = "http://translate.google.com/translate_tts?client=tw-ob&ie=";
  private static String URL_QUERY = "&q=";
  private static String URL_TL = "&tl=";
  private static String USER_AGENT_LITERAL = "User-Agent";
  private static String USER_AGENT = "Mozilla/4.7";
  private MediaPlayer mediaPlayer = new MediaPlayer();
  private int BUFFER_SIZE = 4096*8;
  private Activity activity;

  public void say(String phrase, String lang , Activity activity) {
      this.activity = activity;
    Log.i("google speaking",phrase);
    try {
      //Make full URL
      phrase=URLEncoder.encode(phrase, ENCODING); //assign value to variable with name 'phrase' by use method encode from class URLEncoder
      String sURL = URL_BEGINNING + ENCODING + URL_TL + lang + URL_QUERY + phrase; //assign value to variable sURL
          URL url = new URL(sURL); // make instance url with constructor

          //Create connection
          URLConnection urlConn = url.openConnection(); //assign value to variable urlConn
          HttpURLConnection httpUrlConn = (HttpURLConnection) urlConn; //Declaring  httpUrlConn var of type HttpURLConnection, assigning it  value to  var urlConn (reduce to  HttpURLConnection)
          httpUrlConn.addRequestProperty(USER_AGENT_LITERAL, USER_AGENT);// use method 

          //Create stream
          try{
        	  InputStream mp3WebStream = urlConn.getInputStream();//create instance and assign it a value
	          //Play stream
//	          Player plr = new Player(mp3WebStream); //create instance plr with constructor
//	          plr.play(); //use method
              playMp3(InputStreamTOByte(mp3WebStream));
          }catch(Exception e){
        	  System.out.println("maybe no internet");
          }
          
    }
      //use exception with name ex
      catch (Exception ex) {
      ex.printStackTrace(); //use method
    }
  }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "mp3", activity.getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // resetting mediaplayer instance to evade problems
            mediaPlayer.reset();

            // In case you run into issues with threading consider new instance like:
            // MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }

    private byte[] InputStreamTOByte(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while((count = in.read(data,0,BUFFER_SIZE)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }
}
