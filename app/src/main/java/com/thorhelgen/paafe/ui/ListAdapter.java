package com.thorhelgen.paafe.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.thorhelgen.paafe.R;
import com.thorhelgen.paafe.data.crypto.Coder;
import com.thorhelgen.paafe.data.database.DBWorker;
import com.thorhelgen.paafe.data.database.PasswordRecord;
import com.thorhelgen.paafe.data.database.PasswordsDB;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private PasswordsDB database;
    private RecyclerView recycler;

    ListAdapter(Context context) {
        database = DBWorker.getDB(context);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recycler = recyclerView;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ViewHolder holder, int position) {
        // Setting the record description to the list item
        final PasswordRecord[] record = new PasswordRecord[1];
        DBWorker.makeRequest(() -> {
            record[0] = database.recordDAO().getRecordByIndex(position);
        });
        holder.description.setText(Coder.decode(record[0].description));
        // Switch to the record fragment with passing the record ID through arguments
        holder.description.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString(v.getContext().getString(R.string.rec_id_arg), String.valueOf(position));
            Navigation.findNavController(v).navigate(R.id.nav_record, args);
        });
        // Context actions of items of the list
        holder.description.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(v.getContext().getString(R.string.delete)).setOnMenuItemClickListener(item -> {
                // Removing the record list item
                recycler.removeViewAt(position);
                // Removing record from the DB
                return DBWorker.makeRequest(()-> database.recordDAO().delete(record[0].recId));
            });

        });
    }

    @Override
    public int getItemCount() {
        final int[] result = new int[]{0};
        DBWorker.makeRequest(() -> {
            result[0] = database.recordDAO().count();
        });
        return result[0];
    }
    // View of the list item
    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description_item);
        }
    }
}
