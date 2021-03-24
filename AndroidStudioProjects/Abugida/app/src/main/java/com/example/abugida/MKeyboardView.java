package com.example.abugida;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;

import com.example.abogida.R;

import java.util.ArrayList;
import java.util.List;

public class MKeyboardView extends KeyboardView {
    public MKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    static String fetchedEditTextValue = "";
    static String currentKeyboarrdLayout = "hahu";
    static String hahuLayoutName = "hahu";
    static String qwertyLayoutName = "qwerty";
    static String numbersLayoutName = "numbers";
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
                            paint.setColor(Color.RED);

                            Paint rectangle = new Paint();
                            rectangle.setStrokeWidth(10);
                            int childPrimaryCode = key.codes[0] + whichChildLetter(i);
                            char code = (char) childPrimaryCode;
                            if (i == 0) {
                                rectangle.setColor(Color.rgb(20, 50, 150));
                                canvas.drawRect(key.x, key.y - key.height, key.x + key.width, key.y, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), key.y - (key.height / 3), paint);
                            } else if (i == 1) {
                                rectangle.setColor(Color.BLUE);
                                canvas.drawRect(key.x + key.width, key.y - key.height, key.x + (2 * key.width), key.y, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), key.y - (key.height / 3), paint);
                            }
                            if (i == 2) {
                                rectangle.setColor(Color.rgb(20, 50, 150));
                                canvas.drawRect(key.x + key.width, key.y, key.x + (2 * key.width), key.y + key.height, rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                            }
                            if (i == 3) {
                                rectangle.setColor(Color.BLUE);
                                canvas.drawRect(key.x + key.width, key.y + key.height, key.x + (2 * key.width), key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), key.x + key.width + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 4) {
                                rectangle.setColor(Color.rgb(20, 50, 150));
                                canvas.drawRect(key.x, key.y + key.height, key.x + key.width, key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), key.x + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 5) {
                                rectangle.setColor(Color.BLUE);
                                canvas.drawRect(key.x - key.width, key.y + key.height, key.x, key.y + (2 * key.height), rectangle);
                                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + (2 * key.height)) - (key.height / 3), paint);
                            }
                            if (i == 6) {
                                rectangle.setColor(Color.rgb(20, 50, 150));
                                canvas.drawRect(key.x - key.width, key.y, key.x, key.y + key.height, rectangle);
                                canvas.drawText(String.valueOf(code), (key.x - key.width) + (key.width / 4), (key.y + key.height) - (key.height / 3), paint);
                            }
                            if (i == 7) {
                                rectangle.setColor(Color.BLUE);
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

    public void displayWord(Canvas canvas){

        String allWords = fetchedEditTextValue;
        String[] words = allWords.split(" ");
        String finalWord = words[words.length-1];
        /*if(wordStarted){
            for(int i=0; i<wordFormationList.size(); i++){
                char code = (char) (int)wordFormationList.get(i);
                finalWord = finalWord + code;
            }
        }*/
        Paint paint = new Paint();
        paint.setTextSize(45);
        paint.setColor(Color.rgb(195, 195, 195));


        canvas.drawText(finalWord, 250, 65, paint);

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
            if(key.codes[0] >= 4608 && key.codes[0] < 4952) {
                Drawable dr = this.getResources().getDrawable(R.drawable.rounded_keys);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);

                int keyTextColor = Color.rgb(240, 240, 240);

                setKeyText(canvas, key, keyTextColor);
            }else {
                Drawable dr = this.getResources().getDrawable(R.drawable.black_background);
                dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                dr.draw(canvas);

                int keyTextColor = Color.YELLOW;

                setKeyText(canvas, key, keyTextColor);
            }
        }
    }

    private void setKeyText(Canvas canvas, Keyboard.Key key, int keyTextColor){

        int primaryCode = key.codes[0];
        //char code = (char) childPrimaryCode;
        if((primaryCode >= 4608 && primaryCode < 4952) || (primaryCode > 47 && primaryCode < 150)) {
            Paint paint = new Paint();
            paint.setTextSize(50);
            paint.setColor(keyTextColor);

            String label = (String) key.label;
            canvas.drawText(String.valueOf(label), key.x + (key.width / 3), key.y + (2 * key.height / 3), paint);
        }else{
            Paint paint = new Paint();
            paint.setTextSize(25);
            paint.setColor(keyTextColor);

            String label = (String) key.label;
            canvas.drawText(String.valueOf(label), key.x + (key.width / 4), key.y + (3 * key.height / 4), paint);
        }



    }
}
