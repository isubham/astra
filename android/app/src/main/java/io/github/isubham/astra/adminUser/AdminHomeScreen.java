package io.github.isubham.astra.adminUser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.github.isubham.astra.R;
import io.github.isubham.astra.databinding.AdminHomeScreenBinding;
import io.github.isubham.astra.generalUser.CreateGeneralUser;

public class AdminHomeScreen extends AppCompatActivity {

    private AdminHomeScreenBinding adminHomeScreenBinding;
    private ProgressBar progressBar;
    private boolean backPressedToExitOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adminHomeScreenBinding = AdminHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(adminHomeScreenBinding.getRoot());

        findViewByIds();
        toolbarSetup();
        //showProgressBar();
        getBundleData();
        hideProgressBar();

    }

    private void toolbarSetup() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private void getBundleData() {
    }

    private void findViewByIds() {
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_home_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.register_user:
                startActivity(new Intent(this, CreateGeneralUser.class));
                return true;
            case R.id.logout:
                sendStatusForLogout();
                startActivity(new Intent(this, AdminSignIn.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * TODO - to send status flag for user Logout action
     */
    private void sendStatusForLogout() {
    }

    public void scanCode(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(AdminHomeScreen.this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        intentIntegrator.setCameraId(0);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.setPrompt("Scanning through ASTRA");
        intentIntegrator.setBeepEnabled(true);
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();

    }

    public void searchForUser(View view) {
        if (!TextUtils.isEmpty(adminHomeScreenBinding.adminHomeInputId.getText())) {

            Toast.makeText(AdminHomeScreen.this, "" + adminHomeScreenBinding.adminHomeInputId.getText(), Toast.LENGTH_SHORT).show();
            adminHomeScreenBinding.adminHomeInputId.setText(null);
            hideKeyboard();

            startActivity(new Intent(this, AdminVerifyDoc.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                Toast.makeText(this, result.getFormatName() + "\n" + result.getContents(), Toast.LENGTH_SHORT).show();
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedToExitOnce) {
            super.onBackPressed();
        } else {
            this.backPressedToExitOnce = true;
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
