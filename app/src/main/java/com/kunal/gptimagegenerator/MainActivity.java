package com.kunal.gptimagegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText inputText;
    MaterialButton generateBtn;
    ProgressBar progressBar;
    ImageView imageView;

    public static final MediaType JSON = MediaType.get("application/json");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.input_text);
        generateBtn = findViewById(R.id.generate_btn);
        progressBar = findViewById(R.id.porgress_bar);
        imageView = findViewById(R.id.image_view);

        generateBtn.setOnClickListener((v) -> {
            String text = inputText.getText().toString().trim();
            if (text.isEmpty()) {
                inputText.setError("Text can't be empty");
                return;
            }
            callAPI(text);
        });
    }

    void callAPI(String text) {
        setProgressBar(true);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt", text);
            jsonBody.put("size", "256x256");
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer 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") // Replace with your API key
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Failed to generate Image", Toast.LENGTH_LONG).show();
                    setProgressBar(false);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    loadImage(imageUrl);
                    setProgressBar(false);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Error processing response", Toast.LENGTH_LONG).show();
                        setProgressBar(false);
                    });
                }
            }
        });
    }

    void setProgressBar(boolean inProgress) {
        runOnUiThread(() -> {
            if (inProgress) {
                progressBar.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                generateBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    void loadImage(String url) {
        runOnUiThread(() -> {
            Picasso.get().load(url).into(imageView);
        });
    }
}
