package com.mdpgrp10.androidmobilecontrollermodule;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.mdpgrp10.androidmobilecontrollermodule.Utils.*;
import static com.mdpgrp10.androidmobilecontrollermodule.Maze.*;

public class MainActivity extends ActionBarActivity implements SensorEventListener{

    public static final String TAG = "MainActivity";
    private static boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    //Maze variables
    Maze maze;
    private TextView RobotStatus;
    private EditText MapGrid;
    private Switch SwitchAutoUpdateMap;
    public boolean autoUpdateMap = false;
    private Button ButtonUpdateMap;
    Queue<String> MapQueue = new LinkedList<String>();

    //Bluetooth variables
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    //Robot control variables
    private ImageButton ImageButtonForward;
    private ImageButton ImageButtonAnticlockwise;
    private ImageButton ImageButtonClockwise;
    private Button ButtonF1;
    private Button ButtonF2;
    private Button ButtonExplore;
    private Button ButtonShortestPath;

    //Tilt control variables
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    float [] history = new float[2];
    private ToggleButton ToggleTilt;
    boolean ToggleisSet;

    private SharedPreferences spf;


    /*------------------------------------Default Methods------------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            if (mChatService == null)
                setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
        MapQueue.clear();
    }

    /*@Override
    public synchronized void onResume() {
        super.onResume();
        ivanQueue.clear();
        jenQueue.clear();
        if (D)
            Log.e(TAG, "+ ON RESUME +");
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }
    }*/

