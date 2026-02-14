package com.example.abugida;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThemeManager {
    private static final String PREFS_NAME = "keyboard_prefs";
    private static final String PREF_THEME_ID = "theme_id";
    private static final String DEFAULT_THEME_ID = "sand";
    public static final String ACTION_THEME_CHANGED = "com.example.abugida.ACTION_THEME_CHANGED";

    private static final List<KeyboardTheme> THEMES = buildThemes();

    private static List<KeyboardTheme> buildThemes() {
        List<KeyboardTheme> themes = new ArrayList<>();

        themes.add(new KeyboardTheme(
                "sand",
                "Sand",
                Color.parseColor("#F2E9E1"),
                Color.parseColor("#EAD7C2"),
                Color.parseColor("#CBB9A4"),
                Color.parseColor("#2B2B2B"),
                Color.parseColor("#1F1F1F"),
                Color.parseColor("#F7EFE7"),
                Color.parseColor("#3A2D21"),
                Color.parseColor("#B34A1E"),
                Color.parseColor("#6A5548"),
                Color.parseColor("#6A5548")
        ));

        themes.add(new KeyboardTheme(
                "night",
                "Night",
                Color.parseColor("#121417"),
                Color.parseColor("#1E242B"),
                Color.parseColor("#2A323B"),
                Color.parseColor("#E9EEF2"),
                Color.parseColor("#D4DADF"),
                Color.parseColor("#1B2026"),
                Color.parseColor("#E9EEF2"),
                Color.parseColor("#FFD166"),
                Color.parseColor("#AAB4BD"),
                Color.parseColor("#AAB4BD")
        ));

        themes.add(new KeyboardTheme(
                "forest",
                "Forest",
                Color.parseColor("#0E1B16"),
                Color.parseColor("#1D3027"),
                Color.parseColor("#2B3E34"),
                Color.parseColor("#E3F1E9"),
                Color.parseColor("#CBE3D6"),
                Color.parseColor("#16241D"),
                Color.parseColor("#E3F1E9"),
                Color.parseColor("#F2C14E"),
                Color.parseColor("#A7C2B2"),
                Color.parseColor("#A7C2B2")
        ));

        themes.add(new KeyboardTheme(
                "slate",
                "Slate",
                Color.parseColor("#1E1F24"),
                Color.parseColor("#2C2F36"),
                Color.parseColor("#3B3F49"),
                Color.parseColor("#F2F3F5"),
                Color.parseColor("#D6D8DD"),
                Color.parseColor("#26292F"),
                Color.parseColor("#F2F3F5"),
                Color.parseColor("#56C1FF"),
                Color.parseColor("#B9BEC7"),
                Color.parseColor("#B9BEC7")
        ));

        themes.add(new KeyboardTheme(
                "sun",
                "Sun",
                Color.parseColor("#FAF4DE"),
                Color.parseColor("#F7E4A6"),
                Color.parseColor("#E7D4A5"),
                Color.parseColor("#2F2415"),
                Color.parseColor("#3A2D1A"),
                Color.parseColor("#FFF7E9"),
                Color.parseColor("#2F2415"),
                Color.parseColor("#C05B19"),
                Color.parseColor("#7A6041"),
                Color.parseColor("#7A6041")
        ));

        themes.add(new KeyboardTheme(
                "ocean",
                "Ocean",
                Color.parseColor("#0E1A22"),
                Color.parseColor("#153040"),
                Color.parseColor("#1D3C4F"),
                Color.parseColor("#E3F2FB"),
                Color.parseColor("#C7E1F2"),
                Color.parseColor("#132531"),
                Color.parseColor("#E3F2FB"),
                Color.parseColor("#FFB703"),
                Color.parseColor("#9AB7C8"),
                Color.parseColor("#9AB7C8")
        ));

        return themes;
    }

    public static List<KeyboardTheme> getThemes() {
        return Collections.unmodifiableList(THEMES);
    }

    public static KeyboardTheme getThemeById(String id) {
        if (id == null) {
            return THEMES.get(0);
        }
        for (KeyboardTheme theme : THEMES) {
            if (theme.getId().equals(id)) {
                return theme;
            }
        }
        return THEMES.get(0);
    }

    public static KeyboardTheme loadTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String id = prefs.getString(PREF_THEME_ID, DEFAULT_THEME_ID);
        return getThemeById(id);
    }

    public static void saveTheme(Context context, KeyboardTheme theme) {
        if (theme == null) {
            return;
        }
        saveTheme(context, theme.getId());
    }

    public static void saveTheme(Context context, String themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(PREF_THEME_ID, themeId).apply();
    }
}
