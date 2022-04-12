package com.thorhelgen.paafe.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.thorhelgen.paafe.R;

public class ListFragment extends Fragment {

    @SuppressLint("StaticFieldLeak")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        final RecyclerView list = (RecyclerView) view;
        assert list != null;
        list.setAdapter(new ListAdapter(view.getContext()));

        return view;
    }
}
