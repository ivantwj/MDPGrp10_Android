package com.mdpgrp10.androidmobilecontrollermodule;

/**
 * Created by Glambert on 10/2/2016.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.util.LinkedHashSet;
import java.util.Set;

public class Maze extends SurfaceView implements Callback {
    private PanelThread _thread;
    private int arcBlinkDirection;
    private int arcColor;
    //private Set<Obstacle> autoObstacles;
    private Paint axisPaint;
    //private Set<Obstacle> exploredPath;
    private String hiddendir;
    private String hiddenx;
    private String hiddeny;
    //private Set<Obstacle> manualObstacles;
    private String[] mapMeta;
    private float offset;
    private Paint paint;
    private Canvas pcanvas;
    private float pps;
    private float ratio;
    private int f5x;
    private int f6y;

    class PanelThread extends Thread {
        private Maze _panel;
        private boolean _run;
        private SurfaceHolder _surfaceHolder;

        public PanelThread(SurfaceHolder surfaceHolder, Maze panel) {
            this._run = false;
            this._surfaceHolder = surfaceHolder;
            this._panel = panel;
        }

        public void setRunning(boolean run) {
            this._run = run;
        }

        public void run() {
            while (this._run) {
                Canvas c = null;
                try {
                    c = this._surfaceHolder.lockCanvas(null);
                    synchronized (this._surfaceHolder) {
                        Maze.this.postInvalidate();
                    }
                    if (c != null) {
                        this._surfaceHolder.unlockCanvasAndPost(c);
                    }
                } catch (Throwable th) {
                    if (c != null) {
                        this._surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }

    public Maze(Context context) {
        super(context);
        this.paint = new Paint();
        this.axisPaint = new Paint();
        this.pcanvas = new Canvas();
        this.arcColor = 80;
        this.arcBlinkDirection = 1;
        this.ratio = 8.0f;
        this.pps = 10.0f;
        this.offset = 5.0f;
        /*this.autoObstacles = new LinkedHashSet();
        this.manualObstacles = new LinkedHashSet();
        this.exploredPath = new LinkedHashSet();*/
        this.mapMeta = new String[]{"80", "60", "1", "1", "90", "1", "1", "72", "52"};
        this.f5x = Integer.parseInt(this.mapMeta[0]);
        this.f6y = Integer.parseInt(this.mapMeta[1]);
        this.hiddenx = this.mapMeta[2];
        this.hiddeny = this.mapMeta[3];
        this.hiddendir = this.mapMeta[4];
        setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        getHolder().addCallback(this);
        this._thread = new PanelThread(getHolder(), this);
    }

    public Maze(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint();
        this.axisPaint = new Paint();
        this.pcanvas = new Canvas();
        this.arcColor = 80;
        this.arcBlinkDirection = 1;
        this.ratio = 8.0f;
        this.pps = 10.0f;
        this.offset = 5.0f;
        /*this.autoObstacles = new LinkedHashSet();
        this.manualObstacles = new LinkedHashSet();
        this.exploredPath = new LinkedHashSet();*/
        this.mapMeta = new String[]{"80", "60", "1", "1", "90", "1", "1", "72", "52"};
        this.f5x = Integer.parseInt(this.mapMeta[0]);
        this.f6y = Integer.parseInt(this.mapMeta[1]);
        this.hiddenx = this.mapMeta[2];
        this.hiddeny = this.mapMeta[3];
        this.hiddendir = this.mapMeta[4];
    }

    public Maze(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.paint = new Paint();
        this.axisPaint = new Paint();
        this.pcanvas = new Canvas();
        this.arcColor = 80;
        this.arcBlinkDirection = 1;
        this.ratio = 8.0f;
        this.pps = 10.0f;
        this.offset = 5.0f;
        /*this.autoObstacles = new LinkedHashSet();
        this.manualObstacles = new LinkedHashSet();
        this.exploredPath = new LinkedHashSet();*/
        this.mapMeta = new String[]{"80", "60", "1", "1", "90", "1", "1", "72", "52"};
        this.f5x = Integer.parseInt(this.mapMeta[0]);
        this.f6y = Integer.parseInt(this.mapMeta[1]);
        this.hiddenx = this.mapMeta[2];
        this.hiddeny = this.mapMeta[3];
        this.hiddendir = this.mapMeta[4];
    }

    public void resetMap(int x, int y) {
        setRobot((float) x, (float) y, 90.0f);
        /*this.autoObstacles.clear();
        this.manualObstacles.clear();*/
    }

    public void updateMap() {
        //this.autoObstacles.addAll(this.manualObstacles);
        this.mapMeta[2] = this.hiddenx;
        this.mapMeta[3] = this.hiddeny;
        this.mapMeta[4] = this.hiddendir;
    }

    public void onDraw(Canvas canvas) {
        this.pcanvas = canvas;
        this.paint.setColor(Color.BLACK);
        this.paint.setStrokeWidth(1.0f);
        float mazeWidth = (float) super.getMeasuredWidth();
        float mazeHeight = (float) super.getMeasuredHeight();
        this.f5x = Integer.parseInt(this.mapMeta[0]);
        this.f6y = Integer.parseInt(this.mapMeta[1]);
        if (this.f5x >= this.f6y) {
            this.pps = mazeWidth / ((float) (this.f5x + 3));
        } else {
            this.pps = mazeHeight / ((float) (this.f6y + 2));
        }
        paintArena();
        paintGrid();
        //paintObstacles();
        paintStartingPoint();
        paintGoal();
        paintRobot();
        //drawExploredPath();
    }

    public void colorGrid(float left, float top, int color) {
        this.paint.setColor(color);
        this.paint.setStrokeWidth(3.0f);
        this.pcanvas.drawCircle(left + 30.0f, top + 30.0f, 30.0f, this.paint);
    }

    public void paintArena() {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1.0f);
        this.pcanvas.drawRect(this.pps + 2.0f, this.pps + 2.0f, ((float) (this.f5x + 2)) * this.pps, ((float) (this.f6y + 2)) * this.pps, paint);
    }

    public void paintGrid() {
        int i;
        float startY = this.pps;
        float startX = startY;
        float endX = (((float) this.f5x) * this.pps) + startX;
        float endY = (((float) this.f6y) * this.pps) + startY;
        for (i = 0; i <= this.f5x; i++) {
            //this.pcanvas.drawLine(startX, startY, startX, endY, this.paint);
            startX += this.pps;
        }
        startY = this.pps;
        startX = startY;
        for (i = 0; i <= this.f6y; i++) {
            //this.pcanvas.drawLine(startX, startY, endX, startY, this.paint);
            startY += this.pps;
        }
        i = 0;
        while (i <= this.f6y) {
            for (int o = 0; o <= this.f5x; o++) {
                if (o % 4 == 0 && i % 4 == 0) {
                    this.axisPaint.setColor(Color.WHITE);
                    this.axisPaint.setStrokeWidth(1.0f);
                    this.pcanvas.drawCircle((((float) o) * this.pps) + this.pps, (((float) i) * this.pps) + this.pps, 2.0f, this.axisPaint);
                }
            }
            i++;
        }
    }

    public void setRobot(float x, float y) {
        this.mapMeta[2] = Integer.toString(((int) x) * 4);
        this.mapMeta[3] = Integer.toString(((int) y) * 4);
        this.hiddenx = this.mapMeta[2];
        this.hiddeny = this.mapMeta[3];
    }

    public void setRobot(float x, float d, float facing) {
        this.mapMeta[2] = Integer.toString(((int) x) * 4);
        this.mapMeta[3] = Integer.toString(((int) d) * 4);
        this.hiddenx = this.mapMeta[2];
        this.hiddeny = this.mapMeta[3];
        this.mapMeta[4] = Integer.toString((int) facing);
        this.hiddendir = this.mapMeta[4];
    }

    public void setStartingPt(float x, float y) {
        this.mapMeta[5] = Integer.toString((int) x);
        this.mapMeta[6] = Integer.toString((int) y);
    }

    public void paintStartingPoint() {
        Paint paint = new Paint();
        float x = Float.parseFloat(this.mapMeta[5]);
        float y = Float.parseFloat(this.mapMeta[6]);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3.0f);
        paint.setAlpha(90);
        this.pcanvas.drawRect(this.pps * x, this.pps * y, (x + 9.0f) * this.pps, (y + 9.0f) * this.pps, paint);
    }

    public void setDirection(float direction) {
        this.mapMeta[4] = Integer.toString((int) direction);
        this.hiddendir = this.mapMeta[4];
    }

    public void paintRobot() {
        float facing;
        float y = 0.0f;
        float x = 0.0f;
        float r = (this.pps * this.ratio) / 2.0f;
        try {
            x = Float.parseFloat(this.mapMeta[2]);
        } catch (Exception e) {
            x = 0.0f;
        }
        try {
            y = Float.parseFloat(this.mapMeta[3]);
        } catch (Exception e2) {
            y = 0.0f;
        }
        try {
            facing = Float.parseFloat(this.mapMeta[4]) - 90.0f;
        } catch (Exception e3) {
            facing = 0.0f;
        }
        this.paint.setColor(Color.rgb(100, 190, 221));
        this.paint.setStrokeWidth(3.0f);
        this.pcanvas.drawCircle((this.pps * x) + this.pps, (this.pps * y) + this.pps, r, this.paint);
        /*Paint arcPaint = new Paint();
        if (this.arcBlinkDirection == 1) {
            this.arcColor += 3;
            if (this.arcColor >= 142) {
                this.arcBlinkDirection = 0;
            }
        } else {
            this.arcColor -= 3;
            if (this.arcColor <= 80) {
                this.arcBlinkDirection = 1;
            }
        }
        arcPaint.setColor(Color.rgb(this.arcColor, this.arcColor, this.arcColor));
        arcPaint.setStyle(Style.STROKE);
        arcPaint.setStrokeWidth(3.0f);
        RectF oval = new RectF();
        oval.set((((this.pps * x) - r) - this.offset) + this.pps, (((this.pps * y) - r) - this.offset) + this.pps, (((this.pps * x) + r) + this.offset) + this.pps, (((this.pps * y) + r) + this.offset) + this.pps);
        this.pcanvas.drawArc(oval, facing - (130.0f / 2.0f), 130.0f, false, arcPaint);*/
    }

    /*public void paintObstacle(float x, float y) {
        x *= 4.0f;
        y *= 4.0f;
        this.paint.setColor(SupportMenu.CATEGORY_MASK);
        this.paint.setStrokeWidth(1.0f);
        this.pcanvas.drawRect((this.pps * x) + this.pps, (this.pps * y) + this.pps, (((this.pps * x) + (this.pps * 4.0f)) + this.pps) + 1.0f, (((this.pps * y) + (this.pps * 4.0f)) + this.pps) + 1.0f, this.paint);
    }

    public void paintExploredPath(float x, float y) {
        x *= 4.0f;
        y *= 4.0f;
        this.paint.setColor(-3355444);
        this.paint.setStrokeWidth(3.0f);
        this.paint.setAlpha(90);
        this.pcanvas.drawRect((this.pps * x) - (this.pps * 3.0f), (this.pps * y) - (this.pps * 3.0f), (((this.pps * x) + (this.pps * 4.0f)) + this.pps) + 1.0f, (((this.pps * y) + (this.pps * 4.0f)) + this.pps) + 1.0f, this.paint);
    }

    public void setGoal(float x, float y) {
        this.mapMeta[7] = Integer.toString((int) x);
        this.mapMeta[8] = Integer.toString((int) y);
    }*/

    public void paintGoal() {
        Paint paint = new Paint();
        float x = Float.parseFloat(this.mapMeta[7]);
        float y = Float.parseFloat(this.mapMeta[8]);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(1.0f);
        paint.setAlpha(90);
        this.pcanvas.drawRect((x + 1.0f) * this.pps, (y + 1.0f) * this.pps, (x + 9.0f) * this.pps, (y + 9.0f) * this.pps, paint);
    }

    /*public void setObstacle(float x, float y) {
        this.autoObstacles.add(new Obstacle(x, y));
        System.out.println("setObstacle: (" + x + ", " + y + ") -> " + "[" + ((((float) this.f5x) * y) + x) + "]");
    }

    public void paintObstacles() {
        for (Obstacle obstacle : this.autoObstacles) {
            paintObstacle((float) obstacle.f7x, (float) obstacle.f8y);
        }
    }

    public void setExploredPath(float x, float y) {
        this.exploredPath.add(new Obstacle(x, y));
        System.out.println("setExploredPath: (" + x + ", " + y + ") -> " + "[" + ((((float) this.f5x) * y) + x) + "]");
    }

    public void drawExploredPath() {
        for (Obstacle obstacle : this.exploredPath) {
            paintExploredPath((float) obstacle.f7x, (float) obstacle.f8y);
        }
    }

    public void setHiddenRobot(float x, float y) {
        this.hiddenx = Integer.toString((int) x);
        this.hiddeny = Integer.toString((int) y);
    }

    public void setHiddenRobot(float x, float y, float facing) {
        this.hiddenx = Integer.toString((int) x);
        this.hiddeny = Integer.toString((int) y);
        this.hiddendir = Integer.toString((int) facing);
    }

    public void setHiddenDirection(float direction) {
        this.hiddendir = Integer.toString((int) direction);
    }

    public void setHiddenObstacle(float x, float y) {
        this.manualObstacles.add(new Obstacle(x, y));
    }*/

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    public void surfaceCreated(SurfaceHolder holder) {
        setWillNotDraw(false);
        this._thread = new PanelThread(getHolder(), this);
        this._thread.setRunning(true);
        this._thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            this._thread.setRunning(false);
            this._thread.join();
        } catch (InterruptedException e) {
        }
    }
}
