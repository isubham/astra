package io.gitub.isubham.astra.generalUser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.gitub.isubham.astra.R;

public class GeneralUserHomeScreen extends AppCompatActivity {

    Button existing_user,register_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_user_home_screen);
        existing_user = findViewById(R.id.button_existing_user);
        register_user = findViewById(R.id.button_register_user);

        setContentView(R.layout.general_user_home_screen);

    }
    public void ExistingUser(View view){

    }

    public void RegisterUser(View view){

    }




}
