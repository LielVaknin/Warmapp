package com.example.warmapp.traineeActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.warmapp.R;
import com.example.warmapp.classes.MyAdapter;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainer;
import com.example.warmapp.trainerActivities.CalendarActivity;
import com.example.warmapp.trainerActivities.TrainerRequestsActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

public class SearchActivity extends AppCompatActivity implements Serializable {

    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<TrainingModel> trainings;

    Button mDatePickerBtn;
    Button searchBtn;
    RangeSlider rangeSlider;

    TextInputLayout textInputLayoutCity;
    AutoCompleteTextView autoCompleteTextCity;
    TextInputLayout textInputLayoutTitle;
    AutoCompleteTextView autoCompleteTextViewTitle;

    Chip chipGroupTraining;
    Chip chipOutdoorTraining;
    Chip chipForMenOnly;
    Chip chipForWomenOnly;
    Chip chipYouth;
    Chip chipChildren;
    Chip chipForPregnantWomen;
    Chip chipPostpartumWomen;
    Chip chipPreparationForCompetitions;
    Chip chipInjuryRehabilitation;
    ArrayList<String> selectedChips;
    MaterialDatePicker<Long> materialDatePicker;

    HashSet<String> userRequestsTrainings = new HashSet<>();
    HashSet<String> userTrainings = new HashSet<>();

    boolean titleSelected = true;
    boolean citySelected = true;
    boolean dateSelected = false;

    String userType ;
    String userID;
    DatabaseReference databaseReference;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        userID = "mEMcSemGPIMgWbP9anZ668m5KHH2";
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_search);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu_profile:

                        /*intent = new Intent(TraineeRequestsActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();*/
                        return true;
                    case R.id.menu_search:
                        /*intent = new Intent(TraineeRequestsActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();*/
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(SearchActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(SearchActivity.this, TrainerRequestsActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        return true;
                }
                return false;



            }
        });

// Search
        recyclerView = findViewById(R.id.m_RecycleView);
        searchBtn = findViewById(R.id.serach_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trainings.clear();
                if(myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }
                userRequestsTrainings.clear();
                userTrainings.clear();

                FirebaseDatabase.getInstance().getReference().child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("f",snapshot.hasChild("requests")+"");
                        Log.d("f",snapshot.hasChild("trainings")+"");
                        if(!snapshot.hasChild("requests")){
                            if(!snapshot.hasChild("trainings")) {
                                findSelectedElements();
                            }
                            else{
                                getUserTrainings();
                            }
                        }else{
                            if(!snapshot.hasChild("trainings")){
                                getUserRequests("found");
                            }
                            else{
                                getUserRequests("trainings");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



            }

        });

        // Chips
        chipGroupTraining = findViewById(R.id.group_training);
        chipOutdoorTraining = findViewById(R.id.outdoor_training);
        chipForMenOnly = findViewById(R.id.for_men_only);
        chipForWomenOnly = findViewById(R.id.for_women_only);
        chipYouth = findViewById(R.id.youth);
        chipChildren = findViewById(R.id.children);
        chipForPregnantWomen = findViewById(R.id.for_pregnant_women);
        chipPostpartumWomen = findViewById(R.id.postpartum_women);
        chipPreparationForCompetitions = findViewById(R.id.preparation_for_competitions);
        chipInjuryRehabilitation = findViewById(R.id.injury_rehabilitation);

        selectedChips = new ArrayList<>();
        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedChips.add(buttonView.getText().toString());
                } else {
                    selectedChips.remove(buttonView.getText().toString());
                }
            }
        };

        chipGroupTraining.setOnCheckedChangeListener(checkedChangeListener);
        chipOutdoorTraining.setOnCheckedChangeListener(checkedChangeListener);
        chipForMenOnly.setOnCheckedChangeListener(checkedChangeListener);
        chipForWomenOnly.setOnCheckedChangeListener(checkedChangeListener);
        chipYouth.setOnCheckedChangeListener(checkedChangeListener);
        chipChildren.setOnCheckedChangeListener(checkedChangeListener);
        chipForPregnantWomen.setOnCheckedChangeListener(checkedChangeListener);
        chipPostpartumWomen.setOnCheckedChangeListener(checkedChangeListener);
        chipPreparationForCompetitions.setOnCheckedChangeListener(checkedChangeListener);
        chipInjuryRehabilitation.setOnCheckedChangeListener(checkedChangeListener);

        // Calender
        mDatePickerBtn = findViewById(R.id.date_picker_btn);
        materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                .build();
        materialDatePicker.addOnNegativeButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerBtn.setText("Select a date");
            }
        });
        mDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "Date picker");
            }
        });
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                mDatePickerBtn.setText(simpleFormat.format(selection));
                dateSelected = true;
            }
        });

        // Range slider
        rangeSlider = findViewById(R.id.sliderRange);
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(Currency.getInstance("ILS"));
                return currencyFormat.format((int) value);
            }
        });

        // Titles and Cities
        textInputLayoutTitle = findViewById(R.id.menu_drop2);
        autoCompleteTextViewTitle = findViewById(R.id.drop_titles);
        textInputLayoutCity = findViewById(R.id.menu_drop);
        autoCompleteTextCity = findViewById(R.id.drop_cities);
