package com.clem.jstravawrapper.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.clem.jstravawrapper.R;
import com.clem.jstravawrapper.network.JStravaV3Retrofit;
import com.clem.jstravawrapper.utils.StravaPreferences;
import com.clem.jstravawrapper.views.IStravaAuthentDialogListener;
import com.clem.jstravawrapper.views.StravaAuthentDialog;

import org.jstrava.entities.activity.UploadStatus;
import org.jstrava.entities.athlete.Athlete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StravaActivity extends AppCompatActivity implements IStravaAuthentDialogListener {

    private static final int REQUEST_WRITE_STORAGE = 777;

    /****
     * FILL THIS WITH YOUR INFORMATION
     *********/
    private static final String API_KEY = "REPLACE_HERE";
    private static final String SECRET_KEY = "REPLACE_HERE";

    //This is any string we want to use. This will be used for avoid CSRF attacks. You can generate one here: http://strongpasswordgenerator.com/
    private static final String KEY_CSRF = "E3ZYKC1T6H2yP4z";

    private static final String REDIRECT_URI = "http://localhost/redirecturi";

    @BindView(R.id.strava_auth)
    ImageView stravaAuth;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.button_getathlete)
    Button buttonGetathlete;
    @BindView(R.id.button_uploadactivity)
    Button buttonUploadactivity;

    //StravaDialog
    private StravaAuthentDialog stravaAuthent;

    //Retrofit
    private JStravaV3Retrofit stravaRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_strava);
        ButterKnife.bind(this);

        checkStravaPermission();

        //Title
        toolbar.setTitle(getResources().getString(R.string.app_name));
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkStravaAuthentState();
    }

    @Override
    public void authenticationSuccess() {
        Toast.makeText(getApplicationContext(), "Authentication success", Toast.LENGTH_LONG).show();
        checkStravaAuthentState();
    }

    @Override
    public void authenticationFailed() {
        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_LONG).show();
    }

    @OnClick({R.id.button_getathlete, R.id.button_uploadactivity, R.id.strava_auth})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_getathlete:

                //Get athlete
                stravaRetrofit.getService().getCurrentAthlete().enqueue(new Callback<Athlete>() {
                    @Override
                    public void onResponse(Call<Athlete> call, Response<Athlete> response) {
                        if(response!=null && response.body()!=null) {
                            Toast.makeText(getApplicationContext(), "Current Athlete: " + response.body().getFirstname(), Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Request result: "+response.message(), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Athlete> call, Throwable t) {
                        Toast.makeText(getApplicationContext(),"Cannot get athlete: "+t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

                break;
            case R.id.button_uploadactivity:

                //Simulate a file to upload
                AssetManager am = getAssets();
                InputStream inputStream = null;
                try {
                    inputStream = am.open("itineraire1.gpx");
                    File file = createFileFromInputStream(inputStream,"itineraire1.gpx");
                    RequestBody fileRequest = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), fileRequest);

                    RequestBody dataType = RequestBody.create(MediaType.parse("multipart/form-data"),"gpx");

                    stravaRetrofit.getService().uploadActivity(body,dataType).enqueue(new Callback<UploadStatus>() {
                        @Override
                        public void onResponse(Call<UploadStatus> call, Response<UploadStatus> response) {
                            if(response!=null && response.body()!=null) {
                                Toast.makeText(getApplicationContext(), "Works", Toast.LENGTH_LONG).show();
                            }else{
                                //Error
                                Toast.makeText(getApplicationContext(), "Error"+response.message(), Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<UploadStatus> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),"Upload status failure: "+t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;

            case R.id.strava_auth:

                stravaAuthent = new StravaAuthentDialog(StravaActivity.this);
                stravaAuthent.initWebView(this, API_KEY, SECRET_KEY, REDIRECT_URI, KEY_CSRF);
                stravaAuthent.show();

                break;
        }
    }

    /**
     * Check Strava authent state
     */
    private void checkStravaAuthentState(){
        String accessToken = StravaPreferences.getStravaAccessToken(StravaActivity.this);
        if(accessToken!=null){
            stravaRetrofit = new JStravaV3Retrofit(accessToken);
        }
        if(stravaRetrofit!=null){
            buttonGetathlete.setEnabled(true);
            buttonUploadactivity.setEnabled(true);
        }
    }



    private File createFileFromInputStream(InputStream inputStream, String fileName) {
        try{
            String path = Environment.getExternalStorageDirectory().getPath()+File.separator+fileName;
            File f = new File(path);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
            Log.d("TAG","Error: "+e.getMessage());
        }

        return null;
    }

    private void checkStravaPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(StravaActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(StravaActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    finish();
                }
                return;
            }
        }
    }
}
