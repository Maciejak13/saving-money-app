package com.example.zpiexpensemanger.firebase;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zpiexpensemanger.Models.Goal;
import com.example.zpiexpensemanger.databinding.ItemContainerGoalsBinding;
import com.example.zpiexpensemanger.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GoalsAdapter extends  RecyclerView.Adapter<GoalsAdapter.GoalsViewHolder>{

    private final List<Goal> goals;

    public GoalsAdapter(List<Goal> goals) {
        this.goals = goals;
    }

    @NonNull
    @Override
    public GoalsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerGoalsBinding itemContainerGoalsBinding = ItemContainerGoalsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new GoalsViewHolder(itemContainerGoalsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalsViewHolder holder, int position) {
        holder.setGoalData(goals.get(position));

        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Pobranie ID celu do usunięcia z bazy danych
                String goalId = goals.get(adapterPosition).getId();
                // Usunięcie elementu z bazy danych
                deleteGoalFromDatabase(goalId);
                // Usunięcie elementu z listy goals
                goals.remove(adapterPosition);
                // Poinformowanie adaptera o zmianach
                notifyItemRemoved(adapterPosition);
            }
        });

    }

    private void deleteGoalFromDatabase(String goalId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_GOALS)
                .document(goalId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Operacja usunięcia zakończona sukcesem
                })
                .addOnFailureListener(e -> {
                    // Obsługa błędu
                });
    }





    @Override
    public int getItemCount() {
        return goals.size();
    }

    class GoalsViewHolder extends RecyclerView.ViewHolder{

        ItemContainerGoalsBinding binding;

        GoalsViewHolder(ItemContainerGoalsBinding itemContainerGoalsBinding) {
            super(itemContainerGoalsBinding.getRoot());
            binding = itemContainerGoalsBinding;
        }

        void setGoalData(Goal goal) {
            binding.textGoal.setText(goal.goaltitle);
            binding.textGoalMuch.setText(goal.goalsplus);
            binding.textDate.setText(goal.date);

            binding.ExpenseView.setImageResource(goal.goalImageResourceId);
        }
    }




}
