package com.tehkonnos.lighthouse.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.firebase.Timestamp;
import com.tehkonnos.lighthouse.R;
import java.text.SimpleDateFormat;

public class CustomButton extends ConstraintLayout {
    private TextView title,location,username,date;

    public CustomButton(@NonNull Context context) {
        this(context,null);
    }

    public CustomButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.custom_button, this);

        this.title = view.findViewById(R.id.feedTitle);
        this.location = view.findViewById(R.id.feedLocation);
        this.username = view.findViewById(R.id.feedUser);
        this.date = view.findViewById(R.id.feedDate);


    }

    public void setTitle(String text){
        this.title.setText(text);
        invalidate();
        requestLayout();
    }

    public void setLocation(String text){
        this.location.setText(text);
        invalidate();
        requestLayout();
    }

    public void setUsername(String text){
        this.username.setText(text);
        invalidate();
        requestLayout();
    }

    public void setDate(Timestamp date) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy");
        String DateStr = timeStampFormat.format(date.toDate());

        this.date.setText(DateStr);
        invalidate();
        requestLayout();
    }
}
