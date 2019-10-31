package com.example.appdrone.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.example.appdrone.data.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import com.example.appdrone.R;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.io.ByteArrayOutputStream;


public class PruebaActivity extends Activity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "PruebaActivity";

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private String encodedImage;
    private JSONObject jsonObject;
    private JSONObject Response;
    private Uri mImageUri;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private StorageTask mUploadTask;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prueba);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mUploadTask != null && mUploadTask.isInProgress()){
                    Toast.makeText(PruebaActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();

                }else {

                    uploadFile();
                }
            }
        });

        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Log.d(TAG, data.getData().getPath());
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                ByteArrayOutputStream byteImage = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, byteImage);
                byte[] byteII = byteImage.toByteArray();
                encodedImage = Base64.encodeToString(byteII, Base64.DEFAULT);
                new UploadImages().execute();
                Picasso.get().load(mImageUri).into(mImageView);
            }
            catch (IOException e) {
            e.printStackTrace();
            }
        }
    }

    private String getFileExtension (Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void uploadFile(){

        if (mImageUri != null){

            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+
                    getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Handler handler = new Handler();

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(PruebaActivity.this,"Upload succesful", Toast.LENGTH_LONG).show();
                            //Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                    //mStorageRef.getDownloadUrl().toString());

                            String uploadId = mDatabaseRef.push().getKey();
                           // mDatabaseRef.child(uploadId).setValue(upload);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(PruebaActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);

                        }
                    });



        } else {

            Toast.makeText(this,"no file selected", Toast.LENGTH_SHORT).show();
        }


    }


    public void HTTPPost(byte m[], String nUrl) throws MalformedURLException {

        URL url = new URL("http://181.63.18.205:8000/onem2m/DroneListenerIPE/drones/DroneX/captures");
        HttpURLConnection client = null;
        try {
            String imageB64 = Base64.encodeToString(m, Base64.DEFAULT);
            JSONObject json = new JSONObject();

            json.put("id","Parrot-1");
            json.put("image",imageB64);
            json.put("latitude","4.0");
            json.put("longitude","5.0");

            String data = json.toString();

            client = (HttpURLConnection) url.openConnection();
            client.setDoOutput(true);
            client.setDoInput(true);
            client.setRequestMethod("POST");
            // por confirmar
            client.setFixedLengthStreamingMode(data.getBytes().length);
            client.setRequestProperty("Content-Type", "application/vnd.onem2m-res+json");

            OutputStream out = new BufferedOutputStream(client.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            Log.d("Vicky", "Data to php = " + data);
            writer.flush();
            writer.close();
            out.close();
            client.connect();

            InputStream in = new BufferedInputStream(client.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    in, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String result = sb.toString();
            Log.d("Vicky", "Response from php = " + result);
            //Response = new JSONObject(result);
            client.disconnect();
            /*client.setRequestProperty("id","Parrot-1");
            client.setRequestProperty("image",imageB64);
            client.setRequestProperty("latitude","4.0");
            client.setRequestProperty("longitude","5.0");
            client.setDoOutput(true);*/


           /* OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            outputPost.write(m);
            outputPost.flush();
            outputPost.close();


            client.setFixedLengthStreamingMode(outputPost.toString().getBytes().length);
            client.setChunkedStreamingMode(0);*/

        } catch(MalformedURLException error) {

            //Handles an incorrectly entered URL
        }
        catch (JSONException error)
        {

        }
        catch(SocketTimeoutException error) {

            //Handles URL access timeout.
        }
        catch (IOException error) {


            //Handles input and output errors

        } finally {
            if(client != null) // Make sure the connection is not null.
                client.disconnect();
        }


    }

    private class UploadImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Log.d("APP", "encodedImage = " + encodedImage);
                jsonObject = new JSONObject();
                jsonObject.put("id","Parrot-1");
                jsonObject.put("image",encodedImage);
                jsonObject.put("latitude","4.0");
                jsonObject.put("longitude","5.0");
                String data = jsonObject.toString();
                String yourURL = "http://186.81.103.30:8000/onem2m/DroneListenerIPE/drones/DroneX/captures";
                URL url = new URL(yourURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/vnd.onem2m-res+json");
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                Log.d("APP", "Data  = " + data);
                writer.flush();
                writer.close();
                out.close();
                connection.connect();

                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                String result = sb.toString();
                Log.d("Vicky", "Response from php = " + result);
                //Response = new JSONObject(result);
                connection.disconnect();
            } catch (Exception e) {
                Log.d("Vicky", "Error Encountered");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

        }
    }


}
