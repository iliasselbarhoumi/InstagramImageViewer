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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    ProgressDialog progressDialog;
    Button GetImage;
    EditText Username;

    String url = "https://www.instagram.com/";
    String username;

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
                url+= Username.getText().toString();
                username = Username.getText().toString();
                Toast.makeText(this, ""+url, Toast.LENGTH_SHORT).show();
                new Content().execute();

            }
        }
    }


    private class Content extends AsyncTask<Void, Void, Void> {

        String before, imageurl;
        String imageText;
        Bitmap bitmap;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                //Connect to the website
                Document document = Jsoup.connect(url).post();
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


            } catch (IOException e) {
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

