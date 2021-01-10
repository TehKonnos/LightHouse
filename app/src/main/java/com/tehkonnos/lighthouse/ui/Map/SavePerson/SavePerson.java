package com.tehkonnos.lighthouse.ui.Map.SavePerson;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.tehkonnos.lighthouse.R;
import com.tehkonnos.lighthouse.ui.firestore.Feed;
import com.tehkonnos.lighthouse.ui.firestore.SavedMarker;
import java.util.Date;
import java.util.Objects;

import static com.tehkonnos.lighthouse.MainActivity.db;

public class SavePerson extends AppCompatActivity {
    GeoPoint geopoint;
    Double longitude,latitude;
    EditText description, needs, comments;
    TextView error;
    Button saveButton;
    Timestamp date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_person);

        Bundle b = getIntent().getExtras();
        assert b != null;
        latitude = b.getDouble("latitude");
        longitude = b.getDouble("longitude");
        geopoint = new GeoPoint(latitude,longitude);

        description = findViewById(R.id.editTextTextPersonName);
        needs = findViewById(R.id.editTextTextPersonName2);
        comments = findViewById(R.id.editTextTextPersonName4);
        error = findViewById(R.id.errorSave);

        date = new Timestamp(new Date());

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkData()) sendData();
            }
        });


    }

    private boolean checkData(){
        if(description.getText()==null || description.getText().toString().equals("")){
            error.setText("Δεν μπορείς να αποθηκεύσεις κάποια ανάγκη χωρίς την σχετική περιγραφή.");
            error.setTextColor(Color.RED);
            return false;
        }
        if(needs.getText()==null || needs.getText().toString().equals("")){
            error.setText("Δεν μπορείς να αποθηκεύσεις κάποια ανάγκη χωρίς να γράψεις τις ανάγκες.");
            error.setTextColor(Color.RED);
            return false;
        }
        error.setText("");
        return true;
    }

    private void sendData(){
        try{
            SavedMarker savedMarker = new SavedMarker();
            savedMarker.setDescription(description.getText().toString());
            savedMarker.setNeeds(needs.getText().toString());
            savedMarker.setComments(comments.getText().toString());


            final DocumentReference doc = db.collection("SavedMarkers").document();
            doc.set(savedMarker).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Feed feed = new Feed();
                    feed.setMarker(doc);
                    feed.setUser(db.collection("Users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())));
                    feed.setDate(date);
                    feed.setGeoPoint(geopoint);
                    feed.setCategory(1); //1-Ανάγκη, 2-Προσφορά
                    db.collection("Feed").document().set(feed).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Ευχαριστούμε που βοήθησες έναν άνθρωπο!",Toast.LENGTH_LONG).show(); //Ενημερώνουμε τον χρήστη ότι όλα πήγαν καλά!
                            finish();
                        }
                    });
                }
            });

        }catch(Exception e){
            Toast.makeText(this,"Σφάλμα! Τα στοιχεία δεν αποθηκεύτηκαν. Παρακαλώ ξαναδοκιμάστε.",Toast.LENGTH_LONG).show(); //Ενημερώνουμε τον χρήστη ότι κάτι πήγε λάθος
            Log.e("Save failed: ",""+e);

        }

    }

}