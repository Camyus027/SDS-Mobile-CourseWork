package com.example.pong2dgame;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Ball {
    private float coordinate_x;
    private float coordinate_y;
    private float velocity_x;
    private float velocity_y;

    private int radius;
    private Paint paint; //To paint the ball in the screen

    public Ball(int radius, Paint paint) {
        this.paint = paint;
        this.radius = radius;
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(coordinate_x, coordinate_y, radius, paint);
    }

    public void moveBall(Canvas canvas){
        coordinate_x += velocity_x;
        coordinate_y +=velocity_y;

        // To ensure that the ball stays within the canvas limits
        if (coordinate_y < radius){
            coordinate_y = radius;
        }else if (coordinate_y + radius >= canvas.getHeight()){
            coordinate_y = canvas.getHeight() -radius -1;
        }
    }

    @Override
    public String toString() {
        return "Coordinate X " + coordinate_x
                + " Coordinate y " + coordinate_y
                + " velocity of x: " + velocity_x
                + " velocity of y:  " + velocity_y;
    }

    /* GETTERS AND SETTERS */

    public float getCoordinate_x() {
        return coordinate_x;
    }

    public void setCoordinate_x(float coordinate_x) {
        this.coordinate_x = coordinate_x;
    }

    public float getCoordinate_y() {
        return coordinate_y;
    }

    public void setCoordinate_y(float coordinate_y) {
        this.coordinate_y = coordinate_y;
    }

    public float getVelocity_x() {
        return velocity_x;
    }

    public void setVelocity_x(float velocity_x) {
        this.velocity_x = velocity_x;
    }

    public float getVelocity_y() {
        return velocity_y;
    }

    public void setVelocity_y(float velocity_y) {
        this.velocity_y = velocity_y;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
}
