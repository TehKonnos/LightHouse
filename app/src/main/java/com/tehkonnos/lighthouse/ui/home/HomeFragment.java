package com.tehkonnos.lighthouse.ui.home;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import com.google.firebase.firestore.GeoPoint;
import com.tehkonnos.lighthouse.MainActivity;
import com.tehkonnos.lighthouse.R;
import com.tehkonnos.lighthouse.ui.firestore.DatabaseData;
import com.tehkonnos.lighthouse.ui.firestore.Feed;
import com.tehkonnos.lighthouse.ui.firestore.SavedMarker;
import com.tehkonnos.lighthouse.ui.firestore.Users;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private View root;
    private List<SavedMarker> myMarker = new ArrayList<>();
    private List<Users> myUser= new ArrayList<>();
    private List<Feed> feedList =new ArrayList<>();
    private final int[] colors ={R.color.illuminating_yellow}; //List with button colors
    private Dialog myDialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        DatabaseData databaseData = MainActivity.getDatabaseData();

        root = inflater.inflate(R.layout.fragment_home, container, false);

        databaseData.getMutableFeed().observe(getViewLifecycleOwner(), new Observer<List<Feed>>() {
            @Override
            public void onChanged(List<Feed> feeds) {
                feedList =feeds;
            }
        });

        databaseData.getMutableMarker().observe(getViewLifecycleOwner(), new Observer<List<SavedMarker>>() {
            @Override
            public void onChanged(List<SavedMarker> savedMarkers) {
                myMarker = savedMarkers;
            }
        });

        databaseData.getMutableUser().observe(getViewLifecycleOwner(), new Observer<List<Users>>() {
            @Override
            public void onChanged(List<Users> users) {
                myUser = users;
                try {
                    createButton();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        myDialog = new Dialog(requireContext());

        return root;
    }

    public void ShowPopup(final int id) throws IOException {
        myDialog.setContentView(R.layout.custompopup);

        TextView location = myDialog.findViewById(R.id.popup1location);
        location.setText(findAddress(feedList.get(id).getGeoPoint()));

        TextView description = myDialog.findViewById(R.id.popup1Desc);
        description.setText(myMarker.get(id).getDescription());

        TextView needs = myDialog.findViewById(R.id.popup1Needs);
        needs.setText(myMarker.get(id).getNeeds());

        TextView comments = myDialog.findViewById(R.id.popup1Comments);
        String comment=myMarker.get(id).getComments();
        if(comment.equals("")) comments.setText("Δεν υπάρχει κάποιο σχόλιο");
        else comments.setText(comment);

        Button btn = myDialog.findViewById(R.id.popup1btnMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putInt("ID",id);
                MainActivity.navController.navigate(R.id.navigation_map,bundle);
                myDialog.dismiss();
            }
        });

        TextView txtclose;
        txtclose =myDialog.findViewById(R.id.txtclose);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        Objects.requireNonNull(myDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }

    private void createButton() throws IOException {
        for(int i=0;i<feedList.size();i++) {

            int clr = feedList.get(i).getCategory() - 1;
            int color = ResourcesCompat.getColor(getResources(),colors[clr], null);
            Resources res = requireContext().getResources();
            Drawable myBorder = Objects.requireNonNull(ResourcesCompat.getDrawable(res, R.drawable.customborder, null)).mutate();
            myBorder.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            CustomButton myButton = new CustomButton(root.getContext());

            myButton.setTitle(myMarker.get(i).getDescription());
            myButton.setDate(feedList.get(i).getDate());
            myButton.setLocation(findAddress(feedList.get(i).getGeoPoint()));
            myButton.setUsername(getUsername(myUser.get(i)));
            myButton.setId(i);
            myButton.setOnClickListener(this);
            myButton.setBackground(myBorder);


            LinearLayout linearlayout = root.findViewById(R.id.scrollChild);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            buttonParams.setMargins(20, 10, 20, 10);
            linearlayout.addView(myButton, buttonParams);
    }

    }

    private String findAddress (GeoPoint geopoint) throws IOException {
        Geocoder geocoder =new Geocoder(getContext());
        List<Address> addressList = geocoder.getFromLocation(geopoint.getLatitude(),geopoint.getLongitude(),1);
        String locality =addressList.get(0).getAddressLine(0);
        if(locality==null) locality="Άγνωστη τοποθεσία";

        return locality;
    }

    private String getUsername(Users user){
        if(user.getShownName() && user.getUsername()!=null) return user.getUsername();
        return "Ανώνυμος";
    }

    @Override
    public void onClick(View view) {
        try {
            ShowPopup(view.getId());
        } catch (IOException e) {
            Log.e("ShowPopUp Error: ", ""+e.getMessage());
        }
    }

}