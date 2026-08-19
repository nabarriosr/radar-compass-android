package com.orbitalcompass.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

public final class CompassView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float heading, displayed;
    private float pitch, roll, shownPitch, shownRoll;
    private boolean gps, trueNorth;
    private int accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
    private String error;

    public CompassView(Context context) {
        this(context, null);
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
    }

    public void setHeading(float h, boolean north) { heading = h; trueNorth = north; postInvalidateOnAnimation(); }
    public void setTilt(float p, float r) { pitch = clamp(p, -45f, 45f); roll = clamp(r, -45f, 45f); postInvalidateOnAnimation(); }
    public void setGps(boolean value) { gps = value; invalidate(); }
    public void setAccuracy(int value) { accuracy = value; invalidate(); }
    public void setError(String value) { error = value; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight();
        drawAlienWorld(c, w, h);
        drawHeader(c, w, h);

        float cx = w / 2f;
        float cy = h * .50f;
        float radius = Math.min(w * .405f, h * .285f);
        float headingDelta = shortest(heading - displayed);
        displayed = wrap(displayed + headingDelta * .14f);
        shownPitch += (pitch - shownPitch) * .12f;
        shownRoll += (roll - shownRoll) * .12f;

        drawRadarBody(c, cx, cy, radius);

        c.save();
        c.rotate(shownRoll * .18f, cx, cy);
        float squash = 1f - Math.min(0.18f, Math.abs(shownPitch) / 250f);
        c.scale(1f, squash, cx, cy);
        drawRadarFace(c, cx, cy, radius);
        c.save();
        c.rotate(-displayed, cx, cy);
        drawTicks(c, cx, cy, radius);
        drawCardinals(c, cx, cy, radius);
        c.restore();
        drawNeedle(c, cx, cy, radius * .61f);
        drawEnergyNodes(c, cx, cy, radius);
        c.restore();

        drawLevelBubble(c, cx, cy, radius);
        drawReadout(c, cx, cy, w);
        drawStatus(c, cx, h, w);

        if (Math.abs(headingDelta) > .05f || Math.abs(pitch - shownPitch) > .05f || Math.abs(roll - shownRoll) > .05f) {
            postInvalidateOnAnimation();
        }
    }

    private void drawAlienWorld(Canvas c, float w, float h) {
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, 0, h, new int[]{
                Color.rgb(31, 190, 143), Color.rgb(86, 219, 181), Color.rgb(21, 112, 110), Color.rgb(6, 37, 61)
        }, new float[]{0f, .38f, .67f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);

        p.setShader(new RadialGradient(w * .79f, h * .13f, w * .17f,
                Color.argb(230, 255, 244, 143), Color.argb(0, 255, 244, 143), Shader.TileMode.CLAMP));
        c.drawCircle(w * .79f, h * .13f, w * .17f, p);
        p.setShader(null); p.setColor(Color.rgb(255, 246, 170)); c.drawCircle(w * .79f, h * .13f, w * .062f, p);
        p.setColor(Color.argb(150, 205, 255, 224)); c.drawCircle(w * .18f, h * .20f, w * .028f, p);

        Path ridge = new Path(); ridge.moveTo(0, h * .68f);
        ridge.cubicTo(w * .12f, h * .55f, w * .22f, h * .64f, w * .33f, h * .51f);
        ridge.cubicTo(w * .49f, h * .68f, w * .64f, h * .50f, w * .78f, h * .61f);
        ridge.cubicTo(w * .90f, h * .52f, w, h * .59f, w, h * .72f); ridge.close();
        p.setColor(Color.rgb(57, 45, 105)); c.drawPath(ridge, p);
        p.setColor(Color.rgb(18, 95, 92)); c.drawRect(0, h * .68f, w, h, p);

        p.setColor(Color.argb(115, 96, 255, 210)); p.setStrokeWidth(2); p.setStyle(Paint.Style.STROKE);
        for (int i = 0; i < 8; i++) c.drawLine(0, h * (.72f + i * .035f), w, h * (.72f + i * .035f), p);
        p.setStyle(Paint.Style.FILL);
        drawOrbTree(c, w * .08f, h * .65f, w * .055f);
        drawOrbTree(c, w * .91f, h * .60f, w * .07f);
        drawOrbTree(c, w * .78f, h * .69f, w * .04f);
    }

    private void drawOrbTree(Canvas c, float x, float y, float r) {
        p.setColor(Color.rgb(221, 236, 201)); c.drawRoundRect(new RectF(x - r * .13f, y, x + r * .13f, y + r * 1.7f), r, r, p);
        p.setColor(Color.rgb(58, 53, 130)); c.drawCircle(x, y, r, p);
        p.setColor(Color.argb(150, 123, 255, 183)); c.drawCircle(x - r * .27f, y - r * .22f, r * .42f, p);
    }

    private void drawHeader(Canvas c, float w, float h) {
        p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL);
        p.setShadowLayer(12, 0, 3, Color.argb(190, 0, 0, 0));
        p.setColor(Color.rgb(255, 222, 62)); p.setTextSize(w * .074f); c.drawText("ORBITAL COMPASS", w / 2f, h * .088f, p);
        p.setColor(Color.rgb(246, 255, 226)); p.setTextSize(w * .028f); c.drawText("RADAR DE ORIENTACIÓN Y NIVEL", w / 2f, h * .122f, p);
        p.clearShadowLayer();
    }

    private void drawRadarBody(Canvas c, float cx, float cy, float r) {
        p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(125, 0, 0, 0));
        c.drawOval(new RectF(cx - r * 1.08f, cy - r * .82f + 24, cx + r * 1.08f, cy + r * .82f + 48), p);
        for (int i = 18; i >= 0; i--) {
            float rr = r + i;
            int red = 115 + i * 5;
            p.setColor(Color.rgb(Math.min(255, red), 55 + i * 3, 18));
            c.drawOval(new RectF(cx - rr, cy - rr, cx + rr, cy + rr), p);
        }
        p.setShader(new LinearGradient(cx - r, cy - r, cx + r, cy + r,
                new int[]{Color.rgb(255, 226, 77), Color.rgb(238, 104, 12), Color.rgb(112, 35, 12)}, null, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r + 8, p); p.setShader(null);
        p.setColor(Color.rgb(18, 42, 37)); c.drawCircle(cx, cy, r - 4, p);
    }

    private void drawRadarFace(Canvas c, float cx, float cy, float r) {
        p.setShader(new RadialGradient(cx - r * .28f, cy - r * .34f, r * 1.3f,
                new int[]{Color.rgb(42, 113, 75), Color.rgb(10, 55, 48), Color.rgb(3, 24, 32)}, null, Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL); c.drawCircle(cx, cy, r - 7, p); p.setShader(null);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(Color.argb(130, 101, 255, 160));
        for (int i = 1; i <= 4; i++) c.drawCircle(cx, cy, (r - 12) * i / 4f, p);
        c.drawLine(cx - r + 12, cy, cx + r - 12, cy, p);
        c.drawLine(cx, cy - r + 12, cx, cy + r - 12, p);
        p.setColor(Color.argb(60, 154, 255, 190));
        for (int d = 0; d < 360; d += 30) {
            double a = Math.toRadians(d); c.drawLine(cx, cy, cx + (float) Math.sin(a) * (r - 12), cy - (float) Math.cos(a) * (r - 12), p);
        }
    }

    private void drawTicks(Canvas c, float cx, float cy, float r) {
        p.setStyle(Paint.Style.STROKE); p.setColor(Color.argb(220, 225, 255, 228));
        for (int d = 0; d < 360; d += 5) {
            c.save(); c.rotate(d, cx, cy); p.setStrokeWidth(d % 30 == 0 ? 4 : 1.5f);
            float len = d % 30 == 0 ? 19 : 8; c.drawLine(cx, cy - r + 9, cx, cy - r + 9 + len, p); c.restore();
        }
    }

    private void drawCardinals(Canvas c, float cx, float cy, float r) {
        drawCardinal(c, "N", cx, cy - r + 48, Color.rgb(255, 82, 54));
        drawCardinal(c, "E", cx + r - 36, cy + 10, Color.WHITE);
        drawCardinal(c, "S", cx, cy + r - 25, Color.WHITE);
        drawCardinal(c, "O", cx - r + 36, cy + 10, Color.WHITE);
    }

    private void drawCardinal(Canvas c, String text, float x, float y, int color) {
        p.setStyle(Paint.Style.FILL); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(getWidth() * .055f); p.setColor(color); c.drawText(text, x, y, p);
    }

    private void drawNeedle(Canvas c, float cx, float cy, float len) {
        Path north = new Path(); north.moveTo(cx, cy - len); north.lineTo(cx - 15, cy + 5); north.lineTo(cx + 15, cy + 5); north.close();
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(255, 73, 43)); c.drawPath(north, p);
        Path south = new Path(); south.moveTo(cx, cy + len); south.lineTo(cx - 15, cy - 5); south.lineTo(cx + 15, cy - 5); south.close();
        p.setColor(Color.rgb(223, 237, 230)); c.drawPath(south, p);
    }

    private void drawEnergyNodes(Canvas c, float cx, float cy, float r) {
        float[][] nodes = {{-.49f,-.36f},{.50f,-.18f},{.32f,.49f},{-.57f,.31f},{.05f,-.66f}};
        for (int i = 0; i < nodes.length; i++) {
            float x = cx + nodes[i][0] * r, y = cy + nodes[i][1] * r;
            p.setShader(new RadialGradient(x - 4, y - 5, 18, Color.WHITE, Color.argb(0, 255, 185, 35), Shader.TileMode.CLAMP));
            c.drawCircle(x, y, 18, p); p.setShader(null); p.setColor(Color.rgb(255, 173, 34)); c.drawCircle(x, y, 7 + i % 2, p);
            p.setColor(Color.rgb(255, 242, 144)); c.drawCircle(x - 2, y - 2, 2.5f, p);
        }
    }

    private void drawLevelBubble(Canvas c, float cx, float cy, float r) {
        float max = r * .22f;
        float bx = cx + clamp(shownRoll / 30f, -1f, 1f) * max;
        float by = cy + clamp(shownPitch / 30f, -1f, 1f) * max;
        float tilt = (float) Math.sqrt(shownPitch * shownPitch + shownRoll * shownRoll);
        boolean level = tilt < 3f;
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(level ? Color.rgb(125, 255, 167) : Color.argb(190, 255, 222, 85));
        c.drawCircle(cx, cy, max, p); c.drawCircle(cx, cy, max * .38f, p);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(bx - 7, by - 8, 27,
                level ? Color.WHITE : Color.rgb(255, 250, 185),
                level ? Color.argb(40, 78, 255, 144) : Color.argb(30, 255, 136, 24), Shader.TileMode.CLAMP));
        c.drawCircle(bx, by, 27, p); p.setShader(null);
        p.setColor(level ? Color.rgb(78, 255, 144) : Color.rgb(255, 171, 31)); c.drawCircle(bx, by, 12, p);
        p.setColor(Color.argb(210, 255, 255, 255)); c.drawCircle(bx - 4, by - 5, 4, p);
    }

    private void drawReadout(Canvas c, float cx, float cy, float w) {
        float tilt = (float) Math.sqrt(shownPitch * shownPitch + shownRoll * shownRoll);
        boolean level = tilt < 3f;
        p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); p.setTextSize(w * .115f);
        p.setShadowLayer(8, 0, 2, Color.BLACK); c.drawText(String.format(Locale.getDefault(), "%03.0f°", heading), cx, cy + 18, p); p.clearShadowLayer();
        p.setTextSize(w * .029f); p.setColor(Color.rgb(170, 255, 199));
        c.drawText(direction(heading) + "  •  " + (trueNorth ? "NORTE VERDADERO" : "NORTE MAGNÉTICO"), cx, cy + 48, p);
        p.setColor(level ? Color.rgb(98, 255, 151) : Color.rgb(255, 220, 76));
        c.drawText(level ? "● PARALELO AL HORIZONTE LOCAL" : String.format(Locale.getDefault(), "INCLINACIÓN %.0f°  ·  CENTRA LA BURBUJA", tilt), cx, cy + 73, p);
    }

    private void drawStatus(Canvas c, float cx, float h, float w) {
        p.setTextAlign(Paint.Align.CENTER); p.setTextSize(w * .029f); p.setStyle(Paint.Style.FILL);
        if (error != null) { p.setColor(Color.rgb(255, 94, 70)); c.drawText(error, cx, h * .91f, p); return; }
        boolean low = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW;
        p.setColor(low ? Color.rgb(255, 211, 76) : Color.rgb(164, 255, 194));
        c.drawText(low ? "CALIBRA MOVIENDO EL EQUIPO EN FORMA DE 8" : "CAMPO MAGNÉTICO ESTABLE", cx, h * .885f, p);
        p.setColor(gps ? Color.rgb(97, 255, 150) : Color.rgb(211, 225, 225));
        c.drawText(gps ? "● GPS ACTIVO · DECLINACIÓN CORREGIDA" : "○ SIN GPS · NORTE MAGNÉTICO", cx, h * .925f, p);
    }

    private String direction(float h) {
        String[] d = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};
        return d[Math.round(h / 45f) % 8];
    }

    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
    private static float wrap(float v) { return (v % 360f + 360f) % 360f; }
    private static float shortest(float v) { return (v + 540f) % 360f - 180f; }
}
