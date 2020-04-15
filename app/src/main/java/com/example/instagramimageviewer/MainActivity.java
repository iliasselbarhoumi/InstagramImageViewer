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


    ProgressDialog progressDialog;
    AlertDialog.Builder alertDialog;
    Button GetImage;
    EditText Username;
    final String urlInsta = "https://www.instagram.com/";
    String url = "https://www.instagram.com/";
    String username;
    String Entry;

    static final Integer WRITE_EXST = 0x3;

    ConnectivityManager conMan;
    NetworkInfo.State Etatmobile;
    NetworkInfo.State Etatwifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Username = findViewById(R.id.username);
        GetImage = findViewById(R.id.getImage);
        GetImage.setOnClickListener(this);


        conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Etatmobile = conMan.getNetworkInfo(0).getState();
        Etatwifi = conMan.getNetworkInfo(1).getState();

        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
        askForPermission(Manifest.permission.ACCESS_WIFI_STATE,WRITE_EXST);

        alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("The application need to be connected to Internet")
                .setMessage("Please check your WiFi/Mobile Data connection");




    }

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

    @Override
    public void onClick(View v) {

        if(v.getId()== R.id.getImage )
        {
            Etatmobile = conMan.getNetworkInfo(0).getState();
            Etatwifi = conMan.getNetworkInfo(1).getState();

            if(!Username.getText().equals(""))
            {

                Entry = Username.getText().toString();


                // Wifi or Mobile data are disabled
                if( (Etatwifi == NetworkInfo.State.CONNECTED || Etatwifi == NetworkInfo.State.CONNECTING) || (Etatmobile == NetworkInfo.State.CONNECTED || Etatmobile == NetworkInfo.State.CONNECTING))
                {
                    if(Entry.contains("https://www.instagram.com/"))
                    {
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

                    if(Entry.contains("https://instagram.com/"))
                    {
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

                    else
                    {
                        url+= Entry;
                        username = Entry;
                        new GetInstaImage().execute();
                    }

                }

                // Wifi or Mobile data are disabled
                else{
                    System.out.println("show dialog");
                    alertDialog.show();
                    Etatmobile = conMan.getNetworkInfo(0).getState();
                    Etatwifi = conMan.getNetworkInfo(1).getState();
                }


            }
        }
    }


    private class GetInstaImage extends AsyncTask<Void, Void, Void> {

        String before, imageurl;
        String imageText;

        private  Document connect(String url) throws org.jsoup.HttpStatusException{
            Document doc = null;
            try {
                if(Jsoup.connect(url).response().statusCode() == 404) {doc = null;}
                else {doc = Jsoup.connect(url).get();}
            } catch (NullPointerException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                doc = null;

            } catch (org.jsoup.HttpStatusException e) {
                e.printStackTrace();
                doc = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                doc = null;

            }
            catch(Exception e){e.printStackTrace();
                doc = null;}

            return doc;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Searching for Account");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {



                Document document = connect(url);
                System.out.println("Get document with post method : "+document);

                if(document == null)
                {
                    System.out.println("Sorry! Account not found");
                    imageurl = "";
                    url = urlInsta;
                }

                else
                    {
                        String result = document.toString();

                        System.out.println("result : "+result);

                        int indexS = result.indexOf("profile_pic_url_hd");
                        int indexE = result.indexOf("requested_by_viewer");

                        imageText = result.substring(indexS,indexE);
                        imageText = imageText.replace("\\u0026", "&");


                        String[] resultSplit = imageText.split(":");
                        String s = resultSplit[1]+":"+resultSplit[2];
                        before = s.substring(1,s.length()-3);

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

            if(imageurl.equals(""))
            {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Sorry, Account not found!", Toast.LENGTH_SHORT).show();
            }
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

