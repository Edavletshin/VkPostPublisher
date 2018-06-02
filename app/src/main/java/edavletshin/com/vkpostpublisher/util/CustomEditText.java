package edavletshin.com.vkpostpublisher.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Created by edgar on 13.09.2017.
 */

public class CustomEditText extends EditText {

    HideKeyBoard delegate = null;

    public interface HideKeyBoard {
        void onBackPressed();
    }

    public void setOnHideKeyboardListener(HideKeyBoard delegate){
        this.delegate = delegate;
    }

    public CustomEditText(Context context) {
        super(context);
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if(KeyEvent.KEYCODE_BACK == event.getKeyCode()) {
            if (delegate!=null)
                delegate.onBackPressed();
        }
        return super.dispatchKeyEventPreIme(event);
    }

}
