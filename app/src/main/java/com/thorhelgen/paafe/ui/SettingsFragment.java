package com.thorhelgen.paafe.ui;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.thorhelgen.paafe.R;

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

public class SettingsFragment extends Fragment {


    private final String WRONG_PASSWORD_MSG = "KeyStore integrity check failed.";
    private final String OLD_INVALID = "Old password is invalid";
    private final String DONT_MATCH = "New passwords do not match";

    private File keyStoreFile;
    KeyStore keyStore;
    private String secret;
    private SharedPreferences prefs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final EditText oldPasswdEdt = view.findViewById(R.id.spOldPwdEdt);
        final EditText newPasswdEdt = view.findViewById(R.id.spNewPwdEdt);
        final EditText retNewPasswdEdt = view.findViewById(R.id.spRetNewPasswdEdt);
        final CheckBox showPasswdsChk = view.findViewById(R.id.spPasswdChk);
        final Button saveBtn = view.findViewById(R.id.spSaveBtn);
        // KeyStore file
        keyStoreFile = new File(getActivity().getFilesDir().getAbsolutePath(), getString(R.string.ks_file_name));

        keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        } catch (KeyStoreException ex) {
            ex.printStackTrace();
        }
        // Setting the monitoring of changes
        TextWatcher edtWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((oldPasswdEdt.getText().toString().length() & newPasswdEdt.getText().toString().length()
                        & retNewPasswdEdt.getText().toString().length()) > 7) {
                    saveBtn.setEnabled(true);
                }
                else {
                    saveBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        oldPasswdEdt.addTextChangedListener(edtWatcher);
        newPasswdEdt.addTextChangedListener(edtWatcher);
        retNewPasswdEdt.addTextChangedListener(edtWatcher);
        // Show or hide text of all passwords of the fragment
        showPasswdsChk.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                TransformationMethod visible = new TransformationMethod() {
                    @Override
                    public CharSequence getTransformation(CharSequence source, View view) {
                        return source;
                    }

                    @Override
                    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

                    }
                };
                oldPasswdEdt.setTransformationMethod(visible);
                newPasswdEdt.setTransformationMethod(visible);
                retNewPasswdEdt.setTransformationMethod(visible);
            }
            else {
                TransformationMethod invisible = new PasswordTransformationMethod();
                oldPasswdEdt.setTransformationMethod(invisible);
                newPasswdEdt.setTransformationMethod(invisible);
                retNewPasswdEdt.setTransformationMethod(invisible);
            }
        });

        saveBtn.setOnClickListener(v -> {
            // Check new passwords for equality
            if (!newPasswdEdt.getText().toString().equals(retNewPasswdEdt.getText().toString())) {
                Toast.makeText(getContext(), DONT_MATCH, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                // Getting the coder's secret key with old access password
                InputStream is = new FileInputStream(keyStoreFile);
                keyStore.load(is, oldPasswdEdt.getText().toString().toCharArray());
                SecretKey key = (SecretKey)keyStore.getKey(getString(R.string.coder_key_alias), oldPasswdEdt.getText().toString().toCharArray());
                is.close();
                // Saving the coder's secret key with new access password
                OutputStream os = new FileOutputStream(keyStoreFile);
                KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(key);
                KeyStore.ProtectionParameter entryPassword = new KeyStore.PasswordProtection(newPasswdEdt.getText().toString().toCharArray());
                keyStore.load(null, newPasswdEdt.getText().toString().toCharArray());
                keyStore.setEntry(getString(R.string.coder_key_alias), keyEntry, entryPassword);
                keyStore.store(os, newPasswdEdt.getText().toString().toCharArray());
                os.flush();
                os.close();
            } catch (IOException ex) {
                // If old password is wrong then the KeyStore.load function will throw the exception with the special message
                if (ex.getMessage() == WRONG_PASSWORD_MSG) {
                    Toast.makeText(getContext(), "Old password is wrong", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getContext(), "Exception occurred during KeyStore file reading", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return;
            } catch (GeneralSecurityException ex) {
                Toast.makeText(getContext(), "Exception occurred during operating with the KeyStore", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return;
            }

            // Switch to the list of records
            Navigation.findNavController(view).navigate(R.id.nav_list);
        });

        return view;
    }
}
