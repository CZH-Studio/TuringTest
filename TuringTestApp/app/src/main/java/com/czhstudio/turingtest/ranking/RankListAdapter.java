package com.czhstudio.turingtest.ranking;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import com.czhstudio.turingtest.R;
import java.util.List;

public class RankListAdapter extends RecyclerView.Adapter<RankListAdapter.RankViewHolder> {
    private final List<Rank> rankList;
    private final Context context;

    public RankListAdapter(Context context, RankList rankList) {
        this.rankList = rankList.getData();
        // 构造函数，传入排行榜数据
        this.rankList.add(0, new Rank(context.getString(R.string.string_item_username),
                context.getString(R.string.string_item_score),
                context.getString(R.string.string_item_rank)));   // 自动添加表头
        this.context = context;
    }

    @NotNull
    @Override
    public RankViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rank, parent, false);
        return new RankViewHolder(this.context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankViewHolder viewHolder, int i) {
        viewHolder.bind(rankList.get(i), i);
    }

    @Override
    public int getItemCount() {
        return rankList.size();
    }

    public static class RankViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvRanking;
        private final TextView tvUsername;
        private final TextView tvScore;
        private final View view;
        private final Context context;

        public RankViewHolder(Context context, @NonNull View itemView) {
            super(itemView);
            view = itemView;
            tvRanking = itemView.findViewById(R.id.item_ranking_rank);
            tvUsername = itemView.findViewById(R.id.item_ranking_username);
            tvScore = itemView.findViewById(R.id.item_ranking_score);
            this.context = context;
        }

        public void bind(Rank rank, int i) {
            Typeface face = Typeface.createFromAsset(context.getAssets(), "fonts/write.ttf");
            tvRanking.setText(rank.getRank());
            tvUsername.setText(rank.getUsername());
            tvScore.setText(rank.getScore());
            tvRanking.setTypeface(face);
            tvUsername.setTypeface(face);
            tvScore.setTypeface(face);
            if (i == 0){
                view.setBackgroundColor(context.getColor(R.color.dark_green));
            } else if (i % 2 == 0) {
                view.setBackgroundColor(context.getColor(R.color.transparent));
            } else {
                view.setBackgroundColor(context.getColor(R.color.light_green));
            }
        }
    }
}
