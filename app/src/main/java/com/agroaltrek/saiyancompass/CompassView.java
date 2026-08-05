package com.agroaltrek.saiyancompass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.view.View;

public final class CompassView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float heading, displayed;
    private boolean gps, trueNorth;
    private int accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
    private String error;

    public CompassView(Context context) { super(context); p.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD)); }
    public void setHeading(float h, boolean trueNorth) { heading = h; this.trueNorth = trueNorth; postInvalidateOnAnimation(); }
    public void setGps(boolean value) { gps = value; invalidate(); }
    public void setAccuracy(int value) { accuracy = value; invalidate(); }
    public void setError(String value) { error = value; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth(), h = getHeight(), cx = w / 2f, cy = h * .48f;
        c.drawColor(Color.rgb(7, 12, 23));
        drawEnergy(c, w, h);
        title(c, cx, h);
        float radius = Math.min(w * .42f, h * .31f);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(12, 35, 37)); c.drawCircle(cx, cy, radius + 16, p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(8); p.setColor(Color.rgb(255, 139, 20)); c.drawCircle(cx, cy, radius + 9, p);
        p.setStrokeWidth(3); p.setColor(Color.rgb(64, 238, 126)); c.drawCircle(cx, cy, radius, p);
        p.setStrokeWidth(1); p.setColor(Color.argb(95, 64, 238, 126));
        for (int i = 1; i < 4; i++) c.drawCircle(cx, cy, radius * i / 4f, p);
        c.drawLine(cx - radius, cy, cx + radius, cy, p); c.drawLine(cx, cy - radius, cx, cy + radius, p);

        float delta = ((heading - displayed + 540f) % 360f) - 180f;
        displayed = (displayed + delta * .14f + 360f) % 360f;
        c.save(); c.rotate(-displayed, cx, cy);
        drawTicks(c, cx, cy, radius);
        drawCardinal(c, "N", cx, cy - radius + 38, Color.rgb(255, 74, 64));
        drawCardinal(c, "E", cx + radius - 30, cy + 10, Color.WHITE);
        drawCardinal(c, "S", cx, cy + radius - 20, Color.WHITE);
        drawCardinal(c, "O", cx - radius + 30, cy + 10, Color.WHITE);
        c.restore();
        drawNeedle(c, cx, cy, radius * .63f);

        p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); p.setTextSize(w * .13f);
        c.drawText(String.format(java.util.Locale.getDefault(), "%03.0f°", heading), cx, cy + 20, p);
        p.setTextSize(w * .035f); p.setColor(Color.rgb(132, 255, 170));
        c.drawText(direction(heading) + "  •  " + (trueNorth ? "NORTE VERDADERO" : "NORTE MAGNÉTICO"), cx, cy + 54, p);
        status(c, cx, h);
        if (delta > .05f || delta < -.05f) postInvalidateOnAnimation();
    }

    private void title(Canvas c, float cx, float h) {
        p.setTextAlign(Paint.Align.CENTER); p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(255, 210, 55)); p.setTextSize(getWidth() * .077f);
        c.drawText("RADAR COMPASS", cx, h * .105f, p);
        p.setTextSize(getWidth() * .031f); p.setColor(Color.rgb(255, 142, 24)); c.drawText("ENCUENTRA TU CAMINO", cx, h * .142f, p);
    }

    private void drawEnergy(Canvas c, float w, float h) {
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(Color.argb(55, 255, 180, 40));
        for (int i = 0; i < 18; i++) { float x = (i * 73 % 101) / 101f * w; float y = (i * 47 % 97) / 97f * h; c.drawCircle(x, y, 3 + i % 5, p); }
    }

    private void drawTicks(Canvas c, float cx, float cy, float r) {
        p.setStyle(Paint.Style.STROKE); p.setColor(Color.argb(185, 215, 255, 225));
        for (int d = 0; d < 360; d += 5) { c.save(); c.rotate(d, cx, cy); p.setStrokeWidth(d % 30 == 0 ? 4 : 2); float len = d % 30 == 0 ? 18 : 9; c.drawLine(cx, cy - r, cx, cy - r + len, p); c.restore(); }
    }

    private void drawCardinal(Canvas c, String text, float x, float y, int color) {
        p.setStyle(Paint.Style.FILL); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(getWidth() * .06f); p.setColor(color); c.drawText(text, x, y, p);
    }

    private void drawNeedle(Canvas c, float cx, float cy, float len) {
        Path north = new Path(); north.moveTo(cx, cy - len); north.lineTo(cx - 18, cy + 8); north.lineTo(cx + 18, cy + 8); north.close();
        p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(255, 65, 52)); c.drawPath(north, p);
        Path south = new Path(); south.moveTo(cx, cy + len); south.lineTo(cx - 18, cy - 8); south.lineTo(cx + 18, cy - 8); south.close();
        p.setColor(Color.rgb(225, 232, 240)); c.drawPath(south, p);
        p.setColor(Color.rgb(255, 211, 55)); c.drawCircle(cx, cy, 12, p); p.setColor(Color.rgb(77, 37, 8)); c.drawCircle(cx, cy, 4, p);
    }

    private void status(Canvas c, float cx, float h) {
        p.setTextAlign(Paint.Align.CENTER); p.setTextSize(getWidth() * .031f);
        if (error != null) { p.setColor(Color.rgb(255, 90, 70)); c.drawText(error, cx, h * .89f, p); return; }
        boolean low = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE || accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW;
        p.setColor(low ? Color.rgb(255, 180, 55) : Color.rgb(127, 216, 155));
        c.drawText(low ? "Mueve el teléfono en forma de 8 para calibrar" : "SENSOR ESTABLE", cx, h * .865f, p);
        p.setColor(gps ? Color.rgb(83, 237, 135) : Color.rgb(178, 187, 202));
        c.drawText(gps ? "● GPS ACTIVO · NORTE CORREGIDO" : "○ SIN GPS · USANDO NORTE MAGNÉTICO", cx, h * .91f, p);
    }

    private String direction(float h) {
        String[] d = {"N", "NE", "E", "SE", "S", "SO", "O", "NO"};
        return d[Math.round(h / 45f) % 8];
    }
}
