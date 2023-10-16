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

    private boolean sensorsOn;
    private final Context mainContext;
    private final SurfaceHolder surfaceHolder;
    private final PongTable pongTable;
    private final Handler gameStatusHandler;
    private final Handler scoreHandler;

    private boolean run = false;
    private int gameState;
    private Object runLock;
    private static final int PHYSICS_FPS = 60;

    public GameThread(Context mainContext, SurfaceHolder surfaceHolder, PongTable pongTable,
                      Handler gameStatusHandler, Handler scoreHandler) {
        this.mainContext = mainContext;
        this.surfaceHolder = surfaceHolder;
        this.pongTable = pongTable;
        this.gameStatusHandler = gameStatusHandler;
        this.scoreHandler = scoreHandler;

        runLock = new Object();
    }


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

    public void setState(int state){
        synchronized (surfaceHolder){
            gameState = state;
            Resources res = mainContext.getResources();

            switch (gameState){
                case STATE_READY:
                    // set up new round
                    break;
                case STATE_RUNNING:
                    // hideStatus()
                    break;
                case STATE_PAUSED:
                    break;
                case STATE_WIN:
                    break;
                case STATE_LOSE:
                    break;
            }
        }
    }

    public void setUpNewRound(){
        synchronized (surfaceHolder){
            pongTable.setUpTable();
        }
    }

    public void setRunning(boolean running){
        synchronized (runLock){
            run = running;
        }
    }

    public boolean isBetweenRounds(){
        return gameState != STATE_RUNNING;
    }

    private void setStatusText(String text){
        Message message = gameStatusHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        bundle.putInt("visibility", View.VISIBLE);
        message.setData(bundle);
        gameStatusHandler.sendMessage(message);
    }

    private void hideStatusText(){
        Message message = gameStatusHandler.obtainMessage();
        Bundle bundle =  new Bundle();
        bundle.putInt("visibility", View.INVISIBLE);
        message.setData(bundle);
        gameStatusHandler.sendMessage(message);
    }

    public void setScoreText(String playerScore, String opponentScore){
        Message message = scoreHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("player", playerScore);
        bundle.putString("opponent", opponentScore);
        message.setData(bundle);
        scoreHandler.sendMessage(message);
    }

    /* GETTERS AND SETTERS */

    public boolean isSensorsOn() {
        return sensorsOn;
    }

    public void setSensorsOn(boolean sensorsOn) {
        this.sensorsOn = sensorsOn;
    }

    public Context getMainContext() {
        return mainContext;
    }

    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    public PongTable getPongTable() {
        return pongTable;
    }

    public Handler getGameStatusHandler() {
        return gameStatusHandler;
    }

    public Handler getScoreHandler() {
        return scoreHandler;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public int getGameState() {
        return gameState;
    }

    public void setGameState(int gameState) {
        this.gameState = gameState;
    }

    public Object getRunLock() {
        return runLock;
    }

    public void setRunLock(Object runLock) {
        this.runLock = runLock;
    }
}
