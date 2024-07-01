package com.example.zpiexpensemanger.firebase;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zpiexpensemanger.Models.Expense;
import com.example.zpiexpensemanger.databinding.ItemContainerExpenseBinding;
import com.example.zpiexpensemanger.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;


public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses;

    public ExpenseAdapter(List<Expense> expenses) {this.expenses = expenses;}

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerExpenseBinding itemContainerExpenseBinding = ItemContainerExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false
        );
        return new ExpenseViewHolder(itemContainerExpenseBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        holder.setExpenseData(expenses.get(position));
        holder.itemView.setOnClickListener(view -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Pobranie ID wydatku do usunięcia z bazy danych
                String expenseId = expenses.get(adapterPosition).getId();
                // Usunięcie elementu z bazy danych
                deleteExpenseFromDatabase(expenseId);
                // Usunięcie elementu z listy expenses
                expenses.remove(adapterPosition);
                // Poinformowanie adaptera o zmianach
                notifyItemRemoved(adapterPosition);
            }
        });
    }

    private void deleteExpenseFromDatabase(String expenseId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_EXPENSE)
                .document(expenseId)
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
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {

        ItemContainerExpenseBinding binding;

        ExpenseViewHolder(ItemContainerExpenseBinding itemContainerExpenseBinding) {
            super(itemContainerExpenseBinding.getRoot());
            binding = itemContainerExpenseBinding;
        }

        void setExpenseData(Expense expense) {
            binding.textExpense.setText(expense.expenseplus);
            binding.textDate.setText(expense.date);
            binding.textCategory.setText(expense.category);

            binding.ExpenseView.setImageResource(expense.categoryImageResourceId);
        }


    }
}
