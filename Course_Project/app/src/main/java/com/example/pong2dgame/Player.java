package com.example.pong2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

public class Player {
    private final int racquetWidth;
    private final int racquetHeight;
    private int score;
    private final Paint paint;
    private final RectF bounds; // To prevent collisions

    /**
     * Main constructor of the player class
     * @param racquetWidth width of the racquet
     * @param racquetHeight height of the racquet
     * @param paint Paint class over which to base the racquet properties
     */
    public Player(int racquetWidth, int racquetHeight, Paint paint) {
        this.racquetWidth = racquetWidth;
        this.racquetHeight = racquetHeight;
        this.paint = paint;
        score = 0;
        bounds = new RectF(0,0,racquetWidth,racquetHeight);
    }

    /**
     * It draws the player racquet
     * @param canvas canvas in which to draw the racquet
     */
    public void draw(Canvas canvas){
        canvas.drawRoundRect(bounds,5,5, paint);
    }

    @NonNull
    @Override
    public String toString() {
        return "Player{" +
                "score=" + score +
                '}' + "Width: " + racquetWidth
                + "Height: " + racquetHeight
                ;
    }

    /**
     * Adjust the bounds offset according to a given left and top values
     * @param left left bound limit
     * @param top top bound limit
     */
    public void adjustBounds(float left, float top){
        this.bounds.offsetTo(left, top);
    }

    /* GETTERS AND SETTERS */

    public int getRacquetWidth() {
        return racquetWidth;
    }

    public int getRacquetHeight() {
        return racquetHeight;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public RectF getBounds() {
        return bounds;
    }

}
