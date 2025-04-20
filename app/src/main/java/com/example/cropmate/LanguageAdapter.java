package com.example.cropmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private List<Language> languageList;
    private Context context;
    private int selectedPosition = -1; // No selection initially

    public LanguageAdapter(Context context, List<Language> languageList) {
        this.context = context;
        this.languageList = languageList;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        Language language = languageList.get(position);
        holder.languageText.setText(language.getName());

        // Change border color when selected
        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.Green)); // Selected color
            holder.cardView.setCardElevation(8f); // Elevate selected card
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.red)); // Default color
            holder.cardView.setCardElevation(4f);
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged(); // Refresh UI
        });
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    public String getSelectedLanguage() {
        if (selectedPosition != -1) {
            return languageList.get(selectedPosition).getName();
        }
        return null;
    }

    static class LanguageViewHolder extends RecyclerView.ViewHolder {
        TextView languageText;
        CardView cardView;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            languageText = itemView.findViewById(R.id.languageText);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
