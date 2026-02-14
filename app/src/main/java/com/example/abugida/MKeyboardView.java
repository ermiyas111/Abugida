package com.example.abugida;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MKeyboardView extends KeyboardView {
    private int colorMilky;
    private int colorLightGrey;
    private int keyBackgroundPrimary;
    private int keyBackgroundSecondary;
    private int keyTextPrimary;
    private int keyTextSecondary;
    private int keyboardBackground;
    private int suggestionTextColor;
    private int popupBackgroundPrimary;
    private int popupBackgroundSecondary;
        private int capsHighlightColor;
    private boolean showSuggestions = true;
    private boolean capsLockActive = false;
    private boolean shiftHighlightActive = false;
    private int shiftHighlightColor;
        private static final int[] CAPS_TOGGLE_CODES = new int[] {
            4608, 4624, 4656, 4640, 4704, 4712,
            4768, 4816, 4928, 4920, 4792, 4736
        };
    public MKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        colorMilky = r.getColor(R.color.colorMilky);
        colorLightGrey = r.getColor(R.color.colorLightGrey);
        keyBackgroundPrimary = r.getColor(R.color.colorMilky);
        keyBackgroundSecondary = r.getColor(R.color.colorBlack);
        keyTextPrimary = r.getColor(android.R.color.white);
        keyTextSecondary = r.getColor(android.R.color.white);
        keyboardBackground = r.getColor(R.color.colorBlack);
        suggestionTextColor = Color.rgb(195, 195, 195);
        popupBackgroundPrimary = adjustPopupColor(keyBackgroundPrimary);
        popupBackgroundSecondary = adjustPopupColor(keyBackgroundSecondary);
        capsHighlightColor = adjustCapsHighlightColor(keyBackgroundPrimary);
        shiftHighlightColor = adjustShiftHighlightColor(keyBackgroundPrimary);
        loadDictionary();
    }

    public MKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources r = context.getResources();
        colorMilky = r.getColor(R.color.colorMilky);
        colorLightGrey = r.getColor(R.color.colorLightGrey);
        keyBackgroundPrimary = r.getColor(R.color.colorMilky);
        keyBackgroundSecondary = r.getColor(R.color.colorBlack);
        keyTextPrimary = r.getColor(android.R.color.white);
        keyTextSecondary = r.getColor(android.R.color.white);
        keyboardBackground = r.getColor(R.color.colorBlack);
        suggestionTextColor = Color.rgb(195, 195, 195);
        popupBackgroundPrimary = adjustPopupColor(keyBackgroundPrimary);
        popupBackgroundSecondary = adjustPopupColor(keyBackgroundSecondary);
        capsHighlightColor = adjustCapsHighlightColor(keyBackgroundPrimary);
        shiftHighlightColor = adjustShiftHighlightColor(keyBackgroundPrimary);
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
    private List<EnglishWordEntry> commonEnglishWords;
    private int maxEnglishFrequency = 1;
    private VariantOverlayView variantOverlayView;
    private int popupKeyX;
    private int popupKeyY;
    private int popupKeyWidth;
    private int popupKeyHeight;
    private int popupKeyCode;
    private boolean popupKeyValid;


    private SoftKeyboard mService;

    @Override
    public void onDraw(Canvas canvas) {
        setKeyColorForAmharicKeyboard(canvas);

        if (variantOverlayView != null && fidelPressed) {
            variantOverlayView.invalidate();
        }

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for(Keyboard.Key key: keys){
            drawPopupSelection(canvas, key);
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
            if (fidelPressed) {
                drawSurroundingLetters(canvas, key);
            } else {
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

        // Suggestions bar removed.

    }

    private void drawPopupSelection(Canvas canvas, Keyboard.Key key) {
        if (variantOverlayView != null) {
            return;
        }
        if (key.codes[0] != pressedFidelPrimaryCode || !fidelPressed) {
            return;
        }
        Paint rectangle = new Paint();
        rectangle.setStrokeWidth(10);
        rectangle.setColor(popupBackgroundPrimary);

        Paint paint = new Paint();
        paint.setTextSize(60);
        paint.setColor(Color.BLACK);

        int childPrimaryCode = key.codes[0] + whichChildLetter(whichBoxTouched);
        char code = (char) childPrimaryCode;

        boolean isTopRow = (key.y == 0);
        if (whichBoxTouched == 0) {
            if (isTopRow) {
                canvas.drawRect(key.x - (key.width / 2), key.y, key.x + (key.width * 1.5f), key.y + key.height, rectangle);
                canvas.drawText(String.valueOf(code), key.x + (key.width / 3), key.y + (key.height * 0.6f), paint);
            } else {
                canvas.drawRect(key.x - (key.width / 10), key.y - (2 * key.height), key.x + key.width + (key.width / 10), key.y, rectangle);
                canvas.drawText(String.valueOf(code), key.x - (key.width / 10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
            }
        } else if (whichBoxTouched == 1) {
            if (isTopRow) {
                canvas.drawRect(key.x + key.width, key.y, key.x + (3 * key.width), key.y + key.height, rectangle);
                canvas.drawText(String.valueOf(code), key.x + (1.5f * key.width), key.y + (key.height * 0.6f), paint);
            } else {
                canvas.drawRect(key.x + key.width - (key.width / 10), key.y - (2 * key.height), key.x + (2 * key.width) + (key.width / 10), key.y, rectangle);
                canvas.drawText(String.valueOf(code), key.x + key.width - (key.width / 10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
            }
        } else if (whichBoxTouched == 2) {
            canvas.drawRect(key.x + key.width - (key.width / 10), key.y - key.height, key.x + (2 * key.width) + (key.width / 10), key.y + key.height, rectangle);
            canvas.drawText(String.valueOf(code), key.x + key.width - (key.width / 10) + (key.width / 3), key.y - (key.height / 3), paint);
        } else if (whichBoxTouched == 3) {
            canvas.drawRect(key.x + key.width - (key.width / 10), key.y, key.x + (2 * key.width) + (key.width / 10), key.y + (2 * key.height), rectangle);
            canvas.drawText(String.valueOf(code), key.x + key.width - (key.width / 10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
        } else if (whichBoxTouched == 4) {
            canvas.drawRect(key.x - (key.width / 10), key.y, key.x + key.width + (key.width / 10), key.y + (2 * key.height), rectangle);
            canvas.drawText(String.valueOf(code), key.x - (key.width / 10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
        } else if (whichBoxTouched == 5) {
            canvas.drawRect(key.x - key.width - (key.width / 10), key.y, key.x + (key.width / 10), key.y + (2 * key.height), rectangle);
            canvas.drawText(String.valueOf(code), key.x - key.width - (key.width / 10) + (key.width / 3), key.y + key.height - (key.height / 3), paint);
        } else if (whichBoxTouched == 6) {
            canvas.drawRect(key.x - key.width - (key.width / 10), key.y - key.height, key.x + (key.width / 10), key.y + key.height, rectangle);
            canvas.drawText(String.valueOf(code), key.x - key.width - (key.width / 10) + (key.width / 3), key.y - (key.height / 3), paint);
        } else if (whichBoxTouched == 7) {
            if (isTopRow) {
                canvas.drawRect(key.x - key.width - (key.width / 10), key.y, key.x + (key.width / 10), key.y + key.height, rectangle);
                canvas.drawText(String.valueOf(code), key.x - key.width - (key.width / 10) + (key.width / 3), key.y + (key.height * 0.6f), paint);
            } else {
                canvas.drawRect(key.x - key.width - (key.width / 10), key.y - (2 * key.height), key.x + (key.width / 10), key.y, rectangle);
                canvas.drawText(String.valueOf(code), key.x - key.width - (key.width / 10) + (key.width / 3), key.y - key.height - (key.height / 3), paint);
            }
        }
    }

    private void drawSurroundingLetters(Canvas canvas, Keyboard.Key key) {
        if (stopShowingSurroundingLetters || !key.pressed || key.codes[0] < 4608 || key.codes[0] >= 4952) {
            return;
        }
        stopShowingSurroundingLetters = true;
        fidelPressed = true;
        tempKeyCode = key.codes[0];
        updatePopupKey(key);
        if (variantOverlayView != null) {
            variantOverlayView.invalidate();
            return;
        }
        boolean isTopRow = (key.y == 0);
        for (int i = 0; i < 8; i++) {
            Paint paint = new Paint();
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);

            Paint rectangle = new Paint();
            rectangle.setStrokeWidth(10);
            int childPrimaryCode = key.codes[0] + whichChildLetter(i);
            char code = (char) childPrimaryCode;
            if (i == 0) {
                rectangle.setColor(popupBackgroundPrimary);
                int top = isTopRow ? key.y : key.y - key.height;
                int bottom = isTopRow ? key.y + key.height : key.y;
                float textY = isTopRow ? key.y + (key.height * 0.6f) : key.y - (key.height / 3);
                canvas.drawRect(key.x, top, key.x + key.width, bottom, rectangle);
                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), textY, paint);
            } else if (i == 1) {
                rectangle.setColor(popupBackgroundSecondary);
                int top = isTopRow ? key.y : key.y - key.height;
                int bottom = isTopRow ? key.y + key.height : key.y;
                float textY = isTopRow ? key.y + (key.height * 0.6f) : key.y - (key.height / 3);
                canvas.drawRect(key.x + key.width, top, key.x + (2 * key.width), bottom, rectangle);
                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), textY, paint);
            }
            if (i == 2) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(key.x + key.width, key.y, key.x + (2 * key.width), key.y + key.height, rectangle);
                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
            }
            if (i == 3) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(key.x + key.width, key.y + key.height, key.x + (2 * key.width), key.y + (2 * key.height), rectangle);
                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
            }
            if (i == 4) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(key.x, key.y + key.height, key.x + key.width, key.y + (2 * key.height), rectangle);
                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
            }
            if (i == 5) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(key.x - key.width, key.y + key.height, key.x, key.y + (2 * key.height), rectangle);
                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
            }
            if (i == 6) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(key.x - key.width, key.y, key.x, key.y + key.height, rectangle);
                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
            }
            if (i == 7) {
                rectangle.setColor(popupBackgroundSecondary);
                int top = isTopRow ? key.y : key.y - key.height;
                int bottom = isTopRow ? key.y + key.height : key.y;
                float textY = isTopRow ? key.y + (key.height * 0.6f) : key.y - (key.height / 3);
                canvas.drawRect(key.x - key.width, top, key.x, bottom, rectangle);
                canvas.drawText(String.valueOf(code), (key.x + (key.width / 4)) - key.width, textY, paint);
            }
        }
    }

    public void setVariantOverlayView(VariantOverlayView overlayView) {
        this.variantOverlayView = overlayView;
    }

    public void clearVariantOverlay() {
        popupKeyValid = false;
        if (variantOverlayView != null) {
            variantOverlayView.invalidate();
        }
    }

    public void drawVariantOverlay(Canvas canvas, int offsetY) {
        if (!fidelPressed || !popupKeyValid) {
            return;
        }
        drawSurroundingLettersOverlay(canvas, offsetY);
        drawPopupSelectionOverlay(canvas, offsetY);
    }

    private void updatePopupKey(Keyboard.Key key) {
        popupKeyX = key.x;
        popupKeyY = key.y;
        popupKeyWidth = key.width;
        popupKeyHeight = key.height;
        popupKeyCode = key.codes[0];
        popupKeyValid = true;
    }

    private void drawSurroundingLettersOverlay(Canvas canvas, int offsetY) {
        if (popupKeyCode < 4608 || popupKeyCode >= 4952) {
            return;
        }

        int keyX = popupKeyX;
        int keyY = popupKeyY + offsetY;
        int keyWidth = popupKeyWidth;
        int keyHeight = popupKeyHeight;

        for (int i = 0; i < 8; i++) {
            Paint paint = new Paint();
            paint.setTextSize(40);
            paint.setColor(Color.BLACK);

            Paint rectangle = new Paint();
            rectangle.setStrokeWidth(10);
            int childPrimaryCode = popupKeyCode + whichChildLetter(i);
            char code = (char) childPrimaryCode;
            if (i == 0) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(keyX, keyY - keyHeight, keyX + keyWidth, keyY, rectangle);
                canvas.drawText(String.valueOf(code), keyX + (keyWidth / 4), keyY - (keyHeight / 3), paint);
            } else if (i == 1) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(keyX + keyWidth, keyY - keyHeight, keyX + (2 * keyWidth), keyY, rectangle);
                canvas.drawText(String.valueOf(code), keyX + keyWidth + (keyWidth / 4), keyY - (keyHeight / 3), paint);
            }
            if (i == 2) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(keyX + keyWidth, keyY, keyX + (2 * keyWidth), keyY + keyHeight, rectangle);
                canvas.drawText(String.valueOf(code), keyX + keyWidth + (keyWidth / 4), (keyY + keyHeight) - (keyHeight / 3), paint);
            }
            if (i == 3) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(keyX + keyWidth, keyY + keyHeight, keyX + (2 * keyWidth), keyY + (2 * keyHeight), rectangle);
                canvas.drawText(String.valueOf(code), keyX + keyWidth + (keyWidth / 4), (keyY + (2 * keyHeight)) - (keyHeight / 3), paint);
            }
            if (i == 4) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(keyX, keyY + keyHeight, keyX + keyWidth, keyY + (2 * keyHeight), rectangle);
                canvas.drawText(String.valueOf(code), keyX + (keyWidth / 4), (keyY + (2 * keyHeight)) - (keyHeight / 3), paint);
            }
            if (i == 5) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(keyX - keyWidth, keyY + keyHeight, keyX, keyY + (2 * keyHeight), rectangle);
                canvas.drawText(String.valueOf(code), (keyX - keyWidth) + (keyWidth / 4), (keyY + (2 * keyHeight)) - (keyHeight / 3), paint);
            }
            if (i == 6) {
                rectangle.setColor(popupBackgroundPrimary);
                canvas.drawRect(keyX - keyWidth, keyY, keyX, keyY + keyHeight, rectangle);
                canvas.drawText(String.valueOf(code), (keyX - keyWidth) + (keyWidth / 4), (keyY + keyHeight) - (keyHeight / 3), paint);
            }
            if (i == 7) {
                rectangle.setColor(popupBackgroundSecondary);
                canvas.drawRect(keyX - keyWidth, keyY - keyHeight, keyX, keyY, rectangle);
                canvas.drawText(String.valueOf(code), (keyX + (keyWidth / 4)) - keyWidth, keyY - (keyHeight / 3), paint);
            }
        }
    }

    private void drawPopupSelectionOverlay(Canvas canvas, int offsetY) {
        if (popupKeyCode != pressedFidelPrimaryCode || !fidelPressed) {
            return;
        }
        if (whichBoxTouched == 100) {
            return;
        }

        int keyX = popupKeyX;
        int keyY = popupKeyY + offsetY;
        int keyWidth = popupKeyWidth;
        int keyHeight = popupKeyHeight;

        Paint rectangle = new Paint();
        rectangle.setStrokeWidth(10);
        rectangle.setColor(popupBackgroundPrimary);

        Paint paint = new Paint();
        paint.setTextSize(60);
        paint.setColor(Color.BLACK);

        int childPrimaryCode = popupKeyCode + whichChildLetter(whichBoxTouched);
        char code = (char) childPrimaryCode;

        if (whichBoxTouched == 0) {
            canvas.drawRect(keyX - (keyWidth / 10), keyY - (2 * keyHeight), keyX + keyWidth + (keyWidth / 10), keyY, rectangle);
            canvas.drawText(String.valueOf(code), keyX - (keyWidth / 10) + (keyWidth / 3), keyY - keyHeight - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 1) {
            canvas.drawRect(keyX + keyWidth - (keyWidth / 10), keyY - (2 * keyHeight), keyX + (2 * keyWidth) + (keyWidth / 10), keyY, rectangle);
            canvas.drawText(String.valueOf(code), keyX + keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY - keyHeight - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 2) {
            canvas.drawRect(keyX + keyWidth - (keyWidth / 10), keyY - keyHeight, keyX + (2 * keyWidth) + (keyWidth / 10), keyY + keyHeight, rectangle);
            canvas.drawText(String.valueOf(code), keyX + keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 3) {
            canvas.drawRect(keyX + keyWidth - (keyWidth / 10), keyY, keyX + (2 * keyWidth) + (keyWidth / 10), keyY + (2 * keyHeight), rectangle);
            canvas.drawText(String.valueOf(code), keyX + keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY + keyHeight - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 4) {
            canvas.drawRect(keyX - (keyWidth / 10), keyY, keyX + keyWidth + (keyWidth / 10), keyY + (2 * keyHeight), rectangle);
            canvas.drawText(String.valueOf(code), keyX - (keyWidth / 10) + (keyWidth / 3), keyY + keyHeight - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 5) {
            canvas.drawRect(keyX - keyWidth - (keyWidth / 10), keyY, keyX + (keyWidth / 10), keyY + (2 * keyHeight), rectangle);
            canvas.drawText(String.valueOf(code), keyX - keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY + keyHeight - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 6) {
            canvas.drawRect(keyX - keyWidth - (keyWidth / 10), keyY - keyHeight, keyX + (keyWidth / 10), keyY + keyHeight, rectangle);
            canvas.drawText(String.valueOf(code), keyX - keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY - (keyHeight / 3), paint);
        } else if (whichBoxTouched == 7) {
            canvas.drawRect(keyX - keyWidth - (keyWidth / 10), keyY - (2 * keyHeight), keyX + (keyWidth / 10), keyY, rectangle);
            canvas.drawText(String.valueOf(code), keyX - keyWidth - (keyWidth / 10) + (keyWidth / 3), keyY - keyHeight - (keyHeight / 3), paint);
        }
    }

    public void setService(SoftKeyboard service) {
        mService = service;
    }

    public void applyTheme(KeyboardTheme theme) {
        if (theme == null) {
            return;
        }
        keyBackgroundPrimary = theme.getKeyBackgroundPrimary();
        keyBackgroundSecondary = theme.getKeyBackgroundSecondary();
        keyTextPrimary = theme.getKeyTextPrimary();
        keyTextSecondary = theme.getKeyTextSecondary();
        keyboardBackground = theme.getKeyboardBackground();
        suggestionTextColor = theme.getSuggestionTextColor();
        colorMilky = theme.getKeyBackgroundPrimary();
        colorLightGrey = theme.getKeyBackgroundSecondary();
        popupBackgroundPrimary = adjustPopupColor(keyBackgroundPrimary);
        popupBackgroundSecondary = adjustPopupColor(keyBackgroundSecondary);
        capsHighlightColor = adjustCapsHighlightColor(keyBackgroundPrimary);
        shiftHighlightColor = adjustShiftHighlightColor(keyBackgroundPrimary);
        setBackgroundColor(keyboardBackground);
        invalidate();
    }

    public void setCapsLockActive(boolean capsLockActive) {
        this.capsLockActive = capsLockActive;
        invalidate();
    }

    public void setShiftHighlightState(boolean shiftHighlightActive, boolean capsLockActive) {
        this.shiftHighlightActive = shiftHighlightActive;
        this.capsLockActive = capsLockActive;
        invalidate();
    }

    public void setShowSuggestions(boolean showSuggestions) {
        this.showSuggestions = showSuggestions;
        if (!showSuggestions) {
            suggestionRects.clear();
            currentSuggestions.clear();
        }
        invalidate();
    }

    public String getTopSuggestion() {
        if (currentSuggestions == null || currentSuggestions.isEmpty()) {
            return null;
        }
        return currentSuggestions.get(0);
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
        InputStream inputStreamAmharic = getContext().getResources().openRawResource(R.raw.amharic_words);
        BufferedReader readerAmharic = new BufferedReader(new InputStreamReader(inputStreamAmharic, StandardCharsets.UTF_8));

        try {
            String line;
            while ((line = readerAmharic.readLine()) != null) {
                // Add each word from the file to our list
                if (!line.trim().isEmpty()) {
                    commonAmharicWords.add(line.trim());
                }
            }
        } catch (IOException e) {
            // Handle exceptions, e.g., by logging an error
            Log.e("MKeyboardView", "Error loading Amharic dictionary", e);
        } finally {
            try {
                readerAmharic.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load English Dictionary (word + frequency)
        commonEnglishWords = new ArrayList<>();
        maxEnglishFrequency = 1;
        // Get the resource from the raw folder
        InputStream inputStreamEnglish = getContext().getResources().openRawResource(R.raw.english_words);
        BufferedReader readerEnglish = new BufferedReader(new InputStreamReader(inputStreamEnglish, StandardCharsets.UTF_8));

        try {
            String line;
            while ((line = readerEnglish.readLine()) != null) {
                // Add each word from the file to our list
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                String[] parts = trimmed.split("\\s+");
                if (parts.length == 0) {
                    continue;
                }
                String word = parts[0].toLowerCase();
                int frequency = 0;
                if (parts.length > 1) {
                    try {
                        frequency = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        frequency = 0;
                    }
                }
                commonEnglishWords.add(new EnglishWordEntry(word, frequency));
                if (frequency > maxEnglishFrequency) {
                    maxEnglishFrequency = frequency;
                }
            }
        } catch (IOException e) {
            // Handle exceptions, e.g., by logging an error
            Log.e("MKeyboardView", "Error loading English dictionary", e);
        } finally {
            try {
                readerEnglish.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if ((me.getAction() == MotionEvent.ACTION_DOWN && !stopShowingSurroundingLetters && Objects.equals(currentKeyboarrdLayout, hahuLayoutName)) ||
                (me.getAction() == MotionEvent.ACTION_UP && Objects.equals(currentKeyboarrdLayout, qwertyLayoutName))) {
            if (!currentSuggestions.isEmpty()) {
                if (handleSuggestionTap((int) me.getX(), (int) me.getY())) {
                    return true;
                }
            }
        }
        if (fidelPressed && getWhichBoxTouched() != 100 && me.getAction() != MotionEvent.ACTION_UP ) {
            // If we are currently selecting a sub-letter (sliding),
            // we manually trigger our own redraw and DO NOT call super.
            invalidate();
            return true; // We handled it, don't let KeyboardView logic interfere
        }
        return super.onTouchEvent(me);
    }

    @Override
    protected boolean onLongPress(Keyboard.Key key) {
        if (key != null && key.codes != null && key.codes.length > 0 && key.codes[0] == 32) {
            if (mService != null) {
                mService.showThemePicker();
                return true;
            }
        }
        return super.onLongPress(key);
    }

    public String universalTrim(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "";
        }

        // 1. Get the last segment based on spaces
        String[] segments = inputText.split("\\s+");
        String lastSegment = segments[segments.length - 1];

        // 2. Iterate through the last segment to find the "break point"
        // We look for the last character that ISN'T Amharic or English
        for (int i = 0; i < lastSegment.length(); i++) {
            char character = lastSegment.charAt(i);

            if (!isAmharicOrEnglish(character)) {
                // Split by this "illegal" character and take the last part
                String[] parts = lastSegment.split(Pattern.quote(String.valueOf(character)));
                if (parts.length > 0) {
                    lastSegment = parts[parts.length - 1];
                } else {
                    lastSegment = ""; // Case where character is at the very end
                }
                // Reset loop to check the new lastSegment for further non-alphanumeric chars
                i = -1;
            }
        }
        return lastSegment;
    }

    /**
     * Checks if a character is within the Ge'ez (Amharic) block
     * or the standard English alphabet.
     */
    private boolean isAmharicOrEnglish(char c) {
        // Amharic Unicode Range: 4608 (0x1200) to 4952 (0x1358)
        boolean isAmharic = (c >= 4608 && c <= 4952);

        // English Alphabet: A-Z (65-90) and a-z (97-122)
        boolean isEnglish = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');

        // Optional: Allow numbers or apostrophes (e.g., "don't")
        boolean isExtra = Character.isDigit(c) || c == '\'';

        return isAmharic || isEnglish || isExtra;
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

        if (!(charCode >= 4608 && charCode < 4952) && !(charCode >= 65 && charCode <= 122) ) {
            // The last character is NOT a valid Amharic letter (it might be '.', '?', ',', etc.).
            // So, we should not show any suggestions.
            return; // Exit the method.
        }

        String finalWord = universalTrim(allWords);

        Paint paint = new Paint();
        paint.setTextSize(45);
        paint.setColor(suggestionTextColor);
        paint.setTextAlign(Paint.Align.LEFT); // Important for accurate positioning

        // Get the list of suggestions
        if(currentKeyboarrdLayout.equals(hahuLayoutName)) {
            currentSuggestions = findClosestMatchAmharic(finalWord);
        } else if(currentKeyboarrdLayout.equals(qwertyLayoutName)) {
            currentSuggestions = findClosestMatchEnglish(finalWord);
        }

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
            boolean isPrimaryKey = (key.codes[0] >= 4608 && key.codes[0] < 4952)
                    || (key.codes[0] >= 65 && key.codes[0] <= 122)
                    || (key.codes[0] >= 48 && key.codes[0] <= 57)
                    || key.codes[0] == 32;

            int keyBackground = isPrimaryKey ? keyBackgroundPrimary : keyBackgroundSecondary;
            if (currentKeyboarrdLayout.equals(hahuLayoutName) && isCapsToggleKey(key.codes[0])) {
                if (capsLockActive) {
                    keyBackground = capsHighlightColor;
                } else if (shiftHighlightActive) {
                    keyBackground = shiftHighlightColor;
                }
            }
            int keyTextColor = isPrimaryKey ? keyTextPrimary : keyTextSecondary;

            float inset = dpToPx(2.5f);
            float radius = dpToPx(10f);
            Paint rectangle = new Paint();
            rectangle.setColor(keyBackground);
            rectangle.setStrokeWidth(10);
            RectF rect = new RectF(
                    key.x + inset,
                    key.y + inset,
                    key.x + key.width - inset,
                    key.y + key.height - inset
            );
            canvas.drawRoundRect(rect, radius, radius, rectangle);

            setKeyText(canvas, key, keyTextColor);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private int adjustPopupColor(int baseColor) {
        double luminance = (0.299 * Color.red(baseColor)
                + 0.587 * Color.green(baseColor)
                + 0.114 * Color.blue(baseColor)) / 255.0;
        if (luminance > 0.5) {
            return shadeColor(baseColor, 0.75f);
        }
        return shadeColor(baseColor, 1.25f);
    }

    private int shadeColor(int color, float factor) {
        int red = Math.min(255, Math.max(0, Math.round(Color.red(color) * factor)));
        int green = Math.min(255, Math.max(0, Math.round(Color.green(color) * factor)));
        int blue = Math.min(255, Math.max(0, Math.round(Color.blue(color) * factor)));
        return Color.rgb(red, green, blue);
    }

    private int adjustCapsHighlightColor(int baseColor) {
        double luminance = (0.299 * Color.red(baseColor)
                + 0.587 * Color.green(baseColor)
                + 0.114 * Color.blue(baseColor)) / 255.0;
        if (luminance > 0.5) {
            return shadeColor(baseColor, 0.7f);
        }
        return shadeColor(baseColor, 1.3f);
    }

    private int adjustShiftHighlightColor(int baseColor) {
        double luminance = (0.299 * Color.red(baseColor)
                + 0.587 * Color.green(baseColor)
                + 0.114 * Color.blue(baseColor)) / 255.0;
        if (luminance > 0.5) {
            return shadeColor(baseColor, 0.85f);
        }
        return shadeColor(baseColor, 1.15f);
    }

    private boolean isCapsToggleKey(int code) {
        for (int toggleCode : CAPS_TOGGLE_CODES) {
            if (toggleCode == code) {
                return true;
            }
        }
        return false;
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

    public List<String> findClosestMatchEnglish(String inputWord) {
        if (inputWord == null || inputWord.trim().isEmpty()) {
            return Collections.emptyList();
        }

        inputWord = inputWord.trim().toLowerCase();
        int inputLength = inputWord.length();
        char firstChar = inputWord.charAt(0);

        // Short inputs should prioritize prefix matches for stability.
        if (inputLength <= 2) {
            List<String> prefixMatches = new ArrayList<>();
            for (EnglishWordEntry entry : commonEnglishWords) {
                String lowerWord = entry.word;
                if (lowerWord.startsWith(inputWord)) {
                    prefixMatches.add(entry.word);
                    if (prefixMatches.size() >= 15) {
                        break;
                    }
                }
            }
            if (!prefixMatches.isEmpty()) {
                return prefixMatches;
            }
        }

        String inputSoundex = getSoundex(inputWord);

        List<WordDistance> wordScores = new ArrayList<>();

        for (EnglishWordEntry entry : commonEnglishWords) {
            String lowerWord = entry.word;

            int lengthDiff = Math.abs(lowerWord.length() - inputLength);
            boolean startsWithInput = lowerWord.startsWith(inputWord);
            boolean containsInput = !startsWithInput && lowerWord.contains(inputWord);
            boolean soundexMatch = getSoundex(lowerWord).equals(inputSoundex);

            if (inputLength >= 3) {
                if (lengthDiff > 4 && !startsWithInput) {
                    continue;
                }
                if (lowerWord.charAt(0) != firstChar && !startsWithInput && !soundexMatch) {
                    continue;
                }
            }

            // 1. Damerau-Levenshtein handles common swaps better than plain edit distance.
            double levScore = damerauLevenshteinDistance(inputWord, lowerWord);

            // 2. Phonetic Score (bonus if they sound the same)
            double phoneticScore = soundexMatch ? -0.6 : 0.0;

            // 3. Prefix + containment bonus (strongly favor words that start the same)
            double prefixBonus = 0.0;
            if (startsWithInput) {
                prefixBonus = -(2.8 + (inputLength * 0.2));
            } else if (containsInput) {
                prefixBonus = -0.6;
            }

            // 4. Light penalty for first-letter mismatch
            double firstLetterPenalty = 0.0;
            if (!lowerWord.isEmpty() && lowerWord.charAt(0) != firstChar) {
                firstLetterPenalty = 0.4;
            }

            // 5. Length penalty to avoid overly long candidates
            double lengthPenalty = lengthDiff * 0.12;

            // 6. Frequency bonus (higher frequency => better ranking)
            double frequencyBonus = 0.0;
            if (entry.frequency > 0 && maxEnglishFrequency > 0) {
                double freqNorm = Math.log10(entry.frequency) / Math.log10(maxEnglishFrequency);
                frequencyBonus = -1.2 * freqNorm;
            }

            // 7. Exact match bonus
            double exactBonus = lowerWord.equals(inputWord) ? -6.0 : 0.0;

            double finalScore = levScore + phoneticScore + prefixBonus + firstLetterPenalty
                    + lengthPenalty + frequencyBonus + exactBonus;

            wordScores.add(new WordDistance(entry.word, finalScore));
        }

        // Sort by score ascending
        wordScores.sort(Comparator.comparingDouble(wd -> wd.distance));

        List<String> closestMatches = new ArrayList<>();
        for (int i = 0; i < wordScores.size() && i < 15; i++) {
            closestMatches.add(wordScores.get(i).word);
        }

        return closestMatches;
    }

    /**
     * Simplified Soundex Algorithm
     * Converts words to a code representing their sound (e.g., "Robert" & "Rupert" = R163)
     */
    private String getSoundex(String s) {
        if (s == null || s.isEmpty()) return "";
        s = s.toUpperCase();

        char firstLetter = s.charAt(0);
        StringBuilder code = new StringBuilder().append(firstLetter);

        for (int i = 1; i < s.length() && code.length() < 4; i++) {
            char c = getSoundexCode(s.charAt(i));
            // Don't add if same as previous or '0' (vowels/ignored)
            if (c != '0' && c != code.charAt(code.length() - 1)) {
                code.append(c);
            }
        }

        while (code.length() < 4) code.append('0');
        return code.toString();
    }

    private char getSoundexCode(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V': return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z': return '2';
            case 'D': case 'T': return '3';
            case 'L': return '4';
            case 'M': case 'N': return '5';
            case 'R': return '6';
            default: return '0'; // Vowels, H, W, Y are ignored
        }
    }



    /**
     * A more advanced method to find the 5 closest Amharic words.
     * @param inputWord The word the user is typing.
     * @return A list of the 5 best suggestions.
     */
    public List<String> findClosestMatchAmharic(String inputWord) {
        if (inputWord == null || inputWord.trim().isEmpty()) {
            return Collections.emptyList();
        }

        inputWord = inputWord.trim();
        int inputLength = inputWord.length();
        int inputFamily = getAmharicFamilyIndex(inputWord.charAt(0));

        if (inputLength <= 1) {
            List<String> prefixMatches = new ArrayList<>();
            for (String word : commonAmharicWords) {
                if (word.startsWith(inputWord)) {
                    prefixMatches.add(word);
                    if (prefixMatches.size() >= 15) {
                        break;
                    }
                }
            }
            if (!prefixMatches.isEmpty()) {
                return prefixMatches;
            }
        }

        // A list to hold words and their calculated scores.
        List<WordDistance> wordScores = new ArrayList<>();

        for (String word : commonAmharicWords) {
            if (word == null || word.isEmpty()) {
                continue;
            }

            int lengthDiff = Math.abs(word.length() - inputLength);
            boolean startsWithInput = word.startsWith(inputWord);
            boolean containsInput = !startsWithInput && word.contains(inputWord);

            if (inputLength >= 2 && lengthDiff > 4 && !startsWithInput) {
                continue;
            }

            // Calculate the score for each metric
            double levenshteinScore = damerauLevenshteinDistance(inputWord, word) * WEIGHT_LEVENSHTEIN;
            double abugidaScore = abugidaProximityDistance(inputWord, word) * WEIGHT_ABUGIDA;

            double prefixBonus = 0.0;
            if (startsWithInput) {
                prefixBonus = -(2.8 + (inputLength * 0.2));
            } else if (containsInput) {
                prefixBonus = -0.6;
            }

            double familyPenalty = 0.0;
            int candidateFamily = getAmharicFamilyIndex(word.charAt(0));
            if (inputFamily != -1 && candidateFamily != -1 && inputFamily != candidateFamily) {
                familyPenalty = 0.6;
            }

            double lengthPenalty = lengthDiff * 0.12;
            double exactBonus = word.equals(inputWord) ? -6.0 : 0.0;

            double finalScore = levenshteinScore + abugidaScore + prefixBonus
                    + familyPenalty + lengthPenalty + exactBonus;

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
            int base1 = getAmharicFamilyIndex(c1);
            int base2 = getAmharicFamilyIndex(c2);

            if (base1 != -1 && base2 != -1 && base1 == base2) {
                totalDistance += 0.1; // Very small penalty for being in the same family
            } else {
                totalDistance += 1.0; // Full penalty for being different families
            }
        }
        return totalDistance;
    }

    private int getAmharicFamilyIndex(char c) {
        if (c < 4608 || c > 4952) {
            return -1;
        }
        return (c - 4608) / 8;
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

    private static class EnglishWordEntry {
        String word;
        int frequency;

        EnglishWordEntry(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
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

    private int damerauLevenshteinDistance(String s1, String s2) {
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
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                int deletion = dp[i - 1][j] + 1;
                int insertion = dp[i][j - 1] + 1;
                int substitution = dp[i - 1][j - 1] + cost;

                int value = Math.min(Math.min(deletion, insertion), substitution);

                if (i > 1 && j > 1
                        && s1.charAt(i - 1) == s2.charAt(j - 2)
                        && s1.charAt(i - 2) == s2.charAt(j - 1)) {
                    value = Math.min(value, dp[i - 2][j - 2] + cost);
                }

                dp[i][j] = value;
            }
        }

        return dp[len1][len2];
    }
}
