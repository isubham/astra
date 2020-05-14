package io.github.isubham.astra.generalUser;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.github.isubham.astra.R;
import io.github.isubham.astra.databinding.GeneralUserSearchUserBinding;
import io.github.isubham.astra.model.GeneralUser;
import io.github.isubham.astra.tools.ApplicationController;
import io.github.isubham.astra.tools.Constants;
import io.github.isubham.astra.tools.CustomDatePickerFragment;
import io.github.isubham.astra.tools.Errors;
import io.github.isubham.astra.tools.LoginPersistance;

public class GeneralUserSearchUser extends AppCompatActivity {
    private String TAG = "GeneralUserSearchUser";
    private Gson gson;
    private GeneralUserSearchUserBinding binding;

    final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = GeneralUserSearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //DatePicker
        binding.generalUserEtDob.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                selectDate(v);
            }
        });
    }

    private void updateLabel(){
        Date maxDate = Calendar.getInstance().getTime();
        Calendar c2 = Calendar.getInstance();
        c2.set(Calendar.YEAR,1920);
        c2.set(Calendar.MONTH,01);
        c2.set(Calendar.DATE,01);
        Date minDate = c2.getTime();
        Log.d("maxDate",maxDate.toString());
        Log.d("maxDate",minDate.toString());
        Log.d("selectedDate",myCalendar.getTime().toString());
        if(myCalendar.getTime().compareTo(maxDate)>= 0)
        {
            binding.generalUserTilDob.setError("Date can't be of future or todays date");

        }
        else if(myCalendar.getTime().compareTo(minDate)<= 0)
        {
            binding.generalUserTilDob.setError("Date can't be after 1900");
        }
        else {
            String myFormat = "MM-dd-yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
            binding.generalUserEtDob.setText(sdf.format(myCalendar.getTime()));
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean validateString(String s) {
        return s.length() > 2;
    }

    public void searchUser(View view) {
        hideKeyboard();
        final String username = binding.generalUserTilName.getEditText().getText().toString().trim();
        final String userdob = binding.generalUserTilDob.getEditText().getText().toString().trim();
        final String userfathername = binding.generalUserTilFatherName.getEditText().getText().toString().trim();

        // string validation for username
        if (!validateString(username)) {
            binding.generalUserTilName.setError("Please enter full name as your ID!");
        } else {
            binding.generalUserTilName.setErrorEnabled(false);
        }

        // string validation for dob
        if (!validateString(userdob)) {
            binding.generalUserTilDob.setError("Please enter DOB as your ID!");
        } else {
            binding.generalUserTilDob.setErrorEnabled(false);
        }

        // string validation for username
        if (!validateString(userfathername)) {
            binding.generalUserTilFatherName.setError("Please enter father name as your ID!");
        } else {
            binding.generalUserTilFatherName.setErrorEnabled(false);
        }

        Toast.makeText(getApplicationContext(), username + ' ' + userdob + ' ' + userfathername, Toast.LENGTH_SHORT).show();

        String url = "https://aastra-stag.herokuapp.com/person/fuzzy/";
        HashMap<String, String> searchDetails = new HashMap<>();
        searchDetails.put("name", username);
        searchDetails.put("dob", userdob);
        searchDetails.put("father_name", userfathername);

        JsonObjectRequest searchUserRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(searchDetails), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                parseResponse(String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Errors.handleVolleyError(error, TAG, GeneralUserSearchUser.this);

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        ApplicationController.getInstance().addToRequestQueue(searchUserRequest);

    }

    private void parseResponse(String response) {
        gson = new Gson();
        GeneralUser generalUser = gson.fromJson(response, GeneralUser.class);

        LoginPersistance.update(generalUser.getProfile_pic(), generalUser.getId_front(), generalUser.getId_back(), this);
        startActivity(new Intent(GeneralUserSearchUser.this, GeneralUserViewQr.class)
                .putExtra(Constants.USER_NAME, generalUser.getUsername()).putExtra(Constants.USER_TYPE, Constants.USER_TYPE_GENERAL));

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void selectDate(View view) {
//        DialogFragment fragment = new CustomDatePickerFragment();
//        fragment.show(getSupportFragmentManager(), "date picker");
        binding.generalUserEtDob.setText("");
        DatePickerDialog.OnDateSetListener mdob = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                updateLabel();
            }
        };

//       else if()
        new DatePickerDialog(GeneralUserSearchUser.this,mdob,myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
  }
}
