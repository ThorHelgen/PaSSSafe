package com.thorhelgen.paafe.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.thorhelgen.paafe.R;
import com.thorhelgen.paafe.data.crypto.Coder;
import com.thorhelgen.paafe.data.database.DBWorker;
import com.thorhelgen.paafe.data.database.PasswordRecord;
import com.thorhelgen.paafe.data.database.PasswordsDB;

public class RecordFragment extends Fragment {

    private final String COPIED = "Copied to clipboard";
    private final String NOT_FILED_MSG = "All fields must be filled!";

    private int brightness = 0;
    private boolean brightness_changed = false;
    private ToggleButton brtBtn;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        final EditText loginEdt = view.findViewById(R.id.rpLoginEdt);
        final EditText passwdEdt = view.findViewById(R.id.rpPasswdEdt);
        final EditText descriptionEdt = view.findViewById(R.id.rpDscEdt);
        final ImageButton copyBtn = view.findViewById(R.id.rpCopypasteBtn);
        final CheckBox showPasswdsBtn = view.findViewById(R.id.rpPasswdChk);
        final Button saveBtn = view.findViewById(R.id.rpSaveBtn);
        brtBtn = view.findViewById(R.id.rpBrtBtn);
        // ID of the record that was open from records list
        final long recId = Long.parseLong(getArguments().getString(getContext().getString(R.string.rec_id_arg)));
        // Loading record from the DB and filling EditTexts
        PasswordsDB database = DBWorker.getDB(getContext());
        final PasswordRecord[] record = new PasswordRecord[1];
        DBWorker.makeRequest(() -> {
            record[0] = database.recordDAO().getRecordByIndex(recId);
        });
        loginEdt.setText(Coder.decode(record[0].log));
        passwdEdt.setText(Coder.decode(record[0].pass));
        descriptionEdt.setText(Coder.decode(record[0].description));

        copyBtn.setImageResource(R.drawable.copy);
        // Copy the password from EditText to clipboard if the button is pressed
        copyBtn.setOnClickListener((v) -> {
            ClipboardManager cb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            cb.setPrimaryClip(ClipData.newPlainText("pass", passwdEdt.getText().toString()));
            Toast.makeText(getContext(), COPIED, Toast.LENGTH_SHORT).show();
        });
        // Show or hide text of the password
        showPasswdsBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwdEdt.setTransformationMethod(new TransformationMethod() {
                    @Override
                    public CharSequence getTransformation(CharSequence source, View view) {
                        return source;
                    }

                    @Override
                    public void onFocusChanged(View view, CharSequence sourceText, boolean focused, int direction, Rect previouslyFocusedRect) {

                    }
                });
            }
            else {
                passwdEdt.setTransformationMethod(new PasswordTransformationMethod());
            }
        });
        saveBtn.setText(getString(R.string.record_save_btn));
        // Update the existing record in the DB and switch to records list
        saveBtn.setOnClickListener(v -> {
            if (loginEdt.getText().toString() == null || passwdEdt.getText().toString() == null || descriptionEdt.getText().toString() == null) {
                Toast.makeText(getContext(), NOT_FILED_MSG, Toast.LENGTH_SHORT).show();
                return;
            }

            record[0].description = Coder.encode(descriptionEdt.getText().toString());
            record[0].log = Coder.encode(loginEdt.getText().toString());
            record[0].pass = Coder.encode(passwdEdt.getText().toString());

            DBWorker.makeRequest(() -> {
                database.recordDAO().update(record[0]);
            });
            Navigation.findNavController(view).navigate(R.id.nav_list);
        });

        brtBtn.setText(null);
        brtBtn.setTextOn(null);
        brtBtn.setTextOff(null);
        brtBtn.setBackgroundResource(R.drawable.toggle_btn);
        brtBtn.setOnCheckedChangeListener((toggleBtn, isChecked) -> {
            // Checking the access to the system settings change
            if (!Settings.System.canWrite(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
                toggleBtn.setChecked(false);
                return;
            }

            ContentResolver resolver = getContext().getContentResolver();
            if (isChecked) {
                // Saving current screen brightness level
                brightness = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0);
                // Setting screen brightness to the lowest level
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, 0);
            }
            else {
                // Setting screen brightness to the saved level
                Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        if (brtBtn.isChecked()) {
            // Setting screen brightness to the saved level
            brtBtn.setChecked(false);
            brightness_changed = true;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        if (brightness_changed) {
            // Setting screen brightness to the lowest level
            brtBtn.setChecked(true);
            brightness_changed = false;
        }
        super.onResume();
    }
}
