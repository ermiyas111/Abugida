package com.example.abugida;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyboardSettings {
    private static final String PREFS_NAME = "keyboard_prefs";
    private static final String PREF_SOUND_ON = "sound_on_keypress";
    private static final String PREF_SHOW_SUGGESTIONS = "show_suggestions";
    private static final String PREF_SHOW_KEY_PREVIEW = "show_key_preview";
    private static final String PREF_AUTO_PICK = "auto_pick_suggestion";

    private boolean soundOnKeypress;
    private boolean showSuggestions;
    private boolean showKeyPreview;
    private boolean autoPickSuggestionOnSpace;

    public KeyboardSettings(boolean soundOnKeypress,
                            boolean showSuggestions,
                            boolean showKeyPreview,
                            boolean autoPickSuggestionOnSpace) {
        this.soundOnKeypress = soundOnKeypress;
        this.showSuggestions = showSuggestions;
        this.showKeyPreview = showKeyPreview;
        this.autoPickSuggestionOnSpace = autoPickSuggestionOnSpace;
    }

    public boolean isSoundOnKeypress() {
        return soundOnKeypress;
    }

    public boolean isShowSuggestions() {
        return showSuggestions;
    }

    public boolean isShowKeyPreview() {
        return showKeyPreview;
    }

    public boolean isAutoPickSuggestionOnSpace() {
        return autoPickSuggestionOnSpace;
    }

    public static KeyboardSettings load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean soundOnKeypress = prefs.getBoolean(PREF_SOUND_ON, true);
        boolean showSuggestions = prefs.getBoolean(PREF_SHOW_SUGGESTIONS, true);
        boolean showKeyPreview = prefs.getBoolean(PREF_SHOW_KEY_PREVIEW, false);
        boolean autoPickSuggestion = prefs.getBoolean(PREF_AUTO_PICK, false);
        return new KeyboardSettings(soundOnKeypress, showSuggestions, showKeyPreview, autoPickSuggestion);
    }

    public static void save(Context context,
                            boolean soundOnKeypress,
                            boolean showSuggestions,
                            boolean showKeyPreview,
                            boolean autoPickSuggestionOnSpace) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(PREF_SOUND_ON, soundOnKeypress)
                .putBoolean(PREF_SHOW_SUGGESTIONS, showSuggestions)
                .putBoolean(PREF_SHOW_KEY_PREVIEW, showKeyPreview)
                .putBoolean(PREF_AUTO_PICK, autoPickSuggestionOnSpace)
                .apply();
    }
}
