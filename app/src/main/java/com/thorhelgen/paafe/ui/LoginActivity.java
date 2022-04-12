package com.thorhelgen.paafe.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.thorhelgen.paafe.R;
import com.thorhelgen.paafe.data.crypto.Coder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.crypto.SecretKey;

public class LoginActivity extends AppCompatActivity {

    private final String WRONG_PASSWORD_MSG = "KeyStore integrity check failed.";

    private File keyStoreFile;
    private String secret;
    KeyStore keyStore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);

        keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            ex.printStackTrace();
        }
        // Setting the monitoring of changes
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start + count > 7) {
                        loginButton.setEnabled(true);
                } else {
                    loginButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        // KeyStore file
        keyStoreFile = new File(getFilesDir().getAbsolutePath(), getString(R.string.ks_file_name));
        // Validate existing access password if KeyStore file created
        // Else save new access password
        if (keyStoreFile.exists()) {
            loginButton.setOnClickListener(v -> {
                validate(passwordEditText.getText().toString());
            });
        } else {
            if (!getFilesDir().exists()) {
                getFilesDir().mkdirs();
            }
            findViewById(R.id.register_prompt).setVisibility(View.VISIBLE);
            loginButton.setOnClickListener(v -> {
                register(passwordEditText.getText().toString());
            });
        }
    }

    private void register(String password) {
        try (OutputStream os = new FileOutputStream(keyStoreFile)) {
            // Getting a new secret key of the coder
            KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(Coder.init());
            KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(password.toCharArray());
            // Necessary for correct work of the KeyStore
            keyStore.load(null, password.toCharArray());
            // Saving the KeyStore file with the new access password
            keyStore.setEntry(getString(R.string.coder_key_alias), keyEntry, entryPassword);
            keyStore.store(os, password.toCharArray());
        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(), "Exception occurred during KeyStore file handling", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return;
        } catch (GeneralSecurityException ex) {
            Toast.makeText(getApplicationContext(), "Exception occurred during operating with the KeyStore", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return;
        }
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    private void validate(String password) {
        try (InputStream is = new FileInputStream(keyStoreFile)) {
            // Loading the KeyStore file with existing access password
            keyStore.load(is, password.toCharArray());
            // Initializing of the coder with its secret key
            SecretKey key = (SecretKey)keyStore.getKey(getString(R.string.coder_key_alias), password.toCharArray());
            Coder.init(key);
        } catch (IOException ex) {
            // If the password is wrong then the KeyStore.load function will throw the exception with the special message
            if (ex.getMessage() == WRONG_PASSWORD_MSG) {
                Toast.makeText(getApplicationContext(), "Password is wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "Exception occurred during KeyStore file reading", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return;
        } catch (GeneralSecurityException ex) {
            Toast.makeText(getApplicationContext(), "Exception occurred during operating with the KeyStore", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            return;
        }
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }
}
