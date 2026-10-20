package za.nmu.wrpv.sagradaclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import nmu.sagrada.core.Colour;

/** A single draft-pool die. Selected dice gain a gold ring; unplayable dice fade back. */
public class DieView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pip = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private String dieId;
    private boolean chosen;
    private boolean playable = true;
    private int goldColour;
    private int emptyColour;

    public DieView(Context context) {
        super(context);
        init();
    }

    public DieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        goldColour = getResources().getColor(R.color.gold, null);
        emptyColour = getResources().getColor(R.color.surfaceRaised, null);
        stroke.setStyle(Paint.Style.STROKE);
        pip.setStyle(Paint.Style.FILL);
    }

    public void setDie(String dieId) {
        this.dieId = dieId;
        setContentDescription(Dice.describe(getContext(), dieId));
        invalidate();
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
        invalidate();
    }

    public void setPlayable(boolean playable) {
        this.playable = playable;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = getWidth() * 0.08f;
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        float size = rect.width();
        float radius = size * 0.2f;

        Colour colour = Dice.colourOf(dieId);
        if (colour == null) {
            fill.setColor(emptyColour);
            fill.setAlpha(80);
            canvas.drawRoundRect(rect, radius, radius, fill);
            fill.setAlpha(255);
            return;
        }

        int alpha = playable ? 255 : 110;
        fill.setColor(Dice.colour(getContext(), colour));
        fill.setAlpha(alpha);
        canvas.drawRoundRect(rect, radius, radius, fill);
        fill.setAlpha(255);

        stroke.setColor(chosen ? goldColour : 0x40000000);
        stroke.setStrokeWidth(size * (chosen ? 0.11f : 0.045f));
        canvas.drawRoundRect(rect, radius, radius, stroke);

        pip.setColor(0xFFFFFFFF);
        pip.setAlpha(alpha);
        Dice.drawPips(canvas, rect.centerX(), rect.centerY(), size, Dice.valueOf(dieId), pip);
    }
}
