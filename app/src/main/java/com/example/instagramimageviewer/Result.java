package com.example.instagramimageviewer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Result extends AppCompatActivity implements View.OnClickListener {

    //******* Views
    ImageView Image;
    Bitmap bitmap;
    Button Save;
    Button Back;

    //******* Variables
    String url, username;
    TextView UsernameMessage;

    //******** Code of Writing in external storage permission
    static final Integer WRITE_EXST = 0x3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        //********* Initializing Views
        Image = findViewById(R.id.Image);
        Save = findViewById(R.id.Save);
        Back = findViewById(R.id.Back);
        UsernameMessage = findViewById(R.id.username_message);

        //********* Setting ClickListener Adapter
        Save.setOnClickListener(this);
        Back.setOnClickListener(this);

        //********* get the informations sent from the mainactivity in the intent (url, username)
        Intent i = getIntent();
        url = i.getStringExtra("url");
        username = i.getStringExtra("username");

        //********* Setting textview's text to show the username that we are looking for his profile image
        UsernameMessage.setText(username+"'s image");

        //********* Picasso library get the image and set it in the imageview
        //********* without download it and store it in a bitmap then show it
        Picasso.with(getApplicationContext()).load(url).into(Image);
        //********* Need more informations, google it or see the documentation in their website


    }

    @Override
    public void onClick(View v) {

        if(v.getId()==R.id.Save)
        {
            askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                new storeImage().execute();
            }
        }

        if(v.getId()==R.id.Back)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
    }

    //******************************************************//
    //*********  Asking for permissions function   *********//
    //******************************************************//

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
            else { ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);}
        }

        else {System.out.println("" + permission + " is already granted.");}
    }



    //**************************************************************************************************************************//
    //*********  Private class to store the image in the internal storage after a click on "Save" button   ********************//
    //************************************************************************************************************************//

    private class storeImage extends AsyncTask<Void, Void, Void> {


        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Result.this);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                //***********************************************************************************************************//
                //*********  This code store create a new folder if doesn't exixts named "InstagramImageViewer    **********//
                //*********             store image in that folder, refresh the phone gallery                    **********//
                //*********                       so that the image appear in the gallery                       **********//
                //*******************************************************************************************************//

                InputStream input = new java.net.URL(url).openStream();
                System.out.println("image downloaded");

                bitmap = BitmapFactory.decodeStream(input);

                String path = Environment.getExternalStorageDirectory() + "/DCIM/InstagramImageViewer";
                File folder = new File(path);

                File file = new File(path+"/"+username+".png");

                //********* check is the folder exists, we can store the image
                boolean success = true;
                if (!folder.exists()) {
                    //Toast.makeText(Result.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
                    System.out.println("Directory Does Not Exist, Create It");
                    success = folder.mkdir();
                }

                //********* If doesn't exists, we create the folder, than we store the image
                if (success)
                {
                    System.out.println("Directory Created");
                    FileOutputStream fos = null;
                    try
                    {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    }
                    catch (Exception e) { e.printStackTrace();}
                    finally
                    {
                        try { fos.close();}
                        catch (IOException e) {e.printStackTrace();}
                    }

                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(file));
                    sendBroadcast(intent);
                }

                //********* If something is wrong we do nothing, we print a message in the log
                else { System.out.println("Failed - Error");}
            }
            catch (IOException e) {e.printStackTrace();}

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //********* When everything is okey, the image is stored succesfully, and you can check your phone gallery
            progressDialog.dismiss();
            Toast.makeText(Result.this, "Image downloaded succesfully", Toast.LENGTH_SHORT).show();

        }
    }
}
