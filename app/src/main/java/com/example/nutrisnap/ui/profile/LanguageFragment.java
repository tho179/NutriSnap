package com.example.nutrisnap.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.nutrisnap.MainActivity;
import com.example.nutrisnap.R;
import com.example.nutrisnap.utils.LocaleHelper;

public class LanguageFragment extends Fragment {

    private String selectedLanguageCode = "en";
    private String selectedLanguageName = "English";
    private TextView tvHeaderTitle;
    private ImageView imgCheckEn, imgCheckVi;
    private TextView tvLangEn, tvLangVi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language, container, false);

        tvHeaderTitle = view.findViewById(R.id.tv_language_title);
        imgCheckEn = view.findViewById(R.id.img_check_en);
        imgCheckVi = view.findViewById(R.id.img_check_vi);
        
        tvLangEn = view.findViewById(R.id.tv_lang_en);
        tvLangVi = view.findViewById(R.id.tv_lang_vi);

        RelativeLayout layoutEn = view.findViewById(R.id.layout_lang_en);
        RelativeLayout layoutVi = view.findViewById(R.id.layout_lang_vi);
        
        ImageView btnBack = view.findViewById(R.id.btn_back_language);
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_language);

        // Khởi tạo trạng thái tick dựa trên ngôn ngữ đang sử dụng
        String currentLang = LocaleHelper.getLanguage(requireContext());
        if (currentLang.equals("vi")) {
            updateSelection("vi", getString(R.string.lang_vi));
        } else {
            updateSelection("en", getString(R.string.lang_en));
        }

        layoutEn.setOnClickListener(v -> updateSelection("en", getString(R.string.lang_en)));
        layoutVi.setOnClickListener(v -> updateSelection("vi", getString(R.string.lang_vi)));

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        btnSave.setOnClickListener(v -> {
            // Thay đổi ngôn ngữ hệ thống
            LocaleHelper.setLocale(requireContext(), selectedLanguageCode);
            
            Toast.makeText(requireContext(), getString(R.string.language_changed, selectedLanguageName), Toast.LENGTH_SHORT).show();
            
            // Khởi động lại Activity chính để áp dụng ngôn ngữ mới cho toàn bộ App
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    private void updateSelection(String code, String name) {
        selectedLanguageCode = code;
        selectedLanguageName = name;
        tvHeaderTitle.setText(name);

        // Reset all
        imgCheckEn.setVisibility(View.GONE);
        imgCheckVi.setVisibility(View.GONE);
        
        tvLangEn.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvLangVi.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set selected
        if (code.equals("en")) {
            imgCheckEn.setVisibility(View.VISIBLE);
            tvLangEn.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (code.equals("vi")) {
            imgCheckVi.setVisibility(View.VISIBLE);
            tvLangVi.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}
