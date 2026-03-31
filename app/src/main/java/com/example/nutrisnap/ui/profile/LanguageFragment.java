package com.example.nutrisnap.ui.profile;

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
import com.example.nutrisnap.R;

public class LanguageFragment extends Fragment {

    private String selectedLanguage = "English";
    private TextView tvHeaderTitle;
    private ImageView imgCheckEn, imgCheckVi, imgCheckJa;
    private TextView tvLangEn, tvLangVi, tvLangJa;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_language, container, false);

        tvHeaderTitle = view.findViewById(R.id.tv_language_title);
        imgCheckEn = view.findViewById(R.id.img_check_en);
        imgCheckVi = view.findViewById(R.id.img_check_vi);
        imgCheckJa = view.findViewById(R.id.img_check_ja);
        
        tvLangEn = view.findViewById(R.id.tv_lang_en);
        tvLangVi = view.findViewById(R.id.tv_lang_vi);
        tvLangJa = view.findViewById(R.id.tv_lang_ja);

        RelativeLayout layoutEn = view.findViewById(R.id.layout_lang_en);
        RelativeLayout layoutVi = view.findViewById(R.id.layout_lang_vi);
        RelativeLayout layoutJa = view.findViewById(R.id.layout_lang_ja);
        
        ImageView btnBack = view.findViewById(R.id.btn_back_language);
        AppCompatButton btnSave = view.findViewById(R.id.btn_save_language);

        layoutEn.setOnClickListener(v -> updateSelection("English"));
        layoutVi.setOnClickListener(v -> updateSelection("Tiếng Việt"));
        layoutJa.setOnClickListener(v -> updateSelection("日本語 (Japanese)"));

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnSave.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Language saved: " + selectedLanguage, Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    private void updateSelection(String lang) {
        selectedLanguage = lang;
        tvHeaderTitle.setText(lang);

        // Reset all
        imgCheckEn.setVisibility(View.GONE);
        imgCheckVi.setVisibility(View.GONE);
        imgCheckJa.setVisibility(View.GONE);
        
        tvLangEn.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvLangVi.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvLangJa.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set selected
        if (lang.equals("English")) {
            imgCheckEn.setVisibility(View.VISIBLE);
            tvLangEn.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (lang.equals("Tiếng Việt")) {
            imgCheckVi.setVisibility(View.VISIBLE);
            tvLangVi.setTypeface(null, android.graphics.Typeface.BOLD);
        } else if (lang.equals("日本語 (Japanese)")) {
            imgCheckJa.setVisibility(View.VISIBLE);
            tvLangJa.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }
}