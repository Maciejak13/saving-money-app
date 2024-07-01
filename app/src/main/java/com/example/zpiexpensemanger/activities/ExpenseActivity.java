package com.example.zpiexpensemanger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.zpiexpensemanger.Models.Expense;
import com.example.zpiexpensemanger.Models.Income;
import com.example.zpiexpensemanger.R;
import com.example.zpiexpensemanger.databinding.ActivityExpenseBinding;
import com.example.zpiexpensemanger.firebase.ExpenseAdapter;
import com.example.zpiexpensemanger.firebase.IncomeAdapter;
import com.example.zpiexpensemanger.utilities.Constants;
import com.example.zpiexpensemanger.utilities.PreferenceManger;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {


    private ActivityExpenseBinding binding;

    private PreferenceManger preferenceManger;

    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());
        setListeners();
        getExpenses();
        getExpensesSummary();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Spinner inputCategory = findViewById(R.id.inputCategory);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.category, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        inputCategory.setAdapter(adapter);

    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> addExpense());
        binding.imageBack.setOnClickListener(v -> onBackPressed());

    }

    private void addExpense() {
        String expenseTotal = binding.inputExpense.getText().toString();
        String categoryExpense = binding.inputCategory.getSelectedItem().toString();
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);
        if (!expenseTotal.isEmpty()) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> expense = new HashMap<>();
            expense.put(Constants.KEY_EXPENSE_TOTAL, expenseTotal);
            expense.put(Constants.KEY_USER_ID, userId);
            expense.put(Constants.KEY_CATEGORY_EXPENSE, categoryExpense);
            expense.put(Constants.KEY_INCOME_ID, userId);// Dodaj USER_ID do danych przychodu
            expense.put(Constants.KEY_TIMESTAMP, FieldValue.serverTimestamp());

            database.collection(Constants.KEY_COLLECTION_EXPENSE)
                    .add(expense)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManger.putString(Constants.KEY_EXPENSE_ID, documentReference.getId());
                        preferenceManger.putString(Constants.KEY_EXPENSE_TOTAL, expenseTotal);
                        preferenceManger.putString(Constants.KEY_CATEGORY_EXPENSE, categoryExpense);

                        showToast("Dane dodane do bazy");
                    })
                    .addOnFailureListener(exception -> {
                        showToast("Nie udało się dodać danych do bazy: " + exception.getMessage());
                    });
        } else {
            showToast("Wprowadź kwotę przychodu");
        }

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void getExpenses() {

        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_EXPENSE)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    List<Expense> expenses = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                        Expense expense = new Expense();
                        expense.setId(queryDocumentSnapshot.getId());
                        expense.expenseplus = queryDocumentSnapshot.getString(Constants.KEY_EXPENSE_TOTAL);
                        expense.category = queryDocumentSnapshot.getString(Constants.KEY_CATEGORY_EXPENSE);

                        Timestamp timestamp = queryDocumentSnapshot.getTimestamp(Constants.KEY_TIMESTAMP);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        expense.date = timestamp != null ? dateFormat.format(((Timestamp) timestamp).toDate()) : "";

                        if ("Rozrywka".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.party;
                        }
                        if ("Czynsz i rachunki".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.czynsz;
                        }
                        if ("Jedzenie".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.food;
                        }
                        if ("Inne".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.other;
                        }
                        if ("Paliwo i dojazdy".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.travel;
                        }
                        if ("Wyjścia".equals(expense.category)) {
                            expense.categoryImageResourceId = R.drawable.goingout;
                        }


                        expenses.add(expense);
                    }

                    Collections.sort(expenses, (o1, o2) -> {
                        try {
                            Date date1 = dateFormat.parse(o1.date);
                            Date date2 = dateFormat.parse(o2.date);
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                    if (expenses.size() > 0) {
                        ExpenseAdapter expenseAdapter = new ExpenseAdapter(expenses);
                        binding.expenseRecyclerView.setAdapter(expenseAdapter);
                        binding.expenseRecyclerView.setVisibility(View.VISIBLE);
                        expenseAdapter.notifyDataSetChanged();
                    } else {
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", " użytkownicy nie są aktywni"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void getExpensesSummary() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expenseCollection = db.collection(Constants.KEY_COLLECTION_EXPENSE);

        // Dodaj nasłuchiwanie zmian w kolekcji
        expenseCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for expense documents", e);
                        return;
                    }

                    Integer totalExpense = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_EXPENSE_TOTAL)) {
                            String expenseTotalAsString = document.getString(Constants.KEY_EXPENSE_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                Integer expenseTotal = Integer.parseInt(expenseTotalAsString);
                                totalExpense += expenseTotal;
                            } catch (NumberFormatException ex) {
                                // Obsługa błędu w przypadku, gdy wartość nie może być przekształcona na double
                                Log.e("TAG", "Error parsing expense total as double", ex);
                            }
                        }
                    }

                    // Po obliczeniu sumy, ustaw ją w EditText
                    binding.textView.setText(String.valueOf(totalExpense));
                });
    }

}