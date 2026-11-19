package com.speedhunt.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Small vector-style icons drawn in code. */
final class IconView extends View {
    static final int BACK = 0, CLOSE = 1, PLAY = 2, GEAR = 3, BAG = 4, HELP = 5, CHECK = 6;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF r = new RectF();
    private final int type, color;

    IconView(Context c, int type, int color) {
        super(c);
        this.type = type;
        this.color = color;
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), s = Math.min(w, h), cx = w / 2f, cy = h / 2f;
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(s * 0.11f);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        switch (type) {
            case BACK:
                c.drawLine(cx + s * .30f, cy, cx - s * .30f, cy, p);
                c.drawLine(cx - s * .30f, cy, cx - s * .06f, cy - s * .24f, p);
                c.drawLine(cx - s * .30f, cy, cx - s * .06f, cy + s * .24f, p);
                break;
            case CLOSE:
                c.drawLine(cx - s * .24f, cy - s * .24f, cx + s * .24f, cy + s * .24f, p);
                c.drawLine(cx + s * .24f, cy - s * .24f, cx - s * .24f, cy + s * .24f, p);
                break;
            case PLAY:
                p.setStyle(Paint.Style.FILL_AND_STROKE);
                p.setStrokeWidth(s * 0.06f);
                path.reset();
                path.moveTo(cx - s * .20f, cy - s * .30f);
                path.lineTo(cx + s * .32f, cy);
                path.lineTo(cx - s * .20f, cy + s * .30f);
                path.close();
                c.drawPath(path, p);
                break;
            case GEAR:
                p.setStrokeWidth(s * .10f);
                c.drawCircle(cx, cy, s * .20f, p);
                p.setStrokeWidth(s * .13f);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    float cs = (float) Math.cos(a), sn = (float) Math.sin(a);
                    c.drawLine(cx + cs * s * .31f, cy + sn * s * .31f,
                            cx + cs * s * .42f, cy + sn * s * .42f, p);
                }
                break;
            case BAG:
                r.set(cx - s * .30f, cy - s * .14f, cx + s * .30f, cy + s * .34f);
                c.drawRoundRect(r, s * .08f, s * .08f, p);
                r.set(cx - s * .16f, cy - s * .36f, cx + s * .16f, cy + s * .02f);
                c.drawArc(r, 180, 180, false, p);
                break;
            case CHECK:
                c.drawLine(cx - s * .28f, cy, cx - s * .08f, cy + s * .22f, p);
                c.drawLine(cx - s * .08f, cy + s * .22f, cx + s * .30f, cy - s * .22f, p);
                break;
            default:
                p.setStrokeWidth(s * .09f);
                c.drawCircle(cx, cy, s * .36f, p);
                p.setStyle(Paint.Style.FILL);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(s * .5f);
                p.setFakeBoldText(true);
                c.drawText("?", cx, cy + s * .18f, p);
                break;
        }
    }
}
