package com.example.cropmate;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.tvMessage.setText(message.getText());

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvMessage.getLayoutParams();
        if (message.isUser()) {
            params.gravity = Gravity.END;
            holder.tvMessage.setBackgroundResource(R.drawable.status_chip_bg);
            holder.tvMessage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.primary)));
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        } else {
            params.gravity = Gravity.START;
            holder.tvMessage.setBackgroundResource(R.drawable.status_chip_bg);
            holder.tvMessage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.white)));
            holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }
        holder.tvMessage.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
        }
    }
}
