package za.nmu.wrpv.sagradaclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import nmu.sagrada.core.Cell;
import nmu.sagrada.core.Colour;
import nmu.sagrada.core.Die;
import nmu.sagrada.core.WindowCard;

/**
 * Draws a Sagrada window: cell restrictions, placed dice and legal-move highlights.
 *
 * <p>The previous build stacked twenty fixed-size ImageViews on top of a stretched window bitmap,
 * so the tappable cells only lined up with the artwork at one screen size. Drawing the grid from
 * the pattern string keeps the cells and the artwork the same thing at any size.
 */
public class WindowGridView extends View {

    public interface OnCellClickListener {
        void onCellClick(int index);
    }

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pip = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frame = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private WindowCard window;
    private boolean[] legal = new boolean[WindowCard.SIZE];
    private int selectedCell = -1;
    private OnCellClickListener listener;

    /**
     * Highlights are white rather than the app's gold: every restriction cell is tinted with one of
     * the five die colours, and a yellow restriction is indistinguishable from a gold ring.
     */
    private static final int HIGHLIGHT = 0xFFFFFFFF;

    private int glassColour;
    private int outlineColour;
    private int mutedColour;
    private int frameColour;

    private float pitch;
    private float cellSize;
    private float gridLeft;
    private float gridTop;

    public WindowGridView(Context context) {
        super(context);
        init();
    }

    public WindowGridView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindowGridView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        glassColour = getResources().getColor(R.color.surfaceRaised, null);
        outlineColour = getResources().getColor(R.color.outline, null);
        mutedColour = getResources().getColor(R.color.textMuted, null);
        frameColour = getResources().getColor(R.color.night, null);
        stroke.setStyle(Paint.Style.STROKE);
        frame.setStyle(Paint.Style.STROKE);
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        this.listener = listener;
    }

    public void setWindow(WindowCard window) {
        this.window = window;
        invalidate();
    }

    public void setLegalCells(boolean[] legal) {
        this.legal = legal == null ? new boolean[WindowCard.SIZE] : legal;
        invalidate();
    }

    public void clearHighlights() {
        legal = new boolean[WindowCard.SIZE];
        selectedCell = -1;
        invalidate();
    }

    public void setSelectedCell(int index) {
        selectedCell = index;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int natural = Math.round(width * WindowCard.ROWS / (float) WindowCard.COLUMNS);
        if (heightMode == MeasureSpec.EXACTLY) {
            setMeasuredDimension(width, height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, Math.min(natural, height));
        } else {
            setMeasuredDimension(width, natural);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float usableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float usableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (usableWidth <= 0 || usableHeight <= 0) return;

        pitch = Math.min(usableWidth / WindowCard.COLUMNS, usableHeight / WindowCard.ROWS);
        float gap = pitch * 0.08f;
        cellSize = pitch - gap;
        float gridWidth = pitch * WindowCard.COLUMNS;
        float gridHeight = pitch * WindowCard.ROWS;
        gridLeft = getPaddingLeft() + (usableWidth - gridWidth) / 2f;
        gridTop = getPaddingTop() + (usableHeight - gridHeight) / 2f;

        drawFrame(canvas, gridWidth, gridHeight, gap);
        for (int index = 0; index < WindowCard.SIZE; index++) {
            drawCell(canvas, index, gap);
        }
    }

    private void drawFrame(Canvas canvas, float gridWidth, float gridHeight, float gap) {
        rect.set(gridLeft + gap / 2f - gap, gridTop + gap / 2f - gap,
                gridLeft + gridWidth - gap / 2f + gap, gridTop + gridHeight - gap / 2f + gap);
        fill.setColor(frameColour);
        fill.setAlpha(170);
        canvas.drawRoundRect(rect, pitch * 0.16f, pitch * 0.16f, fill);
        fill.setAlpha(255);
        frame.setColor(outlineColour);
        frame.setStrokeWidth(Math.max(1f, pitch * 0.03f));
        canvas.drawRoundRect(rect, pitch * 0.16f, pitch * 0.16f, frame);
    }

    private void drawCell(Canvas canvas, int index, float gap) {
        int row = index / WindowCard.COLUMNS;
        int column = index % WindowCard.COLUMNS;
        float left = gridLeft + column * pitch + gap / 2f;
        float top = gridTop + row * pitch + gap / 2f;
        rect.set(left, top, left + cellSize, top + cellSize);
        float radius = cellSize * 0.16f;
        float centreX = rect.centerX();
        float centreY = rect.centerY();

        Cell cell = window == null ? null : window.getCell(index);
        Die die = cell == null ? null : cell.getDie();

        if (die != null) {
            fill.setColor(Dice.colour(getContext(), die.getColour()));
            canvas.drawRoundRect(rect, radius, radius, fill);
            stroke.setColor(0x33000000);
            stroke.setStrokeWidth(Math.max(1f, cellSize * 0.04f));
            canvas.drawRoundRect(rect, radius, radius, stroke);
            pip.setStyle(Paint.Style.FILL);
            pip.setColor(0xFFFFFFFF);
            Dice.drawPips(canvas, centreX, centreY, cellSize, die.getValue(), pip);
        } else {
            Colour restriction = cell == null ? null : cell.getColourRestriction();
            int value = cell == null ? 0 : cell.getValueRestriction();
            if (restriction != null) {
                fill.setColor(Dice.colour(getContext(), restriction));
                fill.setAlpha(110);
                canvas.drawRoundRect(rect, radius, radius, fill);
                fill.setAlpha(255);
                stroke.setColor(Dice.colour(getContext(), restriction));
                stroke.setStrokeWidth(Math.max(1f, cellSize * 0.05f));
                canvas.drawRoundRect(rect, radius, radius, stroke);
            } else {
                fill.setColor(glassColour);
                fill.setAlpha(value == 0 ? 90 : 140);
                canvas.drawRoundRect(rect, radius, radius, fill);
                fill.setAlpha(255);
                stroke.setColor(outlineColour);
                stroke.setStrokeWidth(Math.max(1f, cellSize * 0.035f));
                canvas.drawRoundRect(rect, radius, radius, stroke);
                if (value != 0) {
                    pip.setStyle(Paint.Style.STROKE);
                    pip.setStrokeWidth(Math.max(1f, cellSize * 0.035f));
                    pip.setColor(mutedColour);
                    Dice.drawPips(canvas, centreX, centreY, cellSize, value, pip);
                }
            }
        }

        boolean highlight = index < legal.length && legal[index];
        if (highlight || index == selectedCell) {
            stroke.setColor(HIGHLIGHT);
            stroke.setStrokeWidth(Math.max(2f, cellSize * (index == selectedCell ? 0.12f : 0.08f)));
            canvas.drawRoundRect(rect, radius, radius, stroke);
            if (die == null) {
                fill.setColor(HIGHLIGHT);
                fill.setAlpha(index == selectedCell ? 80 : 48);
                canvas.drawRoundRect(rect, radius, radius, fill);
                fill.setAlpha(255);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (listener == null || !isEnabled()) return false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) return true;
        if (event.getAction() != MotionEvent.ACTION_UP) return false;
        int index = cellAt(event.getX(), event.getY());
        if (index < 0) return false;
        performClick();
        listener.onCellClick(index);
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int cellAt(float x, float y) {
        if (pitch <= 0) return -1;
        int column = (int) Math.floor((x - gridLeft) / pitch);
        int row = (int) Math.floor((y - gridTop) / pitch);
        if (column < 0 || column >= WindowCard.COLUMNS || row < 0 || row >= WindowCard.ROWS) return -1;
        return row * WindowCard.COLUMNS + column;
    }
}
