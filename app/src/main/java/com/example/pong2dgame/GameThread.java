package com.example.pong2dgame;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.View;


public class GameThread extends Thread{


    public static final int STATE_READY = 0;
    public static final int STATE_PAUSED = 1;
    public static final int STATE_RUNNING = 2;
    public static final int STATE_WIN = 3;
    public static final int STATE_LOSE = 4;
    private final Context mainContext;
    private final SurfaceHolder surfaceHolder;
    private final PongTable pongTable;
    private final Handler gameStatusHandler;
    private final Handler scoreHandler;

    private boolean run = false;
    private int gameState;
    private final Object runLock;
    private static final int PHYSICS_FPS = 60;

    /**
     * Main constructor of the Game Thread
     * @param mainContext actual context in which we are working
     * @param surfaceHolder surface in which we are working
     * @param pongTable Table of the game
     * @param gameStatusHandler handler for the status of the game
     * @param scoreHandler handler to keep track of the score
     */
    public GameThread(Context mainContext, SurfaceHolder surfaceHolder, PongTable pongTable,
                      Handler gameStatusHandler, Handler scoreHandler) {
        this.mainContext = mainContext;
        this.surfaceHolder = surfaceHolder;
        this.pongTable = pongTable;
        this.gameStatusHandler = gameStatusHandler;
        this.scoreHandler = scoreHandler;

        runLock = new Object();
    }


    /**
     * This method is activated when the Thread is set to run.
     * It calculates a the games ticks to ensure that the graphics works properly
     * independent of the device.
     * It also update the canvas which the needed information as long as the game is running
     */
    @Override
    public void run() {
        long nextGameTick = SystemClock.uptimeMillis();
        int skipTicks = 1000/PHYSICS_FPS;

        // To draw the canvas and update the necessary views
        while(run){
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                if (canvas != null){
                    synchronized (surfaceHolder){
                        if (gameState == STATE_RUNNING){
                            pongTable.update(canvas);
                        }
                        synchronized (runLock){
                            if (run){
                                pongTable.draw(canvas);
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                if (canvas != null){
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            // To ensure that the game can run in any system, independent of its speed:
            nextGameTick += skipTicks;
            long sleepTime = nextGameTick - SystemClock.uptimeMillis();
            if (sleepTime > 0){
                try {
                    Thread.sleep(sleepTime);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        super.run();

    }

    /**
     * It update the status of the game depending on the value received.
     * If the game is ready to be play it setUp the next round.
     * If the game is already running it hides the status code.
     * If the player have won a round it update the score, prepares next round and shows a message
     * If the player have lost a round it update the score, prepares next round and shows a message
     * If the game have been paused by any reason, like an incoming call, it shows it on the screen
     * @param state code to know in which state the system is
     */
    public void setState(int state){
        synchronized (surfaceHolder){
            gameState = state;
            Resources res = mainContext.getResources();

            switch (gameState){
                case STATE_READY:
                    setUpNewRound();
                    break;
                case STATE_RUNNING:
                    hideStatusText();
                    break;
                case STATE_PAUSED:
                    setStatusText(res.getString(R.string.mode_paused));
                    break;
                case STATE_WIN:
                    setStatusText(res.getString(R.string.mode_win));
                    pongTable.getMainPlayer().setScore(pongTable.getMainPlayer().getScore() + 1);
                    setUpNewRound();
                    break;
                case STATE_LOSE:
                    setStatusText(res.getString(R.string.mode_lose));
                    pongTable.getOpponent().setScore(pongTable.getOpponent().getScore() +1);
                    setUpNewRound();
                    break;
            }
        }
    }

    /**
     * It prepares the table for the next round
     */
    public void setUpNewRound(){
        synchronized (surfaceHolder){
            pongTable.setUpTable();
        }
    }

    /**
     * It update the variable that determine whether the game is running or not
     * @param running boolean to know if the game is running or not
     */
    public void setRunning(boolean running){
        synchronized (runLock){
            run = running;
        }
    }

    /**
     * @return if we are in the middle of a round or if a new run have started
     */
    public boolean isBetweenRounds(){
        return gameState != STATE_RUNNING;
    }

    /**
     * It updates the text message in the screen
     * @param text message to be displayed
     */
    private void setStatusText(String text){
        Message message = gameStatusHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        bundle.putInt("visibility", View.VISIBLE);
        message.setData(bundle);
        gameStatusHandler.sendMessage(message);
    }

    /**
     * It hides the status message when needed
     */
    private void hideStatusText(){
        Message message = gameStatusHandler.obtainMessage();
        Bundle bundle =  new Bundle();
        bundle.putInt("visibility", View.INVISIBLE);
        message.setData(bundle);
        gameStatusHandler.sendMessage(message);
    }

    /**
     * It sets the score in the screen
     * @param playerScore score of the player
     * @param opponentScore score of the opponent
     */
    public void setScoreText(String playerScore, String opponentScore){
        Message message = scoreHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("player", playerScore);
        bundle.putString("opponent", opponentScore);
        message.setData(bundle);
        scoreHandler.sendMessage(message);
    }
}
