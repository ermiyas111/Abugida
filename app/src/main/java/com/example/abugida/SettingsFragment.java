package com.example.abugida;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.abogida.R;

public class SettingsFragment extends Fragment {

    private SwitchCompat switchSound;
    private SwitchCompat switchSuggestions;
    private SwitchCompat switchKeyPreview;
    private SwitchCompat switchAutoPick;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switchSound = view.findViewById(R.id.switch_sound);
        switchSuggestions = view.findViewById(R.id.switch_suggestions);
        switchKeyPreview = view.findViewById(R.id.switch_key_preview);
        switchAutoPick = view.findViewById(R.id.switch_auto_pick);

        KeyboardSettings settings = KeyboardSettings.load(requireContext());
        switchSound.setChecked(settings.isSoundOnKeypress());
        switchSuggestions.setChecked(settings.isShowSuggestions());
        switchKeyPreview.setChecked(settings.isShowKeyPreview());
        switchAutoPick.setChecked(settings.isAutoPickSuggestionOnSpace());

        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                persistSettings();
            }
        };

        switchSound.setOnCheckedChangeListener(listener);
        switchSuggestions.setOnCheckedChangeListener(listener);
        switchKeyPreview.setOnCheckedChangeListener(listener);
        switchAutoPick.setOnCheckedChangeListener(listener);
    }

    private void persistSettings() {
        KeyboardSettings.save(
                requireContext(),
                switchSound.isChecked(),
                switchSuggestions.isChecked(),
                switchKeyPreview.isChecked(),
                switchAutoPick.isChecked()
        );
    }
}
