package com.example.abugida;

import android.content.ClipDescription;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.example.abogida.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;

public class SoftKeyboard extends InputMethodService
        implements MKeyboardView.OnKeyboardActionListener {

    //private KeyboardView kv;
    private MKeyboardView kv;
    private Keyboard keyboard;
    private boolean isShifted = false;
    //private double factor = 0.3;
    private double factor = 0;
    PopupWindow popup;
    //PopupWindow popup2;


    private boolean caps = false;
    int touchingPrimaryCode;
    int  pressedPrimaryCode;

    public boolean otherButtonsLocked = false;
    public boolean onPressedCalled = false;

    List<Keyboard.Key> keyList;
    List<Integer> rightHandCodeList = new ArrayList<>();
    List<Double> slopeList = new ArrayList<Double>();
    List<Double> offsetList = new ArrayList<Double>();
    List<Double> angleList = new ArrayList<Double>();
    List<Double> endPointListX = new ArrayList<Double>();
    List<Double> endPointListY = new ArrayList<Double>();

    double touchingPointX, touchingPointY;
    double pressedPointX, pressedPointY;

    double leftX, topY;
    double touchingLeftX, touchingTopY;
    double rightX, buttomY;
    double touchingRightX, touchingButtomY;
    double centerX,centerY;
    double touchingCenterX,touchingCenterY;
    double keyWidth, keyHeight;
    double touchingKeyWidth, touchingKeyHeight;
    double pWidth=200;
    double pHeight=100;


    static final boolean DEBUG = false;

    static final boolean PROCESS_HARD_KEYS = true;
    private InputMethodManager mInputMethodManager;
    private KeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;

    private Keyboard mSymbolsKeyboard;
    private Keyboard mSymbolsShiftedKeyboard;
    private Keyboard mQwertyKeyboard;

    private Keyboard mCurKeyboard;

    private String mWordSeparators;


    Button pop;

    private int currentSwipingBox = 100;


    @Override
    public View onCreateInputView() {
        Log.d("debugging","is it: here");
        Log.d("debugging","ascii፡: " + (int)'.');
        Log.d("debugging","ascii።: " + (int)',');
        Log.d("debugging","ascii፤: " + (int)'ጓ');
        Log.d("debugging","ascii፣: " + (int)'ጿ');
        Log.d("debugging","ascii፥: " + (int)'፥');
        Log.d("debugging","ascii፦: " + (int)'፦');
        Log.d("debugging","ascii፧: " + (int)'፧');
        Log.d("debugging","ascii3፨: " + (int)'፨');
        Log.d("debugging","ascii፠: " + (int)'፠');




        kv = (MKeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        setKeyBoardLayout(MKeyboardView.hahuLayoutName);
        // keyboard = new Keyboard(this, R.xml.hahu);

        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        kv.setService(this);
        kv.setKeyboard(keyboard);
        kv.setDrawingCacheEnabled(true);
        mPredictionOn = false;

        for (Keyboard.Key key : keyboard.getKeys()) {
            // Find 'ሀ' and add 'ሐ' as superscript
            if (key.codes[0] == 4608) { // ሀ
                key.label = createStyledLabel("ሀ", "ሐ");
            }
            // Find 'ሰ' and add 'ሠ' as superscript
            else if (key.codes[0] == 4656) { // ሰ
                key.label = createStyledLabel("ሰ", "ሠ");
            }
            // Find 'በ' and add 'ቨ' as superscript
            else if (key.codes[0] == 4704) { // በ
                key.label = createStyledLabel("በ", "ቨ");
            }
            // Find 'አ' and add 'ዐ' as superscript
            else if (key.codes[0] == 4768) { // አ
                key.label = createStyledLabel("አ", "ዐ");
            }
            // Find 'ፀ' and add 'ጸ' as superscript
            else if (key.codes[0] == 4928) { // ፀ
                key.label = createStyledLabel("ፀ", "ጸ");
            }
            // Find 'ኸ' and add 'ኀ' as superscript
            else if (key.codes[0] == 4792) { // ኸ
                key.label = createStyledLabel("ኸ", "ኀ");
            }
        }

        popup = new PopupWindow(this);

                View custom = LayoutInflater.from(this).inflate(R.layout.popup,new FrameLayout(this));
                PopupWindow popup = new PopupWindow(this);
                if(custom.getParent() != null){
                    ((ViewGroup)custom.getParent()).removeView(custom);
                }
                popup.setContentView(custom);

        //retrieveKeys();

        kv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MKeyboardView.setXZ((int)event.getX());
                MKeyboardView.setYZ((int)event.getY());

                //get if child letter is changed late after swiping and vibrate
                if(otherButtonsLocked) {
                    //the if condition prevents it from running before on press called and after on release called
                    int temp = whichBox(event.getX(), event.getY());
                    Log.d("DebuggingTemp", String.valueOf(temp));
                    if (temp != currentSwipingBox && temp != 100) {
                        //vibrate
                        vibrate();

                        currentSwipingBox = temp;
                    }
                }

                if(onPressedCalled){
                    Log.d("Debugging", "children: of 0 which:" + whichBox(event.getX(), event.getY()));
                    MKeyboardView.setWhichBoxTouched(whichBox(event.getX(), event.getY()));
                    MKeyboardView.setPressedFidelPrimaryCode(pressedPrimaryCode);
                    Log.d("Debugging", "box returned: fidelPressed" + whichBox(event.getX(), event.getY()));
                }

                for(Keyboard.Key k: keyList){
                    if(k.isInside((int) event.getX(), (int) event.getY())) {
                        Log.d("Debugging", "Key pressed: X=" + event.getX() + " - Y=" + event.getY());
                        rightHandCodeList.add(4656);
                        rightHandCodeList.add(4664);
                        rightHandCodeList.add(4672);
                        rightHandCodeList.add(4704);
                        rightHandCodeList.add(4912);
                        rightHandCodeList.add(4760);
                        rightHandCodeList.add(4768);
                        rightHandCodeList.add(4776);
                        rightHandCodeList.add(4808);
                        rightHandCodeList.add(4824);
                        rightHandCodeList.add(4896);
                        rightHandCodeList.add(4904);
                        rightHandCodeList.add(4928);
                        rightHandCodeList.add(4936);
                        if(rightHandCodeList.contains(k.codes[0])){
                            Log.d("Debugging", "Shifting: sure");
                            /*touchingLeftX = k.x - k.width;
                            touchingTopY = k.y;
                            touchingRightX = k.x + k.width - k.width;
                            touchingButtomY = k.y + k.height;
                            touchingCenterX = k.x + (k.width/2) - k.width;
                            touchingCenterY = k.y + (k.height/2);*/
                            touchingKeyWidth = k.width;
                            touchingKeyHeight = k.height;
                            for(Keyboard.Key l: keyList) {
                                if (l.isInside((int) event.getX() - (k.width), (int) event.getY())) {
                                    if((double)event.getX() >= k.x && (double)event.getX() <= k.x + (touchingKeyWidth * (8/10))){
                                        touchingPrimaryCode = l.codes[0];
                                        touchingLeftX = l.x + (touchingKeyWidth * (8/10));
                                        touchingTopY = k.y;
                                        touchingRightX = l.x + k.width + (touchingKeyWidth * (8/10));
                                        touchingButtomY = k.y + k.height;
                                        touchingCenterX = l.x + (k.width/2) + (touchingKeyWidth * (8/10));
                                        touchingCenterY = k.y + (k.height/2);
                                    }else if((double)event.getX() < k.x + k.width && (double)event.getX() > k.x + (touchingKeyWidth * (8/10))){
                                        touchingPrimaryCode = k.codes[0];
                                        touchingLeftX = k.x + (touchingKeyWidth * (8/10));
                                        touchingTopY = k.y;
                                        touchingRightX = k.x + k.width + (touchingKeyWidth * (8/10));
                                        touchingButtomY = k.y + k.height;
                                        touchingCenterX = k.x + (k.width/2) + (touchingKeyWidth * (8/10));
                                        touchingCenterY = k.y + (k.height/2);
                                    }

                                }
                            }
                        }else{
                            touchingPrimaryCode = k.codes[0];
                            touchingLeftX = k.x;
                            touchingTopY = k.y;
                            touchingRightX = k.x + k.width;
                            touchingButtomY = k.y + k.height;
                            touchingCenterX = k.x + (k.width/2);
                            touchingCenterY = k.y + (k.height/2);
                        }
                        touchingPointX = event.getX();
                        touchingPointY = event.getY();

                        touchingKeyWidth = k.width;
                        touchingKeyHeight = k.height;
                        //Log.d("Debugging", "Centre of the key pressed: X=" + topX + " - Y=" + topY);

                        rightHandCodeList.clear();
                    }
                }

                /*for(Keyboard.Key k: keyList){
                    if(onPressedCalled){
                        if(k.codes[0] == pressedPrimaryCode){
                            if(!k.isInside((int) event.getX(), (int) event.getY())) {
                                if(kv.isPreviewEnabled()){
                                    kv.setPreviewEnabled(false);
                                }
                            }
                        }
                    }
                }*/
                return false;
            }
        });
        return kv;
    }

    @NonNull
    Context getDisplayContext() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            // createDisplayContext is not available.
            return this;
        }
        // TODO (b/133825283): Non-activity components Resources / DisplayMetrics update when
        //  moving to external display.
        // An issue in Q that non-activity components Resources / DisplayMetrics in
        // Context doesn't well updated when the IME window moving to external display.
        // Currently we do a workaround is to create new display context directly and re-init
        // keyboard layout with this context.
        final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        return createDisplayContext(wm.getDefaultDisplay());
    }

    @Override public void onInitializeInterface() {
        final Context displayContext = getDisplayContext();
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new Keyboard(displayContext, R.xml.qwerty);
        mSymbolsKeyboard = new Keyboard(displayContext, R.xml.symbols);
        mSymbolsShiftedKeyboard = new Keyboard(displayContext, R.xml.symbols_shift);
    }



    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(getDisplayContext());
        mCandidateView.setService(this);
        return mCandidateView;
    }


    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        retrieveKeys();

        mComposing.setLength(0);
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = true;
        mCompletionOn = false;
        mCompletions = null;

        updateShiftKeyState(info);

        //get mime types supported by the app to send emojis
        String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(info);


        boolean gifSupported = false;
        for (String mimeType : mimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, "image/gif")) {
                Log.d("DebuggingEmoji", "GifSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/jpeg")) {
                Log.d("DebuggingEmoji", "JpegSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/jpg")) {
                Log.d("DebuggingEmoji", "JpgSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/png")) {
                Log.d("DebuggingEmoji", "PngSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/x-ms-bmp")) {
                Log.d("DebuggingEmoji", "BmpSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/vnd.wap.wbmp")) {
                Log.d("DebuggingEmoji", "WbmpSupported");
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/webp")) {
                Log.d("DebuggingEmoji", "WebpSupported");
                // gifSupported = true;
            }
        }

        if (gifSupported) {
            // the target editor supports GIFs. enable corresponding content
        } else {
            // the target editor does not support GIFs. disable corresponding content
        }

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if(!otherButtonsLocked) {
            InputConnection ic = getCurrentInputConnection();
            playClick(primaryCode);
            Keyboard currentKeyboard = kv.getKeyboard();
            if (currentKeyboard == null);
            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    ic.deleteSurroundingText(1, 0);
                    break;
                // ~ line 440
                case Keyboard.KEYCODE_SHIFT:
                    // Toggle the shift state flag
                    isShifted = !isShifted;

                    // Find and update the specific keys
                    for (Keyboard.Key key : currentKeyboard.getKeys()) {
                        if (key.codes[0] == -1) {
                            if (isShifted) {
                                key.icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_solid);
                            } else {
                                key.icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_hollow);
                            }
                        }
                        // Check for the 'ሀ'/'ሐ' key (Unicode 4608 and 4624)
                        else if (key.codes[0] == 4608 || key.codes[0] == 4624) {
                            if (isShifted) {
                                key.label = "ሐ";
                                key.codes[0] = 4624;
                            } else {
                                key.label = createStyledLabel("ሀ", "ሐ");;
                                key.codes[0] = 4608;
                            }
                        }
                        // Check for the 'ሰ'/'ሠ' key (Unicode 4656 and 4640)
                        else if (key.codes[0] == 4656 || key.codes[0] == 4640) {
                            if (isShifted) {
                                key.label = "ሠ";
                                key.codes[0] = 4640;
                            } else {
                                key.label = createStyledLabel("ሰ", "ሠ");
                                key.codes[0] = 4656;
                            }
                        }

                        // Check for the 'በ'/'ቨ' key (Unicode 4704 and 4712)
                        else if (key.codes[0] == 4704 || key.codes[0] == 4712) {
                            if (isShifted) {
                                key.label = "ቨ";
                                key.codes[0] = 4712;
                            } else {
                                key.label = createStyledLabel("በ", "ቨ");
                                key.codes[0] = 4704;
                            }
                        }

                        // Check for the 'አ'/'ዐ' key (Unicode 4768 and 4816)
                        else if (key.codes[0] == 4768 || key.codes[0] == 4816) {
                            if (isShifted) {
                                key.label = "ዐ";
                                key.codes[0] = 4816;
                            } else {
                                key.label = createStyledLabel("አ", "ዐ");
                                key.codes[0] = 4768;
                            }
                        }

                        // Check for the 'ፀ'/'ጸ' key (Unicode 4928 and 4920)
                        else if (key.codes[0] == 4928 || key.codes[0] == 4920) {
                            if (isShifted) {
                                key.label = "ጸ";
                                key.codes[0] = 4920;
                            } else {
                                key.label = createStyledLabel("ፀ", "ጸ");
                                key.codes[0] = 4928;
                            }
                        }

                        // Check for the 'ኸ'/'ኀ' key (Unicode 4736 and 4784)
                        else if (key.codes[0] == 4736 || key.codes[0] == 4792) {
                            if (isShifted) {
                                key.label = "ኀ";
                                key.codes[0] = 4736;
                            } else {
                                key.label = createStyledLabel("ኸ", "ኀ");
                                key.codes[0] = 4792;
                            }
                        }

                        // Check for standard English letters (a-z and A-Z)
                        else if (key.codes[0] >= 65 && key.codes[0] <= 122) {
                            String label = key.label.toString();
                            if (isShifted) {
                                key.label = label.toUpperCase();
                                key.codes[0] = Character.toUpperCase(key.codes[0]);
                            } else {
                                key.label = label.toLowerCase();
                                key.codes[0] = Character.toLowerCase(key.codes[0]);
                            }
                        }
                    }

                    // This tells the keyboard that the shift state has changed for the icon
                    currentKeyboard.setShifted(isShifted);

                    // This forces the keyboard to redraw all keys with the new labels
                    kv.invalidateAllKeys();
                    break;

                case Keyboard.KEYCODE_DONE:
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    break;
                case 10000:
                    setKeyBoardLayout(MKeyboardView.qwertyLayoutName);
                    break;
                case 10001:
                    setKeyBoardLayout(MKeyboardView.hahuLayoutName);
                    break;
                case 10002:
                    setKeyBoardLayout(MKeyboardView.numbersLayoutName);
                    break;
                case 10003:
                    if(MKeyboardView.currentKeyboarrdLayout.equals(MKeyboardView.hahuLayoutName)){
                        setKeyBoardLayout(MKeyboardView.hahuLayoutName);
                    }else if(MKeyboardView.currentKeyboarrdLayout.equals(MKeyboardView.qwertyLayoutName)){
                        setKeyBoardLayout(MKeyboardView.qwertyLayoutName);
                    }

                    break;
                default:
                    if (primaryCode < 4608 || primaryCode >= 4952) {
                        char code = (char) primaryCode;
                        ic.commitText(String.valueOf(code), 1);
                        if (primaryCode >= 65 && primaryCode <= 90 && currentKeyboard.isShifted()){
                            isShifted = !isShifted;
                            for (Keyboard.Key key : currentKeyboard.getKeys()) {
                                if (key.codes[0] == -1) {
                                    key.icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_hollow);
                                }
                                else if (key.codes[0] >= 65 && key.codes[0] <= 90) {
                                    String label = key.label.toString();
                                    key.label = label.toLowerCase();
                                    key.codes[0] = Character.toLowerCase(key.codes[0]);
                                }
                            }
                            currentKeyboard.setShifted(isShifted);
                        }
                    }
                    /*char code = (char)primaryCode;
                    if(Character.isLetter(code) && caps){
                        code = Character.toUpperCase(code);
                    }
                    ic.commitText(String.valueOf(code),1);*/
            }

            //fetch string on edit text
            ExtractedText extracted = ic.getExtractedText(new ExtractedTextRequest(), 0);
            if(extracted != null){
                MKeyboardView.fetchedEditTextValue = (String) extracted.text;
            }

        }else{
            //onPressedCalled = false;
            //kv.setPreviewEnabled(true);
        }
    }

    @Override
    public void onPress(int primaryCode) {

        if(primaryCode >= 4608 && primaryCode < 4952) {
            otherButtonsLocked = true;
            setPressedParameters(primaryCode);
            onPressedCalled = true;
            MKeyboardView.setFidelPressed(true);

            //lineEquationsToList();

            if (primaryCode % 8 == 0) {
                final InputConnection ic = getCurrentInputConnection();
                /*KeyboardView custom = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
                Keyboard popupKeys = new Keyboard(this, R.xml.zer_popup);
                custom.setKeyboard(popupKeys);
                custom.setOnKeyboardActionListener(this);
                popup = new PopupWindow(this);*/

                /*View custom = LayoutInflater.from(this).inflate(R.layout.popup,new FrameLayout(this));
                PopupWindow popup = new PopupWindow(this);
                if(custom.getParent() != null){
                    ((ViewGroup)custom.getParent()).removeView(custom);
                }
                popup.setContentView(custom);
                //popup.setTouchable(true);
                //popup.setFocusable(false);
                //popup.setOutsideTouchable(false);


                /*if (popup.isShowing()) {
                    popup.update(leftX, topY, pWidth, pHeight);
                } else {
                    popup.setWidth(pWidth);
                    popup.setHeight(pHeight);
                    popup.showAtLocation(kv, Gravity.NO_GRAVITY, leftX, topY);
                }

                pop = (Button)custom.findViewById(R.id.button);
                pop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        char code = (char)4672;
                        ic.commitText(String.valueOf(code),1);
                    }
                });

                /*popup2 = new PopupWindow(this);
                if(custom.getParent() != null){
                    ((ViewGroup)custom.getParent()).removeView(custom);
                }
                popup2.setContentView(custom);

                if (popup2.isShowing()) {
                    popup2.update(topX, buttomY, pWidth, pHeight);
                } else {
                    popup2.setWidth(pWidth);
                    popup2.setHeight(pHeight);
                    popup2.showAtLocation(kv, Gravity.NO_GRAVITY, topX, buttomY);
                }*/
            }
        }else{
            //for testing remove -6
            if(primaryCode != -6) {
                //vibrate for other buttons other than hahu
                vibrate();
            }
        }
    }

    @Override
    public void onRelease(int primaryCode) {

        //popup.dismiss();
        /*if(otherButtonsLocked) {
            otherButtonsLocked = false;
            InputConnection ic = getCurrentInputConnection();
            if (isInCircle(touchingPointX, touchingPointY)) {
                if (isInKey(touchingPointX, touchingPointY)) {
                    char code = (char) pressedPrimaryCode;
                    ic.commitText(String.valueOf(code), 1);
                } else {
                    int childPrimaryCode = pressedPrimaryCode + whichSector(touchingPointX, touchingPointY);
                    char code = (char) childPrimaryCode;
                    ic.commitText(String.valueOf(code), 1);
                }
            }
            slopeList.clear();
            offsetList.clear();
            angleList.clear();
            endPointListX.clear();
            endPointListY.clear();
        }*/

        Log.d("Actionaas", String.valueOf(primaryCode));

        if(otherButtonsLocked) {
            currentSwipingBox = 100;
            MKeyboardView.setFidelPressed(false);
            MKeyboardView.setPressedFidelPrimaryCode(0);
            MKeyboardView.setWhichBoxTouched(100);
            otherButtonsLocked = false;
            onPressedCalled = false;
            InputConnection ic = getCurrentInputConnection();

            if (isInKey(touchingPointX, touchingPointY)) {
                //if point release is in the parent key
                //char code = (char) pressedPrimaryCode;
                //ic.commitText(String.valueOf(code), 1);

                //MKeyboardView.getWordFormationList().add(pressedPrimaryCode);
            } else {
                //if point release is in the child key
                int childPrimaryCode = pressedPrimaryCode + whichChildLetter(whichBox(touchingPointX, touchingPointY));
                char code = (char) childPrimaryCode;
                ic.commitText(String.valueOf(code), 1);

                //MKeyboardView.getWordFormationList().add(childPrimaryCode);
            }

            //MKeyboardView.setWordStarted(true);

            //fetch string on edit text
            ExtractedText extracted = ic.getExtractedText(new ExtractedTextRequest(), 0);
            if(extracted != null){
                MKeyboardView.fetchedEditTextValue = (String) extracted.text;
            }
        }


        //previous method of displaying word being written
        /*if(primaryCode == 32 || primaryCode == -4){
            MKeyboardView.getWordFormationList().clear();
            MKeyboardView.setWordStarted(false);
        }else if(primaryCode == -5){
            if(MKeyboardView.getWordFormationList().size() > 0) {
                MKeyboardView.getWordFormationList().remove(MKeyboardView.getWordFormationList().size() - 1);
            }
            MKeyboardView.setWordStarted(true);
        }*/
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }

    // Add this new helper method inside your SoftKeyboard class

    /**
     * Creates a styled CharSequence with a main character and a smaller superscript character.
     * @param mainText The primary character for the key (e.g., "ሀ").
     * @param superText The character to display as a superscript (e.g., "ሐ").
     * @return A CharSequence with the superscript style applied.
     */
    private CharSequence createStyledLabel(String primaryChar, String shiftedChar) {
        // 1. Combine characters with HTML for superscript
        // The <small> tag is a common trick to make the shifted char look like a superscript.
        // We use a small, non-breaking space (&#x200B;) for spacing.
        String html = String.format(
                "<b>%s</b>",
                primaryChar // The small, superscript character
        );

        // 2. Return the parsed HTML as a Spanned object
        return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
    }


    private void playClick(int keyCode){
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(keyCode){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    public void retrieveKeys(){
        keyList = kv.getKeyboard().getKeys();
    }

    public void setPressedParameters(int primaryCode){
        Log.d("Debugging", "children: of 1: param " + touchingPrimaryCode);
        //pressedPrimaryCode = primaryCode;
        pressedPrimaryCode = touchingPrimaryCode;
        pressedPointX = touchingPointX;
        pressedPointY = touchingPointY;
        leftX = touchingLeftX;
        topY = touchingTopY;
        rightX = touchingRightX;
        buttomY = touchingButtomY;
        centerX = touchingCenterX;
        centerY = touchingCenterY;
        keyWidth = touchingKeyWidth;
        keyHeight = touchingKeyHeight;
    }

    public boolean isInRectangle(double pressedX, double pressedY, double releaseX, double releaseY){
        double pressedRight = pressedX + (keyWidth/2);
        double centerRight = centerX + (keyWidth/2);
        double pressedLeft = pressedX - (keyWidth/2);
        double centerLeft = centerX - (keyWidth/2);
        double pressedButtom = pressedY + (keyHeight/2);
        double centerButtom = centerY + (keyHeight/2);
        double pressedTop = pressedY - (keyHeight/2);
        double centerTop = centerY - (keyHeight/2);

        double recTop, recButtom, recLeft, recRight;

        if(pressedRight <= rightX && centerRight <= rightX){
            recRight = Math.max(pressedRight, centerRight);
        }else{
            recRight = rightX;
        }

        if(pressedLeft >= leftX && centerLeft >= leftX){
            recLeft = Math.min(pressedLeft, centerLeft);
        }else{
            recLeft = rightX;
        }

        if(pressedTop >= topY && centerTop >= topY){
            recTop = Math.min(pressedTop, centerTop);
        }else{
            recTop = topY;
        }

        if(pressedButtom <= buttomY && centerButtom <= buttomY){
            recButtom = Math.max(pressedButtom, centerButtom);
        }else{
            recButtom = buttomY;
        }

        if(releaseX >= recLeft && releaseX <= recRight && releaseY >= recTop && releaseY <= recButtom){
            return true;
        }else{
            return false;
        }
    }

    public boolean isInKey(double releaseX, double releaseY){
        if(releaseX >= leftX && releaseX <= rightX && releaseY >= topY && releaseY <= buttomY){
            return true;
        }else{
            return false;
        }
    }

    public boolean isInCircle(double releaseX, double releaseY){
        double radius = Math.sqrt(Math.pow(keyWidth * 2, 2) + Math.pow(keyHeight * 2, 2));
        double distanceFromCentre = Math.sqrt(Math.pow(releaseX - centerX, 2) + Math.pow(releaseY - centerY, 2));

        if(radius >= distanceFromCentre){
            return true;
        }else{
            return false;
        }
    }

    public void lineEquationsToList(){
        double radius = Math.sqrt(Math.pow(keyWidth * 2, 2) + Math.pow(keyHeight * 2, 2));

        slopeList.add(2.41);
        slopeList.add(-2.41);
        slopeList.add(-0.41);
        slopeList.add(0.41);
        slopeList.add(2.41);
        slopeList.add(-2.41);
        slopeList.add(-0.41);
        slopeList.add(0.41);

        offsetList.add(centerY - (2.41 * centerX));
        offsetList.add(centerY - (-2.41 * centerX));
        offsetList.add(centerY - (-0.41 * centerX));
        offsetList.add(centerY - (0.41 * centerX));
        offsetList.add(centerY - (2.41 * centerX));
        offsetList.add(centerY - (-2.41 * centerX));
        offsetList.add(centerY - (-0.41 * centerX));
        offsetList.add(centerY - (0.41 * centerX));

        angleList.add(67.5);
        angleList.add(112.5);
        angleList.add(157.5);
        angleList.add(22.5);
        angleList.add(67.5);
        angleList.add(112.5);
        angleList.add(157.5);
        angleList.add(22.5);

        endPointListX.add(centerX + (Math.cos(Math.toRadians(67.5))*radius));
        endPointListY.add(centerY - (Math.sin(Math.toRadians(67.5))*radius));

        Log.d("Debugging", "close sector: " + centerX);
        Log.d("Debugging", "close sector: " + centerY);

        endPointListX.add(centerX + (Math.cos(Math.toRadians(22.5))*radius));
        endPointListY.add(centerY - (Math.sin(Math.toRadians(22.5))*radius));

        endPointListX.add(centerX + (Math.cos(Math.toRadians(22.5))*radius));
        endPointListY.add(centerY + (Math.sin(Math.toRadians(22.5))*radius));

        endPointListX.add(centerX + (Math.cos(Math.toRadians(67.5))*radius));
        endPointListY.add(centerY + (Math.sin(Math.toRadians(67.5))*radius));

        endPointListX.add(centerX - (Math.cos(Math.toRadians(67.5))*radius));
        endPointListY.add(centerY + (Math.sin(Math.toRadians(67.5))*radius));

        endPointListX.add(centerX - (Math.cos(Math.toRadians(22.5))*radius));
        endPointListY.add(centerY + (Math.sin(Math.toRadians(22.5))*radius));

        endPointListX.add(centerX - (Math.cos(Math.toRadians(22.5))*radius));
        endPointListY.add(centerY - (Math.sin(Math.toRadians(22.5))*radius));

        endPointListX.add(centerX - (Math.cos(Math.toRadians(67.5))*radius));
        endPointListY.add(centerY - (Math.sin(Math.toRadians(67.5))*radius));

        Log.d("Debugging", "close sector: " + centerX);
        Log.d("Debugging", "close sector: " + centerY);
    }

    public double findDistance(double X1, double Y1, double X2, double Y2){
        double distance = Math.sqrt(Math.pow(X2 - X1, 2) + Math.pow(Y2 - Y1, 2));
        return distance;
    }

    public int whichSector(double releaseX, double releaseY){
        double radius = Math.sqrt(Math.pow(keyWidth * 2, 2) + Math.pow(keyHeight * 2, 2));

        double slopeLine = Math.abs(centerY-releaseY)/Math.abs(centerX-releaseX);
        double angleLine = Math.atan(slopeLine) * 180*7/22;

        double closestDistanceYet = 100000000;
        int closestSectorYet = 0;


        for(int i=0; i<8; i++){
            int nextLine = i+1;
            if(i==7){
                nextLine = 0;
            }


            double sumOfDistance = findDistance(endPointListX.get(i), endPointListY.get(i), releaseX, releaseY) + findDistance(endPointListX.get(nextLine), endPointListY.get(nextLine), releaseX, releaseY);

            //Log.d("Debugging", "close sector: jk");
            //Log.d("Debugging", "close sector: " + endPointListX.get(i));
            //Log.d("Debugging", "close sector: " + endPointListY.get(i));
            if(sumOfDistance<closestDistanceYet){
                closestSectorYet = i;
                closestDistanceYet = sumOfDistance;
            }

            /*if(Math.abs(angleLine - angleList.get(i)) < 45 && Math.abs(angleLine - angleList.get(nextLine)) <45){
                if(findDistance(endPointListX.get(i), endPointListY.get(i), releaseX, releaseY) < radius){
                    return i;
                }
            }*/
        }
        Log.d("Debugging", "Close Sector: " + closestSectorYet);
        return closestSectorYet;
    }

    /*public int whichBox(double releaseX, double releaseY){
        if(releaseY <= topY && releaseX >= leftX && releaseX <= rightX){
            return 0;
        }else if(releaseY <= topY && releaseX > rightX){
            return 1;
        }else if(releaseX >= rightX && releaseY > topY && releaseY <= buttomY){
            return 2;
        }else if(releaseY > buttomY && releaseX > rightX){
            return 3;
        }else if(releaseY >= buttomY && releaseX >= leftX && releaseX <= rightX){
            return 4;
        }else if(releaseY > buttomY && releaseX < leftX){
            return 5;
        }else if(releaseX <= leftX && releaseY > topY && releaseY <= buttomY){
            return 6;
        }else if(releaseY <= topY && releaseX < leftX){
            return 7;
        }else{
            return 100;
        }
    }*/

    public int whichBox(double releaseX, double releaseY){
        //if(pressedPrimaryCode != 4608 && pressedPrimaryCode != 4616 && pressedPrimaryCode != 4632 && pressedPrimaryCode != 4648 && pressedPrimaryCode != 4656 && pressedPrimaryCode != 4664 && pressedPrimaryCode != 4672 && pressedPrimaryCode != 4704 && pressedPrimaryCode != 4912){
        Log.d("Debugging", "children: of 0: scope " + pressedPrimaryCode);
        if(pressedPrimaryCode == 4608 || pressedPrimaryCode == 4720) {
            if (releaseY <= topY && releaseX >= leftX && releaseX <= rightX + ((topY - releaseY) * factor)) {
                return 0;
            } else if (releaseY <= topY - ((releaseX - rightX) * factor) && releaseX > rightX + ((topY - releaseY) * factor)) {
                return 1;
            } else if (releaseX >= rightX && releaseY > topY - ((releaseX - rightX) * factor) && releaseY <= buttomY + ((releaseX - rightX) * factor)) {
                return 2;
            } else if (releaseY > buttomY + ((releaseX - rightX) * factor) && releaseX > rightX + ((releaseY - buttomY) * factor)) {
                return 3;
            } else if (releaseY >= buttomY && releaseX >= leftX && releaseX <= rightX + ((releaseY - buttomY) * factor)) {
                return 4;
            } else if (releaseY > buttomY && releaseX < leftX) {
                return 5;
            } else if (releaseX <= leftX && releaseY > topY && releaseY <= buttomY) {
                return 6;
            } else if (releaseY <= topY && releaseX < leftX) {
                return 7;
            } else {
                return 100;
            }
        }else if(pressedPrimaryCode == 4912 || pressedPrimaryCode == 4824) {
            if (releaseY <= topY && releaseX >= leftX - ((topY - releaseY) * factor) && releaseX <= rightX) {
                return 0;
            } else if (releaseY <= topY && releaseX > rightX) {
                return 1;
            } else if (releaseX >= rightX && releaseY > topY && releaseY <= buttomY) {
                return 2;
            } else if (releaseY > buttomY && releaseX > rightX) {
                return 3;
            } else if (releaseY >= buttomY && releaseX >= leftX - ((releaseY - buttomY) * factor) && releaseX <= rightX) {
                return 4;
            } else if (releaseY > buttomY + ((leftX - releaseX) * factor) && releaseX < leftX - ((releaseY - buttomY) * factor)) {
                return 5;
            } else if (releaseX <= leftX && releaseY > topY - ((leftX - releaseX) * factor) && releaseY <= buttomY + ((leftX - releaseX) * factor)) {
                return 6;
            } else if (releaseY <= topY - ((leftX - releaseX) * factor) && releaseX < leftX - ((topY - releaseY) * factor)) {
                return 7;
            } else {
                return 100;
            }
        }else{
            Log.d("Debugging", "children: of 0: " + releaseX + ", " + releaseY + ": " + topY + ", " + buttomY + ": " + leftX + ", " + rightX);
            if (releaseY < topY && releaseX >= leftX - ((topY - releaseY) * factor) && releaseX <= rightX + ((topY - releaseY) * factor)) {
                Log.d("Debugging", "children: of 0: touch " + pressedPrimaryCode);
                Log.d("Debugging", "touching space: " + topY);
                return 0;
            } else if (releaseY <= topY - ((releaseX - rightX) * factor) && releaseX > rightX + ((topY - releaseY) * factor)) {
                Log.d("Debugging", "box seized: one");
                return 1;
            } else if (releaseX >= rightX && releaseY > topY - ((releaseX - rightX) * factor) && releaseY <= buttomY + ((releaseX - rightX) * factor)) {
                return 2;
            } else if (releaseY > buttomY + ((releaseX - rightX) * factor) && releaseX > rightX + ((releaseY - buttomY) * factor)) {
                return 3;
            } else if (releaseY >= buttomY && releaseX >= leftX - ((releaseY - buttomY) * factor) && releaseX <= rightX + ((releaseY - buttomY) * factor)) {
                Log.d("Debugging", "touching space: " + buttomY);
                return 4;
            } else if (releaseY > buttomY + ((leftX - releaseX) * factor) && releaseX < leftX - ((releaseY - buttomY) * factor)) {
                return 5;
            } else if (releaseX <= leftX && releaseY > topY - ((leftX - releaseX) * factor) && releaseY <= buttomY + ((leftX - releaseX) * factor)) {
                return 6;
            } else if (releaseY <= topY - ((leftX - releaseX) * factor) && releaseX < leftX - ((topY - releaseY) * factor)) {
                return 7;
            } else {
                Log.d("Debugging", "children: of 1: touch " + pressedPrimaryCode);
                return 100;
            }
        }
        /*}else{
            if(releaseY <= topY && releaseX >= leftX && releaseX <= rightX){
                return 0;
            }else if(releaseY <= topY && releaseX > rightX){
                return 1;
            }else if(releaseX >= rightX && releaseY > topY && releaseY <= buttomY){
                return 2;
            }else if(releaseY > buttomY && releaseX > rightX){
                return 3;
            }else if(releaseY >= buttomY && releaseX >= leftX && releaseX <= rightX){
                return 4;
            }else if(releaseY > buttomY && releaseX < leftX){
                return 5;
            }else if(releaseX <= leftX && releaseY > topY && releaseY <= buttomY){
                return 6;
            }else if(releaseY <= topY && releaseX < leftX){
                return 7;
            }else{
                return 100;
            }
        }*/

    }

    public int whichChildLetter(int boxOrder){
        if(boxOrder == 1){
            switch (pressedPrimaryCode){
                case 4608:
                    return 139;
                case 4672:
                    return 11;
                case 4776:
                    return 11;
                case 4872:
                    return 11;
                case 4928:
                    return -1;
            }
            return 7;
        }else if(boxOrder > 1){
            return boxOrder - 1;
        }else{
            return boxOrder;
        }
    }



    public void pickSuggestionManually(String suggestion) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        // You need to figure out how many characters to delete.
        // This is a simple implementation that deletes the last word.
        String allWords = ic.getTextBeforeCursor(100, 0).toString();
        String[] words = allWords.split(" ");
        if (words.length > 0) {
            String lastWord = words[words.length - 1];
            ic.deleteSurroundingText(lastWord.length(), 0);
        }
        Log.d("touchasd", "final");

        // Commit the selected suggestion, followed by a space
        ic.commitText(suggestion + " ", 1);
    }


    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //vibrate for 500 millisecoonds
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            //depricated in API 26
            v.vibrate(50);
        }
    }

    private void setKeyBoardLayout(String layoutName){
        if(layoutName.equals(MKeyboardView.hahuLayoutName)){
            MKeyboardView.currentKeyboarrdLayout = MKeyboardView.hahuLayoutName;
            keyboard = new Keyboard(this, R.xml.hahu);
        }else if(layoutName.equals(MKeyboardView.qwertyLayoutName)){
            MKeyboardView.currentKeyboarrdLayout = MKeyboardView.qwertyLayoutName;
            keyboard = new Keyboard(this, R.xml.qwerty);
        }else if(layoutName.equals(MKeyboardView.numbersLayoutName)){
            keyboard = new Keyboard(this, R.xml.numbers);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        retrieveKeys();
    }
}