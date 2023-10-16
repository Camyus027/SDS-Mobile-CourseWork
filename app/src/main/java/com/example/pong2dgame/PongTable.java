package com.example.pong2dgame;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class PongTable extends SurfaceView implements SurfaceHolder.Callback{

    private GameThread game;
    private TextView status;
    private TextView playerScore;
    private TextView opponentScore;

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

    private float AiProbability; // To control the opponent movements

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

        Handler statusHandle = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                status.setVisibility(msg.getData().getInt("visibility"));
                status.setText(msg.getData().getString("text"));
            }
        };

        Handler scoreHandle = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                playerScore.setText(msg.getData().getString("player"));
                opponentScore.setText(msg.getData().getString("opponent"));
            }
        };

        game = new GameThread(this.getContext(), holder, this,statusHandle, scoreHandle);



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

        AiProbability = 0.8f;

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
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
        game.setRunning(true);
        game.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder,
                               int format, int width, int height) {
        tableWidth = width;
        tableHeight = height;

        game.setUpNewRound();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

        Boolean retry = true;
        game.setRunning(false);
        while(retry){
            try {
                game.join();
                retry = false;
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

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

    public void update(Canvas canvas) {

        // Collisions detetion code



        if (new Random(System.currentTimeMillis()).nextFloat() < AiProbability){
            doAi();
        }
        ball.moveBall(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!game.isSensorsOn()){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (game.isBetweenRounds()){
                        game.setGameState(GameThread.STATE_RUNNING);
                    }else{
                        if (isTouchOnRacquet(event, mainPlayer)){
                            moving = true;
                            lastTouchY = event.getY();
                        }
                    }
                break;

                case MotionEvent.ACTION_MOVE:
                    if (moving){
                        float y = event.getY();
                        float directionY = y - lastTouchY;
                        lastTouchY = y;
                        movePlayerRacquet(directionY, mainPlayer);
                    }
                break;

                case MotionEvent.ACTION_UP:
                    moving = false;
                    break;
            }
        }else{
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                if (game.isBetweenRounds()){
                    game.setGameState(GameThread.STATE_RUNNING);
                }
            }
        }

        return true;
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

    public void setUpTable(){
        placeBall();
        placePlayers();
    }

    private void placePlayers(){
        mainPlayer.adjustBounds(2,(tableHeight-mainPlayer.getRacquetHeight()) /2);
        opponent.adjustBounds(tableWidth-opponent.getRacquetWidth() - 2,
                (tableHeight - opponent.getRacquetHeight()) /2);

    }

    private void placeBall(){
        ball.setCoordinate_x(tableWidth/2);
        ball.setCoordinate_y(tableHeight/2);
        ball.setVelocity_y(ball.getVelocity_y() / Math.abs(ball.getVelocity_y()) * PHY_BALL_SPEED);
        ball.setVelocity_x(ball.getVelocity_x() / Math.abs(ball.getVelocity_x()) * PHY_BALL_SPEED);
    }


    public void setScorePlayer(TextView view){
        playerScore = view;
    }

    public void setOpponentScore (TextView view){
        opponentScore = view;
    }

    public void setStatusView(TextView view){
        status = view;
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

    public float getAiProbability() {
        return AiProbability;
    }

    public void setAiProbability(float aiProbability) {
        AiProbability = aiProbability;
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

    public GameThread getGame() {
        return game;
    }

    public void setGame(GameThread game) {
        this.game = game;
    }
}
