/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.graphics;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;

/**
 * Shows how to use the text measurement methods: Paint.getTextWidths(),
 * Paint.measureText(), and Paint.getTextBounds() to determine what area
 * each character in a string as well as the complete string will occupy
 * when they are drawn using Canvas.drawText, then draws a colored rectangle
 * around each string and a line under each at the baseline. Text was too
 * small so I modified it a bit.
 */
public class MeasureText extends GraphicsActivity {

    /**
     * Called when the activity is starting. First we call through to our super's implementation of
     * {@code onCreate}, then we set our content view to a new instance of {@code SampleView}.
     *
     * @param savedInstanceState we do not override {@code onSaveInstanceState} so do not use.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SampleView(this));
    }

    /**
     * Only used in {@code createColors} so unused
     */
    private static final int WIDTH = 50;
    /**
     * Only used in {@code createColors} so unused
     */
    private static final int HEIGHT = 50;
    /**
     * Only used in {@code createColors} so unused
     */
    private static final int STRIDE = 64;   // must be >= WIDTH

    /**
     * Unused, so who cares.
     *
     * @return an array of colors
     */
    @SuppressWarnings("unused")
    private static int[] createColors() {
        int[] colors = new int[STRIDE * HEIGHT];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int r = x * 255 / (WIDTH - 1);
                int g = y * 255 / (HEIGHT - 1);
                int b = 255 - Math.min(r, g);
                int a = Math.max(r, g);
                colors[y * STRIDE + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        return colors;
    }

    /**
     * Custom view which displays three lines of text, and using the text measuring methods draws a
     * tight, light green rectangle around the text, and a RED line at the baseline of the text.
     */
    private static class SampleView extends View {
        /**
         * {@code Paint} used to draw with in the method {@code showText} (called from {@code onDraw}.
         */
        private Paint mPaint;
        /**
         * Starting x coordinate for our {@code onDraw} to draw to its Canvas
         */
        private float mOriginX = 10;
        /**
         * Starting y coordinate for our {@code onDraw} to draw to its Canvas
         */
        private float mOriginY = 80;

        /**
         * Constructor for our View. First we call our super's constructor, then we enable our view
         * to receive focus. We allocate a new {@code Paint} for our field {@code Paint mPaint}, set
         * its antialias flag to true, set the stroke width to 5, set the line cap style to ROUND,
         * text size to 64, and finally set its typeface to a typeface object of the SERIF family,
         * and ITALIC style
         *
         * @param context {@code Context} used for resources, this {@code MeasureText} activity when
         *                called from our {@code onCreate} override.
         */
        public SampleView(Context context) {
            super(context);
            setFocusable(true);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(5);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setTextSize(64);
            mPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.ITALIC));
        }

        /**
         * Convenience method to measure the {@code String text}, draw a light green rectangle of
         * that size, draw the {@code text}, and draw a RED horizontal line across the baseline of
         * the text. First we allocate {@code Rect bounds} which we will use to hold the rectangle
         * that encloses all of {@code String text}, and {@code float[] widths} which will hold the
         * widths of each of the corresponding characters in {@code text}. We set the text size of
         * {@code Paint mPaint} to 100.
         *
         * @param canvas {@code Canvas} we are to draw to
         * @param text text string we are to draw
         * @param align not used
         */
        @SuppressWarnings("UnusedParameters")
        private void showText(Canvas canvas, String text, Paint.Align align) {
            //   mPaint.setTextAlign(align);

            Rect bounds = new Rect();
            float[] widths = new float[text.length()];

            mPaint.setTextSize((float) 100.0);
            int count = mPaint.getTextWidths(text, 0, text.length(), widths);
            float w = mPaint.measureText(text, 0, text.length());
            mPaint.getTextBounds(text, 0, text.length(), bounds);

            mPaint.setColor(0xFF88FF88);
            canvas.drawRect(bounds, mPaint);
            mPaint.setColor(Color.BLACK);
            canvas.drawText(text, 0, 0, mPaint);

            float[] pts = new float[2 + count * 2];
            float x = 0;
            float y = 0;
            pts[0] = x;
            pts[1] = y;
            for (int i = 0; i < count; i++) {
                x += widths[i];
                pts[2 + i * 2] = x;
                pts[2 + i * 2 + 1] = y;
            }
            mPaint.setColor(Color.RED);
            mPaint.setStrokeWidth(0);
            canvas.drawLine(0, 0, w, 0, mPaint);
            mPaint.setStrokeWidth(5);
            canvas.drawPoints(pts, 0, (count + 1) << 1, mPaint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            canvas.translate(mOriginX, mOriginY);

            showText(canvas, "Measure", Paint.Align.LEFT);
            canvas.translate(0, 180);
            showText(canvas, "wiggy!", Paint.Align.CENTER);
            canvas.translate(0, 180);
            showText(canvas, "Text", Paint.Align.RIGHT);
        }
    }
}
