package com.mdpgrp10.androidmobilecontrollermodule;

/**
 * Created by Glambert on 10/2/2016.
 */


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class Maze extends SurfaceView implements SurfaceHolder.Callback {

    //private final Bitmap bmp;
    private PanelThread _thread;
    private Canvas pcanvas;



    public Maze(Context context) {
        super(context);
        //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        getHolder().addCallback(this);
    }


    @Override
    public void onDraw(Canvas canvas) {
        this.pcanvas = canvas;
        //paint.setStyle(Paint.Style.FILL);
        // make the entire canvas white
        /*paint.setColor(Color.BLUE);
        canvas.drawPaint(paint); */
        //canvas.drawColor(Color.GRAY);
        //this.pcanvas = canvas;
        /*// draw a solid blue circle
        paint.setColor(Color.BLUE);
        canvas.drawCircle(20, 20, 15, paint);*/

        paintArena();

        // draw blue circle with antialiasing turned on
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(20, 20, 15, paint);


    }

    public void paintArena() {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        pcanvas.drawRect(0, 0, 0+screenWidth, (0+screenHeight)/2, paint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {


    }

    class PanelThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private Maze _maze;
        private boolean _run = false;


        public PanelThread(SurfaceHolder surfaceHolder, Maze maze) {
            _surfaceHolder = surfaceHolder;
            _maze = maze;
        }


        public void setRunning(boolean run) { //Allow us to stop the thread
            _run = run;
        }


        @Override
        public void run() {
            Canvas c;
            while (_run) {     //When setRunning(false) occurs, _run is
                c = null;      //set to false and loop ends, stopping thread


                try {


                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {


                        //Insert methods to modify positions of items in onDraw()
                        postInvalidate();


                    }
                } finally {
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {


        setWillNotDraw(false); //Allows us to use invalidate() to call onDraw()


        _thread = new PanelThread(getHolder(), this); //Start the thread that
        _thread.setRunning(true);                     //will make calls to
        _thread.start();                              //onDraw()
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            _thread.setRunning(false);                //Tells thread to stop
            _thread.join();                           //Removes thread from mem.
        } catch (InterruptedException e) {}
    }
}