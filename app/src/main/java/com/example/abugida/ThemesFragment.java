package com.example.abugida;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.abogida.R;

public class ThemesFragment extends Fragment {

    private LinearLayout themeContainer;
    private LinearLayout previewCard;
    private LinearLayout previewPanel;
    private TextView previewTitle;
    private EditText previewInput;
    private Button applyButton;
    private KeyboardTheme selectedTheme;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_themes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        themeContainer = view.findViewById(R.id.theme_container);
        previewCard = view.findViewById(R.id.theme_preview_card);
        previewPanel = view.findViewById(R.id.theme_preview_panel);
        previewTitle = view.findViewById(R.id.theme_preview_title);
        previewInput = view.findViewById(R.id.theme_preview_input);
        applyButton = view.findViewById(R.id.theme_apply_button);

        selectedTheme = ThemeManager.loadTheme(requireContext());
        applyPreviewTheme(selectedTheme);
        updateApplyButton();

        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTheme != null) {
                    ThemeManager.saveTheme(requireContext(), selectedTheme);
                    Intent intent = new Intent(ThemeManager.ACTION_THEME_CHANGED);
                    intent.putExtra(ThemeManager.EXTRA_THEME_ID, selectedTheme.getId());
                    requireContext().sendBroadcast(intent);
                    updateApplyButton();
                    renderThemes();
                }
            }
        });

        renderThemes();
    }

    private void renderThemes() {
        if (themeContainer == null) {
            return;
        }
        themeContainer.removeAllViews();
        final KeyboardTheme currentTheme = ThemeManager.loadTheme(requireContext());

        for (final KeyboardTheme theme : ThemeManager.getThemes()) {
            Button button = new Button(requireContext());
            String label = theme.getName();
            if (currentTheme != null && theme.getId().equals(currentTheme.getId())) {
                label = label + " (current)";
            }
            button.setText(label);
            button.setAllCaps(false);

            GradientDrawable background = new GradientDrawable();
            background.setColor(theme.getKeyBackgroundPrimary());
            background.setCornerRadius(22f);
            button.setBackground(background);
            button.setTextColor(theme.getKeyTextPrimary());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.bottomMargin = 16;
            button.setLayoutParams(params);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedTheme = theme;
                    applyPreviewTheme(theme);
                    updateApplyButton();
                    Intent intent = new Intent(ThemeManager.ACTION_THEME_PREVIEW);
                    intent.putExtra(ThemeManager.EXTRA_THEME_ID, theme.getId());
                    requireContext().sendBroadcast(intent);
                }
            });

            themeContainer.addView(button);
        }
    }

    private void applyPreviewTheme(KeyboardTheme theme) {
        if (theme == null) {
            return;
        }
        if (previewPanel != null) {
            previewPanel.setBackgroundColor(theme.getKeyboardBackground());
        }
        if (previewCard != null) {
            previewCard.setBackgroundColor(theme.getKeyboardBackground());
        }
        if (previewTitle != null) {
            previewTitle.setTextColor(theme.getKeyTextPrimary());
        }
        if (previewInput != null) {
            previewInput.setTextColor(theme.getKeyTextPrimary());
            previewInput.setHintTextColor(theme.getKeyTextSecondary());
            previewInput.setBackgroundColor(theme.getKeyBackgroundPrimary());
        }
        if (applyButton != null) {
            GradientDrawable background = new GradientDrawable();
            background.setColor(theme.getKeyBackgroundSecondary());
            background.setCornerRadius(22f);
            applyButton.setBackground(background);
            applyButton.setTextColor(theme.getKeyTextSecondary());
        }
    }

    private void updateApplyButton() {
        if (applyButton == null) {
            return;
        }
        KeyboardTheme currentTheme = ThemeManager.loadTheme(requireContext());
        boolean isCurrent = currentTheme != null
                && selectedTheme != null
                && currentTheme.getId().equals(selectedTheme.getId());
        applyButton.setEnabled(!isCurrent);
        applyButton.setText(isCurrent ? "Applied" : "Apply Theme");
    }
}
