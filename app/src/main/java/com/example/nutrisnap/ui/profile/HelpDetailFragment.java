package com.example.nutrisnap.ui.profile;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.nutrisnap.R;

public class HelpDetailFragment extends Fragment {

    private static final String ARG_CONTENT = "help_content";

    public static HelpDetailFragment newInstance(String content) {
        HelpDetailFragment fragment = new HelpDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_detail, container, false);

        ImageView btnBack = view.findViewById(R.id.btn_back_help_detail);
        TextView tvContent = view.findViewById(R.id.tv_help_content);

        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        if (getArguments() != null) {
            String content = getArguments().getString(ARG_CONTENT);
            if (content != null) {
                tvContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            }
        }

        return view;
    }
}