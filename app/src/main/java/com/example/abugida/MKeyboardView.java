package com.example.abugida;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.example.abogida.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MKeyboardView extends KeyboardView {
    private int colorMilky;
    private int colorLightGrey;
    public MKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        colorMilky = r.getColor(R.color.colorMilky);
        colorLightGrey = r.getColor(R.color.colorLightGrey);
        loadDictionary();
    }

    public MKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        loadDictionary(); // Load the dictionary when the view is created
    }

    static String fetchedEditTextValue = "";
    static String currentKeyboarrdLayout = "hahu";
    static String hahuLayoutName = "hahu";
    static String qwertyLayoutName = "qwerty";
    static String numbersLayoutName = "numbers";
    private static final double WEIGHT_LEVENSHTEIN = 1.0;
    private static final double WEIGHT_ABUGIDA = 1.5;
    private static final double PREFIX_BONUS = -3.0;

    static boolean fidelPressed = false;
    static boolean wordStarted = false;
    static boolean stopShowingSurroundingLetters = false;
    static int pressedFidelPrimaryCode;
    static int whichBoxTouched;
    static int XZ;
    static int YZ;
    int tempKeyCode;

    MKeyboardView context = this;

    static List<Integer> wordFormationList = new ArrayList<>();
    private List<Rect> suggestionRects = new ArrayList<>();
    private List<String> currentSuggestions = new ArrayList<>();
    private List<String> commonAmharicWords;


    private SoftKeyboard mService;

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d("debugging","is it: yes it is");

        Log.d("Debugging", "touching space: " + XZ + "  " + YZ);

        setKeyColorForAmharicKeyboard(canvas);

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for(Keyboard.Key key: keys){
            if (key.codes[0] == pressedFidelPrimaryCode) {
                Log.d("Debugging", "box returned: keyCode");
                if (fidelPressed) {
                    Log.d("Debugging", "box returned: fidelPressed");
                    Paint rectangle = new Paint();
                    rectangle.setStrokeWidth(10);
                    rectangle.setColor(Color.rgb(220, 220, 220));

                    Paint paint = new Paint();
                    paint.setTextSize(60);
                    paint.setColor(Color.BLACK);

                    int childPrimaryCode = key.codes[0] + whichChildLetter(whichBoxTouched);
                    char code = (char) childPrimaryCode;
                    //canvas.drawText(String.valueOf(code), key.x + key.width - (key.width/10) + (key.width / 4), key.y - (2 * key.height) - (key.height / 3), paint);
                    Log.d("Debugging", "children: " + code);
                    if (whichBoxTouched == 0) {
                        Log.d("Debugging", "box returned: rectZeroDrawen");
                        canvas.drawRect(key.x - (key.width/10), key.y - (2 * key.height), key.x + key.width + (key.width/10), key.y, rectangle);
                        canvas.drawText(String.valueOf(code), key.x - (key.width/10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 1) {
                        Log.d("Debugging", "box returned: rectOneDrawn");
                        canvas.drawRect(key.x + key.width - (key.width/10), key.y - (2 * key.height), key.x + (2 * key.width) + (key.width/10), key.y, rectangle);
                        canvas.drawText(String.valueOf(code), key.x + key.width - (key.width/10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 2) {
                        canvas.drawRect(key.x + key.width - (key.width/10), key.y - key.height, key.x + (2 * key.width) + (key.width/10), key.y + key.height, rectangle);
                        canvas.drawText(String.valueOf(code), key.x + key.width - (key.width/10) + (key.width / 3), key.y - (key.height / 3), paint);
                    } else if (whichBoxTouched == 3) {
                        canvas.drawRect(key.x + key.width - (key.width/10), key.y, key.x + (2 * key.width) + (key.width/10), key.y + (2 * key.height), rectangle);
                        canvas.drawText(String.valueOf(code), key.x + key.width - (key.width/10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 4) {
                        canvas.drawRect(key.x - (key.width/10), key.y, key.x + key.width + (key.width/10), key.y + (2 * key.height), rectangle);
                        canvas.drawText(String.valueOf(code), key.x - (key.width/10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 5) {
                        canvas.drawRect(key.x - key.width - (key.width/10), key.y, key.x + (key.width/10), key.y + (2 * key.height), rectangle);
                        canvas.drawText(String.valueOf(code), key.x - key.width - (key.width/10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 6) {
                        canvas.drawRect(key.x - key.width - (key.width/10), key.y - key.height, key.x + (key.width/10), key.y + key.height, rectangle);
                        canvas.drawText(String.valueOf(code), key.x - key.width - (key.width/10) + (key.width / 3), key.y - (key.height / 3), paint);
                    } else if (whichBoxTouched == 7) {
                        canvas.drawRect(key.x - key.width - (key.width/10), key.y - (2 * key.height), key.x + (key.width/10), key.y, rectangle);
                        canvas.drawText(String.valueOf(code), key.x - key.width - (key.width/10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
                    } else if (whichBoxTouched == 100) {
                        //canvas.drawRect(key.x - (key.width/10), key.y - key.height, key.x + key.width + (key.width/10), key.y + key.height, rectangle);
                    }

                }
            }
        }


        /*Paint paint = new Paint();
        paint.setTextSize(15);
        paint.setColor(Color.GRAY);

        List<Keyboard.Key> keys2 = getKeyboard().getKeys();
        for(Keyboard.Key key: keys2){
            if (key.codes[0] == 113) {
                canvas.drawText("1", key.x+(key.width/2), key.y + 25, paint);
            }
        }*/


        List<Keyboard.Key> keys2 = getKeyboard().getKeys();
        for(Keyboard.Key key: keys2){
            Log.d("fidelOWPressed:", "false1");
            if(fidelPressed){
                Log.d("fidelOWPressed:", "false2");
                if(!stopShowingSurroundingLetters) {
                    if (key.pressed && key.codes[0] >= 4608 && key.codes[0] < 4952) {
                        stopShowingSurroundingLetters = true;
                        Log.d("fidelOWPressed:", "false3");
                        fidelPressed = true;
                        tempKeyCode = key.codes[0];
                        for (int i = 0; i < 8; i++) {
                            Paint paint = new Paint();
                            paint.setTextSize(40);
                            paint.setColor(Color.BLACK);

                            Paint rectangle = new Paint();
                            rectangle.setStrokeWidth(10);
                            int childPrimaryCode = key.codes[0] + whichChildLetter(i);
                            char code = (char) childPrimaryCode;
                            if (i == 0) {
                                rectangle.setColor(colorMilky);
                                canvas.drawRect(key.x, key.y - key.height, key.x + key.width, key.y, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), key.y - (key.height / 3), paint);
                            } else if (i == 1) {
                                rectangle.setColor(colorLightGrey);
                                canvas.drawRect(key.x + key.width, key.y - key.height, key.x + (2 * key.width), key.y, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), key.y - (key.height / 3), paint);
                            }
                            if (i == 2) {
                                rectangle.setColor(colorMilky);
                                canvas.drawRect(key.x + key.width, key.y, key.x + (2 * key.width), key.y + key.height, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                            }
                            if (i == 3) {
                                rectangle.setColor(colorLightGrey);
                                canvas.drawRect(key.x + key.width, key.y + key.height, key.x + (2 * key.width), key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 4) {
                                rectangle.setColor(colorMilky);
                                canvas.drawRect(key.x, key.y + key.height, key.x + key.width, key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 5) {
                                rectangle.setColor(colorLightGrey);
                                canvas.drawRect(key.x - key.width, key.y + key.height, key.x, key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 6) {
                                rectangle.setColor(colorMilky);
                                canvas.drawRect(key.x - key.width, key.y, key.x, key.y + key.height, rectangle);
                                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                            }
                            if (i == 7) {
                                rectangle.setColor(colorLightGrey);
                                canvas.drawRect(key.x - key.width, key.y - key.height, key.x, key.y, rectangle);
                                canvas.drawText(String.valueOf(code), (key.x + (key.width / 4)) - key.width, key.y - (key.height / 3), paint);
                            }
                        }
                    }
                }
            }else{
                stopShowingSurroundingLetters = false;
                /*if(key.codes[0]==tempKeyCode){
                    for (int i = 0; i < 8; i++) {
                        Paint paint = new Paint();
                        paint.setTextSize(40);
                        paint.setColor(Color.RED);

                        Paint rectangle = new Paint();
                        rectangle.setStrokeWidth(10);
                        int childPrimaryCode = key.codes[0] + whichChildLetter(i);
                        char code = (char) childPrimaryCode;
                        if (i == 0) {
                            rectangle.setColor(Color.rgb(20,50,150));
                            canvas.drawRect(key.x, key.y - key.height, key.x + key.width, key.y, rectangle);
                            canvas.drawText(String.valueOf(code), key.x + (key.width / 4), key.y - (key.height / 3), paint);
                        } else if (i == 1) {
                            rectangle.setColor(Color.BLUE);
                            canvas.drawRect(key.x + key.width, key.y - key.height, key.x + (2 * key.width), key.y, rectangle);
                            canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), key.y - (key.height / 3), paint);
                        }
                        if (i == 2) {
                            rectangle.setColor(Color.rgb(20,50,150));
                            canvas.drawRect(key.x + key.width, key.y, key.x + (2 * key.width), key.y + key.height, rectangle);
                            canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                        }
                        if (i == 3) {
                            rectangle.setColor(Color.BLUE);
                            canvas.drawRect(key.x + key.width, key.y + key.height, key.x + (2 * key.width), key.y + (2 * key.height), rectangle);
                            canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                        }
                        if (i == 4) {
                            rectangle.setColor(Color.rgb(20,50,150));
                            canvas.drawRect(key.x, key.y + key.height, key.x + key.width, key.y + (2 * key.height), rectangle);
                            canvas.drawText(String.valueOf(code), key.x + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                        }
                        if (i == 5) {
                            rectangle.setColor(Color.BLUE);
                            canvas.drawRect(key.x - key.width, key.y + key.height, key.x, key.y + (2 * key.height), rectangle);
                            canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                        }
                        if (i == 6) {
                            rectangle.setColor(Color.rgb(20,50,150));
                            canvas.drawRect(key.x - key.width, key.y, key.x, key.y + key.height, rectangle);
                            canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                        }
                        if (i == 7) {
                            rectangle.setColor(Color.BLUE);
                            canvas.drawRect(key.x - key.width, key.y - key.height, key.x, key.y, rectangle);
                            canvas.drawText(String.valueOf(code), (key.x + (key.width / 4)) - key.width, key.y - (key.height / 3), paint);
                        }
                    }
                }*/
            }
        }

        /*Paint rectangle = new Paint();
        rectangle.setColor(Color.YELLOW);
        rectangle.setStrokeWidth(10);
        //rectangle.setStyle(Paint.Style.STROKE);
        canvas.drawRect(200, 108, 285, 400, rectangle);*/
        //canvas.drawRect(200, 109, 285, 400, rectangle);

        if(currentKeyboarrdLayout.equals(hahuLayoutName)){
            displayWord(canvas);
        }

    }

    public void setService(SoftKeyboard service) {
        mService = service;
    }

    // This method checks if a suggestion was tapped and returns true if so.
    private boolean handleSuggestionTap(int x, int y) {
        // Iterate through the stored locations of our suggestions
        for (int i = 0; i < suggestionRects.size(); i++) {
            Rect rect = suggestionRects.get(i);

//            Log.d("Touchasd", "Rect for '" + currentSuggestions.get(i) + "': " +
//                    "Top-Left: (" + rect.left + ", " + rect.top + "), " +
//                    "Top-Right: (" + rect.right + ", " + rect.top + "), " +
//                    "Bottom-Left: (" + rect.left + ", " + rect.bottom + "), " +
//                    "Bottom-Right: (" + rect.right + ", " + rect.bottom + ")");
//            // --- End of new code ---
//
//            Log.d("Touchasd", "X: " + String.valueOf(x) + ", Y: " + String.valueOf(y) + ":" + i);
//            Log.d("Touchasd", String.valueOf(rect.contains(x, y)));

            // Check if the tap coordinates (x, y) are inside this suggestion's rectangle
            if (rect.contains(x, y)) {
                // A suggestion was tapped!
                String selectedSuggestion = currentSuggestions.get(i);
                // Here, you need to tell your InputMethodService to handle the word.
                // This assumes you have a reference to your service.
                if (mService != null) { // 'mService' would be your SoftKeyboard instance
                    mService.pickSuggestionManually(selectedSuggestion);
                }

                // We handled the event, so return true
                return true;
            }
        }
        // No suggestion was tapped
        return false;
    }

    private void loadDictionary() {
        commonAmharicWords = new ArrayList<>();
        // Get the resource from the raw folder
        InputStream inputStream = getContext().getResources().openRawResource(R.raw.amharic_words);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Add each word from the file to our list
                if (!line.trim().isEmpty()) {
                    commonAmharicWords.add(line.trim());
                }
            }
        } catch (IOException e) {
            // Handle exceptions, e.g., by logging an error
            Log.e("MKeyboardView", "Error loading dictionary", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN && !stopShowingSurroundingLetters) {
            Log.d("touchasd", "onTouchEvent:"+String.valueOf(!currentSuggestions.isEmpty()));
            if (!currentSuggestions.isEmpty()) {
                if (handleSuggestionTap((int) me.getX(), (int) me.getY())) {
                    return true;
                }
            }
        }
        return super.onTouchEvent(me);
    }

    public String amharicTrim(String inputText) {
        String[] words = inputText.split(" ");
        String lastWord = words[words.length - 1];
        for (int i=0; i<lastWord.length(); i++){
            char character = lastWord.charAt(i);
            int charCode = (int) character; // Cast the character to its integer code

            // This is the check you are asking for:
            if (charCode < 4608 || charCode >= 4952) {
                String[] amhWords = lastWord.split(String.valueOf(character));
                lastWord = amhWords[amhWords.length - 1];
                break;
            }
        }
        return lastWord;
    }

    public void displayWord(Canvas canvas) {
        // Clear previous suggestion data
        suggestionRects.clear();
        currentSuggestions.clear();

        String allWords = fetchedEditTextValue;
        if (allWords == null || allWords.isEmpty() || allWords.endsWith(" ")) {
            return; // Nothing to do if there's no text
        }

        char lastChar = allWords.charAt(allWords.length() - 1);
        int charCode = (int) lastChar;

        if (charCode < 4608 || charCode >= 4952) {
            // The last character is NOT a valid Amharic letter (it might be '.', '?', ',', etc.).
            // So, we should not show any suggestions.
            return; // Exit the method.
        }

        Log.d("touchasd", String.valueOf(allWords.endsWith(" ")) + allWords);
        String finalWord = amharicTrim(allWords);

        Paint paint = new Paint();
        paint.setTextSize(45);
        paint.setColor(Color.rgb(195, 195, 195));
        paint.setTextAlign(Paint.Align.LEFT); // Important for accurate positioning

        // Get the list of suggestions
        currentSuggestions = findClosestMatch(finalWord);

        // Starting position for the first suggestion
        float currentX = 60;
        float yPosition = 95; // The vertical position of your suggestion bar

        for (String suggestion : currentSuggestions) {
            // Measure the width of the suggestion word
            float wordWidth = paint.measureText(suggestion);

            // Define the clickable area (Rect) for this word
            Rect wordRect = new Rect(
                    (int) currentX - 30,
                    (int) (yPosition - paint.getTextSize() - 50), // top
                    (int) (currentX + wordWidth + 30),            // right
                    (int) yPosition + 75                         // bottom
            );
            suggestionRects.add(wordRect);

            // Draw the suggestion word
            canvas.drawText(suggestion, currentX, yPosition, paint);

            // Update the starting X for the next word, adding some padding
            currentX += wordWidth + 60; // 60 pixels of padding
        }
    }

    public int whichChildLetter(int boxOrder){
        if(boxOrder == 1){
            return 7;
        }else if(boxOrder > 1){
            return boxOrder - 1;
        }else{
            return boxOrder;
        }
    }

    public static boolean isFidelPressed() {
        return fidelPressed;
    }

    public static void setFidelPressed(boolean fidelPressed) {
        MKeyboardView.fidelPressed = fidelPressed;
    }

    public static int getPressedFidelPrimaryCode() {
        return pressedFidelPrimaryCode;
    }

    public static void setPressedFidelPrimaryCode(int pressedFidelPrimaryCode) {
        MKeyboardView.pressedFidelPrimaryCode = pressedFidelPrimaryCode;
    }

    public static int getWhichBoxTouched() {
        return whichBoxTouched;
    }

    public static void setWhichBoxTouched(int whichChildLetterTouched) {
        MKeyboardView.whichBoxTouched = whichChildLetterTouched;
    }

    public static int getXZ() {
        return XZ;
    }

    public static void setXZ(int XZ) {
        MKeyboardView.XZ = XZ;
    }

    public static int getYZ() {
        return YZ;
    }

    public static void setYZ(int YZ) {
        MKeyboardView.YZ = YZ;
    }

    public static boolean isWordStarted() {
        return wordStarted;
    }

    public static void setWordStarted(boolean wordStarted) {
        MKeyboardView.wordStarted = wordStarted;
    }

    public static List<Integer> getWordFormationList() {
        return wordFormationList;
    }

    public static void setWordFormationList(List<Integer> wordFormationList) {
        MKeyboardView.wordFormationList = wordFormationList;
    }

    private void setKeyColorForAmharicKeyboard(Canvas canvas){
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for(Keyboard.Key key: keys) {
            if(key.codes[0] >= 4608 && key.codes[0] < 4952 || key.codes[0] == 32) {
                Drawable dr = this.getResources().getDrawable(R.drawable.rounded_keys);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);

                int keyTextColor = Color.rgb(255, 255, 255);

                setKeyText(canvas, key, keyTextColor);
            }else {
                Drawable dr = this.getResources().getDrawable(R.drawable.black_background);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);

                int keyTextColor = Color.WHITE;

                setKeyText(canvas, key, keyTextColor);
            }
        }
    }

    private void setKeyText(Canvas canvas, Keyboard.Key key, int keyTextColor) {
        // Check if the key has an icon to draw
        if (key.icon != null) {
            // The key has an icon, so we draw the icon instead of a text label.

            // Center the icon within the key's bounds
            int iconWidth = key.icon.getIntrinsicWidth();
            int iconHeight = key.icon.getIntrinsicHeight();
            int left = key.x + (key.width - iconWidth) / 2;
            int top = key.y + (key.height - iconHeight) / 2;
            int right = left + iconWidth;
            int bottom = top + iconHeight;

            // Set the bounds and draw the icon
            key.icon.setBounds(left, top, right, bottom);
            key.icon.draw(canvas);

        } else if (key.label != null) {
            // The key has a text label, so we draw the text as before.
            int primaryCode = key.codes[0];

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG); // Use Anti-aliasing for smooth text
            paint.setColor(keyTextColor);
            paint.setTextAlign(Paint.Align.CENTER); // Center align the text for easier positioning

            if ((primaryCode >= 4608 && primaryCode < 4952) || (primaryCode > 47 && primaryCode < 150)) {
                paint.setTextSize(50);
            } else if(primaryCode == 4961 || primaryCode == 4964 || primaryCode == 4962 || primaryCode == 4963) {
                paint.setTextSize(50);
            } else {
                paint.setTextSize(32);
            }

            // Calculate the correct X and Y to draw the text centered in the key
            float centerX = key.x + key.width / 2.0f;
            float centerY = key.y + key.height / 2.0f - (paint.descent() + paint.ascent()) / 2.0f;

            canvas.drawText(key.label.toString(), centerX, centerY, paint);
        }
        // If a key has neither an icon nor a label, we draw nothing for it.
    }



    /**
     * A more advanced method to find the 5 closest Amharic words.
     * @param inputWord The word the user is typing.
     * @return A list of the 5 best suggestions.
     */
    public List<String> findClosestMatch(String inputWord) {
        if (inputWord == null || inputWord.trim().isEmpty()) {
            return Collections.emptyList();
        }

        inputWord = inputWord.trim();

        // A list to hold words and their calculated scores.
        List<WordDistance> wordScores = new ArrayList<>();

        for (String word : commonAmharicWords) {
            // Calculate the score for each metric
            double levenshteinScore = levenshteinDistance(inputWord, word) * WEIGHT_LEVENSHTEIN;
            double abugidaScore = abugidaProximityDistance(inputWord, word) * WEIGHT_ABUGIDA;

            double finalScore = levenshteinScore + abugidaScore;

            // Apply a large bonus if the dictionary word starts with the input word
            if (word.startsWith(inputWord)) {
                finalScore += PREFIX_BONUS;
            }

            wordScores.add(new WordDistance(word, finalScore));
        }

        // Sort the list based on the final score (lowest score is best)
        Collections.sort(wordScores, Comparator.comparingDouble(wd -> wd.distance));

        // Extract the top 5 words
        List<String> closestMatches = new ArrayList<>();
        for (int i = 0; i < wordScores.size() && i < 15; i++) {
            closestMatches.add(wordScores.get(i).word);
        }

        return closestMatches;
    }

    /**
     * Calculates a distance score based on the Abugida families of characters.
     * Treats characters from the same family (ሀ, ሁ, ሂ...) as being very close.
     */
    private double abugidaProximityDistance(String s1, String s2) {
        double totalDistance = 0;
        int maxLength = Math.max(s1.length(), s2.length());
        for (int i = 0; i < maxLength; i++) {
            if (i >= s1.length() || i >= s2.length()) {
                totalDistance += 1.0; // Penalty for length difference
                continue;
            }

            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);

            if (c1 == c2) {
                continue; // No distance if characters are identical
            }

            // Check if they belong to the same family
            int base1 = (c1 - 4608) / 8; // Integer division gives the family index
            int base2 = (c2 - 4608) / 8;

            if (base1 == base2) {
                totalDistance += 0.1; // Very small penalty for being in the same family
            } else {
                totalDistance += 1.0; // Full penalty for being different families
            }
        }
        return totalDistance;
    }


    // Helper class to store word and its score (use double for score)
    private static class WordDistance {
        String word;
        double distance;

        WordDistance(String word, double distance) {
            this.word = word;
            this.distance = distance;
        }
    }

    /**
     * Calculates the Levenshtein distance (edit distance) between two strings.
     * (This method remains unchanged, but is included for completeness).
     * @param s1 First string
     * @param s2 Second string
     * @return The edit distance between the two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return Integer.MAX_VALUE;
        }

        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == 0) return len2;
        if (len2 == 0) return len1;

        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(
                            Math.min(dp[i - 1][j], dp[i][j - 1]),
                            dp[i - 1][j - 1]
                    );
                }
            }
        }

        return dp[len1][len2];
    }
}
