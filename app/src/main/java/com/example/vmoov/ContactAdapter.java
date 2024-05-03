package com.example.vmoov;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private List<Contacts> contactsList;

    public ContactAdapter(Context context, List<Contacts> contactsList) {
        this.context = context;
        this.contactsList = contactsList;
    }

    public void setContacts(List<Contacts> contactsList) {
        this.contactsList = contactsList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_row, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contacts contact = contactsList.get(position);
        holder.bind(contact);
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView contactNameTextView;
        private TextView relationshipTextView;


        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            contactNameTextView = itemView.findViewById(R.id.contactNameTextView);
            relationshipTextView = itemView.findViewById(R.id.relationshipTextView);
        }

        public void bind(Contacts contact) {
            Log.d("ContactAdapter", "Binding contact: " + contact.getContactName());
            contactNameTextView.setText(contact.getContactName());
            relationshipTextView.setText(contact.getRelationship());
        }
    }
}
