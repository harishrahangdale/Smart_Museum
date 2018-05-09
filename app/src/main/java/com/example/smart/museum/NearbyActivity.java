package com.example.smart.museum;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Harish on 07-05-2018.
 */

public class NearbyActivity extends Activity {
    private DatabaseReference mDatabase;
    TextView information;
    TextView name;
    ImageView image;
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> newlist = new ArrayList<String>();
    ArrayList<String> mylist = new ArrayList<String>();
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);
        mylist = (ArrayList<String>) getIntent().getSerializableExtra("key009");
        information = (TextView) findViewById(R.id.info);
        name = (TextView) findViewById(R.id.name);
        image = (ImageView) findViewById(R.id.imageView1);

        //information.setText(String.valueOf(names));
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("monuments");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        collectnames((Map<String,Object>) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

    }
    private void collectnames(Map<String,Object> monuments) {
        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : monuments.entrySet()){
            //Get user map
            Map singleUser = (Map) entry.getValue();
            //Get latitudes field and append to list
            names.add((String) singleUser.get("name"));
        }

        for (int i=0;i<mylist.size();i++){
            int retval = Float.compare(Float.parseFloat(mylist.get(i)),50);
            if (retval<0) {
                newlist.add(names.get(i) + " at " + String.valueOf(mylist.get(i)) + " meters.\n\n");
                count++;
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String value : newlist) {
            builder.append(value);
        }
        String text = builder.toString();
        if(count==0) {
            name.setText("Oops...");
            information.setText("No places found nearby. Try searching at another location.");
            information.setTextSize(25);
            image.setImageResource(R.drawable.sad);
        }
        else {
            information.setText(text);
        }
    }

}


