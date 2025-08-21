package com.settlex.android.ui.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;

public class PinBoxEditText extends AppCompatEditText {
    private OnBackspaceListener backspaceListener;

    /*====================
    Constructors for all use cases
                    =====================*/
    public PinBoxEditText(Context context) {
        super(context);
    }

    public PinBoxEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinBoxEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*====================
    Override to detect soft backspace key
                    =====================*/
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        InputConnection baseConnection = super.onCreateInputConnection(outAttrs);

        return new InputConnectionWrapper(baseConnection, true) {
            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {

                // Detect soft keyboard backspace press
                if (beforeLength == 1 && afterLength == 0) {
                    sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));

                    // Notify listener if set
                    if (backspaceListener != null) {
                        backspaceListener.onBackspace(PinBoxEditText.this);
                    }
                    return true;
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }
        };
    }

    // Listener interface for backspace key
    public interface OnBackspaceListener {
        void onBackspace(PinBoxEditText view);
    }
}
