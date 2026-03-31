package com.example.nutrisnap.ui.chatbot;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;

public class ChatbotFragment extends Fragment {

    private LinearLayout layoutChatContainer;
    private ScrollView scrollChat;
    private EditText edtChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatbot, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back);
        edtChat = view.findViewById(R.id.edt_chat_message);
        ImageView btnSend = view.findViewById(R.id.btn_send);
        layoutChatContainer = view.findViewById(R.id.layout_chat_container);
        scrollChat = view.findViewById(R.id.scroll_chat);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                hideKeyboard();
                if (getActivity() instanceof MainActivity) {
                    // Gọi phương thức điều hướng về Home trong MainActivity
                    ((MainActivity) getActivity()).navigateToHome();
                } else if (getActivity() != null) {
                    // Fallback
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
                    addUserMessage(msg);
                    edtChat.setText("");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) {
                            addBotMessage("I am processing your request about: " + msg);
                        }
                    }, 1000);
                }
            });
        }

        return view;
    }

    private void addUserMessage(String message) {
        if (getContext() == null) return;
        View userView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_user, layoutChatContainer, false);
        TextView tvMessage = userView.findViewById(R.id.tv_chat_message_user);
        tvMessage.setText(message);
        layoutChatContainer.addView(userView);
        scrollToBottom();
    }

    private void addBotMessage(String message) {
        if (getContext() == null) return;
        View botView = LayoutInflater.from(getContext()).inflate(R.layout.item_chat_bot, layoutChatContainer, false);
        TextView tvMessage = botView.findViewById(R.id.tv_chat_message_bot);
        tvMessage.setText(message);
        layoutChatContainer.addView(botView);
        scrollToBottom();
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