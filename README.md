# JStravaWrapperRetrofit

Exemple of usage of Strava api with Rest.

Based on POJOs from https://github.com/dustedrob/JStrava.
How ever changed all requests using Retrofit.

For now you can get : 
- Athlete
- Upload an activity 

Don't hesitate to complete with others methods.

Clement.

--------------------------

### Current state

Added a webView using OAuth2 to log on and a listener for the callback.
It is also using JStrava objects from https://github.com/dustedrob/JStrava but using retrofit.

### Usage

Example in StravaActivity.java : 

#### 1. Login popup to get accessToken (does not expires)

```
StravaAuthentDialog stravaAuthent = new StravaAuthentDialog(StravaActivity.this);
stravaAuthent.initWebView(this, API_KEY, SECRET_KEY, REDIRECT_URI, KEY_CSRF);
stravaAuthent.show();
```

#### 2. After getting token, create retrofit client

```
JStravaV3Retrofit stravaRetrofit = new JStravaV3Retrofit(accessToken);
```

#### 3. Get athlete

```
 //Get athlete
stravaRetrofit.getService().getCurrentAthlete().enqueue(new Callback<Athlete>() {
    @Override
    public void onResponse(Call<Athlete> call, Response<Athlete> response) {
        if(response!=null && response.body()!=null) {
            Toast.makeText(getApplicationContext(), "Current Athlete: " + response.body().getFirstname(), Toast.LENGTH_LONG).show();
        }else{
            //Bad result
        }
    }
    @Override
    public void onFailure(Call<Athlete> call, Throwable t) {
        Toast.makeText(getApplicationContext(),"Cannot get athlete: "+t.getMessage(), Toast.LENGTH_LONG).show();
    }
});
```

#### 3. Upload activity with compatible GPX (very important)

```
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
```
