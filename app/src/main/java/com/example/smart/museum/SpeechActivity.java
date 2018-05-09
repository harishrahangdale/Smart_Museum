package com.example.smart.museum;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;

/**
 * Created by Harish on 14-04-2018.
 */

public class SpeechActivity extends Activity {
    TextToSpeech t1;
    TextView name;
    TextView id;
    TextView information;
    ImageView image;
    Button b1;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texttospeech);
        Bundle bundle = getIntent().getExtras();
        final String data = bundle.getString("key008");

        final GPSTracker gpsTracker = new GPSTracker(this);

        name = (TextView) findViewById(R.id.name);
        id = (TextView) findViewById(R.id.id);
        id.setText(data);
        final String  xyz = id.getText().toString().trim();
        information = (TextView) findViewById(R.id.info);
        image = (ImageView) findViewById(R.id.imageView1);
        b1 = (Button) findViewById(R.id.button);

        mDatabase = FirebaseDatabase.getInstance().getReference();


            mDatabase.child("monuments").child(xyz).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (gpsTracker.getIsGPSTrackingEnabled())
                    {
                        String lat1 = String.valueOf(gpsTracker.latitude);
                        float f11at = Float.parseFloat(lat1);
                        String lng1 = String.valueOf(gpsTracker.longitude);
                        float f1lng = Float.parseFloat(lng1);
                    }
                    else
                    {
                        // can't get location
                        // GPS or Network is not enabled
                        // Ask user to enable GPS/network in settings
                        gpsTracker.showSettingsAlert();
                    }

                    String mlat = dataSnapshot.child("latitude").getValue(String.class);
                    float f21at = Float.parseFloat(mlat);
                    String mlong = dataSnapshot.child("longitude").getValue(String.class);
                    float f21ng = Float.parseFloat(mlong);

                    if(distFrom( Float.parseFloat(String.valueOf(gpsTracker.latitude)), Float.parseFloat(String.valueOf(gpsTracker.longitude)), f21at, f21ng)>50){
                        name.setText("Oops...\nData Currently Unavailable!");
                        information.setText("Try Scanning QR Code At Its Original Location.");
                        image.setImageResource(R.drawable.sad);
                    }

                    else {
                        String mname = dataSnapshot.child("name").getValue(String.class);

                        Log.d("Name", mname);
                        //Toast.makeText(getApplicationContext(), mname, Toast.LENGTH_SHORT).show();
                        name.setText(mname);

                        String minfo = dataSnapshot.child("information").getValue(String.class);
                        //Log.d("Info", minfo);
                        information.setText(minfo);

                        String mimage = dataSnapshot.child("image1").getValue(String.class);
                        Glide.with(SpeechActivity.this).load(mimage).diskCacheStrategy(NONE).into(image);
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });


        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = (String) information.getText();
                //Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }


    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }


    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
}