//        recyclerView = findViewById(R.id.m_RecycleView);

        String[] titles = getResources().getStringArray(R.array.Titles);
        ArrayAdapter<String> titleAdapter = new ArrayAdapter<>(SearchActivity.this, R.layout.list, titles);
        autoCompleteTextViewTitle.setAdapter(titleAdapter);

        String[] cities = getResources().getStringArray(R.array.Cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(SearchActivity.this, R.layout.list, cities);
        autoCompleteTextCity.setAdapter(cityAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Trainings");
        trainings = new ArrayList<>();
    }
    public void getUserRequests(String callback){
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("requests").addListenerForSingleValueEvent(new ValueEventListener() {
            int counter=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {

                for (DataSnapshot dataSnapshot : snapshot1.getChildren()) {

                    String requestID = dataSnapshot.getKey();
                    FirebaseDatabase.getInstance().getReference().child("Requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            counter++;
                            String requestTrainingID = snapshot.getValue(Request.class).getTrainingID();
                            userRequestsTrainings.add(requestTrainingID);
                            if(counter==snapshot1.getChildrenCount()){
                                if(callback.equals("trainings")){
                                    getUserTrainings();
                                }
                                else{
                                    findSelectedElements();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getUserTrainings() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("trainings").addListenerForSingleValueEvent(new ValueEventListener() {
            int counter=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    counter++;
                    String trainingID = dataSnapshot.getKey();
                    userTrainings.add(trainingID);
                    if(counter==snapshot.getChildrenCount()) {
                        findSelectedElements();
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void filterDataBase(String city, String title, String date, List<Float> values) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countTrainings = 0;
                boolean flag = false;
                if (title.equals("")) {
                    titleSelected = false;
                } else {
                    titleSelected = true;
                }
                if (city.equals("")) {
                    citySelected = false;
                } else {
                    citySelected = true;
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    countTrainings++;
                    Training training = dataSnapshot.getValue(Training.class);
                    TrainingModel trainingModel = new TrainingModel();
                    boolean hasFeatures = true;
                    HashMap<String, String> features = training.getFeatures();
                    if (features != null) {
                        for (int i = 0; i < selectedChips.size() && hasFeatures; i++) {
                            if (features.get(selectedChips.get(i)) == null) {
                                hasFeatures = false;
                            }
                        }
                    } else if (!selectedChips.isEmpty()) {
                        hasFeatures = false;
                    }

                    if ( training.getParticipants()==null ||(training.getParticipants().size() < training.getMaxParticipants()) ) {
                        if (selectedChips.isEmpty() && !titleSelected && !citySelected && !dateSelected && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1)) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (titleSelected && citySelected && dateSelected && city.equals(training.getCity()) && title.equals(training.getTitle()) && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (!titleSelected && citySelected && !dateSelected && city.equals(training.getCity()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (titleSelected && !citySelected && !dateSelected && title.equals(training.getTitle()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (!titleSelected && !citySelected && dateSelected && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (titleSelected && citySelected && !dateSelected && city.equals(training.getCity()) && title.equals(training.getTitle()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (!titleSelected && citySelected && dateSelected && city.equals(training.getCity()) && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (titleSelected && !citySelected && dateSelected && title.equals(training.getTitle()) && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && hasFeatures) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (hasFeatures && !titleSelected && !citySelected && !dateSelected && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1)) {
                            flag = true;
                            setTrainingModel(trainingModel, training);
                        } else if (snapshot.getChildrenCount() == countTrainings && !flag) {
                            Toast.makeText(SearchActivity.this, "No trainings found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setTrainingModel(TrainingModel trainingModel, Training training) {
        trainingModel.training = training;
        if (userRequestsTrainings.contains(training.getTrainingID())) {
            trainingModel.trainingStatus = "request";
        } else if (userTrainings.contains(training.getTrainingID())) {
            trainingModel.trainingStatus = "apply";
        }
        String trainerId = training.getTrainerId();
        FirebaseDatabase.getInstance().getReference().child("Users").child(trainerId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("training:", training.getTitle());
                    UserTrainer userTrainer = task.getResult().getValue(UserTrainer.class);
                    trainingModel.trainerName = userTrainer.getFirstName() + " " + userTrainer.getLastName();
                    trainings.add(trainingModel);
                    //myAdapter.notifyDataSetChanged();
                    setAdapter();
                    Log.d("training:", training.getTitle());

                }
            }
        });
    }

    public void setAdapter() {
        Log.d("adapter:", "" + trainings.size());
        myAdapter = new MyAdapter(this, trainings);
        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
    }

    public void findSelectedElements() {
        Log.d("find","one more");
        String city = autoCompleteTextCity.getText().toString();
        String title = autoCompleteTextViewTitle.getText().toString();
        String date = mDatePickerBtn.getText().toString();
        List<Float> values = rangeSlider.getValues();
        filterDataBase(city, title, date, values);
    }
}