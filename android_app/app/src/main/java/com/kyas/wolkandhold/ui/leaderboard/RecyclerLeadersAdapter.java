package com.kyas.wolkandhold.ui.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kyas.wolkandhold.R;
import com.kyas.wolkandhold.data.database.entities.Route;
import com.kyas.wolkandhold.ui.routesfragment.RecyclerRoutesAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecyclerLeadersAdapter extends RecyclerView.Adapter<RecyclerLeadersAdapter.ViewHolder>{
    private final LayoutInflater inflater;
    private List<LeaderModel> leaders;

    public RecyclerLeadersAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerLeadersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_leader, parent, false);
        return new RecyclerLeadersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerLeadersAdapter.ViewHolder holder, int position) {
        LeaderModel leader = leaders.get(position);
        holder.username.setText(leader.getUsername());
        if (leader.getAvatar() != null) {
            holder.avatar.setImageBitmap(leader.getAvatar());
        }

        holder.totalSquare.setText(String.format(Locale.getDefault(), "%.2f м", leader.getTotalSquare()));

    }

    public void addLeader(LeaderModel leader) {
        leaders.add(leader);
        notifyItemInserted(leaders.size() - 1);
    }

    public void setLeaders(List<LeaderModel> leaders) {
        this.leaders = leaders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return leaders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView username;
        final TextView totalSquare;
        final ImageView avatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.leader_username);
            totalSquare = itemView.findViewById(R.id.leader_total_square);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }

}
