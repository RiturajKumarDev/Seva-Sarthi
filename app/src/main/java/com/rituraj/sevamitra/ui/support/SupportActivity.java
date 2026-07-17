package com.rituraj.sevamitra.ui.support;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rituraj.sevamitra.R;
import com.rituraj.sevamitra.models.UserData;

import java.util.*;

public class SupportActivity extends AppCompatActivity {

    // Toolbar
    private Toolbar toolbar;

    // Support Options Cards
    private CardView cardFAQ, cardLiveChat, cardEmailSupport;
    private CardView cardCallSupport, cardKnowledgeBase, cardFeedback;

    // FAQ Section
    private LinearLayout faqLayout;
    private TextView tvFAQCount;

    // Quick Actions
    private LinearLayout btnWhatsApp, btnEmail, btnCall, btnWebsite;

    // Data
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        // Support Options
        cardFAQ = findViewById(R.id.cardFAQ);
        cardLiveChat = findViewById(R.id.cardLiveChat);
        cardEmailSupport = findViewById(R.id.cardEmailSupport);
        cardCallSupport = findViewById(R.id.cardCallSupport);
        cardKnowledgeBase = findViewById(R.id.cardKnowledgeBase);
        cardFeedback = findViewById(R.id.cardFeedback);

        // FAQ
        faqLayout = findViewById(R.id.faqLayout);
        tvFAQCount = findViewById(R.id.tvFAQCount);

        // Quick Actions
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        btnEmail = findViewById(R.id.btnEmail);
        btnCall = findViewById(R.id.btnCall);
        btnWebsite = findViewById(R.id.btnWebsite);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Support");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupClickListeners() {
        // Support Option Cards
        cardFAQ.setOnClickListener(v -> showFAQDialog());
        cardLiveChat.setOnClickListener(v -> startLiveChat());
        cardEmailSupport.setOnClickListener(v -> sendEmailSupport());
        cardCallSupport.setOnClickListener(v -> callSupport());
        cardKnowledgeBase.setOnClickListener(v -> showKnowledgeBase());
        cardFeedback.setOnClickListener(v -> submitFeedback());

        // Quick Actions
        btnWhatsApp.setOnClickListener(v -> openWhatsApp());
        btnEmail.setOnClickListener(v -> sendEmailSupport());
        btnCall.setOnClickListener(v -> callSupport());
        btnWebsite.setOnClickListener(v -> openWebsite());

        // FAQ Items (dynamically added)
        setupFAQItems();
    }

    private void showFAQDialog() {
        String[] faqs = {
                "How to register as a Worker?",
                "How to create an issue?",
                "How to assign work to a worker?",
                "How to track issue status?",
                "How to generate bills?",
                "How to contact support?",
                "How to update profile?"
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle("Frequently Asked Questions")
                .setItems(faqs, (dialog, which) -> {
                    showFAQAnswer(which);
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showFAQAnswer(int index) {
        String answer = "";
        switch (index) {
            case 0:
                answer = "Go to Registration page, select 'Worker' user type, fill in your details, and submit. You'll be verified by the Founder.";
                break;
            case 1:
                answer = "Login as SevaMitra, go to Issues tab, click 'Create Issue', fill all details and submit.";
                break;
            case 2:
                answer = "Select the issue, click 'Assign Worker', choose from available workers and assign.";
                break;
            case 3:
                answer = "Go to Issues tab, find your issue, click on it to see complete status and timeline.";
                break;
            case 4:
                answer = "After work completion, go to Bills section, generate bill with all work details.";
                break;
            case 5:
                answer = "Use this Support page to raise tickets, or use WhatsApp/Call/Email options below.";
                break;
            case 6:
                answer = "Go to Profile section, click Edit button, update your information and save.";
                break;
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Answer")
                .setMessage(answer)
                .setPositiveButton("OK", null)
                .show();
    }

    private void setupFAQItems() {
        // Add FAQ items dynamically
        String[] faqQuestions = {
                "How to get started with SevaMitra?",
                "What are the user types in SevaMitra?",
                "How to track my work progress?"
        };

        for (String question : faqQuestions) {
            TextView faqItem = new TextView(this);
            faqItem.setText("• " + question);
            faqItem.setTextColor(getColor(R.color.logo_white));
            faqItem.setTextSize(14);
            faqItem.setPadding(0, 12, 0, 12);
            faqItem.setClickable(true);
            faqItem.setOnClickListener(v -> {
                // Show answer
                String answer = "For " + question + ", please refer to the FAQ section or raise a support ticket.";
                Toast.makeText(this, "FAQ: " + question, Toast.LENGTH_SHORT).show();
            });
            faqLayout.addView(faqItem);
        }

        tvFAQCount.setText(faqQuestions.length + " FAQs");
    }

    private void startLiveChat() {
        openWhatsApp();
    }

    private void sendEmailSupport() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:sargamenterprises@zohomail.in"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - " + (currentUser != null ? currentUser.getDisplayName() : "User"));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello Support Team,\n\n");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email client installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void callSupport() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:9135302955"));
        startActivity(callIntent);
    }

    private void openWhatsApp() {
        try {
            String url = "https://wa.me/919135302955?text=Hi, I need support for SevaMitra app.";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWebsite() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://riturajkumardev.github.io/SevaMitra---Privacy-Policy/"));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open website", Toast.LENGTH_SHORT).show();
        }
    }

    private void showKnowledgeBase() {
        openWebsite();
    }

    private void submitFeedback() {
        String packageName = getPackageName();
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
            intent.setPackage("com.android.vending");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
            startActivity(intent);
        }
    }
}