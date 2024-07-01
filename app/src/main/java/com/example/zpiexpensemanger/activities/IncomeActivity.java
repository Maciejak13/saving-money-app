package com.example.zpiexpensemanger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zpiexpensemanger.Models.Income;
import com.example.zpiexpensemanger.databinding.ActivityIncomeBinding;
import com.example.zpiexpensemanger.firebase.IncomeAdapter;
import com.example.zpiexpensemanger.utilities.Constants;
import com.example.zpiexpensemanger.utilities.PreferenceManger;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class IncomeActivity extends AppCompatActivity {

    private ActivityIncomeBinding binding;
    private PreferenceManger preferenceManger;
    private SimpleDateFormat dateFormat;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());
        setListeners();
        getIncomes();
        getIncomesSummary();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    }









    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> addIncome());
        binding.imageBack.setOnClickListener(v -> onBackPressed());

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void addIncome() {
        String incomeTotal = binding.inputIncome.getText().toString();
        String userId = preferenceManger.getString(Constants.KEY_USER_ID); // Pobierz USER_ID
        if (!incomeTotal.isEmpty()) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> income = new HashMap<>();
            income.put(Constants.KEY_INCOME_TOTAL, incomeTotal);
            income.put(Constants.KEY_USER_ID, userId);
            income.put(Constants.KEY_INCOME_ID, userId);// Dodaj USER_ID do danych przychodu
            income.put(Constants.KEY_TIMESTAMP, FieldValue.serverTimestamp());

            database.collection(Constants.KEY_COLLECTION_INCOME)
                    .add(income)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManger.putString(Constants.KEY_INCOME_ID, documentReference.getId());
                        preferenceManger.putString(Constants.KEY_INCOME_TOTAL, incomeTotal);

                        showToast("Dane dodane do bazy");
                    })
                    .addOnFailureListener(exception -> {
                        showToast("Nie udało się dodać danych do bazy: " + exception.getMessage());
                    });
        } else {
            showToast("Wprowadź kwotę przychodu");
        }
    }
    private void getIncomes() {

        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_INCOME)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Obsłuż błąd, jeśli wystąpi
                        return;
                    }

                    List<Income> incomes = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                        Income income = new Income();
                        income.setId(queryDocumentSnapshot.getId());
                        income.incomeplus = queryDocumentSnapshot.getString(Constants.KEY_INCOME_TOTAL);

                        Timestamp timestamp = queryDocumentSnapshot.getTimestamp(Constants.KEY_TIMESTAMP);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        income.date = timestamp != null ? dateFormat.format(((Timestamp) timestamp).toDate()) : "";

                        incomes.add(income);
                    }

                    Collections.sort(incomes, (o1, o2) -> {
                        try {
                            Date date1 = dateFormat.parse(o1.date);
                            Date date2 = dateFormat.parse(o2.date);
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                    if (incomes.size() > 0) {
                        IncomeAdapter incomeAdapter = new IncomeAdapter(incomes);
                        binding.incomeRecyclerView.setAdapter(incomeAdapter);
                        binding.incomeRecyclerView.setVisibility(View.VISIBLE);
                        incomeAdapter.notifyDataSetChanged();
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void getIncomesSummary() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference incomeCollection = db.collection(Constants.KEY_COLLECTION_INCOME);

        // Dodaj nasłuchiwanie zmian w kolekcji
        incomeCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for income documents", e);
                        return;
                    }

                    Integer totalIncome = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_INCOME_TOTAL)) {
                            String incomeTotalAsString = document.getString(Constants.KEY_INCOME_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                Integer incomeTotal = Integer.parseInt(incomeTotalAsString);
                                totalIncome += incomeTotal;
                            } catch (NumberFormatException ex) {
                                // Obsługa błędu w przypadku, gdy wartość nie może być przekształcona na double
                                Log.e("TAG", "Error parsing income total as double", ex);
                            }
                        }
                    }

                    // Po obliczeniu sumy, ustaw ją w EditText
                    binding.textView.setText(String.valueOf(totalIncome));
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", " użytkownicy nie są aktywni"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }




}