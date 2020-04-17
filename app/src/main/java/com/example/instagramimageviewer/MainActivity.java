package com.example.instagramimageviewer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //******* Views
    ProgressDialog progressDialog;
    AlertDialog.Builder alertDialog;
    Button GetImage;
    EditText Username;

    //******* Variables
    final String urlInsta = "https://www.instagram.com/";
    String url = "https://www.instagram.com/";
    String username;
    String Entry;

    //******** Code of Writing in external storage permission
    static final Integer WRITE_EXST = 0x3;

    //******** Variables to get Wifi/Mobile data state
    ConnectivityManager conMan;
    NetworkInfo.State Etatmobile;
    NetworkInfo.State Etatwifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //********* Initializing Views
        Username = findViewById(R.id.username);
        GetImage = findViewById(R.id.getImage);

        //********* Setting ClickListener Adapter
        GetImage.setOnClickListener(this);

        //********* Initializing Network Variables
        conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Etatmobile = conMan.getNetworkInfo(0).getState();
        Etatwifi = conMan.getNetworkInfo(1).getState();

        //******  Asking for writing in storage permission
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);

        //***** Initializing AlertDialog to show it if the phone is not connected to internet
        alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("The application need to be connected to Internet")
                .setMessage("Please check your WiFi/Mobile Data connection");

    }





    @Override
    public void onClick(View v) {

        if(v.getId()== R.id.getImage )
        {
            //********* we check in every click if the phone is connected to internet
            Etatmobile = conMan.getNetworkInfo(0).getState();
            Etatwifi = conMan.getNetworkInfo(1).getState();

            //********* if the text entered isn't empty
            if(!Username.getText().equals(""))
            {

                Entry = Username.getText().toString();

                //********* when Wifi or Mobile data are enabled

                if( (Etatwifi == NetworkInfo.State.CONNECTED || Etatwifi == NetworkInfo.State.CONNECTING) || (Etatmobile == NetworkInfo.State.CONNECTED || Etatmobile == NetworkInfo.State.CONNECTING))
                {
                    //********* We test if the text entered is a link contains (https://www.instagram.com/)
                    //********* We got this types of links when the user copy the url from the browser
                    if(Entry.contains("https://www.instagram.com/"))
                    {
                        //********* we try to get the username from the link

                        if(Entry.contains("?igshid"))
                        {
                            String[] resultSplit = Entry.split("\\?");
                            url = resultSplit[0];
                            System.out.println("url : "+url);
                            username = resultSplit[0].substring(26);
                            System.out.println("username : "+username);
                            new GetInstaImage().execute();
                        }

                        else
                        {
                            url = Entry;
                            username = Entry.substring(26);
                            System.out.println(username);
                            new GetInstaImage().execute();
                        }

                    }

                    //********* We test if the text entered is a link contains (https://instagram.com/)
                    //********* We got this types of links when the user copy the url from the Instagram application
                    if(Entry.contains("https://instagram.com/"))
                    {
                        //********* we try to get the username from the link
                        if(Entry.contains("?igshid"))
                        {
                            String[] resultSplit = Entry.split("\\?");
                            url = resultSplit[0];
                            System.out.println("url : "+url);
                            username = resultSplit[0].substring(22);
                            System.out.println("username : "+username);
                            new GetInstaImage().execute();
                        }

                        else
                        {
                            url = Entry;
                            username = Entry.substring(22);
                            System.out.println(username);
                            new GetInstaImage().execute();
                        }

                    }

                    //********* when the user enter the username, this is the fastest and easyest way
                    //********* we compose the url after that we got the username
                    else
                    {
                        url+= Entry;
                        username = Entry;
                        new GetInstaImage().execute();
                    }

                }

                //********* when Wifi or Mobile data are disabled, we show the AlertDialog
                else{
                    alertDialog.show();
                    Etatmobile = conMan.getNetworkInfo(0).getState();
                    Etatwifi = conMan.getNetworkInfo(1).getState();
                }


            }
        }
    }

    //******************************************************//
    //*********  Asking for permissions function   *********//
    //******************************************************//

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            System.out.println("" + permission + " is already granted.");
        }
    }



    //***********************************************************************************//
    //*********  Private class to that get the instagram image in background   *********//
    //*********************************************************************************//

    private class GetInstaImage extends AsyncTask<Void, Void, Void> {

        String before, imageurl;
        String imageText;

        //******************************************************************************//
        //*********  Execute request to get html page of the concerned user   *********//
        //****************************************************************************//
        private  Document connect(String url){

            Document doc = null;
            try {
                if(Jsoup.connect(url).response().statusCode() == 404) {doc = null;}
                else {doc = Jsoup.connect(url).get();}
                }
            catch (NullPointerException e) {e.printStackTrace(); doc = null;}
            catch (org.jsoup.HttpStatusException e) {e.printStackTrace(); doc = null;}
            catch (IOException e) {e.printStackTrace(); doc = null;}
            catch(Exception e){e.printStackTrace(); doc = null;}

            return doc;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //************ Show ProgressDialog when the thread is working in the background
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Searching for Account");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //******* get the response of the request
                Document document = connect(url);
                System.out.println("Get document with post method : "+document);

                //******* is the response is null, that means that the user not found
                if(document == null)
                {
                    System.out.println("Sorry! Account not found");
                    imageurl = "";
                    url = urlInsta;
                }

                //******* is the response isn't null, we analyse the response to get the image url
                else
                    {
                        String result = document.toString();

                        System.out.println("result : "+result);

                        //************* get the index of two keys in a javascript script
                        //************* between this two index, we find the image url
                        int indexS = result.indexOf("profile_pic_url_hd");
                        int indexE = result.indexOf("requested_by_viewer");

                        //************* we get the url
                        imageText = result.substring(indexS,indexE);

                        //************* we replace '\u0026' by its real character '&'
                        imageText = imageText.replace("\\u0026", "&");

                        //************* these instruction is just to make the link clean (delete any additional characters in the limits of the url)
                        //************* You can print in the log to well understand how thinhs work
                        String[] resultSplit = imageText.split(":");
                        String s = resultSplit[1]+":"+resultSplit[2];
                        before = s.substring(1,s.length()-3);

                        //************* Finally, we get the image url
                        imageurl = before;
                        System.out.println("imageurl : "+imageurl);
                    }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //************* if the user isn't found, or the username/url entered is wrong, we show a toast
            if(imageurl.equals(""))
            {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Sorry, Account not found!", Toast.LENGTH_SHORT).show();
            }

            //************* if the user is found, we get the image url and we send it to the next activity to show the image
            else
            {
                progressDialog.dismiss();
                Intent i = new Intent(getApplicationContext(),Result.class);
                i.putExtra("url",imageurl);
                i.putExtra("username",username);
                startActivity(i);

            }

        }
    }
}

