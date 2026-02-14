package com.example.abugida;

public class KeyboardTheme {
    private final String id;
    private final String name;
    private final int keyboardBackground;
    private final int keyBackgroundPrimary;
    private final int keyBackgroundSecondary;
    private final int keyTextPrimary;
    private final int keyTextSecondary;
    private final int candidateBackground;
    private final int candidateTextNormal;
    private final int candidateTextRecommended;
    private final int candidateTextOther;
    private final int suggestionTextColor;

    public KeyboardTheme(
            String id,
            String name,
            int keyboardBackground,
            int keyBackgroundPrimary,
            int keyBackgroundSecondary,
            int keyTextPrimary,
            int keyTextSecondary,
            int candidateBackground,
            int candidateTextNormal,
            int candidateTextRecommended,
            int candidateTextOther,
            int suggestionTextColor
    ) {
        this.id = id;
        this.name = name;
        this.keyboardBackground = keyboardBackground;
        this.keyBackgroundPrimary = keyBackgroundPrimary;
        this.keyBackgroundSecondary = keyBackgroundSecondary;
        this.keyTextPrimary = keyTextPrimary;
        this.keyTextSecondary = keyTextSecondary;
        this.candidateBackground = candidateBackground;
        this.candidateTextNormal = candidateTextNormal;
        this.candidateTextRecommended = candidateTextRecommended;
        this.candidateTextOther = candidateTextOther;
        this.suggestionTextColor = suggestionTextColor;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getKeyboardBackground() {
        return keyboardBackground;
    }

    public int getKeyBackgroundPrimary() {
        return keyBackgroundPrimary;
    }

    public int getKeyBackgroundSecondary() {
        return keyBackgroundSecondary;
    }

    public int getKeyTextPrimary() {
        return keyTextPrimary;
    }

    public int getKeyTextSecondary() {
        return keyTextSecondary;
    }

    public int getCandidateBackground() {
        return candidateBackground;
    }

    public int getCandidateTextNormal() {
        return candidateTextNormal;
    }

    public int getCandidateTextRecommended() {
        return candidateTextRecommended;
    }

    public int getCandidateTextOther() {
        return candidateTextOther;
    }

    public int getSuggestionTextColor() {
        return suggestionTextColor;
    }
}
