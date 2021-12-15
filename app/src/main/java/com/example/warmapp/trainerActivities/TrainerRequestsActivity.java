package com.example.warmapp.trainerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.warmapp.R;
import com.example.warmapp.classes.MyAdapter;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.RequestModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.UserTrainee;
import com.example.warmapp.traineeActivities.SearchActivity;
import com.example.warmapp.traineeActivities.TraineeRequestsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;

public class TrainerRequestsActivity extends AppCompatActivity {
    ArrayList<RequestModel>requests;
    DatabaseReference firebaseReference;
    RecyclerView recyclerView;
    TextView hasRequests;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_requests);
        recyclerView=findViewById(R.id.trainer_requests_list);
        requests=new ArrayList<>();
        String userID="qIcl2TIXEDbKwUziUDaqNp9Inmo2";
        BottomNavigationView bottomNavigationView =findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_requests);
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
                        intent = new Intent(TrainerRequestsActivity.this, SearchActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(TrainerRequestsActivity.this, CalendarActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        return true;
                    case R.id.menu_home:
                        return true;
                }
                return false;



            }
        });

        firebaseReference=FirebaseDatabase.getInstance().getReference();
        setUpRequests(userID);


    }

    private void setUpRequests(String userID) {
        firebaseReference.child("Users").child(userID).child("requests").
                addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChildren()){
                    hasRequests=findViewById(R.id.has_requests1);
                    hasRequests.setVisibility(View.VISIBLE);
                }
                else {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String requestID = dataSnapshot.getKey();
                        getRequestDetails(requestID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getRequestDetails(String requestID) {
        firebaseReference.child("Requests").child(requestID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Request request=snapshot.getValue(Request.class);
                String traineeID,trainingID,paymentMethod;
                traineeID=request.getTraineeID();
                trainingID=request.getTrainingID();
                paymentMethod=request.getPaymentMethod();
                RequestModel requestModel=new RequestModel();
                requestModel.requestID=requestID;
                requestModel.paymentMethod=paymentMethod;
                getTraineeDetails(traineeID,trainingID,requestModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTraineeDetails(String traineeID, String trainingID,RequestModel requestModel) {
        firebaseReference.child("Users").child(traineeID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserTrainee trainee = snapshot.getValue(UserTrainee.class);
                String traineeName= trainee.getFirstName() + " "+trainee.getLastName();
                requestModel.otherUserID=traineeID;
                requestModel.otherUserName=traineeName;
                requestModel.otherUserPhone=trainee.getPhone();
                requestModel.trainingID=trainingID;
                getTrainingDetails(trainingID,requestModel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTrainingDetails(String trainingID, RequestModel requestModel) {
        firebaseReference.child("Trainings").child(trainingID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Training training=snapshot.getValue(Training.class);
                String trainingTitle,trainingDate,trainingTime;
                trainingTitle=training.getTitle();
                trainingDate=training.getDate();
                trainingTime=training.getStartTraining()+"-"+training.getEndTraining();
                requestModel.trainingTitle=trainingTitle;
                requestModel.trainingDate=trainingDate;
                requestModel.trainingTime=trainingTime;
                requests.add(requestModel);
                showRows();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRows() {
        requests_trainer_RecyclerViewAdapter adapter = new requests_trainer_RecyclerViewAdapter(this,requests);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));


    }
}