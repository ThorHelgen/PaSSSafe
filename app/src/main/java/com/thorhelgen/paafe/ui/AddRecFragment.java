package com.thorhelgen.paafe.ui;

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

public class AddRecFragment extends Fragment {

    private final String NO_DATA_MSG = "There is no data in your clipboard";
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
        final ImageButton pasteBtn = view.findViewById(R.id.rpCopypasteBtn);
        final CheckBox showPasswdsBtn = view.findViewById(R.id.rpPasswdChk);
        final Button addBtn = view.findViewById(R.id.rpSaveBtn);
        brtBtn = view.findViewById(R.id.rpBrtBtn);

        pasteBtn.setImageResource(R.drawable.paste);
        // Copy the password from clipboard to EditText if the button is pressed
        pasteBtn.setOnClickListener(v -> {
            ClipboardManager cb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (!cb.hasPrimaryClip()) {
                Toast.makeText(getContext(), NO_DATA_MSG, Toast.LENGTH_SHORT).show();
                return;
            }
            passwdEdt.setText(cb.getPrimaryClip().getItemAt(0).getText());
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
        addBtn.setText(getString(R.string.record_add_btn));
        // Insert the new record to the DB and switch to records list
        addBtn.setOnClickListener(v -> {
            if (loginEdt.getText().toString().equals("") || passwdEdt.getText().toString().equals("") || descriptionEdt.getText().toString().equals("")) {
                Toast.makeText(getContext(), NOT_FILED_MSG, Toast.LENGTH_SHORT).show();
                return;
            }
            PasswordsDB database = DBWorker.getDB(getContext());
            DBWorker.makeRequest(() -> {
                database.recordDAO().insert(new PasswordRecord(Coder.encode(descriptionEdt.getText().toString()),
                                                               Coder.encode(loginEdt.getText().toString()),
                                                               Coder.encode(passwdEdt.getText().toString())));
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
