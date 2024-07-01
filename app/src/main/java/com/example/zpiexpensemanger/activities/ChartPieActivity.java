package com.example.zpiexpensemanger.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.example.zpiexpensemanger.R;
import com.example.zpiexpensemanger.databinding.ActivityChartPieBinding;
import com.example.zpiexpensemanger.utilities.Constants;
import com.example.zpiexpensemanger.utilities.PreferenceManger;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChartPieActivity extends AppCompatActivity {

    private ActivityChartPieBinding binding;
    private PreferenceManger preferenceManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChartPieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManger = new PreferenceManger(getApplicationContext());
        getFoodAndBillsExpensesSummary();
        setListeners();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getPieChart(float totalFoodExpense, float totalBillsExpense, float totalLocomotionExpense, float totalFunExpense, float totalGoingOutExpense, float totalOtherExpense) {
        PieChart pieChart = findViewById(R.id.chart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        if (totalFoodExpense > 0) {
            entries.add(new PieEntry(totalFoodExpense, "Jedzenie"));
        }
        if (totalBillsExpense > 0) {
            entries.add(new PieEntry(totalBillsExpense, "Czynsz i rachunki"));
        }
        if (totalLocomotionExpense > 0) {
            entries.add(new PieEntry(totalLocomotionExpense, "Paliwo i dojazdy"));
        }
        if (totalFunExpense > 0) {
            entries.add(new PieEntry(totalFunExpense, "Rozrywka"));
        }
        if (totalGoingOutExpense > 0) {
            entries.add(new PieEntry(totalGoingOutExpense, "Wyjścia"));
        }
        if (totalOtherExpense > 0) {
            entries.add(new PieEntry(totalOtherExpense, "Inne"));
        }

        List<Integer> customColors = Arrays.asList(
                Color.rgb(255, 102, 102),   // Czerwony
                Color.rgb(255, 204, 102),   // Pomarańczowy
                Color.rgb(255, 255, 102),   // Żółty
                Color.rgb(102, 255, 102),   // Zielony
                Color.rgb(102, 178, 255),   // Niebieski
                Color.rgb(204, 102, 255)    // Fioletowy
        );

        PieDataSet pieDataSet = new PieDataSet(entries, "");
        pieDataSet.setColors(customColors);

        // Dodaj grubość konturów linii
        pieDataSet.setFormLineWidth(2f);


        pieDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value); // Formatuj wartości jako liczby całkowite
            }
        });
        pieDataSet.setValueTextSize(22f);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTypeface(Typeface.DEFAULT_BOLD);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);

        pieChart.getDescription().setEnabled(false);
        pieChart.animateY(1000);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);

        pieChart.invalidate();
    }

    private void getFoodAndBillsExpensesSummary() {
        String userId = preferenceManger.getString(Constants.KEY_USER_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expenseCollection = db.collection(Constants.KEY_COLLECTION_EXPENSE);

        // Dodaj nasłuchiwanie zmian w kolekcji
        expenseCollection.whereEqualTo(Constants.KEY_USER_ID, userId)
                .whereIn(Constants.KEY_CATEGORY_EXPENSE, getCategories())
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("TAG", "Error listening for expense documents", e);
                        return;
                    }

                    float totalFoodExpense = 0;
                    float totalBillsExpense = 0;
                    float totalLocomotionExpense = 0;
                    float totalFunExpense = 0;
                    float totalOtherExpense = 0;
                    float totalGoingOutExpense = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.contains(Constants.KEY_EXPENSE_TOTAL)) {
                            String expenseTotalAsString = document.getString(Constants.KEY_EXPENSE_TOTAL);

                            // Spróbuj przekształcić wartość na double
                            try {
                                float expenseTotal = Float.parseFloat(expenseTotalAsString);
                                if ("Jedzenie".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalFoodExpense += expenseTotal;
                                } else if ("Czynsz i rachunki".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalBillsExpense += expenseTotal;
                                } else if ("Paliwo i dojazdy".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalLocomotionExpense += expenseTotal;
                                } else if ("Rozrywka".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalFunExpense += expenseTotal;
                                } else if ("Wyjścia".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalGoingOutExpense += expenseTotal;
                                } else if ("Inne".equals(document.getString(Constants.KEY_CATEGORY_EXPENSE))) {
                                    totalOtherExpense += expenseTotal;
                                }
                            } catch (NumberFormatException ex) {
                                // Obsługa błędu w przypadku, gdy wartość nie może być przekształcona na float
                                Log.e("TAG", "Error parsing expense total as float", ex);
                            }
                        }
                    }

                    // Wywołaj metodę getPieChart z zaktualizowanymi wartościami totalFoodExpense i totalBillsExpense
                    getPieChart(totalFoodExpense, totalBillsExpense, totalLocomotionExpense, totalFunExpense, totalGoingOutExpense, totalOtherExpense);
                });
    }

    private List<String> getCategories() {
        return Arrays.asList("Jedzenie", "Czynsz i rachunki", "Paliwo i dojazdy", "Rozrywka", "Wyjścia", "Inne");
    }
}