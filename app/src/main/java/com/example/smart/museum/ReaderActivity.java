package com.example.smart.museum;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

public class ReaderActivity extends AppCompatActivity {
    Button scan_btn;
    Button nearby;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        nearby = (Button) findViewById(R.id.nearby);

        final GPSTracker gpsTracker = new GPSTracker(this);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        if (gpsTracker.getIsGPSTrackingEnabled())
        {
            String stringLatitude = String.valueOf(gpsTracker.latitude);

            String stringLongitude = String.valueOf(gpsTracker.longitude);
        }
        else
        {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gpsTracker.showSettingsAlert();
        }
        scan_btn = (Button) findViewById(R.id.scan_btn);

        final Activity activity = this;
        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

                Query query = reference.child("monuments").orderByChild("id").equalTo(0);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // dataSnapshot is the "issue" node with all children with id 0
                            for (DataSnapshot monuments : dataSnapshot.getChildren()) {

                                // do something with the individual "issues"
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

                //Get datasnapshot at your "users" root node
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("monuments");
                ref.addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                //Get map of users in datasnapshot
                                collectlocation((Map<String,Object>) dataSnapshot.getValue());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //handle databaseError
                            }
                        });

                }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);


        if(result != null){
            if(result.getContents()== null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else {
                String substr=result.getContents().substring(0,4);

                if(substr.length()<4)                {
                    Toast.makeText(this,result.getContents(), Toast.LENGTH_LONG).show();
                }

                else {
                    if (substr.equals("http")) {
                        Intent startIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                        startIntent.putExtra("key007", result.getContents());
                        startActivity(startIntent);
                    } else {
                        Intent startIntent1 = new Intent(getApplicationContext(), SpeechActivity.class);
                        startIntent1.putExtra("key008", result.getContents());
                        startActivity(startIntent1);
                        //Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void collectlocation(Map<String,Object> monuments) {

        final GPSTracker gpsTracker = new GPSTracker(this);

        ArrayList<String> latitudes = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : monuments.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get latitudes field and append to list
            latitudes.add((String) singleUser.get("latitude"));
        }

        ArrayList<String> longitudes = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : monuments.entrySet()){

            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get phone field and append to list
            longitudes.add((String) singleUser.get("longitude"));
        }
        ArrayList<String> mylist = new ArrayList<String>();
        for (int i=0;i<latitudes.size();i++){
            float dist = distFrom( Float.parseFloat(String.valueOf(gpsTracker.latitude)), Float.parseFloat(String.valueOf(gpsTracker.longitude)), Float.parseFloat(latitudes.get(i)),Float.parseFloat(longitudes.get(i)));
            mylist.add(Float.toString(dist));
        }

        //Toast.makeText(this, String.valueOf(mylist), Toast.LENGTH_LONG).show();

        Intent startIntent = new Intent(getApplicationContext(), NearbyActivity.class);
        startIntent.putExtra("key009", mylist);
        startActivity(startIntent);

        //System.out.println(locations.toString());
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
        //int retval = Float.compare(dist,1000);
        //if (retval<0)
        //{
            return round(dist,2);
        //}
        //else
          //  return round((dist/1000),2);
    }

    public static float round(float d, int decimalPlace){
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
