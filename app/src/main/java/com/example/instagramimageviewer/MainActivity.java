package com.example.instagramimageviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    ProgressDialog progressDialog;
    Button GetImage;
    EditText Username;

    String url = "https://www.instagram.com/";
    String username;
    String Entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Username = findViewById(R.id.username);
        GetImage = findViewById(R.id.getImage);
        GetImage.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(v.getId()== R.id.getImage )
        {
            if(!Username.getText().equals(""))
            {
                Entry = Username.getText().toString();

                if(Entry.contains("https://www.instagram.com/"))
                {
                    if(Entry.contains("?igshid"))
                    {
                        String[] resultSplit = Entry.split("\\?");
                        url = resultSplit[0];
                        username = resultSplit[0].substring(26,resultSplit[0].length());
                        System.out.println(username);
                        Toast.makeText(this, ""+url, Toast.LENGTH_SHORT).show();
                        new Content().execute();
                    }

                    else
                        {
                            url = Entry;
                            username = Entry.substring(26,Entry.length());
                            System.out.println(username);
                            Toast.makeText(this, ""+url, Toast.LENGTH_SHORT).show();
                            new Content().execute();
                        }

                }
                else
                    {
                        url+= Entry;
                        username = Entry;
                        Toast.makeText(this, ""+url, Toast.LENGTH_SHORT).show();
                        new Content().execute();
                    }


            }
        }
    }


    private class Content extends AsyncTask<Void, Void, Void> {

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

