package com.rituraj.sevamitra.ui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.rituraj.sevamitra.MainActivity;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.LanguageModel;
import com.rituraj.sevamitra.models.User;

import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private ProgressDialog progressDialog;
    private GoogleSignInClient googleSignInClient;
    private final ArrayList<LanguageModel> languages = new ArrayList<>();
    private static final int RC_SIGN_IN = 1001;
    private SignInButton googleSignInButton;
    private Spinner languageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        languageSetting();
    }

    private void initViews() {
        googleSignInButton = findViewById(R.id.googleSignInButton);
        languageSpinner = findViewById(R.id.languageSpinner);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Login");
        progressDialog.setMessage("Login to your Account");
        progressDialog.setCancelable(false);

        googleSignInButton.setOnClickListener((v) -> {
            progressDialog.show();
            saveLanguage(LoginActivity.this, languages.get(languageSpinner.getSelectedItemPosition()));
            signInSetting();
        });
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void languageSetting() {
        Spinner spinner = findViewById(R.id.languageSpinner);
        languages.add(new LanguageModel("English", "en"));
        languages.add(new LanguageModel("Afrikaans", "af"));
        languages.add(new LanguageModel("Albanian", "sq"));
        languages.add(new LanguageModel("Arabic", "ar"));
        languages.add(new LanguageModel("Belarusian", "be"));
        languages.add(new LanguageModel("Bengali", "bn"));
        languages.add(new LanguageModel("Bulgarian", "bg"));
        languages.add(new LanguageModel("Catalan", "ca"));
        languages.add(new LanguageModel("Chinese", "zh"));
        languages.add(new LanguageModel("Croatian", "hr"));
        languages.add(new LanguageModel("Czech", "cs"));
        languages.add(new LanguageModel("Danish", "da"));
        languages.add(new LanguageModel("Dutch", "nl"));
        languages.add(new LanguageModel("Esperanto", "eo"));
        languages.add(new LanguageModel("Estonian", "et"));
        languages.add(new LanguageModel("Finnish", "fi"));
        languages.add(new LanguageModel("French", "fr"));
        languages.add(new LanguageModel("Galician", "gl"));
        languages.add(new LanguageModel("Georgian", "ka"));
        languages.add(new LanguageModel("German", "de"));
        languages.add(new LanguageModel("Greek", "el"));
        languages.add(new LanguageModel("Gujarati", "gu"));
        languages.add(new LanguageModel("Haitian Creole", "ht"));
        languages.add(new LanguageModel("Hebrew", "he"));
        languages.add(new LanguageModel("Hindi", "hi"));
        languages.add(new LanguageModel("Hungarian", "hu"));
        languages.add(new LanguageModel("Icelandic", "is"));
        languages.add(new LanguageModel("Indonesian", "id"));
        languages.add(new LanguageModel("Irish", "ga"));
        languages.add(new LanguageModel("Italian", "it"));
        languages.add(new LanguageModel("Japanese", "ja"));
        languages.add(new LanguageModel("Kannada", "kn"));
        languages.add(new LanguageModel("Korean", "ko"));
        languages.add(new LanguageModel("Latvian", "lv"));
        languages.add(new LanguageModel("Lithuanian", "lt"));
        languages.add(new LanguageModel("Macedonian", "mk"));
        languages.add(new LanguageModel("Malay", "ms"));
        languages.add(new LanguageModel("Maltese", "mt"));
        languages.add(new LanguageModel("Marathi", "mr"));
        languages.add(new LanguageModel("Norwegian", "no"));
        languages.add(new LanguageModel("Persian", "fa"));
        languages.add(new LanguageModel("Polish", "pl"));
        languages.add(new LanguageModel("Portuguese", "pt"));
        languages.add(new LanguageModel("Romanian", "ro"));
        languages.add(new LanguageModel("Russian", "ru"));
        languages.add(new LanguageModel("Slovak", "sk"));
        languages.add(new LanguageModel("Slovenian", "sl"));
        languages.add(new LanguageModel("Spanish", "es"));
        languages.add(new LanguageModel("Swahili", "sw"));
        languages.add(new LanguageModel("Swedish", "sv"));
        languages.add(new LanguageModel("Tagalog", "tl"));
        languages.add(new LanguageModel("Tamil", "ta"));
        languages.add(new LanguageModel("Telugu", "te"));
        languages.add(new LanguageModel("Thai", "th"));
        languages.add(new LanguageModel("Turkish", "tr"));
        languages.add(new LanguageModel("Ukrainian", "uk"));
        languages.add(new LanguageModel("Urdu", "ur"));
        languages.add(new LanguageModel("Vietnamese", "vi"));
        languages.add(new LanguageModel("Welsh", "cy"));

        ArrayAdapter<LanguageModel> arrayAdapter = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_list_item_1, languages);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
    }

    private void saveLanguage(Context context, LanguageModel language) {
        SharedPreferences prefs =
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("language_name", language.name);
        editor.putString("language_code", language.code);
        editor.apply();
    }

    public void signInSetting() {
        Intent singnIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(singnIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RC_SIGN_IN == requestCode) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account.getIdToken());
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "SignIn Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                User user1 = new User();
                user1.setUserId(user.getUid());
                user1.setEmail(user.getEmail());
                user1.setUserName(user.getDisplayName());
                user1.setProfileUrl(user.getPhotoUrl().toString());
                database.getReference().child("Users").child(user.getUid()).child("userName").setValue(user1.getUserName());
                database.getReference().child("Users").child(user.getUid()).child("email").setValue(user1.getEmail());
                database.getReference().child("Users").child(user.getUid()).child("profileUrl").setValue(user1.getProfileUrl());
                database.getReference().child("Users").child(user.getUid()).child("userId").setValue(user1.getUserId());
                Toast.makeText(LoginActivity.this, "Login", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
                finish();
            } else Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}