package edavletshin.com.vkpostpublisher.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;
import android.util.Log;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

public class RoundedBackgroundSpan extends ReplacementSpan
{
    private int _backgroundColor;
    private int length;
    private float density;

    public RoundedBackgroundSpan(int backgroundColor, float density) {
        super();
        this.density = density;
        _backgroundColor = backgroundColor;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return (int) (paint.measureText(text.subSequence(start, end).toString()));
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {


        float width = paint.measureText(text.subSequence(start, end).toString());
            RectF rect = new RectF(x, top, x + width, bottom);
            paint.setColor(_backgroundColor);
            canvas.drawRoundRect(rect, 8, 8, paint);


    }
}