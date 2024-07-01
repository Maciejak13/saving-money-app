package com.example.zpiexpensemanger.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.zpiexpensemanger.Models.Goal;
import com.example.zpiexpensemanger.R;
import com.example.zpiexpensemanger.databinding.ActivityGoalsBinding;
import com.example.zpiexpensemanger.firebase.GoalsAdapter;
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

public class GoalsActivity extends AppCompatActivity {

    private ActivityGoalsBinding binding;
    private PreferenceManger preferenceManger;

    private double totalIncome = 0;
    private double totalExpense = 0;

    private double totalGoals = 0;

    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoalsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManger = new PreferenceManger(getApplicationContext());

        setListeners();
        getIncomesAndExpensesSummary();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        getGoals();


        addGoal();

    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> addGoal());
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        ///binding.imageInfo.setOnClickListener(v -> );
    }

    private void updateBalanceTextView() {
        // Oblicz różnicę między totalExpense a totalIncome
            int balance = (int) ((totalIncome - totalExpense)-totalGoals);

        // Ustaw tekst w EditText
        binding.textView.setText(String.valueOf(balance));
    }

    private void getIncomesAndExpensesSummary() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference incomeCollection = db.collection(Constants.KEY_COLLECTION_INCOME);
        CollectionReference expenseCollection = db.collection(Constants.KEY_COLLECTION_EXPENSE);
        CollectionReference goalsCollection = db.collection(Constants.KEY_COLLECTION_GOALS);

        // Pobierz dane z kolekcji przychodów
        incomeCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for income documents", e);
                        return;
                    }

                    totalIncome = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_INCOME_TOTAL)) {
                            String incomeTotalAsString = document.getString(Constants.KEY_INCOME_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                Integer incomeTotal = Integer.parseInt(incomeTotalAsString);
                                totalIncome += incomeTotal;
                            } catch (NumberFormatException ex) {
                                Log.e("TAG", "Error parsing income total as double", ex);
                            }
                        }
                    }

                    // Aktualizuj TextView po zakończeniu pobierania danych
                    updateBalanceTextView();
                });

        goalsCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for goals documents", e);
                        return;
                    }

                    totalGoals = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_GOALS_TOTAL)) {
                            String goalsTotalAsString = document.getString(Constants.KEY_GOALS_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                double goalsTotal = Double.parseDouble(goalsTotalAsString);
                                totalGoals += goalsTotal;
                            } catch (NumberFormatException ex) {
                                Log.e("TAG", "Error parsing goals total as double", ex);
                            }
                        }
                    }

                    // Aktualizuj TextView po zakończeniu pobierania danych
                    updateBalanceTextView();
                });

        // Pobierz dane z kolekcji wydatków
        expenseCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for expense documents", e);
                        return;
                    }

                    totalExpense = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_EXPENSE_TOTAL)) {
                            String expenseTotalAsString = document.getString(Constants.KEY_EXPENSE_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                double expenseTotal = Double.parseDouble(expenseTotalAsString);
                                totalExpense += expenseTotal;
                            } catch (NumberFormatException ex) {
                                Log.e("TAG", "Error parsing expense total as double", ex);
                            }
                        }
                    }

                    // Aktualizuj TextView po zakończeniu pobierania danych
                    updateBalanceTextView();
                });
        expenseCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for expense documents", e);
                        return;
                    }

                    totalExpense = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_EXPENSE_TOTAL)) {
                            String expenseTotalAsString = document.getString(Constants.KEY_EXPENSE_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                double expenseTotal = Double.parseDouble(expenseTotalAsString);
                                totalExpense += expenseTotal;
                            } catch (NumberFormatException ex) {
                                Log.e("TAG", "Error parsing expense total as double", ex);
                            }
                        }
                    }

                    // Aktualizuj TextView po zakończeniu pobierania danych
                    updateBalanceTextView();
                });


    }

    private void addGoal() {
        String goalsTotal = binding.inputGoal.getText().toString();
        String goalTitle = binding.inputGoalName.getText().toString(); // Poprawione pobieranie tytułu celu
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        if (!goalsTotal.isEmpty()) {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> goal = new HashMap<>();
            goal.put(Constants.KEY_GOALS_TOTAL, goalsTotal);
            goal.put(Constants.KEY_USER_ID, userId);
            goal.put(Constants.KEY_GOAL_TITLE, goalTitle); // Poprawione dodanie tytułu celu
            goal.put(Constants.KEY_INCOME_ID, userId);
            goal.put(Constants.KEY_TIMESTAMP, FieldValue.serverTimestamp());

            database.collection(Constants.KEY_COLLECTION_GOALS)
                    .add(goal)
                    .addOnSuccessListener(documentReference -> {
                        preferenceManger.putString(Constants.KEY_GOALS_ID, documentReference.getId());
                        preferenceManger.putString(Constants.KEY_GOALS_TOTAL, goalsTotal);
                        preferenceManger.putString(Constants.KEY_GOAL_TITLE, goalTitle);


                        showToast("Dane dodane do bazy");
                    })
                    .addOnFailureListener(exception -> {
                        showToast("Nie udało się dodać danych do bazy: " + exception.getMessage());
                    });
        } else {
            showToast("Wprowadź kwotę celu");
        }
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", " użytkownicy nie są aktywni"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }


    private void getGoals() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_GOALS)
                .whereEqualTo(Constants.KEY_USER_ID, userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Obsłuż błąd, jeśli wystąpi
                        showErrorMessage();
                        return;
                    }

                    List<Goal> goals = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                        if (userId.equals(queryDocumentSnapshot.getId())) {
                            continue;
                        }
                        Goal goal = new Goal();
                        goal.setId(queryDocumentSnapshot.getId());
                        goal.goaltitle = queryDocumentSnapshot.getString(Constants.KEY_GOAL_TITLE);
                        goal.goalsplus = queryDocumentSnapshot.getString(Constants.KEY_GOALS_TOTAL);
                        Timestamp timestamp = queryDocumentSnapshot.getTimestamp(Constants.KEY_TIMESTAMP);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        goal.date = timestamp != null ? dateFormat.format(((Timestamp) timestamp).toDate()) : "";

                        int goalsPlusValue = Integer.parseInt(goal.goalsplus);

                        if (goalsPlusValue >= 0 && goalsPlusValue <= 300) {
                            goal.goalImageResourceId = R.drawable.star1 ;
                        } else if (goalsPlusValue > 100 && goalsPlusValue <= 600) {
                            goal.goalImageResourceId = R.drawable.star2;
                        } else if (goalsPlusValue > 200 && goalsPlusValue <= 1000) {
                            goal.goalImageResourceId = R.drawable.star3;
                        } else if (goalsPlusValue > 300 && goalsPlusValue <= 2000) {
                            goal.goalImageResourceId = R.drawable.star4;
                        } else if (goalsPlusValue > 400 && goalsPlusValue <= 5000000) {
                            goal.goalImageResourceId = R.drawable.baseline_star_half_24;
                        }


                        goals.add(goal);
                    }

                    Collections.sort(goals, (o1, o2) -> {
                        try {
                            Date date1 = dateFormat.parse(o1.date);
                            Date date2 = dateFormat.parse(o2.date);
                            return date2.compareTo(date1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    });

                       if (goals.size() > 0) {
                           GoalsAdapter goalsAdapter = new GoalsAdapter(goals);
                           binding.goalsRecyclerView.setAdapter(goalsAdapter);
                           binding.goalsRecyclerView.setVisibility(View.VISIBLE);
                           goalsAdapter.notifyDataSetChanged();

                       } else {
                           showErrorMessage();
                       }

                });
    }


}


