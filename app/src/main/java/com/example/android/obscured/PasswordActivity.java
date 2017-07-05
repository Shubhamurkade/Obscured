package com.example.android.obscured;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Shubham on 04-07-2017.
 */


public class PasswordActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    EditText mainPassword;
    EditText otherPassword;
    String notMatch = "Passwords do not match!";
    String wrongPass = "Wrong Password";
    EditText password;
    Button nextButton;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences(getString(R.string.password), MODE_PRIVATE);

        if(sharedPreferences.getString(getString(R.string.password), null) != null)
        {
            setContentView(R.layout.password_activity);
            password = (EditText)findViewById(R.id.tv_password);
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            nextButton = (Button)findViewById(R.id.bt_next);
        }
        else
        {
            setContentView(R.layout.new_password);
            mainPassword = (EditText)findViewById(R.id.tv_new_password);
            mainPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            otherPassword = (EditText)findViewById(R.id.tv_new_password_again);
            otherPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            saveButton = (Button)findViewById(R.id.bt_save);
        }

    }

    @Override
    protected void onStart()
    {
        super.onStart();


    }

    public void onButtonClicked(View view)
    {
        editor = sharedPreferences.edit();

        if(view.getId() == R.id.bt_save)
        {
            if(mainPassword.getText().toString().equals(otherPassword.getText().toString()))
            {
                editor.putString(getString(R.string.password), mainPassword.getText().toString());

                if(mainPassword.length() < 6)
                    Toast.makeText(this, "The minimum length of password should be 6", Toast.LENGTH_LONG).show();
                else
                {
                    Intent startMainActivity = new Intent(this, MainActivity.class);
                    startActivity(startMainActivity);
                    finish();
                }
            }
            else
            {
                Toast.makeText(this, notMatch, Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            String storedPass = sharedPreferences.getString(getString(R.string.password), null);
            if(password.getText().toString().equals(storedPass))
            {
                Intent startMainActivity = new Intent(this, MainActivity.class);
                startActivity(startMainActivity);
                finish();
            }
            else Toast.makeText(this, wrongPass, Toast.LENGTH_LONG).show();
        }

        editor.apply();
    }

}
