package com.example.abugida;

import android.content.ClipDescription;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.Gravity;
import android.graphics.drawable.GradientDrawable;

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

    private static final long CAPS_LOCK_TOGGLE_INTERVAL_MS = 800;
    private static final int KEYCODE_SWITCH_TO_QWERTY = 10000;
    private static final int KEYCODE_SWITCH_TO_HAHU = 10001;
    private static final int KEYCODE_SWITCH_TO_NUMBERS = 10002;
    private static final int KEYCODE_TOGGLE_ALPHA = 10003;
    private static final int KEYCODE_SPACE = 32;
    private static final int FIDEL_START = 4608;
    private static final int FIDEL_END = 4952;

    private Keyboard mSymbolsKeyboard;
    private Keyboard mSymbolsShiftedKeyboard;
    private Keyboard mQwertyKeyboard;

    private Keyboard mCurKeyboard;

    private String mWordSeparators;


    Button pop;

    private int currentSwipingBox = 100;

    private KeyboardTheme currentTheme;
    private PopupWindow themePopup;
    private KeyboardSettings keyboardSettings;
    private BroadcastReceiver themeChangeReceiver;
    private String lastAlphaLayout = MKeyboardView.hahuLayoutName;


    @Override
    public void onCreate() {
        super.onCreate();
        themeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ThemeManager.ACTION_THEME_CHANGED.equals(action)) {
                    applyTheme(ThemeManager.loadTheme(context));
                } else if (ThemeManager.ACTION_THEME_PREVIEW.equals(action)) {
                    if (isInputFromApp()) {
                        String themeId = intent.getStringExtra(ThemeManager.EXTRA_THEME_ID);
                        applyTheme(ThemeManager.getThemeById(themeId));
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ThemeManager.ACTION_THEME_CHANGED);
        filter.addAction(ThemeManager.ACTION_THEME_PREVIEW);
        registerReceiver(themeChangeReceiver, filter);
    }

    @Override
    public void onDestroy() {
        if (themeChangeReceiver != null) {
            unregisterReceiver(themeChangeReceiver);
            themeChangeReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public View onCreateInputView() {
        View root = getLayoutInflater().inflate(R.layout.keyboard, null);
        mCandidateView = root.findViewById(R.id.candidate_view);
        if (mCandidateView != null) {
            mCandidateView.setService(this);
            if (currentTheme != null) {
                mCandidateView.applyTheme(currentTheme);
            }
        }
        kv = root.findViewById(R.id.keyboard);
        VariantOverlayView overlayView = root.findViewById(R.id.variant_overlay);
        if (overlayView != null) {
            overlayView.setKeyboardView(kv);
            kv.setVariantOverlayView(overlayView);
            root.post(() -> {
                ViewGroup.LayoutParams params = overlayView.getLayoutParams();
                params.height = root.getHeight();
                overlayView.setLayoutParams(params);
                overlayView.invalidate();
            });
        }
        setKeyBoardLayout(MKeyboardView.hahuLayoutName);
        initKeyboardView();
        applyFidelSuperscripts(keyboard);
        setupTouchListener();
        setCandidatesViewShown(false);
        return root;
    }

    private void initKeyboardView() {
        kv.setPreviewEnabled(false);
        kv.setOnKeyboardActionListener(this);
        kv.setService(this);
        kv.setKeyboard(keyboard);
        kv.setDrawingCacheEnabled(true);
        mPredictionOn = false;
        applyTheme(ThemeManager.loadTheme(this));
        applyUserSettings();

        popup = new PopupWindow(this);

        View custom = LayoutInflater.from(this).inflate(R.layout.popup, new FrameLayout(this));
        PopupWindow popup = new PopupWindow(this);
        if (custom.getParent() != null) {
            ((ViewGroup) custom.getParent()).removeView(custom);
        }
        popup.setContentView(custom);
    }

    private void applyFidelSuperscripts(Keyboard targetKeyboard) {
        if (targetKeyboard == null) {
            return;
        }
        for (Keyboard.Key key : targetKeyboard.getKeys()) {
            if (key.codes[0] == 4608) { // ሀ
                key.label = createStyledLabel("ሀ", "ሐ");
            } else if (key.codes[0] == 4656) { // ሰ
                key.label = createStyledLabel("ሰ", "ሠ");
            } else if (key.codes[0] == 4704) { // በ
                key.label = createStyledLabel("በ", "ቨ");
            } else if (key.codes[0] == 4768) { // አ
                key.label = createStyledLabel("አ", "ዐ");
            } else if (key.codes[0] == 4928) { // ፀ
                key.label = createStyledLabel("ፀ", "ጸ");
            } else if (key.codes[0] == 4792) { // ኸ
                key.label = createStyledLabel("ኸ", "ኀ");
            }
        }
    }

    private void setupTouchListener() {
        kv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MKeyboardView.setXZ((int) event.getX());
                MKeyboardView.setYZ((int) event.getY());

                if (otherButtonsLocked) {
                    int temp = whichBox(event.getX(), event.getY());
                    if (temp != currentSwipingBox && temp != 100) {
                        vibrate();
                        currentSwipingBox = temp;
                    }
                }

                if (onPressedCalled) {
                    MKeyboardView.setWhichBoxTouched(whichBox(event.getX(), event.getY()));
                    MKeyboardView.setPressedFidelPrimaryCode(pressedPrimaryCode);
                }

                for (Keyboard.Key k : keyList) {
                    if (k.isInside((int) event.getX(), (int) event.getY())) {
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
                        if (rightHandCodeList.contains(k.codes[0])) {
                            touchingKeyWidth = k.width;
                            touchingKeyHeight = k.height;
                            for (Keyboard.Key l : keyList) {
                                if (l.isInside((int) event.getX() - (k.width), (int) event.getY())) {
                                    if ((double) event.getX() >= k.x && (double) event.getX() <= k.x + (touchingKeyWidth * (8 / 10))) {
                                        touchingPrimaryCode = l.codes[0];
                                        touchingLeftX = l.x + (touchingKeyWidth * (8 / 10));
                                        touchingTopY = k.y;
                                        touchingRightX = l.x + k.width + (touchingKeyWidth * (8 / 10));
                                        touchingButtomY = k.y + k.height;
                                        touchingCenterX = l.x + (k.width / 2) + (touchingKeyWidth * (8 / 10));
                                        touchingCenterY = k.y + (k.height / 2);
                                    } else if ((double) event.getX() < k.x + k.width && (double) event.getX() > k.x + (touchingKeyWidth * (8 / 10))) {
                                        touchingPrimaryCode = k.codes[0];
                                        touchingLeftX = k.x + (touchingKeyWidth * (8 / 10));
                                        touchingTopY = k.y;
                                        touchingRightX = k.x + k.width + (touchingKeyWidth * (8 / 10));
                                        touchingButtomY = k.y + k.height;
                                        touchingCenterX = k.x + (k.width / 2) + (touchingKeyWidth * (8 / 10));
                                        touchingCenterY = k.y + (k.height / 2);
                                    }

                                }
                            }
                        } else {
                            touchingPrimaryCode = k.codes[0];
                            touchingLeftX = k.x;
                            touchingTopY = k.y;
                            touchingRightX = k.x + k.width;
                            touchingButtomY = k.y + k.height;
                            touchingCenterX = k.x + (k.width / 2);
                            touchingCenterY = k.y + (k.height / 2);
                        }
                        touchingPointX = event.getX();
                        touchingPointY = event.getY();

                        touchingKeyWidth = k.width;
                        touchingKeyHeight = k.height;

                        rightHandCodeList.clear();
                    }
                }
                if (otherButtonsLocked) {
                    touchingPointX = event.getX();
                    touchingPointY = event.getY();
                }
                return false;
            }
        });
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
        return null;
    }


    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        retrieveKeys();

        applyTheme(ThemeManager.loadTheme(this));
        applyUserSettings();
        updateSuggestionsFromText();
        setCandidatesViewShown(true);

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
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/jpeg")) {
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/jpg")) {
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/png")) {
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/x-ms-bmp")) {
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/vnd.wap.wbmp")) {
                // gifSupported = true;
            }
            if (ClipDescription.compareMimeTypes(mimeType, "image/webp")) {
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
                    handleShiftPress(currentKeyboard);
                    break;

                case Keyboard.KEYCODE_DONE:
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    break;
                case KEYCODE_SWITCH_TO_QWERTY:
                case KEYCODE_SWITCH_TO_HAHU:
                case KEYCODE_SWITCH_TO_NUMBERS:
                case KEYCODE_TOGGLE_ALPHA:
                    handleLayoutSwitch(primaryCode);
                    break;
                case KEYCODE_SPACE:
                    commitSpace(ic);
                    break;
                default:
                    if (primaryCode < FIDEL_START || primaryCode >= FIDEL_END) {
                        char code = (char) primaryCode;
                        ic.commitText(String.valueOf(code), 1);
                    }
                    /*char code = (char)primaryCode;
                    if(Character.isLetter(code) && caps){
                        code = Character.toUpperCase(code);
                    }
                    ic.commitText(String.valueOf(code),1);*/
            }

            maybeDisableOneShotShift(primaryCode);

            //fetch string on edit text
            updateFetchedText(ic);
            updateSuggestionsFromText();

        }else{
            //onPressedCalled = false;
            //kv.setPreviewEnabled(true);
        }
    }

    @Override
    public void onPress(int primaryCode) {

        if(primaryCode >= FIDEL_START && primaryCode < FIDEL_END) {
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

        if(otherButtonsLocked) {
            currentSwipingBox = 100;
            MKeyboardView.setFidelPressed(false);
            MKeyboardView.setPressedFidelPrimaryCode(0);
            MKeyboardView.setWhichBoxTouched(100);
            kv.clearVariantOverlay();
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
                maybeDisableOneShotShift(primaryCode);
            }

            //MKeyboardView.setWordStarted(true);

            //fetch string on edit text
            updateFetchedText(ic);
            updateSuggestionsFromText();

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

    private void handleLayoutSwitch(int primaryCode) {
        if (primaryCode == KEYCODE_SWITCH_TO_QWERTY) {
            setKeyBoardLayout(MKeyboardView.qwertyLayoutName);
        } else if (primaryCode == KEYCODE_SWITCH_TO_HAHU) {
            setKeyBoardLayout(MKeyboardView.hahuLayoutName);
        } else if (primaryCode == KEYCODE_SWITCH_TO_NUMBERS) {
            setKeyBoardLayout(MKeyboardView.numbersLayoutName);
        } else if (primaryCode == KEYCODE_TOGGLE_ALPHA) {
            if (MKeyboardView.numbersLayoutName.equals(MKeyboardView.currentKeyboarrdLayout)) {
                setKeyBoardLayout(lastAlphaLayout);
            }
        }
    }

    private void commitSpace(InputConnection ic) {
        if (keyboardSettings != null && keyboardSettings.isAutoPickSuggestionOnSpace()) {
            String suggestion = kv.getTopSuggestion();
            if (suggestion != null && suggestion.length() > 0) {
                pickSuggestionManually(suggestion);
                return;
            }
        }
        ic.commitText(" ", 1);
    }


    private void playClick(int keyCode){
        if (keyboardSettings == null) {
            keyboardSettings = KeyboardSettings.load(this);
        }
        if (!keyboardSettings.isSoundOnKeypress()) {
            return;
        }
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
            if (releaseY < topY && releaseX >= leftX - ((topY - releaseY) * factor) && releaseX <= rightX + ((topY - releaseY) * factor)) {
                return 0;
            } else if (releaseY <= topY - ((releaseX - rightX) * factor) && releaseX > rightX + ((topY - releaseY) * factor)) {
                return 1;
            } else if (releaseX >= rightX && releaseY > topY - ((releaseX - rightX) * factor) && releaseY <= buttomY + ((releaseX - rightX) * factor)) {
                return 2;
            } else if (releaseY > buttomY + ((releaseX - rightX) * factor) && releaseX > rightX + ((releaseY - buttomY) * factor)) {
                return 3;
            } else if (releaseY >= buttomY && releaseX >= leftX - ((releaseY - buttomY) * factor) && releaseX <= rightX + ((releaseY - buttomY) * factor)) {
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

    private void updateSuggestionsFromText() {
        String allWords = MKeyboardView.fetchedEditTextValue;
        boolean amharicMode = MKeyboardView.currentKeyboarrdLayout.equals(MKeyboardView.hahuLayoutName);
        String filteredWords = sanitizeForSuggestions(allWords, amharicMode);
        if (filteredWords == null || filteredWords.isEmpty() || filteredWords.endsWith(" ")) {
            setSuggestions(new ArrayList<String>(), false, false);
            setCandidatesViewShown(true);
            return;
        }
        char lastChar = filteredWords.charAt(filteredWords.length() - 1);
        int charCode = (int) lastChar;
        if (!(charCode >= FIDEL_START && charCode < FIDEL_END) && !(charCode >= 65 && charCode <= 122)) {
            setSuggestions(new ArrayList<String>(), false, false);
            setCandidatesViewShown(true);
            return;
        }
        String finalWord = kv.universalTrim(filteredWords);
        if (finalWord.isEmpty()) {
            setSuggestions(new ArrayList<String>(), false, false);
            setCandidatesViewShown(true);
            return;
        }
        List<String> suggestions = new ArrayList<>();
        if (MKeyboardView.currentKeyboarrdLayout.equals(MKeyboardView.hahuLayoutName)) {
            suggestions = kv.findClosestMatchAmharic(finalWord);
        } else if (MKeyboardView.currentKeyboarrdLayout.equals(MKeyboardView.qwertyLayoutName)) {
            suggestions = kv.findClosestMatchEnglish(finalWord);
        }
        setSuggestions(suggestions, false, true);
        setCandidatesViewShown(true);
    }

    private String sanitizeForSuggestions(String text, boolean amharicMode) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            boolean isAmharic = c >= FIDEL_START && c < FIDEL_END;
            boolean isEnglish = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '\'';
            if (amharicMode) {
                builder.append(isAmharic ? c : ' ');
            } else {
                builder.append(isEnglish ? c : ' ');
            }
        }
        return builder.toString();
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
            lastAlphaLayout = MKeyboardView.hahuLayoutName;
        }else if(layoutName.equals(MKeyboardView.qwertyLayoutName)){
            MKeyboardView.currentKeyboarrdLayout = MKeyboardView.qwertyLayoutName;
            keyboard = new Keyboard(this, R.xml.qwerty);
            lastAlphaLayout = MKeyboardView.qwertyLayoutName;
        }else if(layoutName.equals(MKeyboardView.numbersLayoutName)){
            MKeyboardView.currentKeyboarrdLayout = MKeyboardView.numbersLayoutName;
            keyboard = new Keyboard(this, R.xml.numbers);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        retrieveKeys();
        if (currentTheme != null) {
            applyTheme(currentTheme);
        }
        updateNumbersToggleLabel();
        updateSuggestionsFromText();
    }

    private void updateNumbersToggleLabel() {
        if (keyboard == null || !MKeyboardView.numbersLayoutName.equals(MKeyboardView.currentKeyboarrdLayout)) {
            return;
        }
        String label = MKeyboardView.hahuLayoutName.equals(lastAlphaLayout)
                ? "ሀሁ"
                : "ABC";
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes != null && key.codes.length > 0 && key.codes[0] == KEYCODE_TOGGLE_ALPHA) {
                key.label = label;
                break;
            }
        }
        kv.invalidateAllKeys();
    }

    private void updateFetchedText(InputConnection ic) {
        if (ic == null) {
            return;
        }
        ExtractedText extracted = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if (extracted != null) {
            MKeyboardView.fetchedEditTextValue = (String) extracted.text;
        }
    }

    private void applyTheme(KeyboardTheme theme) {
        if (theme == null || kv == null) {
            return;
        }
        currentTheme = theme;
        kv.applyTheme(theme);
        if (mCandidateView != null) {
            mCandidateView.applyTheme(theme);
        }
    }

    private void applyUserSettings() {
        keyboardSettings = KeyboardSettings.load(this);
        if (kv != null) {
            kv.setPreviewEnabled(keyboardSettings.isShowKeyPreview());
            kv.setShowSuggestions(true);
        }
        mPredictionOn = true;
        setCandidatesViewShown(true);
        updateSuggestionsFromText();
    }

    private boolean isInputFromApp() {
        EditorInfo info = getCurrentInputEditorInfo();
        if (info == null || info.packageName == null) {
            return false;
        }
        return info.packageName.equals(getPackageName());
    }

    public void openSettingsApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void showThemePicker() {
        if (themePopup != null && themePopup.isShowing()) {
            themePopup.dismiss();
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.theme_picker, null);
        LinearLayout container = popupView.findViewById(R.id.theme_list_container);
        TextView title = popupView.findViewById(R.id.theme_picker_title);
        if (currentTheme != null) {
            title.setTextColor(currentTheme.getCandidateTextNormal());
            popupView.setBackgroundColor(currentTheme.getCandidateBackground());
        }

        for (final KeyboardTheme theme : ThemeManager.getThemes()) {
            Button button = new Button(this);
            String label = theme.getName();
            if (currentTheme != null && theme.getId().equals(currentTheme.getId())) {
                label = label + " (selected)";
                button.setEnabled(false);
            }
            button.setText(label);
            button.setAllCaps(false);
            GradientDrawable background = new GradientDrawable();
            background.setColor(theme.getKeyBackgroundPrimary());
            background.setCornerRadius(18f);
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
                    ThemeManager.saveTheme(SoftKeyboard.this, theme);
                    applyTheme(theme);
                    Intent intent = new Intent(ThemeManager.ACTION_THEME_CHANGED);
                    sendBroadcast(intent);
                    if (themePopup != null) {
                        themePopup.dismiss();
                    }
                }
            });
            container.addView(button);
        }

        themePopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        themePopup.setOutsideTouchable(true);
        themePopup.setFocusable(true);
        themePopup.showAtLocation(kv, Gravity.BOTTOM, 0, 0);
    }

    private void handleShiftPress(Keyboard currentKeyboard) {
        if (currentKeyboard == null) {
            return;
        }
        if (mCapsLock) {
            mCapsLock = false;
            mLastShiftTime = 0;
            if (kv != null) {
                kv.setShiftHighlightState(false, false);
            }
            applyShiftState(currentKeyboard, false);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - mLastShiftTime < CAPS_LOCK_TOGGLE_INTERVAL_MS) {
            mCapsLock = true;
            mLastShiftTime = 0;
            if (kv != null) {
                kv.setShiftHighlightState(true, true);
            }
            applyShiftState(currentKeyboard, true);
            return;
        }

        mLastShiftTime = now;
        boolean nextShift = !isShifted;
        if (kv != null) {
            kv.setShiftHighlightState(nextShift, false);
        }
        applyShiftState(currentKeyboard, nextShift);
    }

    private void maybeDisableOneShotShift(int primaryCode) {
        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            return;
        }
        if (mCapsLock || !isShifted) {
            return;
        }
        if (kv != null) {
            kv.setShiftHighlightState(false, false);
        }
        Keyboard currentKeyboard = kv.getKeyboard();
        if (currentKeyboard != null) {
            applyShiftState(currentKeyboard, false);
        }
    }

    private void applyShiftState(Keyboard currentKeyboard, boolean shifted) {
        isShifted = shifted;
        for (Keyboard.Key key : currentKeyboard.getKeys()) {
            if (key.codes[0] == -1) {
                Drawable icon = null;
                if (mCapsLock) {
                    icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_solid);
                    if (icon != null) {
                        icon.setAlpha(255);
                    }
                } else if (isShifted) {
                    icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_hollow);
                    if (icon != null) {
                        icon.setAlpha(255);
                    }
                } else {
                    icon = ContextCompat.getDrawable(this, R.drawable.ic_custom_shift_hollow);
                    if (icon != null) {
                        icon.setAlpha(140);
                    }
                }
                key.icon = icon;
            }
            // Check for the 'ሀ'/'ሐ' key (Unicode 4608 and 4624)
            else if (key.codes[0] == 4608 || key.codes[0] == 4624) {
                if (isShifted) {
                    key.label = "ሐ";
                    key.codes[0] = 4624;
                } else {
                    key.label = createStyledLabel("ሀ", "ሐ");
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

        currentKeyboard.setShifted(isShifted);
        kv.invalidateAllKeys();
    }
}