package com.example.pong2dgame;

import android.annotation.SuppressLint;
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
    private static final float PHY_BALL_SPEED = 15.0f;

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

    /**
     * It initiates the Pong table
     * @param context main context of the class
     * @param attributeSet set of attributes that have to be used
     */
    public void initPongTable(Context context, AttributeSet attributeSet){
        this.mainContext = context;
        holder = getHolder();
        holder.addCallback(this);

        initThread();

        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.PongTable);
        int racquetHeight = array.getInteger(R.styleable.PongTable_racketHeight, 340);
        int racquetWidth = array.getInteger(R.styleable.PongTable_racketWidth, 100);
        int ballRadius = array.getInteger(R.styleable.PongTable_ballRadius, 25);
        // Set Player
        mainPlayer = initPlayer(R.color.player_color, racquetWidth, racquetHeight);
        // set Opponent
        opponent = initPlayer(R.color.opponent_color, racquetWidth, racquetHeight);
        // set Ball
        initBall(ballRadius);
        // Draw Middle lines
        initMiddleLine();
        // Draw bounds
        initBounds();
        AiProbability = 0.8f;

    }

    /**
     * It draw the bounds of the table
     */
    private void initBounds() {
        tableBoundsPaint =  new Paint();
        tableBoundsPaint.setAntiAlias(true);
        tableBoundsPaint.setColor(ContextCompat.getColor(mainContext, R.color.table_color));
        tableBoundsPaint.setStyle(Paint.Style.STROKE);
        tableBoundsPaint.setStrokeWidth(15.f);
    }

    /**
     * It draws the middle line of the table
     */
    private void initMiddleLine() {
        netPaint = new Paint();
        netPaint.setAntiAlias(true);
        netPaint.setColor(Color.WHITE);
        netPaint.setAlpha(80);
        netPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        netPaint.setStrokeWidth(10.f);
        netPaint.setPathEffect(new DashPathEffect(new float[]{5,5}, 0));
    }

    /**
     * It sets the ball characteristics
     * @param ballRadius desire radius of the ball
     */
    private void initBall(int ballRadius) {
        Paint ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(ContextCompat.getColor(mainContext, R.color.ball_color));
        ball = new Ball(ballRadius, ballPaint);
    }

    /**
     * It initiates a player
     * @param player_color color of the player racquet
     * @param racquetWidth width of his racquet
     * @param racquetHeight height of his racquet
     * @return a new player
     */
    private Player initPlayer(int player_color, int racquetWidth, int racquetHeight) {
        Paint playerPaint = new Paint();
        playerPaint.setAntiAlias(true);
        playerPaint.setColor(ContextCompat.getColor(mainContext, player_color));
        return new Player(racquetWidth, racquetHeight, playerPaint);
    }

    /**
     * It prepares and create the Thread in which the app will run
     */
    private void initThread() {
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
    }

    /**
     * Method in charge of drawing all elements in the screen
     * @param canvas Canvas in which to draw the elements
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(ContextCompat.getColor(mainContext, R.color.table_color));
        canvas.drawRect(0,0, tableWidth, tableHeight, tableBoundsPaint);

        int middle = tableWidth/2;
        canvas.drawLine(middle, 1, middle, tableHeight - 1, netPaint);

        game.setScoreText(String.valueOf(mainPlayer.getScore()),
                String.valueOf(opponent.getScore()));


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

        boolean retry = true;
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

    /**
     * Class that control the movements of the AI when the ball is detected.
     */
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

    /**
     * Class that update the information displayed in the canvas depending of the current situation
     * @param canvas Canvas which will be updated
     */
    public void update(Canvas canvas) {

        // Collisions detection code

        if (checkPlayerCollision(mainPlayer, ball)){
            handleCollisions(mainPlayer, ball);
        } else if (checkPlayerCollision(opponent, ball)) {
            handleCollisions(opponent, ball);
        } else if (checkCollisionTopBottomWalls()) {
            ball.setVelocity_y(-ball.getVelocity_y());
        } else if (checkCollisionsLeftWall()) {
            // A ball collides in our field
            game.setState(GameThread.STATE_LOSE);
            return;
        } else if (checkCollisionsRightWall()) {
            // We get a point!
            game.setState(GameThread.STATE_WIN);
            return;
        }

        float random = new Random(System.currentTimeMillis()).nextFloat();
        if (random < AiProbability){
            doAi();
        }
        ball.moveBall(canvas);
    }

    /**
     * Class that check if there have been a collision between the player's racquet and the ball.
     * @param player Player we want to check
     * @param ball ball that is being used to play
     * @return whether or not eh user has hit the ball
     */
    public boolean checkPlayerCollision(Player player, Ball ball){
        return player.getBounds().intersects(
                ball.getCoordinate_x() -ball.getRadius(),
                ball.getCoordinate_y() - ball.getRadius(),
                ball.getCoordinate_x() + ball.getRadius(),
                ball.getCoordinate_y() + ball.getRadius()
        );
    }

    /**
     * @return if the ball has touch the top or down walls
     */
    public boolean checkCollisionTopBottomWalls(){
        return ((ball.getCoordinate_y() <= ball.getRadius()) ||
                (ball.getCoordinate_y() + ball.getRadius() >= tableHeight -1));
    }

    /**
     * @return if the ball has touch the left wall
     */
    public boolean checkCollisionsLeftWall(){
        return ball.getCoordinate_x() <= ball.getRadius();
    }

    /**
     * @return if the ball has touch the right wall
     */
    public boolean checkCollisionsRightWall(){
        return ball.getCoordinate_x() + ball.getRadius() >= tableWidth - 1;
    }

    /**
     * It updates the ball trajectory and speed if there have been a collision
     * @param player player who has touch the ball
     * @param ball ball that has been touch
     */
    private void handleCollisions(Player player, Ball ball){
        ball.setVelocity_x(-ball.getVelocity_x() * 1.05f);
        if (player == mainPlayer){
            ball.setCoordinate_x(mainPlayer.getBounds().right + ball.getRadius());
        }else if (player == opponent){
            ball.setCoordinate_x(opponent.getBounds().left - ball.getRadius());
            PHY_RACQUET_SPEED = PHY_RACQUET_SPEED * 1.05f;
        }
    }

    /**
     * It handle the event of the ball moving through the screen
     * @param event Motion event that have happened
     * @return true
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if (game.isBetweenRounds()){
                        game.setState(GameThread.STATE_RUNNING);
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
        return true;
    }

    /**
     * @param event motion event of the ball
     * @param player player which racquet may have been touch
     * @return if the player racquet has touch the ball
     */
    private boolean isTouchOnRacquet(MotionEvent event, Player player){
        return player.getBounds().contains(event.getX(),event.getY());
    }

    /**
     * It control the movement of a Player racquet
     * @param directionY direction to which it should move
     * @param player player who owns the racquet
     */
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
            left = tableWidth - player.getRacquetWidth() -2;
        }

        if (top < 0){
            top = 0;
        } else if (top + player.getRacquetHeight() >= tableHeight){
            top = tableHeight - player.getRacquetHeight() - 1;
        }

        player.adjustBounds(left, top);

    }

    /**
     * It places the ball and players
     */
    public void setUpTable(){
        placeBall();
        placePlayers();
    }

    /**
     * It place the players in the correct wall
     */
    private void placePlayers(){
        mainPlayer.adjustBounds(2,(tableHeight-mainPlayer.getRacquetHeight()) /2);
        opponent.adjustBounds(tableWidth-opponent.getRacquetWidth() - 2,
                (tableHeight - opponent.getRacquetHeight()) /2);

    }

    /**
     * It place the ball in the center of the field
     */
    private void placeBall(){
        ball.setCoordinate_x(tableWidth/2);
        ball.setCoordinate_y(tableHeight/2);
        ball.setVelocity_y(ball.getVelocity_y() / Math.abs(ball.getVelocity_y()) * PHY_BALL_SPEED);
        ball.setVelocity_x(ball.getVelocity_x() / Math.abs(ball.getVelocity_x()) * PHY_BALL_SPEED);
    }

    /* GETTERS AND SETTERS */

    public void setScorePlayer(TextView view){
        playerScore = view;
    }

    public void setOpponentScore (TextView view){
        opponentScore = view;
    }

    public void setStatusView(TextView view){
        status = view;
    }

    public Player getMainPlayer() {
        return mainPlayer;
    }

    public Player getOpponent() {
        return opponent;
    }

    public static float getPhyBallSpeed() {
        return PHY_BALL_SPEED;
    }


}
