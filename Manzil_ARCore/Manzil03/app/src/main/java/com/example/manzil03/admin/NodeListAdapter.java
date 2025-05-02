package com.example.manzil03.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.manzil03.R;
import com.example.manzil03.model.NavigationNode;

public class NodeListAdapter extends ListAdapter<NavigationNode, NodeListAdapter.NodeViewHolder> {
    private final OnNodeClickListener clickListener;

    public interface OnNodeClickListener {
        void onNodeClick(NavigationNode node);
    }

    public NodeListAdapter(OnNodeClickListener listener) {
        super(new DiffUtil.ItemCallback<NavigationNode>() {
            @Override
            public boolean areItemsTheSame(@NonNull NavigationNode oldItem, @NonNull NavigationNode newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull NavigationNode oldItem, @NonNull NavigationNode newItem) {
                return oldItem.getLabel().equals(newItem.getLabel()) &&
                       oldItem.isClassroom() == newItem.isClassroom() &&
                       oldItem.getClassroomNumber().equals(newItem.getClassroomNumber()) &&
                       oldItem.getX() == newItem.getX() &&
                       oldItem.getY() == newItem.getY() &&
                       oldItem.getZ() == newItem.getZ();
            }
        });
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_node, parent, false);
        return new NodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NodeViewHolder holder, int position) {
        NavigationNode node = getItem(position);
        holder.bind(node, clickListener);
    }

    static class NodeViewHolder extends RecyclerView.ViewHolder {
        private final TextView labelView;
        private final TextView detailsView;

        NodeViewHolder(View itemView) {
            super(itemView);
            labelView = itemView.findViewById(R.id.nodeLabel);
            detailsView = itemView.findViewById(R.id.nodeDetails);
        }

        void bind(NavigationNode node, OnNodeClickListener listener) {
            labelView.setText(node.getLabel());
            
            String details = node.isClassroom() 
                ? "Room " + node.getClassroomNumber()
                : String.format("(%.1f, %.1f, %.1f)", node.getX(), node.getY(), node.getZ());
            detailsView.setText(details);

            itemView.setOnClickListener(v -> listener.onNodeClick(node));
        }
    }
}