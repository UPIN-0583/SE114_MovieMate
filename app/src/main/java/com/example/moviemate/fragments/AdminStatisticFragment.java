package com.example.moviemate.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moviemate.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashMap;

public class AdminStatisticFragment extends Fragment {

    private BarChart revenueBarChart;
    private DatabaseReference ticketsRef;
    private Spinner statisticTypeSpinner, monthSpinner, yearSpinner;
    private View monthFilterLayout, weekFilterLayout;
    private Set<Integer> availableYears = new TreeSet<>();
    private HashMap<Integer, Set<Integer>> yearMonthsMap = new HashMap<>();

    public AdminStatisticFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_statistic, container, false);

        // Initialize UI elements
        revenueBarChart = view.findViewById(R.id.revenueBarChart);
        statisticTypeSpinner = view.findViewById(R.id.statisticTypeSpinner);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        yearSpinner = view.findViewById(R.id.yearSpinner);
        monthFilterLayout = view.findViewById(R.id.monthFilterLayout);
        weekFilterLayout = view.findViewById(R.id.weekFilterLayout);

        // Firebase reference to Tickets
        ticketsRef = FirebaseDatabase.getInstance().getReference("Tickets");

        // Fetch available years and months first
        fetchAvailableData();

        return view;
    }

    private void fetchAvailableData() {
        ticketsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableYears.clear();
                yearMonthsMap.clear();

                // Collect all years and months from tickets
                for (DataSnapshot userTickets : snapshot.getChildren()) {
                    for (DataSnapshot ticketSnapshot : userTickets.getChildren()) {
                        String date = ticketSnapshot.child("date").getValue(String.class);
                        if (date != null) {
                            try {
                                String[] dateParts = date.split("-");
                                int year = Integer.parseInt(dateParts[0]);
                                int month = Integer.parseInt(dateParts[1]);

                                availableYears.add(year);

                                // Store months for each year
                                if (!yearMonthsMap.containsKey(year)) {
                                    yearMonthsMap.put(year, new TreeSet<>());
                                }
                                yearMonthsMap.get(year).add(month);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Setup spinners after getting data
                setupSpinners();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupSpinners() {
        // Setup year spinner
        ArrayList<String> years = new ArrayList<>();
        for (Integer year : availableYears) {
            years.add(String.valueOf(year));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateMonthSpinner();
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupStatisticTypeSpinner();
    }

    private void updateMonthSpinner() {
        if (yearSpinner.getSelectedItem() == null) return;

        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
        Set<Integer> availableMonths = yearMonthsMap.get(selectedYear);
        if (availableMonths == null) availableMonths = new TreeSet<>();

        ArrayList<String> months = new ArrayList<>();
        for (Integer month : availableMonths) {
            months.add("Month " + month);
        }

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (statisticTypeSpinner.getSelectedItemPosition() == 0) {
                    calculateWeeklyRevenue();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupStatisticTypeSpinner() {
        statisticTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: // Weekly
                        monthFilterLayout.setVisibility(View.VISIBLE);
                        weekFilterLayout.setVisibility(View.VISIBLE);
                        calculateWeeklyRevenue();
                        break;
                    case 1: // Monthly
                        monthFilterLayout.setVisibility(View.GONE);
                        weekFilterLayout.setVisibility(View.VISIBLE);
                        calculateMonthlyRevenue();
                        break;
                    case 2: // Yearly
                        monthFilterLayout.setVisibility(View.GONE);
                        weekFilterLayout.setVisibility(View.GONE);
                        calculateYearlyRevenue();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateChart() {
        int selectedType = statisticTypeSpinner.getSelectedItemPosition();
        switch (selectedType) {
            case 0:
                calculateWeeklyRevenue();
                break;
            case 1:
                calculateMonthlyRevenue();
                break;
            case 2:
                calculateYearlyRevenue();
                break;
        }
    }

    private void calculateWeeklyRevenue() {
        if (monthSpinner.getSelectedItem() == null || yearSpinner.getSelectedItem() == null) return;

        ArrayList<String> labels = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            labels.add("Week " + i);
        }

        String selectedMonthStr = monthSpinner.getSelectedItem().toString();
        int selectedMonth = Integer.parseInt(selectedMonthStr.replace("Month ", ""));
        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());

        fetchRevenue(selectedMonth, selectedYear, labels, "weekly");
    }

    private void calculateMonthlyRevenue() {
        if (yearSpinner.getSelectedItem() == null) return;

        int selectedYear = Integer.parseInt(yearSpinner.getSelectedItem().toString());
        Set<Integer> availableMonths = yearMonthsMap.get(selectedYear);
        if (availableMonths == null) return;

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> monthValues = new ArrayList<>();

        for (Integer month : availableMonths) {
            labels.add("Month " + month);
            monthValues.add(month);
        }

        fetchRevenue(0, selectedYear, labels, "monthly", monthValues);
    }

    private void calculateYearlyRevenue() {
        ArrayList<String> labels = new ArrayList<>();
        for (Integer year : availableYears) {
            labels.add(String.valueOf(year));
        }
        fetchRevenue(0, 0, labels, "yearly");
    }

    private void fetchRevenue(int month, int year, ArrayList<String> labels, String type) {
        fetchRevenue(month, year, labels, type, null);
    }

    private void fetchRevenue(int month, int year, ArrayList<String> labels, String type, ArrayList<Integer> monthValues) {
        ticketsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                float[] revenue = new float[labels.size()];

                for (DataSnapshot userTickets : snapshot.getChildren()) {
                    for (DataSnapshot ticketSnapshot : userTickets.getChildren()) {
                        String date = ticketSnapshot.child("date").getValue(String.class);
                        Long totalPrice = ticketSnapshot.child("totalPrice").getValue(Long.class);

                        if (date != null && totalPrice != null) {
                            try {
                                String[] dateParts = date.split("-");
                                int ticketYear = Integer.parseInt(dateParts[0]);
                                int ticketMonth = Integer.parseInt(dateParts[1]);
                                int ticketDay = Integer.parseInt(dateParts[2]);

                                Calendar calendar = Calendar.getInstance();
                                calendar.set(ticketYear, ticketMonth - 1, ticketDay);

                                switch (type) {
                                    case "yearly":
                                        int yearIndex = new ArrayList<>(availableYears).indexOf(ticketYear);
                                        if (yearIndex >= 0 && yearIndex < labels.size()) {
                                            revenue[yearIndex] += totalPrice;
                                        }
                                        break;
                                    case "monthly":
                                        if (ticketYear == year && monthValues != null) {
                                            int monthIndex = monthValues.indexOf(ticketMonth);
                                            if (monthIndex >= 0 && monthIndex < labels.size()) {
                                                revenue[monthIndex] += totalPrice;
                                            }
                                        }
                                        break;
                                    case "weekly":
                                        if (ticketYear == year && ticketMonth == month) {
                                            int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH) - 1;
                                            if (weekOfMonth >= 0 && weekOfMonth < 4) {
                                                revenue[weekOfMonth] += totalPrice;
                                            }
                                        }
                                        break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // Only add entries for non-zero revenue
                for (int i = 0; i < revenue.length; i++) {
                    if (revenue[i] > 0) {
                        entries.add(new BarEntry(i, revenue[i]));
                    }
                }

                setupBarChart(entries, labels);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setupBarChart(ArrayList<BarEntry> entries, ArrayList<String> labels) {
        BarDataSet dataSet = new BarDataSet(entries, "VNĐ");
        dataSet.setColor(Color.YELLOW);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        revenueBarChart.setData(barData);
        revenueBarChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });

        revenueBarChart.getXAxis().setTextColor(Color.WHITE);
        revenueBarChart.getXAxis().setTextSize(12f);
        revenueBarChart.getAxisLeft().setTextColor(Color.WHITE);
        revenueBarChart.getAxisLeft().setTextSize(12f);
        revenueBarChart.getAxisRight().setTextColor(Color.WHITE);
        revenueBarChart.getLegend().setTextColor(Color.WHITE);

        revenueBarChart.getXAxis().setGranularity(1f);
        revenueBarChart.getXAxis().setGranularityEnabled(true);
        revenueBarChart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);

        revenueBarChart.animateY(1000);
        revenueBarChart.invalidate();
    }
}