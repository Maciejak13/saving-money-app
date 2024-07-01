package com.example.zpiexpensemanger.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import android.widget.ImageView;

import com.example.zpiexpensemanger.R;
import com.example.zpiexpensemanger.databinding.ActivityMainBinding;
import com.example.zpiexpensemanger.utilities.Constants;
import com.example.zpiexpensemanger.utilities.PreferenceManger;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import com.google.firebase.Timestamp;

public class MainActivity extends AppCompatActivity {

    private PreferenceManger preferenceManger;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding =ActivityMainBinding.inflate(getLayoutInflater());

        preferenceManger = new PreferenceManger(getApplicationContext());
        setContentView(binding.getRoot());
        loadUserDetails();
        setListeners();
        getIncomesSummary();
        getExpensesSummary();
        getLastIncome();
        getLastExpense();


    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

    }

    private void setListeners() {
        binding.imagesSignOut.setOnClickListener(v -> signOut());
        binding.Income.setOnClickListener(v -> goToIncome());
        binding.ChartPie.setOnClickListener(v -> goToChartPie());
        binding.Expense.setOnClickListener(v -> goToExpense());
        binding.Goals.setOnClickListener(v -> goToGoals());

    }












    private void loadUserDetails() {
        binding.textName.setText(preferenceManger.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManger.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);



    }

    private void signOut() {
        showToast("Wylogowywuje.... :) ");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManger.getString(Constants.KEY_USER_ID)
                );
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManger.clear();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Nie udało się wylogować :("));

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

                    double totalIncome = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_INCOME_TOTAL)) {
                            String incomeTotalAsString = document.getString(Constants.KEY_INCOME_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                double incomeTotal = Double.parseDouble(incomeTotalAsString);
                                totalIncome += incomeTotal;
                            } catch (NumberFormatException ex) {
                                // Obsługa błędu w przypadku, gdy wartość nie może być przekształcona na double
                                Log.e("TAG", "Error parsing income total as double", ex);
                            }
                        }
                    }

                    // Po obliczeniu sumy, ustaw ją w EditText
                    binding.EditTextIncomeBalance.setText(String.valueOf(totalIncome));
                });
    }


    private void getLastIncome() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference incomeCollection = db.collection(Constants.KEY_COLLECTION_INCOME);

        incomeCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        if (document.contains(Constants.KEY_INCOME_TOTAL) && document.contains(Constants.KEY_TIMESTAMP)) {
                            String incomeTotal = document.getString(Constants.KEY_INCOME_TOTAL);
                            Timestamp timestamp = document.getTimestamp(Constants.KEY_TIMESTAMP);

                            binding.lastIncome.setText(incomeTotal);

                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                String formattedDate = formatDate(date);
                                binding.lastIncomeDate.setText(formattedDate);
                            }
                        }
                    } else {
                        // Brak danych w kolekcji Income
                        // Ustawienie wartości domyślnych dla lastIncome i lastIncomeDate
                        binding.lastIncome.setText("Brak danych");
                        binding.lastIncomeDate.setText("Brak danych");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TAG", "Error getting last income record", e);
                    // Ustawienie wartości domyślnych w przypadku błędu
                    binding.lastIncome.setText("Błąd");
                    binding.lastIncomeDate.setText("Błąd");
                });
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

                    double totalExpense = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_EXPENSE_TOTAL)) {
                            String expenseTotalAsString = document.getString(Constants.KEY_EXPENSE_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                double expenseTotal = Double.parseDouble(expenseTotalAsString);
                                totalExpense += expenseTotal;
                            } catch (NumberFormatException ex) {
                                // Obsługa błędu w przypadku, gdy wartość nie może być przekształcona na double
                                Log.e("TAG", "Error parsing expense total as double", ex);
                            }
                        }
                    }

                    // Po obliczeniu sumy, ustaw ją w EditText
                    binding.EditTextExpenseBalance.setText(String.valueOf(totalExpense));
                });
    }

    private void getLastExpense() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expenseCollection = db.collection(Constants.KEY_COLLECTION_EXPENSE);

        expenseCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .orderBy(Constants.KEY_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                        if (document.contains(Constants.KEY_EXPENSE_TOTAL) && document.contains(Constants.KEY_TIMESTAMP)) {
                            String incomeTotal = document.getString(Constants.KEY_EXPENSE_TOTAL);
                            Timestamp timestamp = document.getTimestamp(Constants.KEY_TIMESTAMP);

                            binding.lastExpense.setText(incomeTotal);

                            if (timestamp != null) {
                                Date date = timestamp.toDate();
                                String formattedDate = formatDate(date);
                                binding.lastExpenseDate.setText(formattedDate);
                            }
                        }
                    } else {
                        // Brak danych w kolekcji Income
                        // Ustawienie wartości domyślnych dla lastIncome i lastIncomeDate
                        binding.lastExpense.setText("Brak danych");
                        binding.lastExpenseDate.setText("Brak danych");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TAG", "Error getting last income record", e);
                    // Ustawienie wartości domyślnych w przypadku błędu
                    binding.lastExpense.setText("Błąd");
                    binding.lastExpenseDate.setText("Błąd");
                });

    }








    private void goToIncome() {
        showToast("Przechodzenie do dochodów");
        startActivity(new Intent(getApplicationContext(), IncomeActivity.class));

    }

    private void goToExpense() {
        showToast("Przechodzenie do wydatków");
        startActivity(new Intent(getApplicationContext(), ExpenseActivity.class));

    }

    private void goToChartPie() {
        showToast("Przechodzenie do wykresu wydatków ");
        startActivity(new Intent(getApplicationContext(), ChartPieActivity.class));

    }

    private void goToGoals() {
        showToast("Przechodzenie do celi");
        startActivity(new Intent(getApplicationContext(), GoalsActivity.class));

    }

    private String formatDate(Date date) {
        // Format daty według własnych preferencji
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }






}
