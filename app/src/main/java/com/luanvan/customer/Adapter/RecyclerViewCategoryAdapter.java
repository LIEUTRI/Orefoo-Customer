package com.luanvan.customer.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luanvan.customer.BranchActivity;
import com.luanvan.customer.R;
import com.luanvan.customer.components.Category;

import java.util.List;

public class RecyclerViewCategoryAdapter extends RecyclerView.Adapter<RecyclerViewCategoryAdapter.ViewHolder> {
    private List<Category> list;
    private Activity activity;

    public RecyclerViewCategoryAdapter(Activity activity, List<Category> list) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerViewCategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.category_item, parent, false);
        return new RecyclerViewCategoryAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewCategoryAdapter.ViewHolder holder, int position) {
        final Category category = list.get(position);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.image_not_found);
        Glide.with(activity).load(category.getImageUrl()).apply(options).into(holder.ivCategory);

        holder.tvCategory.setText(category.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, BranchActivity.class);
                intent.putExtra("categoryId", category.getId());
                activity.startActivity(intent);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivCategory;
        public TextView tvCategory;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategory = itemView.findViewById(R.id.ivCategory);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
