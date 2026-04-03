package com.example.nutrisnap.controller;

import com.example.nutrisnap.model.AIOnResultListener;
import com.example.nutrisnap.model.FoodItem;
import com.example.nutrisnap.model.MealCallback;
import com.example.nutrisnap.model.MealRecord;
import com.example.nutrisnap.service.LogMealService;
import com.example.nutrisnap.utils.Config;
import com.google.firebase.firestore.*;
import java.io.File;
import java.util.*;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MealController {
    private FirebaseFirestore db;
    private LogMealService logMealService;

    public MealController() {
        db = FirebaseFirestore.getInstance();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.LOGMEAL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        logMealService = retrofit.create(LogMealService.class);
    }

    public void recognizeMealFromImage(File imageFile, AIOnResultListener listener) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        logMealService.analyzeImage(Config.LOGMEAL_API_TOKEN, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        MealRecord recognizedMeal = parseLogMealJSON(jsonString);
                        listener.onSuccess(recognizedMeal);
                    } catch (Exception e) {
                        listener.onError("Lỗi xử lý dữ liệu AI");
                    }
                } else {
                    listener.onError("Không thể nhận diện hình ảnh");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onError("Lỗi kết nối server AI: " + t.getMessage());
            }
        });
    }

    public void addMeal(String userId, String date, MealRecord meal, MealCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference mealRef = db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .collection("meals").document();

        batch.set(mealRef, meal);
        updateDailySummary(batch, userId, date, meal.getFoods(), 1);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteFoodFromMeal(String userId, String date, String mealId, FoodItem foodToDelete, MealCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference mealRef = db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .collection("meals").document(mealId);

        batch.update(mealRef, "foods", FieldValue.arrayRemove(foodToDelete));
        updateDailySummary(batch, userId, date, Collections.singletonList(foodToDelete), -1);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void addFoodToExistingMeal(String userId, String date, String mealId, FoodItem newFood, MealCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference mealRef = db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .collection("meals").document(mealId);

        batch.update(mealRef, "foods", FieldValue.arrayUnion(newFood));
        updateDailySummary(batch, userId, date, Collections.singletonList(newFood), 1);

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private void updateDailySummary(WriteBatch batch, String userId, String date, List<FoodItem> foods, int multiplier) {
        double cal = 0, pro = 0, carb = 0, fat = 0;
        for (FoodItem f : foods) {
            cal += f.getCalories() * multiplier;
            pro += f.getProtein() * multiplier;
            carb += f.getCarbs() * multiplier;
            fat += f.getFat() * multiplier;
        }

        DocumentReference dailyLogRef = db.collection("users").document(userId)
                .collection("daily_logs").document(date);

        Map<String, Object> update = new HashMap<>();
        update.put("totalCaloriesIn", FieldValue.increment(cal));
        update.put("totalProtein", FieldValue.increment(pro));
        update.put("totalCarbs", FieldValue.increment(carb));
        update.put("totalFat", FieldValue.increment(fat));

        batch.set(dailyLogRef, update, SetOptions.merge());
    }

    private MealRecord parseLogMealJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        String foodName = obj.getJSONArray("foodName").getString(0);
        List<FoodItem> items = new ArrayList<>();
        items.add(new FoodItem(foodName, 1, 400, 20, 50, 10));
        return new MealRecord("Chưa xác định", System.currentTimeMillis(), items);
    }
}
