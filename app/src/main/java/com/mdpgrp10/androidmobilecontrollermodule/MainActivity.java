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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static com.mdpgrp10.androidmobilecontrollermodule.Utils.*;
//import static com.mdpgrp10.androidmobilecontrollermodule.BluetoothChatService.*;

//import static android.support.v4.media.routing.MediaRouterJellybean.UserRouteInfo.setStatus;


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
    private static final String PREFS_NAME = "bt_remote_keyconfig";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final int REQUEST_SAVE_BUTTON = 10;
    private static final int REQUEST_COORD_BUTTON = 100;

    private String F1;
    private String F2;
    private EditText RobotStatus;

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;

    private ImageButton ImageButtonUp;
    private ImageButton ImageButtonDown;
    private ImageButton ImageButtonLeft;
    private ImageButton ImageButtonRight;
    private Button ButtonF1;
    private Button ButtonF2;
    private Button ButtonExplore;
    private Button ButtonShortestPath;

    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    float [] history = new float[2];
    boolean toggleSet;
    private ToggleButton toggle;

    private boolean btAutoConnect = false;

    private SharedPreferences spf;

    private static int msgCounter = 0;

    Maze maze;

    // Whether the Log Fragment is currently shown
    //private boolean mLogShown;

    //private Map canvas;

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

        /*autoModeThread = new AutoThread();
        autoModeThread.start();
        /*mapPanel = (LinearLayout) findViewById(R.id.mapPanel);
        mapSurface = new MapSurface(MainActivity.this);
        mapPanel.addView(mapSurface);*/
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D)
            Log.e(TAG, "+ ON RESUME +");
        if (mChatService != null) {
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                mChatService.start();
            }
        }

        //mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //if(sensorRegistered)
            //sensorMgr.registerListener(MainActivity.this, sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mChatService = new BluetoothChatService(this, mHandler);
        spf = getSharedPreferences(PREF_DB, Context.MODE_PRIVATE);

        this.maze = new Maze(this);
        ((RelativeLayout) findViewById(R.id.surface)).addView(this.maze);

        ImageButtonUp = (ImageButton) findViewById(R.id.imageButtonUp);
        ImageButtonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_UP, SET_UP_DEFAULT), true);
            }
        });

        ImageButtonDown = (ImageButton) findViewById(R.id.imageButtonDown);
        ImageButtonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_DOWN, SET_DOWN_DEFAULT), true);
            }
        });
        ImageButtonLeft = (ImageButton) findViewById(R.id.imageButtonLeft);
        ImageButtonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(spf.getString(SET_LEFT, SET_LEFT_DEFAULT), true);
            }
        });
        ImageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);
        ImageButtonRight.setOnClickListener(new View.OnClickListener() {
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

        toggle = (ToggleButton) findViewById(R.id.toggleButtonTilt);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    toggleSet = true;

                } else {
                    // The toggle is disabled
                    toggleSet = false;
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
    }

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
            //if(ack)
            //    msgCounter ++;
            //message = msgCounter + "," + message;
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
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    // String ack ="-,";
                    // MainActivity.this.sendMessage(ack, true);
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //updateMap(readMessage, true);
                    if(D)
                        //Toast.makeText(MainActivity.this, "Receive from BT: " + readMessage, Toast.LENGTH_SHORT).show();
                        RobotStatus.setText(readMessage);

                    /*if(readMessage.length() >= 4) {
                        if ((readMessage.substring(0, 3)).equals("[V]")) {
                            viewRx.append(readMessage.substring(3) + "\n");
                            final int scrollAmount = viewRx.getLayout().getLineTop(viewRx.getLineCount()) - viewRx.getHeight(); // if there is no need to scroll, scrollAmount will be <=0
                            if (scrollAmount > 0) viewRx.scrollTo(0, scrollAmount); else viewRx.scrollTo(0, 0);
                        }
                    }*/
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
        /*if(btStatus != null && status)
            btStatus.setIcon(R.drawable.bt_on);
        else if(btStatus != null)
            btStatus.setIcon(R.drawable.bt_off);*/
    }

    private final void setStatus(CharSequence subTitle, boolean status) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
        /*if(btStatus != null && status)
            btStatus.setIcon(R.drawable.bt_on);
        else if(btStatus != null)
            btStatus.setIcon(R.drawable.bt_off);*/
    }

    public void updateMap(String mapInfo, boolean init){
        maze.robotChange(mapInfo);
        if(!init) {
            sendMessage("Robot_init(" + mapInfo.substring(5, 18)+")", true);
            /*initRobotMenu.setIcon(R.drawable.location_off);
            initRobotMenu.setEnabled(false);*/
        }
        Log.d(TAG, "Map changed.");
    }

    public void SendStartPos(int x,int y,String head) {
        sendMessage("robot_init(x=" + x + ",y=" + y + ",head=" + head + ")", true);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        SharedPreferences.Editor editor;
        /*while (btAutoConnect == true){
            connectDevice(data, true);
            btAutoConnect = false;
            return;
        }*/
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                    btAutoConnect = true;
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
            case REQUEST_SAVE_BUTTON /*10*/:
                break;
            case REQUEST_COORD_BUTTON /*100*/:
                break;
            default:
                //onActivityResult_VoiceRecognition(requestCode, requestCode, data);

        }

    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device, secure);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //this.maze = new Maze(this);
        //((RelativeLayout) findViewById(R.id.surface)).addView(this.maze);
        /*RelativeLayout surface = (RelativeLayout) findViewById(R.id.surface);
        surface.addView(this.maze);*/

        RobotStatus = (EditText)findViewById(R.id.robotStatus);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);



        //setContentView(new Maze(this));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float xChange = history[0] - event.values[0];
        float yChange = history[1] - event.values[1];

        history[0] = event.values[0];
        history[1] = event.values[1];

            if (xChange > 2 && toggleSet){
                //right
                sendMessage(spf.getString(SET_RIGHT, SET_RIGHT_DEFAULT), true);
            }

            else if (xChange < -2 && toggleSet){
                //right
                sendMessage(spf.getString(SET_LEFT, SET_LEFT_DEFAULT), true);
            }

            if (yChange > 2 && toggleSet){
                //up
                sendMessage(spf.getString(SET_UP, SET_UP_DEFAULT), true);
            }

            else if (yChange < -2 && toggleSet){
                //down
                sendMessage(spf.getString(SET_DOWN, SET_DOWN_DEFAULT), true);
            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

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
            case R.id.action_settings:
                return true;

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

