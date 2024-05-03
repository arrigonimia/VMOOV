package com.example.vmoov;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;

public class NotificationActivity extends AppCompatActivity {

    private Button yes_Button;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        textView = findViewById(R.id.textViewData);
        String data = getIntent().getStringExtra("data");
        textView.setText(data);

        yes_Button = findViewById(R.id.yes_ok);

        yes_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}