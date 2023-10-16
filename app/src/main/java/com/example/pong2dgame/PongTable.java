package com.example.pong2dgame;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class PongTable extends SurfaceView implements SurfaceHolder.Callback{

    private Player mainPlayer;
    private Player opponent;
    private Ball ball;
    private Paint netPaint;
    private Paint tableBoundsPaint;
    private int tableWidth;
    private int tableHeight;
    private Context mainContext;

    SurfaceHolder holder;

    private static float PHY_RACQUET_SPEED = 15.0f;
    private static float PHY_BALL_SPEED = 15.0f;

    private float AiPortability; // To control the opponent movements

    private boolean moving; // checks if the ball moves
    private float lastTouchY;


    public PongTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPongTable(context, attrs);
    }

    public PongTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPongTable(context, attrs);
    }

    // Initiate the pong table and all its variables
    public void initPongTable(Context context, AttributeSet attributeSet){
        this.mainContext = context;
        holder = getHolder();
        holder.addCallback(this);

        // Game loop initialize

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.PongTable);
        int racquetHeight = array.getInteger(R.styleable.PongTable_racketHeight, 340);
        int racquetWidth = array.getInteger(R.styleable.PongTable_racketWidth, 100);
        int ballRadius = array.getInteger(R.styleable.PongTable_ballRadius, 25);

        // Set Player
        Paint playerPaint = new Paint();
        playerPaint.setAntiAlias(true);
        playerPaint.setColor(ContextCompat.getColor(mainContext,R.color.player_color));
        mainPlayer = new Player(racquetWidth, racquetHeight, playerPaint);

        // set Opponent
        Paint opponentPaint = new Paint();
        opponentPaint.setAntiAlias(true);
        opponentPaint.setColor(ContextCompat.getColor(mainContext, R.color.opponent_color));
        opponent = new Player(racquetWidth, racquetHeight, opponentPaint);

        // set Ball
        Paint ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(ContextCompat.getColor(mainContext, R.color.ball_color));
        ball = new Ball(ballRadius, ballPaint);

        // Draw Middle lines
        netPaint = new Paint();
        netPaint.setAntiAlias(true);
        netPaint.setColor(Color.WHITE);
        netPaint.setAlpha(80);
        netPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        netPaint.setStrokeWidth(10.f);
        netPaint.setPathEffect(new DashPathEffect(new float[]{5,5}, 0));

        // Draw bounds
        tableBoundsPaint =  new Paint();
        tableBoundsPaint.setAntiAlias(true);
        tableBoundsPaint.setColor(ContextCompat.getColor(mainContext, R.color.table_color));
        tableBoundsPaint.setStyle(Paint.Style.STROKE);
        tableBoundsPaint.setStrokeWidth(15.f);

        AiPortability = 0.8f;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(ContextCompat.getColor(mainContext, R.color.table_color));
        canvas.drawRect(0,0, tableWidth, tableHeight, tableBoundsPaint);

        int middle = tableWidth/2;
        canvas.drawLine(middle, 1, middle, tableHeight - 1, netPaint);

        mainPlayer.draw(canvas);
        opponent.draw(canvas);
        ball.draw(canvas);

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder,
                               int format, int width, int height) {
        tableWidth = width;
        tableHeight = height;


    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void doAi(){
        // Controls how the  AI opponent moves. It reacts when the ball is close to the top or side

        if (opponent.getBounds().top > ball.getCoordinate_y()){
            movePlayer(opponent,
                    opponent.getBounds().left,
                    opponent.getBounds().top - PHY_RACQUET_SPEED);
        }else if(opponent.getBounds().top + opponent.getRacquetHeight() < ball.getCoordinate_y()){
            movePlayer(opponent,
                    opponent.getBounds().left,
                    opponent.getBounds().top + PHY_RACQUET_SPEED);
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private boolean isTouchOnRacquet(MotionEvent event, Player player){
        return player.getBounds().contains(event.getX(),event.getY());
    }

    public void movePlayerRacquet(float directionY, Player player){

        // To ensure that no other is using the table until this method finishes
        synchronized (holder){
            movePlayer(player, player.getBounds().left, player.getBounds().top + directionY);
        }
    }
    public synchronized void movePlayer(Player player, float left, float top){

        // Check if the movements are inside the boundaries of the table and correct them
        if (left < 2){
            left = 2;
        } else if (left + player.getRacquetWidth() >= tableWidth -2){
            top = tableWidth - player.getRacquetWidth() -2;
        }

        if (top < 0){
            top = 0;
        } else if (top + player.getRacquetHeight() >= tableHeight){
            top = tableHeight - player.getRacquetHeight() - 1;
        }

        player.adjustBounds(left, top);

    }

    /* GETTERS AND SETTERS */

    public Player getMainPlayer() {
        return mainPlayer;
    }

    public void setMainPlayer(Player mainPlayer) {
        this.mainPlayer = mainPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public Ball getBall() {
        return ball;
    }

    public void setBall(Ball ball) {
        this.ball = ball;
    }

    public Paint getNetPaint() {
        return netPaint;
    }

    public void setNetPaint(Paint netPaint) {
        this.netPaint = netPaint;
    }

    public Paint getTableBoundsPaint() {
        return tableBoundsPaint;
    }

    public void setTableBoundsPaint(Paint tableBoundsPaint) {
        this.tableBoundsPaint = tableBoundsPaint;
    }

    public int getTableWidth() {
        return tableWidth;
    }

    public void setTableWidth(int tableWidth) {
        this.tableWidth = tableWidth;
    }

    public int getTableHeight() {
        return tableHeight;
    }

    public void setTableHeight(int tableHeight) {
        this.tableHeight = tableHeight;
    }

    public Context getMainContext() {
        return mainContext;
    }

    public void setMainContext(Context mainContext) {
        this.mainContext = mainContext;
    }

    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    public static float getPhyRacquetSpeed() {
        return PHY_RACQUET_SPEED;
    }

    public static void setPhyRacquetSpeed(float phyRacquetSpeed) {
        PHY_RACQUET_SPEED = phyRacquetSpeed;
    }

    public static float getPhyBallSpeed() {
        return PHY_BALL_SPEED;
    }

    public static void setPhyBallSpeed(float phyBallSpeed) {
        PHY_BALL_SPEED = phyBallSpeed;
    }

    public float getAiPortability() {
        return AiPortability;
    }

    public void setAiPortability(float aiPortability) {
        AiPortability = aiPortability;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public float getLastTouchY() {
        return lastTouchY;
    }

    public void setLastTouchY(float lastTouchY) {
        this.lastTouchY = lastTouchY;
    }
}
