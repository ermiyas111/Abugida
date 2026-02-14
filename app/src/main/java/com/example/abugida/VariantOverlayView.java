package com.example.abugida;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class VariantOverlayView extends View {
    private MKeyboardView keyboardView;

    public VariantOverlayView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public VariantOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public VariantOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    public void setKeyboardView(MKeyboardView keyboardView) {
        this.keyboardView = keyboardView;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (keyboardView == null) {
            return;
        }
        int offsetY = keyboardView.getTop();
        keyboardView.drawVariantOverlay(canvas, offsetY);
    }
}
