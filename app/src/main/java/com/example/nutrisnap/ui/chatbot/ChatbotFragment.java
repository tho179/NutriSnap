package com.example.nutrisnap.ui.chatbot;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class ChatbotFragment extends Fragment {

    private LinearLayout layoutChatContainer;
    private ScrollView scrollChat;
    private EditText edtChat;

    // Các biến phục vụ gọi API Streaming
    private OkHttpClient client;
    private EventSource.Factory eventSourceFactory;
    private String currentSessionId;

    // THAY ĐỔI TẠI ĐÂY: Dùng IP của máy tính (Gõ ipconfig trong cmd để xem IPv4)
    private static final String SERVER_IP = "192.168.0.122";
    private static final String BASE_URL = "http://" + SERVER_IP + ":8080/api/chat/stream";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        edtChat = view.findViewById(R.id.edt_chat_message);
        ImageView btnSend = view.findViewById(R.id.btn_send);
        layoutChatContainer = view.findViewById(R.id.layout_chat_container);
        scrollChat = view.findViewById(R.id.scroll_chat);

        // Khởi tạo Client mạng (có time out dài để đợi AI nghĩ)
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // Không giới hạn thời gian đọc stream
                .build();
        eventSourceFactory = EventSources.createFactory(client);

        // Tạo một ID duy nhất cho đoạn chat này khi mới mở trang
        currentSessionId = UUID.randomUUID().toString();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                hideKeyboard();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToHome();
                } else if (getActivity() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        if (edtChat != null) {
            edtChat.requestFocus();
            new Handler(Looper.getMainLooper()).postDelayed(this::showKeyboard, 300);
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                String msg = edtChat.getText().toString().trim();
                if (!msg.isEmpty()) {
                    // 1. In tin nhắn user lên màn hình
                    addUserMessage(msg);
                    edtChat.setText("");

                    // 2. Tạo sẵn 1 khung tin nhắn Bot trống rỗng
                    TextView currentBotTextView = addBotMessage("");

                    // 3. Gọi API lấy dữ liệu Real-time
                    streamChatFromBackend(msg, currentBotTextView);
                }
            });
        }

        return view;
    }

    private void streamChatFromBackend(String userMessage, TextView botTextView) {
        // Tạo chuỗi JSON gửi đi
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("sessionId", currentSessionId);
            jsonBody.put("userId", "android_user_test"); // Sau này lấy ID thật từ Firebase Auth
            jsonBody.put("message", userMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Accept", "text/event-stream") // Bắt buộc cho SSE
                .post(body)
                .build();

        // Lắng nghe dữ liệu chảy về
        eventSourceFactory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(@NonNull EventSource eventSource, @NonNull Response response) {
                Log.d("ChatbotFragment", "SSE Connection Opened");
            }

            @Override
            public void onEvent(@Nullable EventSource eventSource, @Nullable String id, @Nullable String type, @NonNull String data) {
                // onEvent chạy ngầm, muốn cập nhật UI (TextView) phải đẩy lên Main Thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            // Dữ liệu giờ là JSON, ta bóc key "text" ra để lấy chữ nguyên bản
                            JSONObject json = new JSONObject(data);
                            if (json.has("text")) {
                                botTextView.append(json.getString("text")); // Nối chữ
                                scrollToBottom();         // Kéo cuộn màn hình xuống
                            }
                        } catch (JSONException e) {
                            // Nếu data không phải JSON, có thể là text thô
                            botTextView.append(data);
                            scrollToBottom();
                        }
                    });
                }
            }

            @Override
            public void onClosed(@NonNull EventSource eventSource) {
                Log.d("ChatbotFragment", "SSE Connection Closed");
            }

            @Override
            public void onFailure(@NonNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                Log.e("ChatbotFragment", "SSE Error", t);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi kết nối AI!", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void addUserMessage(String message) {
        if (getContext() == null) return;
        View userView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_user, layoutChatContainer, false);
        TextView tvMessage = userView.findViewById(R.id.tv_chat_message_user);
        tvMessage.setText(message);
        layoutChatContainer.addView(userView);
        scrollToBottom();
    }

    // Đã thay đổi: Trả về đối tượng TextView để hàm bên trên có thể bắn chữ vào
    private TextView addBotMessage(String message) {
        if (getContext() == null) return null;
        View botView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_bot, layoutChatContainer, false);
        TextView tvMessage = botView.findViewById(R.id.tv_chat_message_bot);
        tvMessage.setText(message);
        layoutChatContainer.addView(botView);
        scrollToBottom();
        return tvMessage;
    }

    private void scrollToBottom() {
        if (scrollChat != null) {
            scrollChat.post(() -> scrollChat.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void showKeyboard() {
        if (edtChat != null && isAdded()) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(edtChat, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void hideKeyboard() {
        if (edtChat != null && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(edtChat.getWindowToken(), 0);
            }
        }
    }
}