    /*------------------------------------setupChat------------------------------------*/
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler);
        spf = getSharedPreferences(PREF_DB, Context.MODE_PRIVATE);

        this.maze = new Maze(this);
        ((RelativeLayout) findViewById(R.id.MazeLayout)).addView(this.maze);

        RobotStatus = (TextView)findViewById(R.id.textViewRobotStatus);
        MapGrid = (EditText)findViewById(R.id.editTextMap);

        ImageButtonForward = (ImageButton) findViewById(R.id.imageButtonForward);
        ImageButtonForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_UP, SET_UP_DEFAULT), true);
            }
        });

        ImageButtonAnticlockwise = (ImageButton) findViewById(R.id.imageButtonAnticlockwise);
        ImageButtonAnticlockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_LEFT, SET_LEFT_DEFAULT), true);
            }
        });

        ImageButtonClockwise = (ImageButton) findViewById(R.id.imageButtonClockwise);
        ImageButtonClockwise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_RIGHT, SET_RIGHT_DEFAULT), true);
            }
        });

        ButtonF1 = (Button) findViewById(R.id.buttonF1);
        ButtonF1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = spf.getString(SET_CMD1, SET_CMD1_DEFAULT);
                sendMessage(message, true);
            }
        });

        ButtonF2 = (Button) findViewById(R.id.buttonF2);
        ButtonF2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = spf.getString(SET_CMD2, SET_CMD2_DEFAULT);
                sendMessage(message, true);
            }
        });

        ButtonExplore = (Button) findViewById(R.id.buttonExplore);
        ButtonExplore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = spf.getString(SET_EXPLORE, SET_EXPLORE_DEFAULT);
                sendMessage(message, true);
            }
        });

        ButtonShortestPath = (Button) findViewById(R.id.buttonShortestPath);
        ButtonShortestPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = spf.getString(SET_SHORTEST_PATH, SET_SHORTEST_PATH_DEFAULT);
                sendMessage(message, true);
            }
        });

        ToggleTilt = (ToggleButton) findViewById(R.id.toggleButtonTilt);
        ToggleTilt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    ToggleisSet = true;

                } else {
                    // The toggle is disabled
                    ToggleisSet = false;
                }
            }
        });

        SwitchAutoUpdateMap = (Switch) findViewById(R.id.switchAutoUpdateMap);
        SwitchAutoUpdateMap.setChecked(false);
        SwitchAutoUpdateMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    autoUpdateMap = true;
                    if (MapQueue.peek() != null){
                        maze.robotChange(MapQueue.peek());
                    }
                }
                else{
                    autoUpdateMap = false;
                }
            }
        });

        ButtonUpdateMap = (Button) findViewById(R.id.buttonUpdate);
        ButtonUpdateMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // update the map
                maze.robotChange(MapQueue.peek());
            }
        });
    }

    /*------------------------------------Bluetooth related Methods------------------------------------*/
    private void ensureDiscoverable() {
        if (D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage(String message, boolean ack) {
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Log.d(TAG, R.string.not_connected + "");
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            mChatService.write(send);
            Log.d(TAG, "MSG sent: " + new String(send));
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,
                                    mConnectedDeviceName), true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting, false);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                            setStatus("Listening ...", false);
                            break;
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected, false);
                            break;
                        default:
                            setStatus(R.string.title_not_connected, false);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    if(readMessage.contains("|")){
                        updateRobot(MapQueue.poll(), readMessage);
                        RobotStatus.setText(" Moving...");
                    }

                    else if (readMessage.contains(" ")){
                        updateMaze(readMessage);
                    }

                    if (readMessage.contains(";")){
                        MapGrid.setText(readMessage);
                        RobotStatus.setText(" Stopped");
                    }

                    if(D)
                        Toast.makeText(MainActivity.this, "Receive from BT: " + readMessage, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final void setStatus(int resId, boolean status) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle, boolean status) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //SharedPreferences.Editor editor;

        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    setupChat();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    /*------------------------------------Map Methods------------------------------------*/
    public void updateRobot(String mapInfo, String action){
        String updatedMap = mapInfo;
        String constantInfo = mapInfo.substring(0, 10);
        String robotInfo;
        String[] tmpRobot = new String[4];
        String obstacleInfo;
        int tmpIndex;
        int spaceCounter = 0;

        //remove spaces and get the information of the robot position and orientation
        while (spaceCounter <= 6) {
            tmpIndex = mapInfo.indexOf(" ");
            spaceCounter++;
            if (spaceCounter > 3) {
                tmpRobot[spaceCounter - 4] = mapInfo.substring(0, tmpIndex);
            }
            mapInfo = mapInfo.substring(tmpIndex + 1);
        }

        robotInfo = tmpRobot[0] + " " + tmpRobot[1] + " " + tmpRobot[2] + " " + tmpRobot[3];
        String RobotHead;
        int x = Integer.parseInt(tmpRobot[0]);
        int y = Integer.parseInt(tmpRobot[1]);
        int xHead = Integer.parseInt(tmpRobot[2]);
        int yHead = Integer.parseInt(tmpRobot[3]);

        //get the obstacle/remaining map information
        if (robotInfo.length() == 7)
            obstacleInfo = updatedMap.substring(19);
        else if (robotInfo.length() == 8)
            obstacleInfo = updatedMap.substring(20);
        else if (robotInfo.length() == 9)
            obstacleInfo = updatedMap.substring(21);
        else if (robotInfo.length() == 10)
            obstacleInfo = updatedMap.substring(22);
        else
            obstacleInfo = updatedMap.substring(23);

        if (action.contains("nothing")) {
            sendMessage("coordinate (" + tmpRobot[0] + ", " + tmpRobot[1] + ")", true);
            maze.robotChange(updatedMap);
        }

        else if (action.contains("|")) {
            RobotHead = getRobotHead(x, y, xHead, yHead);
            if (action.equals("W1|")) {
                if (RobotHead.equals("up")) {
                    y = y - 1;
                    yHead = yHead - 1;
                } else if (RobotHead.equals("down")) {
                    y = y + 1;
                    yHead = yHead + 1;
                } else if (RobotHead.equals("left")) {
                    x = x - 1;
                    xHead = xHead - 1;
                } else if (RobotHead.equals("right")) {
                    x = x + 1;
                    xHead = xHead + 1;
                }
            }
            else if (action.equals("A90|")) {
                if (RobotHead.equals("up")) {
                    xHead = xHead - 1;
                    yHead = yHead + 1;
                } else if (RobotHead.equals("down")) {
                    xHead = xHead + 1;
                    yHead = yHead - 1;
                } else if (RobotHead.equals("left")) {
                    xHead = xHead + 1;
                    yHead = yHead + 1;
                } else if (RobotHead.equals("right")) {
                    xHead = xHead - 1;
                    yHead = yHead - 1;
                }
            }

            else if (action.equals("D90|")) {
                if (RobotHead.equals("up")) {
                    xHead = xHead + 1;
                    yHead = yHead + 1;
                } else if (RobotHead.equals("down")) {
                    xHead = xHead - 1;
                    yHead = yHead - 1;
                } else if (RobotHead.equals("left")) {
                    xHead = xHead + 1;
                    yHead = yHead - 1;
                } else if (RobotHead.equals("right")) {
                    xHead = xHead - 1;
                    yHead = yHead + 1;
                }
            }

            String RobotY = String.valueOf(y);
            String RobotYHead = String.valueOf(yHead);
            String RobotX = String.valueOf(x);
            String RobotXHead = String.valueOf(xHead);
            updatedMap = constantInfo + " " + RobotX + " " + RobotY + " " + RobotXHead + " " + RobotYHead +" " + obstacleInfo;
            System.out.println("map is: " + updatedMap);
            MapQueue.poll();
            MapQueue.add(updatedMap);
            if (autoUpdateMap) {
                maze.robotChange(updatedMap);
            }
        }
    }

    public void updateMaze(String mapInfo){
        String initialMap = MapQueue.poll();
        String updatedMap;
        String robotInfo;
        String[] tmpRobot = new String[4];
        int tmpIndex;
        int spaceCounter = 0;

        //remove spaces and get the information of the robot position and orientation
        while (spaceCounter <= 6) {
            tmpIndex = initialMap.indexOf(" ");
            spaceCounter++;
            if (spaceCounter > 3) {
                tmpRobot[spaceCounter - 4] = initialMap.substring(0, tmpIndex);
            }
            initialMap = initialMap.substring(tmpIndex + 1);
        }

        robotInfo = tmpRobot[0] + " " + tmpRobot[1] + " " + tmpRobot[2] + " " + tmpRobot[3];
        updatedMap = "GRID 15 20 " + robotInfo + " " + mapInfo;
        MapQueue.add(updatedMap);
        if (autoUpdateMap) {
            maze.robotChange(updatedMap);
        }
    }

    public String getRobotHead(int x, int y, int xHead, int yHead){
        if(x == xHead){
            if(y < yHead)
                return ("down");
            else
                return ("up");
        }else if(y == yHead) {
            if (x < xHead)
                return ("right");
            else
                return ("left");
        }else
            return "N";
    }

/*------------------------------------Tilt Control Methods------------------------------------*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        history[0] = event.values[0];
        history[1] = event.values[1];

            if (xChange > 2 && ToggleisSet){
                //clockwise
                sendMessage(spf.getString(SET_RIGHT, SET_RIGHT_DEFAULT), true);
            }

            else if (xChange < -2 && ToggleisSet){
                //anticlockwise
                sendMessage(spf.getString(SET_LEFT, SET_LEFT_DEFAULT), true);
            }

            if (yChange > 2 && ToggleisSet){
                //forward
                sendMessage(spf.getString(SET_UP, SET_UP_DEFAULT), true);
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*------------------------------------Menu Bar Methods------------------------------------*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        Intent serverIntent;
        switch (item.getItemId()) {
            case R.id.action_connectB:
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;

            case R.id.action_setCommands:
                SetFunctionActivity setFunct = new SetFunctionActivity();
                setFunct.show(getSupportFragmentManager(), "setFunct");
                return true;

            case R.id.action_discoverable:
                ensureDiscoverable();
                return true;

            case R.id.action_setXy:
                RobotSettingActivity initRobot = new RobotSettingActivity();
                initRobot.show(getSupportFragmentManager(), "initRobot");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}

