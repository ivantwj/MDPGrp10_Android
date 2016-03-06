package com.mdpgrp10.androidmobilecontrollermodule;

/**
 * Created by Glambert on 26/2/2016.
 */

        import android.graphics.Canvas;
        import android.util.Log;
        import android.view.SurfaceHolder;

        import java.util.Queue;

        import static com.mdpgrp10.androidmobilecontrollermodule.Utils.*;

public class RobotThread extends Thread {

    private static final String TAG = "RobotThread";
    private final Maze maze;
    private boolean running = false;

    private SurfaceHolder sh;
    private float initialX;
    private float initialY;
    private float cellWidth;
    private float currentX;
    private float currentY;
    private String mapInfo;
    private String headPos;
    //public Queue<String> ivanQueue;

    public RobotThread(SurfaceHolder surfaceHolder, Maze maze, float initialX, float initialY, float cellWidth){
        sh = surfaceHolder;
        this.maze = maze;
        this.initialX = initialX;
        this.currentX = initialX;
        this.initialY = initialY;
        this.currentY = initialY;
        this.cellWidth = cellWidth;
    }

    public void run(){
        while(running){
            int n = maze.robotActionQueue.size();
            if(n <= 0) {
                try{
                    Thread.sleep(2500);
                }catch (InterruptedException ite){
                    ite.printStackTrace();
                }
                System.out.println("the size of the queue is " + n );
                continue;
            }
            Canvas c = null;
            try{
                c = sh.lockCanvas(null);
                synchronized (sh){
                    /*Log.d(TAG, "updatedInfo[0]: " + currentX);
                    Log.d(TAG, "updatedInfo[1]: " + currentY);
                    Log.d(TAG, "updatedInfo[2]: " + headPos);
                    Log.d(TAG, "updatedInfo[3]: " + mapInfo);*/
                    n = maze.robotActionQueue.size();
                    System.out.println("the size of the queue is " + n);

                    decodeAction(maze.robotActionQueue.poll());
                    /*Log.d(TAG, "updatedInfo[0]: " + currentX);
                    Log.d(TAG, "updatedInfo[1]: " + currentY);
                    Log.d(TAG, "updatedInfo[2]: " + headPos);
                    Log.d(TAG, "updatedInfo[3]: " + mapInfo);*/
                    maze.updateMap(currentX, currentY, headPos, mapInfo);
                    /*Log.d(TAG, "updatedInfo[0]: " + currentX);
                    Log.d(TAG, "updatedInfo[1]: " + currentY);
                    Log.d(TAG, "updatedInfo[2]: " + headPos);
                    Log.d(TAG, "updatedInfo[3]: " + mapInfo);*/
                }
            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG, "drawing robot error: " + e.toString());
            }finally{
                if(c != null) {
                    sh.unlockCanvasAndPost(c);
                }
            }
        }
    }


    private void decodeAction(String newMapInfo){
        String[] updatedInfo = processMapDescriptor(newMapInfo);
        currentX = initialX + (Integer.valueOf(updatedInfo[0]) - 1) * cellWidth;
        currentY = initialY + (Integer.valueOf(updatedInfo[1]) - 1) * cellWidth;
        headPos = updatedInfo[2];
        mapInfo = updatedInfo[3];
        Log.d(TAG, "X: " + (updatedInfo[0]) + " Y: " + (updatedInfo[1]) + " head: " + updatedInfo[2]);
    }

    public void setRunning(boolean running){
        this.running = running;
    }



}
