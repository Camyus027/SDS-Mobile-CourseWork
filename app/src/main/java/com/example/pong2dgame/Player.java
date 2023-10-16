package com.example.pong2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Player {
    private int racquetWidth;
    private int racquetHeight;
    private int score;
    private Paint paint;
    private RectF bounds; // To prevent collisions

    public Player(int racquetWidth, int racquetHeight, Paint paint) {
        this.racquetWidth = racquetWidth;
        this.racquetHeight = racquetHeight;
        this.paint = paint;
        score = 0;
        bounds = new RectF(0,0,racquetWidth,racquetHeight);
    }

    public void draw(Canvas canvas){
        canvas.drawRoundRect(bounds,5,5, paint);
    }

    @Override
    public String toString() {
        return "Player{" +
                "score=" + score +
                '}' + "Width: " + racquetWidth
                + "Height: " + racquetHeight
                ;
    }

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
