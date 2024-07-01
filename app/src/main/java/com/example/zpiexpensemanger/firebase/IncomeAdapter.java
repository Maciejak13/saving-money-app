package com.example.zpiexpensemanger.firebase;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zpiexpensemanger.Models.Income;
import com.example.zpiexpensemanger.databinding.ItemContainerIncomeBinding;
import com.example.zpiexpensemanger.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.IncomeViewHolder>{

    private final List<Income> incomes;

    public IncomeAdapter(List<Income> incomes) {
        this.incomes = incomes;
    }

    @NonNull
    @Override
    public IncomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerIncomeBinding itemContainerIncomeBinding = ItemContainerIncomeBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,false
        );
        return new IncomeViewHolder(itemContainerIncomeBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull IncomeViewHolder holder, int position) {
        holder.setIncomeData(incomes.get(position));
        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Pobranie ID przychodu do usunięcia z bazy danych
                String incomeId = incomes.get(adapterPosition).getId();
                // Usunięcie elementu z bazy danych
                deleteIncomeFromDatabase(incomeId);
                // Usunięcie elementu z listy incomes
                incomes.remove(adapterPosition);
                // Poinformowanie adaptera o zmianach
                notifyItemRemoved(adapterPosition);
            }
        });

    }

    private void deleteIncomeFromDatabase(String incomeId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_INCOME)
                .document(incomeId)
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
        return incomes.size();
    }

    class IncomeViewHolder extends RecyclerView.ViewHolder {

        ItemContainerIncomeBinding binding;
        IncomeViewHolder(ItemContainerIncomeBinding itemContainerIncomeBinding) {
            super(itemContainerIncomeBinding.getRoot());
            binding = itemContainerIncomeBinding;
        }
        void setIncomeData(Income income) {
            binding.textIncome.setText(income.incomeplus);
            binding.textDate.setText(income.date);
        }
    }

}
