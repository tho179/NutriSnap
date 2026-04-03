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
import org.json.JSONArray;
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
                        fetchDetailedNutritionalInfo(jsonString, listener);
                    } catch (Exception e) {
                        listener.onError("Lỗi xử lý dữ liệu AI: " + e.getMessage());
                    }
                } else {
                    listener.onError("Không thể nhận diện hình ảnh. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.onError("Lỗi kết nối server AI: " + t.getMessage());
            }
        });
    }

    private void fetchDetailedNutritionalInfo(String segmentationJson, AIOnResultListener listener) throws Exception {
        JSONObject segmentationObj = new JSONObject(segmentationJson);
        
        // Tạo body cho request lấy thông tin dinh dưỡng chi tiết
        // LogMeal yêu cầu truyền lại segmentation results để tính toán dinh dưỡng
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                segmentationJson
        );

        logMealService.getNutritionalInfo(Config.LOGMEAL_API_TOKEN, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String nutritionJson = response.body().string();
                        MealRecord mealRecord = parseFullMealData(segmentationObj, new JSONObject(nutritionJson));
                        listener.onSuccess(mealRecord);
                    } else {
                        // Nếu không lấy được nutrition chi tiết, vẫn trả về kết quả nhận diện ban đầu
                        listener.onSuccess(parseLogMealJSON(segmentationJson));
                    }
                } catch (Exception e) {
                    listener.onError("Lỗi phân tích dinh dưỡng: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fallback về parsing cơ bản nếu lỗi mạng khi gọi API nutrition
                try {
                    listener.onSuccess(parseLogMealJSON(segmentationJson));
                } catch (Exception e) {
                    listener.onError("Lỗi AI: " + t.getMessage());
                }
            }
        });
    }

    private MealRecord parseFullMealData(JSONObject segmentationObj, JSONObject nutritionObj) throws Exception {
        List<FoodItem> items = new ArrayList<>();
        JSONArray segmentationResults = segmentationObj.optJSONArray("segmentation_results");
        
        if (segmentationResults != null) {
            for (int i = 0; i < segmentationResults.length(); i++) {
                JSONObject segment = segmentationResults.getJSONObject(i);
                JSONArray recognitionResults = segment.optJSONArray("recognition_results");
                
                if (recognitionResults != null && recognitionResults.length() > 0) {
                    JSONObject topResult = recognitionResults.getJSONObject(0);
                    String foodName = topResult.optString("name", "Món ăn lạ");
                    
                    // Lấy dinh dưỡng từ nutritionObj (LogMeal trả về thông tin tổng hợp hoặc theo món)
                    // Ở đây ta trích xuất từ nutritional_info của món ăn đầu tiên hoặc tổng thể
                    JSONObject nutrition = nutritionObj.optJSONObject("nutritional_info");
                    
                    int calories = 200;
                    double protein = 5, carbs = 25, fat = 5;

                    if (nutrition != null) {
                        calories = nutrition.optInt("calories", calories);
                        JSONObject nutrients = nutrition.optJSONObject("total_nutrients");
                        if (nutrients != null) {
                            protein = nutrients.optJSONObject("PROCNT") != null ? nutrients.optJSONObject("PROCNT").optDouble("quantity", 5) : 5;
                            carbs = nutrients.optJSONObject("CHOCDF") != null ? nutrients.optJSONObject("CHOCDF").optDouble("quantity", 25) : 25;
                            fat = nutrients.optJSONObject("FAT") != null ? nutrients.optJSONObject("FAT").optDouble("quantity", 5) : 5;
                        }
                    }
                    
                    items.add(new FoodItem(foodName, 1, calories, protein, carbs, fat));
                }
            }
        }
        
        return new MealRecord("Chưa xác định", System.currentTimeMillis(), items);
    }

    public void addMeal(String userId, String date, MealRecord meal, MealCallback callback) {
        WriteBatch batch = db.batch();
        DocumentReference mealRef = db.collection("users").document(userId)
                .collection("daily_logs").document(date)
                .collection("meals").document();

        meal.setId(mealRef.getId());
        batch.set(mealRef, meal);
        
        updateDailySummary(batch, userId, date, meal.getFoods(), 1, meal.getMealType());

        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private void updateDailySummary(WriteBatch batch, String userId, String date, List<FoodItem> foods, int multiplier, String mealType) {
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

        String mealKcalField = "";
        switch (mealType) {
            case "Sáng": mealKcalField = "breakfastKcal"; break;
            case "Trưa": mealKcalField = "lunchKcal"; break;
            case "Tối": mealKcalField = "dinnerKcal"; break;
            case "Snacks": mealKcalField = "snackKcal"; break;
        }
        if (!mealKcalField.isEmpty()) {
            update.put(mealKcalField, FieldValue.increment(cal));
        }

        batch.set(dailyLogRef, update, SetOptions.merge());
    }

    private MealRecord parseLogMealJSON(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        List<FoodItem> items = new ArrayList<>();
        JSONArray segmentationResults = obj.optJSONArray("segmentation_results");
        if (segmentationResults != null) {
            for (int i = 0; i < segmentationResults.length(); i++) {
                JSONObject segment = segmentationResults.getJSONObject(i);
                JSONArray recognitionResults = segment.optJSONArray("recognition_results");
                if (recognitionResults != null && recognitionResults.length() > 0) {
                    items.add(new FoodItem(recognitionResults.getJSONObject(0).optString("name", "Unknown"), 1, 0, 0, 0, 0));
                }
            }
        }
        return new MealRecord("Chưa xác định", System.currentTimeMillis(), items);
    }
}
