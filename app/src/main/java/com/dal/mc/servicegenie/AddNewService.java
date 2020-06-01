package com.dal.mc.servicegenie;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddNewService extends AppCompatActivity {
    private EditText userNameTV, emailIdTV, serviceNameTV, addServiceComment;
    private Button addServiceBtn;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_service);

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        final String userName = user.getDisplayName();
        final String emailId = user.getEmail();

        // get data from layout
        userNameTV = findViewById(R.id.add_service_userNameVal);
        emailIdTV = findViewById(R.id.add_service_userEmailID);
        serviceNameTV = findViewById(R.id.add_service_ServiceNm);
        addServiceComment = findViewById(R.id.add_service_commentVal);
        addServiceBtn = findViewById(R.id.addServiceBtn);

        userNameTV.setText(userName);
        emailIdTV.setText(emailId);

        // on submit click save the data into database
        addServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serviceName = serviceNameTV.getText().toString();
                String comment = addServiceComment.getText().toString();

                if(serviceName.equalsIgnoreCase(" ")){
                    serviceNameTV.setError("Please enter a Service name");
                } else {
                    databaseReference = FirebaseDatabase.getInstance().getReference();
                    serviceRequest request = new serviceRequest(serviceName, userName, emailId, comment);
                    databaseReference.child("ServiceRequest").push().setValue(request);
                    new AlertDialog.Builder(AddNewService.this).setMessage("Thank you. Your request has been submitted. Our team will review the service request and get back to you.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });
    }
}