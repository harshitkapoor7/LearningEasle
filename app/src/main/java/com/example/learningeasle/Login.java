package com.example.learningeasle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    EditText email_login, password_login;
    Button loginBtn_login;
    TextView createBtn_login, forgot_password;
    ProgressBar progressBar_login;
    FirebaseAuth fAuth_login;
   // FirebaseFirestore fStore;
    FirebaseUser fUser ;
    SignInButton signin;
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    String Tag = "LoginActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
     //   fStore = FirebaseFirestore.getInstance();
        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
        progressBar_login = findViewById(R.id.progressBar_login);
        fAuth_login = FirebaseAuth.getInstance();
        loginBtn_login = findViewById(R.id.login_login);
        createBtn_login = findViewById(R.id.create_login);
        forgot_password = findViewById(R.id.forgot_password);
        signin = findViewById(R.id.googlesignin);

       /* fUser = fAuth_login.getCurrentUser();
        if(fUser!= null) {
            finish();
            startActivity(new Intent(getApplicationContext(), PickInterests.class));
        }*/
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        loginBtn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String email = email_login.getText().toString().trim();
                final String password = password_login.getText().toString().trim();


                //Check for error in entered values
                if (TextUtils.isEmpty(email)) {
                    email_login.setError("Email is Required!");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    password_login.setError("Password is Required!");
                    return;
                }

                if (password.length() < 6) {
                    password_login.setError("Password must be >= 6 characters");
                    return;
                }

                loginUser(email, password);
            }

        });

        //if signup/register button is pressed
        createBtn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

            }
        });


        //forgot password

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(new ContextThemeWrapper(v.getContext(), R.style.AlertDialogCustom));
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter email to receive reset link ");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //extract email and set reset link

                        String mail = resetMail.getText().toString();
                        fAuth_login.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! Reset link not sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });
//                passwordResetDialog.create().show();
                AlertDialog alert = passwordResetDialog.create();
                alert.show();
                //    Customising buttons for dialog
                Button p = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                p.setBackgroundColor(Color.parseColor("#222831"));
                p.setTextColor(Color.parseColor("#D90091EA"));
                Button n = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                n.setBackgroundColor(Color.parseColor("#222831"));
                n.setTextColor(Color.parseColor("#DEFFFFFF"));
            }
        });


    }

    private void signIn() {
        Intent signinintent = googleSignInClient.getSignInIntent();
        startActivityForResult(signinintent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
               Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
               handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Toast.makeText(Login.this,"Successfully Signed in",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(account);
        }catch (ApiException e){
            Toast.makeText(Login.this,"Signed in Failed",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }
    }

    private void FirebaseGoogleAuth(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        fAuth_login.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = fAuth_login.getCurrentUser();
                    startActivity(new Intent(Login.this,PickInterests.class));
                    updateUI(user);
                }else{
                    Toast.makeText(Login.this,"Failed",Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){

        }
    }
    // login function

            private void loginUser(String email, String password) {
               /* SharedPreferences preferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=preferences.edit();
                editor.putString("email_Id",email);
                editor.commit();
                progressBar_login.setVisibility(View.VISIBLE);*/

                // authenticate user
                fAuth_login.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Welcome User!!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), PickInterests.class));
                            overridePendingTransition(R.anim.slide_up, R.anim.slide_down);

                        } else if (task.getException() != null) {
                            Toast.makeText(Login.this, "Error !" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar_login.setVisibility(View.GONE);
                        }
                    }
                });

            }


        }

