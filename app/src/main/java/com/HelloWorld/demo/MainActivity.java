package com.HelloWorld.demo;

import static com.activelook.activelooksdk.types.ImgSaveFormat.MONO_4BPP;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.activelook.activelooksdk.Glasses;
import com.activelook.activelooksdk.types.DeviceInformation;
import com.activelook.activelooksdk.types.ImgStreamFormat;
import com.activelook.activelooksdk.types.Rotation;
import com.activelook.activelooksdk.types.holdFlushAction;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public static Glasses connectedGlasses;
    private TextView glassesidTextView, fwTextView, batteryTextView, serialnumberTextView, largeText, clockText;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sensorSwitch;
    private SeekBar luminanceSeekBar;
    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");

    boolean textMode = false, demoMode = false, gestMode = false, imageMode = false, screenMode = false;
    boolean imageSaved = false, bitmapMode = false, graphicMode = false;
    int txtCount = 0, txtPos = 0, imageCount = 0, animCount = 0, bitmapCount = 1, counter = 1, BattLevel = 0;
    Bitmap img1 = null, img2 = null, img3 = null, img4 = null, img5= null, img6= null, img7= null;
    Handler golfHandler = new Handler();
    Runnable golfRunnable;
    Handler bitmapHandler = new Handler();
    Runnable bitmapRunnable;
    Handler textHandler = new Handler();
    Runnable textRunnable;
    Handler screenHandler = new Handler();
    Runnable screenRunnable;

    // GPSTracker class
    GPSTracker gps;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /*
         * Check location permission (needed for BLE scan)
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN}, 0);
        }

        if (savedInstanceState != null && ((DemoApp) this.getApplication()).isConnected()) {
            connectedGlasses = savedInstanceState.getParcelable("connectedGlasses");
            Objects.requireNonNull(connectedGlasses).setOnDisconnected(glasses -> {
                glasses.disconnect();
                MainActivity.this.disconnect();
            });
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = this.findViewById(R.id.toolbar);

        glassesidTextView = this.findViewById(R.id.glasses_id);
        fwTextView = this.findViewById(R.id.fw);
        batteryTextView = this.findViewById(R.id.battery);
        serialnumberTextView = this.findViewById(R.id.serialnumberTextView);
        largeText = this.findViewById(R.id.largeText);
        luminanceSeekBar = this.findViewById(R.id.luminanceSeekBar);
        sensorSwitch = this.findViewById(R.id.sensorSwitch);
        clockText = this.findViewById(R.id.clock);

        try { img1 = BitmapFactory.decodeStream(getAssets().open("lion_304x256.png"));
            img2 = BitmapFactory.decodeStream(getAssets().open("zebre_304x248.png"));
            img3 = BitmapFactory.decodeStream(getAssets().open("tigre_304x256.png"));
            img4 = BitmapFactory.decodeStream(getAssets().open("chess_304x171.png"));
            img5 = BitmapFactory.decodeStream(getAssets().open("castle_304x256.jpg"));
            img6 = BitmapFactory.decodeStream(getAssets().open("newyork_304x213.png"));
            img7 = BitmapFactory.decodeStream(getAssets().open("engo_304x170.png"));
        }
        catch (IOException e) {throw new RuntimeException(e);}

        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        this.updateVisibility();
        this.bindActions();
    }

    @SuppressLint("DefaultLocale")
    private void displayClock(){
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String clock = sdf.format(new Date());
        final Glasses g = connectedGlasses;
        if (g != null) {
            g.battery(r1 -> { BattLevel=r1; connectedGlasses.color((byte) 15);
                connectedGlasses.cfgSet("ALooK");
                if (r1 < 25) {connectedGlasses.imgDisplay((byte) 1, (short) (272), (short) (255-26));}
                else {connectedGlasses.imgDisplay((byte) 0, (short) (272), (short) (255-26));}
                connectedGlasses.txt(new Point((263), 255), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                        String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                connectedGlasses.txt(new Point(100, 255), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
            });//Glasses Battery
        }
    }

    private void updateVisibility() {
        final Glasses g = connectedGlasses;
        if (g == null) {
            this.findViewById(R.id.connected_content).setVisibility(View.GONE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.connected_content).setVisibility(View.VISIBLE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.GONE);
            g.clear();
            g.txt(new Point(250, 160), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Hello World !");
            g.txt(new Point(235, 100), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "please, wait for");
            g.txt(new Point(245,  65), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "the config upload");
            g.cfgWrite("cfgLaurent", 1, 123456);
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts2.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts3.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts4.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts5.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts6.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("fonts8.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            try {g.loadConfiguration(new BufferedReader(new InputStreamReader(getAssets().open("golf.txt"))));}
            catch (IOException e) {e.printStackTrace();}
            g.color((byte) 0);
            g.rectf((short) 0,(short) 10,(short) 300,(short) 105);
            g.color((byte) 15);
            g.txt(new Point(240, 65), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, "you can start !");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void bindActions() {
        //        this.toast("Binding actions");
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "Your BLUETOOTH is not open !!!/n>>>relaunch the application", Toast.LENGTH_LONG).show();
        }
        this.findViewById(R.id.scan).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            MainActivity.this.startActivityForResult(intent, Activity.RESULT_FIRST_USER);
        });
        this.findViewById(R.id.text_command).setOnClickListener(view -> MainActivity.this.textButton());
        this.findViewById(R.id.demo_command).setOnClickListener(view -> MainActivity.this.demoButton());
        this.findViewById(R.id.gps_command).setOnClickListener(view -> MainActivity.this.gpsButton());
        this.findViewById(R.id.gest_command).setOnClickListener(view -> MainActivity.this.gestureButton());
        this.findViewById(R.id.graphic_command).setOnClickListener(view -> MainActivity.this.graphicButton());
        this.findViewById(R.id.screen_command).setOnClickListener(view -> MainActivity.this.screenButton());
        this.findViewById(R.id.image_command).setOnClickListener(view -> {
            try {MainActivity.this.imageButton();} catch (IOException e) {throw new RuntimeException(e);}});
        this.findViewById(R.id.image2_command).setOnClickListener(view -> {
            try {MainActivity.this.image2Button();} catch (IOException e) {throw new RuntimeException(e);}
        });
        this.findViewById(R.id.bitmaps_command).setOnClickListener(view -> MainActivity.this.bitmapsButton());
        this.findViewById(R.id.anim_command).setOnClickListener(view -> MainActivity.this.animButton());

        this.findViewById(R.id.button_disconnect).setOnClickListener(view -> {
            MainActivity.this.sensorSwitch(true);
            connectedGlasses.sensor(true);
            connectedGlasses.disconnect();
            connectedGlasses = null;
            MainActivity.this.updateVisibility();
            this.snack("Disconnected");
        });
        sensorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> MainActivity.this.sensorSwitch(isChecked));

        luminanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                MainActivity.this.lumaButton(progressChangedValue);
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    //////////     HELLO  button

//    private void helloButton(){
//        this.findViewById(R.id.hello_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
//        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
//        textMode = false; demoMode = false; gestMode = false; imageMode = false; bitmapMode = false; graphicMode = false;
//
//        final Glasses g = this.connectedGlasses;
//        g.clear();
//        g.txt(new Point(250, 120), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "my Hello World !");
//    }

    //////////     TEXT  button
    @SuppressLint("SetTextI18n")
    private void textButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = true ; demoMode = false; gestMode = false; screenMode = false; imageMode = false; bitmapMode = false; graphicMode = false;

        Button buttonText = this.findViewById(R.id.text_command);
        final Glasses glasses = connectedGlasses;
        glasses.clear();
        glasses.cfgWrite("cfgLaurent",1,123456);
        glasses.cfgSet("cfgLaurent");
        if(textHandler != null)
            textHandler.removeCallbacks(textRunnable); // On arrete le callback

        if (txtCount == 0) {
            buttonText.setText("TEXTascii");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font1 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "32:    !  \" # $ % & ' (");
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "41: )  *  +  ,  -  .  /  0 1 2");
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 1,  (byte) 0x0F,
                    "51: 3 4 5 6 7 8 9  :  ;  <");
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "61: =  >  ? @ A B C D E F");
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "81: Q R S T U V W X Y Z");
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "91: [  \\  ]  ^  _  `  a b c d");
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "101: e f g h i j k l m n");
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "111: o p q r s t u v w x");
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                    "121: y  z  {  |  } ~");
        }

        if (txtCount == 1) {
            buttonText.setText("TXTaccent");
            glasses.cfgSet("cfgLaurent2");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font4 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 2 +" "+ (char) 3 +" "+ (char) 4 +" "+ (char) 5 +" "+ (char) 6 +" "+ (char) 7 +" "+ (char) 8 +" "+ (char) 9 +" "+ (char) 10 +" "+ (char) 11 +" "+ (char) 12 +" "+ (char) 13 +" "+ (char) 14);
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 15 +" "+ (char) 16 +" "+ (char) 17 +" "+ (char) 18 +" "+ (char) 19 +" "+ (char) 20 +" "+ (char) 21 +" "+ (char) 22 +" "+ (char) 23 +" "+ (char) 24 +" "+ (char) 25 +" "+ (char) 26 +" "+ (char) 27 +" "+ (char) 28);
            glasses.txt(new Point(300, 170), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 29 +" "+ (char) 30 +" "+ (char) 31 +" "+ (char) 32 +" "+ (char) 33 +" "+ (char) 34 +" "+ (char) 35 +" "+ (char) 36 +" "+ (char) 37 +" "+ (char) 38 +" "+ (char) 39 +" "+ (char) 40 +" "+ (char) 41 +" "+ (char) 42);
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 43 +" "+ (char) 44 +" "+ (char) 45 +" "+ (char) 46 +" "+ (char) 47 +" "+ (char) 48 +" "+ (char) 49 +" "+ (char) 50 +" "+ (char) 51 +" "+ (char) 52 +" "+ (char) 53 +" "+ (char) 54 +" "+ (char) 55 +" "+ (char) 56);
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 57 +" "+ (char) 58 +" "+ (char) 59 +" "+ (char) 60 +" "+ (char) 61 +" "+ (char) 62 +" "+ (char) 63 +" "+ (char) 64 +" "+ (char) 65 +" "+ (char) 66 +" "+ (char) 67 +" "+ (char) 68 +" "+ (char) 69 +" "+ (char) 70);
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 71 +" "+ (char) 72 +" "+ (char) 73 +" "+ (char) 74 +" "+ (char) 75 +" "+ (char) 76 +" "+ (char) 77 +" "+ (char) 78 +" "+ (char) 79 +" "+ (char) 80 +" "+ (char) 81 +" "+ (char) 82 +" "+ (char) 83 +" "+ (char) 84);
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 85 +" "+ (char) 86 +" "+ (char) 87 +" "+ (char) 88 +" "+ (char) 89 +" "+ (char) 90 +" "+ (char) 91 +" "+ (char) 92 +" "+ (char) 93 +" "+ (char) 94 +" "+ (char) 95 +" "+ (char) 96 +" "+ (char) 97 +" "+ (char) 98);
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 99 +" "+ (char) 100 +" "+ (char) 101 +" "+ (char) 102 +" "+ (char) 103 +" "+ (char) 104 +" "+ (char) 105 +" "+ (char) 106 +" "+ (char) 107 +" "+ (char) 108 +" "+ (char) 109 +" "+ (char) 110 +" "+ (char) 111 +" "+ (char) 112);
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                    (char) 113 +" "+ (char) 114 +" "+ (char) 115 +" "+ (char) 116 +" "+ (char) 117 +" "+ (char) 118 +" "+ (char) 119 +" "+ (char) 120 +" "+ (char) 121 +" "+ (char) 122 +" "+ (char) 123 +" "+ (char) 124 +" "+ (char) 125 +" "+ (char) 126);
        }

        if (txtCount == 2) {
            buttonText.setText("TXTgrec");
            glasses.cfgSet("cfgLaurent3");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font6 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "32:    !  \" # $ % & ' (");
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "41: )  *  +  ,  -  .  /  0 1 2");
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 6,  (byte) 0x0F,
                    "51: 3 4 5 6 7 8 9  :  ;  <");
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "61: =  >  ? @ A B C D E F");
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "81: Q R S T U V W X Y Z");
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "91: [  \\  ]  ^  _  `  a b c d");
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "101: e f g h i j k l m n");
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "111: o p q r s t u v w x");
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 6, (byte) 0x0F,
                    "121: y  z  {  |  } ~");
        }

        if (txtCount == 13) {
            buttonText.setText("TXTrusse");
            glasses.cfgSet("cfgLaurent4");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font7 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "32:    !  \" # $ % & ' (");
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "41: )  *  +  ,  -  .  /  0 1 2");
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 7,  (byte) 0x0F,
                    "51: 3 4 5 6 7 8 9  :  ;  <");
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "61: =  >  ? @ A B C D E F");
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "81: Q R S T U V W X Y Z");
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "91: [  \\  ]  ^  _  `  a b c d");
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "101: e f g h i j k l m n");
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "111: o p q r s t u v w x");
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    "121: y  z  {  |  } ~");
        }

        if (txtCount == 3) {
            buttonText.setText("TXTrusse");
            glasses.cfgSet("cfgLaurent4");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font7 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 1 +""+ (char) 2 +""+ (char) 3 +""+ (char) 4 +""+ (char) 5 +""+ (char) 6 +""+ (char) 7 +""+ (char) 8 +""+ (char) 9 +""+ (char) 10 +""+ (char) 11 +""+ (char) 12 +""+ (char) 13 +""+ (char) 14);
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 15 +""+ (char) 16 +""+ (char) 17 +""+ (char) 18 +""+ (char) 19 +""+ (char) 20 +""+ (char) 21 +""+ (char) 22 +""+ (char) 23 +""+ (char) 24 +""+ (char) 25 +""+ (char) 26 +""+ (char) 27 +""+ (char) 28);
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 7,  (byte) 0x0F,
                    (char) 29 +""+ (char) 30 +""+ (char) 31 +""+ (char) 32 +""+ (char) 33 +""+ (char) 34 +""+ (char) 35 +""+ (char) 36 +""+ (char) 37 +""+ (char) 38 +""+ (char) 39 +""+ (char) 40 +""+ (char) 41 +""+ (char) 42);
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 43 +""+ (char) 44 +""+ (char) 45 +""+ (char) 46 +""+ (char) 47 +""+ (char) 48 +""+ (char) 49 +""+ (char) 50 +""+ (char) 51 +""+ (char) 52 +""+ (char) 53 +""+ (char) 54 +""+ (char) 55 +""+ (char) 56);
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 57 +""+ (char) 58 +""+ (char) 59 +""+ (char) 60 +""+ (char) 61 +""+ (char) 62 +""+ (char) 63 +""+ (char) 64 +""+ (char) 65 +""+ (char) 66 +""+ (char) 67 +""+ (char) 68 +""+ (char) 69 +""+ (char) 70);
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 71 +""+ (char) 72 +""+ (char) 73 +""+ (char) 74 +""+ (char) 75 +""+ (char) 76 +""+ (char) 77 +""+ (char) 78 +""+ (char) 79 +""+ (char) 80 +""+ (char) 81 +""+ (char) 82 +""+ (char) 83 +""+ (char) 84);
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 85 +""+ (char) 86 +""+ (char) 87 +""+ (char) 88 +""+ (char) 89 +""+ (char) 90 +""+ (char) 91 +""+ (char) 92 +""+ (char) 93 +""+ (char) 94 +""+ (char) 95 +""+ (char) 96 +""+ (char) 97 +""+ (char) 98);
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 99 +""+ (char) 100 +""+ (char) 101 +""+ (char) 102 +""+ (char) 103 +""+ (char) 104 +""+ (char) 105 +""+ (char) 106 +""+ (char) 107 +""+ (char) 108 +""+ (char) 109 +""+ (char) 110 +""+ (char) 111 +""+ (char) 112);
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 7, (byte) 0x0F,
                    (char) 113 +""+ (char) 114 +""+ (char) 115 +""+ (char) 116 +""+ (char) 117 +""+ (char) 118 +""+ (char) 119 +""+ (char) 120 +""+ (char) 121 +""+ (char) 122 +""+ (char) 123 +""+ (char) 124 +""+ (char) 125 +""+ (char) 126);
        }

        if (txtCount == 4) {
            buttonText.setText("TXTcoréen");
            glasses.cfgSet("cfgLaurent6");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font9 :");
            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " !  \" # $ % & ' (");
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " )  *  +  ,  -  .  /  0 1 2");
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 9,  (byte) 0x0F,
                    " 3 4 5 6 7 8 9  :  ;  <");
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " =  >  ? @ A B C D E F");
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " Q R S T U V W X Y Z");
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " [  \\  ]  ^  _  `  a b c d");
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " e f g h i j k l m n");
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " o p q r s t u v w x");
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 9, (byte) 0x0F,
                    " y  z  {  |  } ~");
        }

        if (txtCount == 5) {
            buttonText.setText("TXTchinois");
            glasses.cfgSet("cfgLaurent5");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font8 :");

//            glasses.txt(new Point(300, 223), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 33 +""+ (char) 34 +""+ (char) 35 +""+ (char) 36 +""+ (char) 37 +""+ (char) 38 +""+ (char) 39 +""+ (char) 40 +""+ (char) 41 +""+ (char) 42 +""+ (char) 43 +""+ (char) 44 +""+ (char) 45);
//            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 45 +""+ (char) 46 +""+ (char) 47 +""+ (char) 48 +""+ (char) 49 +""+ (char) 50 +""+ (char) 51 +""+ (char) 52 +""+ (char) 53 +""+ (char) 54 +""+ (char) 55 +""+ (char) 56 +""+ (char) 57);
//            glasses.txt(new Point(300, 167), Rotation.TOP_LR,  (byte) 8,  (byte) 0x0F,
//                    (char) 57 +""+ (char) 58 +""+ (char) 59 +""+ (char) 60 +""+ (char) 61 +""+ (char) 62 +""+ (char) 63 +""+ (char) 64 +""+ (char) 65 +""+ (char) 66 +""+ (char) 67 +""+ (char) 68 +""+ (char) 69);
//            glasses.txt(new Point(300, 139), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 69 +""+ (char) 70 +""+ (char) 71 +""+ (char) 72 +""+ (char) 73 +""+ (char) 74 +""+ (char) 75 +""+ (char) 76 +""+ (char) 77 +""+ (char) 78 +""+ (char) 79 +""+ (char) 80 +""+ (char) 81);
//            glasses.txt(new Point(300, 111), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 81 +""+ (char) 82 +""+ (char) 83 +""+ (char) 84 +""+ (char) 85 +""+ (char) 86 +""+ (char) 87 +""+ (char) 88 +""+ (char) 89 +""+ (char) 90 +""+ (char) 91 +""+ (char) 92 +""+ (char) 93);
//            glasses.txt(new Point(300, 83), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 93 +""+ (char) 94 +""+ (char) 95 +""+ (char) 96 +""+ (char) 97 +""+ (char) 98 +""+ (char) 99 +""+ (char) 100 +""+ (char) 101 +""+ (char) 102 +""+ (char) 103 +""+ (char) 104 +""+ (char) 105);
//            glasses.txt(new Point(300, 55), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 105 +""+ (char) 106 +""+ (char) 107 +""+ (char) 108 +""+ (char) 109 +""+ (char) 110 +""+ (char) 111 +""+ (char) 112 +""+ (char) 113 +""+ (char) 114 +""+ (char) 115 +""+ (char) 116 +""+ (char) 117);
//            glasses.txt(new Point(300, 27), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
//                    (char) 117 +""+ (char) 118 +""+ (char) 119 +""+ (char) 120 +""+ (char) 121 +""+ (char) 122 +""+ (char) 123 +""+ (char) 124 +""+ (char) 125 +""+ (char) 126);

            glasses.txt(new Point(300, 220), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 1 +""+ (char) 2 +""+ (char) 3 +""+ (char) 4 +""+ (char) 5 +""+ (char) 6 +""+ (char) 7 +""+ (char) 8 +""+ (char) 9 +""+ (char) 10 +""+ (char) 11 +""+ (char) 12 +""+ (char) 13 +""+ (char) 14);
            glasses.txt(new Point(300, 195), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 15 +""+ (char) 16 +""+ (char) 17 +""+ (char) 18 +""+ (char) 19 +""+ (char) 20 +""+ (char) 21 +""+ (char) 22 +""+ (char) 23 +""+ (char) 24 +""+ (char) 25 +""+ (char) 26 +""+ (char) 27 +""+ (char) 28);
            glasses.txt(new Point(300, 170), Rotation.TOP_LR,  (byte) 8,  (byte) 0x0F,
                    (char) 29 +""+ (char) 30 +""+ (char) 31 +""+ (char) 32 +""+ (char) 33 +""+ (char) 34 +""+ (char) 35 +""+ (char) 36 +""+ (char) 37 +""+ (char) 38 +""+ (char) 39 +""+ (char) 40 +""+ (char) 41 +""+ (char) 42);
            glasses.txt(new Point(300, 145), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 43 +""+ (char) 44 +""+ (char) 45 +""+ (char) 46 +""+ (char) 47 +""+ (char) 48 +""+ (char) 49 +""+ (char) 50 +""+ (char) 51 +""+ (char) 52 +""+ (char) 53 +""+ (char) 54 +""+ (char) 55 +""+ (char) 56);
            glasses.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 57 +""+ (char) 58 +""+ (char) 59 +""+ (char) 60 +""+ (char) 61 +""+ (char) 62 +""+ (char) 63 +""+ (char) 64 +""+ (char) 65 +""+ (char) 66 +""+ (char) 67 +""+ (char) 68 +""+ (char) 69 +""+ (char) 70);
            glasses.txt(new Point(300, 95), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 71 +""+ (char) 72 +""+ (char) 73 +""+ (char) 74 +""+ (char) 75 +""+ (char) 76 +""+ (char) 77 +""+ (char) 78 +""+ (char) 79 +""+ (char) 80 +""+ (char) 81 +""+ (char) 82 +""+ (char) 83 +""+ (char) 84);
            glasses.txt(new Point(300, 70), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 85 +""+ (char) 86 +""+ (char) 87 +""+ (char) 88 +""+ (char) 89 +""+ (char) 90 +""+ (char) 91 +""+ (char) 92 +""+ (char) 93 +""+ (char) 94 +""+ (char) 95 +""+ (char) 96 +""+ (char) 97 +""+ (char) 98);
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 99 +""+ (char) 100 +""+ (char) 101 +""+ (char) 102 +""+ (char) 103 +""+ (char) 104 +""+ (char) 105 +""+ (char) 106 +""+ (char) 107 +""+ (char) 108 +""+ (char) 109 +""+ (char) 110 +""+ (char) 111 +""+ (char) 112);
            glasses.txt(new Point(300, 20), Rotation.TOP_LR, (byte) 8, (byte) 0x0F,
                    (char) 113 +""+ (char) 114 +""+ (char) 115 +""+ (char) 116 +""+ (char) 117 +""+ (char) 118 +""+ (char) 119 +""+ (char) 120 +""+ (char) 121 +""+ (char) 122 +""+ (char) 123 +""+ (char) 124 +""+ (char) 125 +""+ (char) 126);
        }

        textRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if (textMode) {
                    int txtShft2 = 298;
                    if ((txtCount == 6 || txtCount == 7 ) && txtPos+txtShft2 < 513) {
                        buttonText.setText("TEXT" + String.format("%d", txtCount));
                        glasses.holdFlush(holdFlushAction.HOLD);
                        glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "Table ASCII font2 :");
                        glasses.txt(new Point(txtShft2+txtPos, 200), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "! \" # $ % & ' ( ) * + , - . / @ ");
                        glasses.txt(new Point(txtShft2+txtPos, 160), Rotation.TOP_LR,  (byte) 2,  (byte) 0x0F,
                                "0 1 2 3 4 5 6 7 8 9 : ; < = > ? ");
                        glasses.txt(new Point(txtShft2+txtPos, 120), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "A B C D E F G H I J .. Q R S T U V W X Y Z ");
                        glasses.txt(new Point(txtShft2+txtPos, 80), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "[  \\  ]  ^  _  `  a b c d e f g h i j k ");
                        glasses.txt(new Point(txtShft2+txtPos, 40), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "l m n o p q r s t u v w x y z { | } ~ ");
                        glasses.holdFlush(holdFlushAction.FLUSH);}
                    int txtShft3 = 290;
                    if (txtCount == 8 && txtPos+txtShft3 < 513) {
                        buttonText.setText("TEXT" + String.format("%d", txtCount));
                        glasses.holdFlush(holdFlushAction.HOLD);
                        glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                                "Table ASCII font3 :");
                        glasses.txt(new Point(txtShft3+txtPos, 200), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,
                                "! \" # $ % & ' ( ) * + , - . / @ ");
                        glasses.txt(new Point(txtShft3+txtPos, 150), Rotation.TOP_LR,  (byte) 3,  (byte) 0x0F,
                                "0 1 2 3 4 5 6 7 8 9 : ; < = > ? ");
                        glasses.txt(new Point(txtShft3+txtPos, 100), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,
                                "A B C .. X Y Z [ \\ ] ^ _ ` a b c ");
                        glasses.txt(new Point(txtShft3+txtPos, 50), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,
                                "o p q r s t u v w x y z { | } ~ ");
                        glasses.holdFlush(holdFlushAction.FLUSH);}
                    textHandler.postDelayed(this,300);
                    txtPos=txtPos+4; if (txtPos+Math.min(txtShft2,txtShft3)>=512) {txtPos=0;glasses.clear();}
                }
            }
        };

        if (txtCount == 6) {glasses.clear();
            buttonText.setText("TEXTempty");
        }
        if (txtCount == 6 || txtCount == 7 || txtCount == 8) {
            textHandler.postDelayed(textRunnable,300);} // on redemande toutes les 300ms

        if (txtCount == 9) {
            buttonText.setText("TXTorient");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 2, (byte) 0x0F,
                    "Table ASCII font1 :");
            glasses.txt(new Point(300, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,"TOP_LR");
            glasses.txt(new Point(300, 150), Rotation.BOTTOM_LR, (byte) 1, (byte) 0x0F,"BOTTOM_LR");
            glasses.txt(new Point(3, 100), Rotation.TOP_RL, (byte) 1, (byte) 0x0F,"TOP_RL");
            glasses.txt(new Point(3,  50), Rotation.BOTTOM_RL, (byte) 1, (byte) 0x0F,"BOTTOM_RL");
            glasses.txt(new Point(225, 3), Rotation.LEFT_BT, (byte) 1, (byte) 0x0F,"LEFT_BT");
            glasses.txt(new Point(175, 3), Rotation.RIGHT_BT, (byte) 1, (byte) 0x0F,"RIGHT_BT");
            glasses.txt(new Point( 125, 220), Rotation.LEFT_TB, (byte) 1, (byte) 0x0F,"LEFT_TB");
            glasses.txt(new Point( 75,  220), Rotation.RIGHT_TB, (byte) 1, (byte) 0x0F,"RIGHT_TB");
        }

        if (txtCount == 10) {
            buttonText.setText("TEXTsized1");
            glasses.cfgSet("cfgLaurent8");
            glasses.txt(new Point(300, 254), Rotation.TOP_LR, (byte) 12, (byte) 0x0F,
                    "12pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 240), Rotation.TOP_LR, (byte) 13, (byte) 0x0F,
                    "13pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 225), Rotation.TOP_LR, (byte) 14, (byte) 0x0F,
                    "14pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 209), Rotation.TOP_LR,  (byte) 15,  (byte) 0x0F,
                    "15pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 192), Rotation.TOP_LR, (byte) 16, (byte) 0x0F,
                    "16pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 174), Rotation.TOP_LR, (byte) 17, (byte) 0x0F,
                    "17pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 155), Rotation.TOP_LR, (byte) 18, (byte) 0x0F,
                    "18pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 135), Rotation.TOP_LR, (byte) 19, (byte) 0x0F,
                    "19pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 114), Rotation.TOP_LR, (byte) 20, (byte) 0x0F,
                    "20pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 92), Rotation.TOP_LR, (byte) 21, (byte) 0x0F,
                    "21pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 69), Rotation.TOP_LR, (byte) 22, (byte) 0x0F,
                    "22pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 45), Rotation.TOP_LR, (byte) 23, (byte) 0x0F,
                    "23pix: B,abcdefghijklmnopq");
        }

        if (txtCount == 11) {
            buttonText.setText("TEXTsized2");
            glasses.cfgSet("cfgLaurent8");
            glasses.txt(new Point(300, 236), Rotation.TOP_LR, (byte) 23, (byte) 0x0F,
                    "23pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 211), Rotation.TOP_LR, (byte) 24, (byte) 0x0F,
                    "24pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 185), Rotation.TOP_LR, (byte) 25, (byte) 0x0F,
                    "25pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 158), Rotation.TOP_LR, (byte) 26, (byte) 0x0F,
                    "26pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 130), Rotation.TOP_LR,  (byte) 27,  (byte) 0x0F,
                    "27pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 101), Rotation.TOP_LR, (byte) 28, (byte) 0x0F,
                    "28pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 71), Rotation.TOP_LR, (byte) 29, (byte) 0x0F,
                    "29pix: B,abcdefghijklmnopq");
            glasses.txt(new Point(300, 40), Rotation.TOP_LR, (byte) 30, (byte) 0x0F,
                    "30pix: B,abcdefghijklmnopq");
        }

        txtCount ++; if (txtCount ==12) {txtCount = 0;}
    }

    //////////     DEMO  button
    @SuppressLint("DefaultLocale")
    private void demoButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = true; gestMode = false; screenMode = false; imageMode = false; bitmapMode = false; graphicMode = false;

        final Glasses glasses = connectedGlasses;
        glasses.clear();
        if( BattLevel != 0) {Toast.makeText(getApplicationContext(),"Your glasses battery level is "+String.format("%d", BattLevel)+"%", Toast.LENGTH_LONG).show();}

        Handler clockHandler = new Handler();
        Runnable clockRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (demoMode) {
                    displayClock();
                    String currentTime = sdf2.format(new Date());
                    glasses.layoutDisplayExtended((byte) 0x0B, (byte) 30, (byte) 145, currentTime);//Chrono
                    clockText.setText(sdf2.format(new Date()));
                    if (BattLevel !=0 ) {batteryTextView.setText(String.format("%d",BattLevel)+"%");}
                    clockHandler.postDelayed(this,1000); }
                }
        };
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable,1000); // on redemande toutes les 1000ms

        glasses.layoutDisplayExtended((byte) 0x0B, (byte) 30, (byte) 145, sdf2.format(new Date()));//Chrono
        glasses.layoutDisplayExtended((byte) 0x0D, (byte) 30, (byte) 85, "   25");//Speed
        glasses.layoutDisplayExtended((byte) 0x0C, (byte) 30, (byte) 25, "   50");//Distance
    }

    //////////     GPS  button
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void gpsButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = false; bitmapMode = false; graphicMode = false;

        final Glasses g = connectedGlasses;
		g.clear();
        displayClock();
        if (BattLevel !=0 ) {batteryTextView.setText(String.format("%d",BattLevel)+"%");}

        gps = new GPSTracker(this, false);
        // Check if GPS enabled
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            double altitude = gps.getAltitude();
            double altitudeAccuracy = gps.getVerticalAccuracyMeters();
            double speed = gps.getSpeed();
            double speedAccuracy = gps.getSpeedAccuracyMetersPerSecond();
            double bearing = gps.getBearing();
            double bearingAccuracy = gps.getBearingAccuracyDegrees();

            // \n is for new line
            Toast.makeText(getApplicationContext(),
                    "Your gps Location is - \nLat: " + latitude + "\nLong: " + longitude + "\nAlt: " + altitude +"m", Toast.LENGTH_LONG).show();
            g.cfgSet("cfgLaurent");
            // g.txt(new Point(288, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0A, "static GPS data");
            g.txt(new Point(300, 200), Rotation.TOP_LR, (byte) 2, (byte) 0x0A,
                    "Lat: " + String.format("%3.4f", latitude));
            g.txt(new Point(300, 160), Rotation.TOP_LR, (byte) 2, (byte) 0x0A,
                    "Long: " + String.format("%3.4f", longitude));
            g.txt(new Point(300, 120), Rotation.TOP_LR, (byte) 2, (byte) 0x0A,
                    "Alt: " + String.format("%.1f", altitude)+" m");
            g.txt(new Point(100, 117), Rotation.TOP_LR, (byte) 1, (byte) 0x0A,
                    "+/-" + String.format("%.1f", altitudeAccuracy));
            g.txt(new Point(300, 80), Rotation.TOP_LR, (byte) 2, (byte) 0x0A,
                    "Spd: " + String.format("%.2f", speed)+" m/s");
            g.txt(new Point(90, 77), Rotation.TOP_LR, (byte) 1, (byte) 0x0A,
                    "+/-" + String.format("%.1f", speedAccuracy));
            g.txt(new Point(300, 40), Rotation.TOP_LR, (byte) 2, (byte) 0x0A,
                    "Crs: " + String.format("%.2f", bearing)+" deg");
            g.txt(new Point(90, 37), Rotation.TOP_LR, (byte) 1, (byte) 0x0A,
                    "+/-" + String.format("%.1f", bearingAccuracy));
        } else {
            // Can't get location.  GPS or network is not enabled.
            gps.showSettingsAlert();
            g.txt(new Point(250, 200), Rotation.TOP_LR, (byte) 1, (byte) 0x0A, "GPS data");
            g.txt(new Point(250, 150), Rotation.TOP_LR, (byte) 1, (byte) 0x0A, "unknown");
        }
    }

    //////////     GESTURE  button
    @SuppressLint("DefaultLocale")
    private void gestureButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = true; screenMode = false; imageMode = false; bitmapMode = false; graphicMode = false;

        Toast.makeText(getApplicationContext(),
                "Put your hand on your left eye\nseveral times ...", Toast.LENGTH_LONG).show();

        final Glasses g = connectedGlasses;
        final int[] gest = {0}, gestureCount = {0}, doublegestureCount = {0}, gestTimer = {0};

        Handler gestHandler = new Handler();
        Runnable gestRunnable = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (gestMode) {
                    displayClock();
                    if (BattLevel !=0 ) {batteryTextView.setText(String.format("%d",BattLevel)+"%");}
                    if (gest[0]==1) {gestTimer[0]++;}
                    if (gestTimer[0]==7 && gest[0]==1) { gestureCount[0]++; gestTimer[0]=0; gest[0]=0;
                            g.txt(new Point(220, 170), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,
                                String.format("%d", gestureCount[0]));} // simple gesture
                    if (gest[0]==2) { doublegestureCount[0]++; gestTimer[0]=0; gest[0]=0;
                            g.txt(new Point(220, 60), Rotation.TOP_LR, (byte) 3, (byte) 0x0F,
                                "("+String.format("%d", doublegestureCount[0])+")");} // double gesture
                    gestHandler.postDelayed(this,200); }
            }
        };
        gestHandler.removeCallbacks(gestRunnable);
        gestHandler.postDelayed(gestRunnable,200); // on redemande toutes les 200ms

        g.clear();
        g.gesture(true);
        g.txt(new Point(300, 210), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Gesture count :");
        g.txt(new Point(280, 100), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Double count :");
        g.subscribeToSensorInterfaceNotifications(() -> { if (gestMode) { gest[0]++; } });

    }

    //////////     GRAPHIC  button
    private void graphicButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = false; bitmapMode = false;

        final Glasses g = connectedGlasses;

        if (!graphicMode) {
            g.clear();

            g.color((byte) 15);
            g.rect(new Point(7, 2), new Point(290, 250));

            // Soleil : cercles
            g.circf(new Point(200, 175), (byte) 10);
            g.color((byte) 11);
            g.circ(new Point(200, 175), (byte) 14);
            g.color((byte) 5);
            g.circ(new Point(200, 175), (byte) 18);
            g.color((byte) 2);
            g.circ(new Point(200, 175), (byte) 22);

            // Soleil : rayons
            g.color((byte) 12);
            g.line(new Point(200, 150), new Point(200, 120));
            g.line(new Point(182, 157), new Point(161, 136));
            g.line(new Point(175, 175), new Point(145, 175));
            g.line(new Point(182, 197), new Point(161, 214));
            g.line(new Point(200, 200), new Point(200, 220));
            g.line(new Point(218, 197), new Point(239, 214));
            g.line(new Point(225, 175), new Point(255, 175));
            g.line(new Point(218, 157), new Point(239, 136));

            // degrade
            for (int x = 15; x > -1; x--) {
                g.color((byte) x);
                g.rectf(new Point(20 + x * 15, 15), new Point(34 + x * 15, 75));
            }

            // spirale
            g.color((byte) 12);
            for (int i = 20; i < 400; i++) { i++;i++;i++;
                g.point((short) (200 + (10 + i / 4) * sin(i * i)), (short) (175 + (10 + i / 4) * cos(i * i)));
            }
        }
    }

    //////////     SCREEN  button
    private void screenButton(){
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = true; imageMode = false; bitmapMode = false; graphicMode = false;

        final Glasses g = connectedGlasses;
        g.clear();
        g.txt(new Point(300, 140), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Wait for the image...");

        screenRunnable = new Runnable() {
            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                if (screenMode) {
                    // create bitmap screen capture
                    View v1 = getWindow().getDecorView().getRootView();
                    v1.setDrawingCacheEnabled(true);
                    Bitmap ScreenShot = Bitmap.createBitmap(v1.getDrawingCache());
                    v1.setDrawingCacheEnabled(false);
                    g.imgStream(getResizedBitmap(ScreenShot, 303,255),
                            ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 0);
                    screenHandler.postDelayed(this,3000);
                }
            }
        };
        screenHandler.removeCallbacks(screenRunnable);
        screenHandler.postDelayed(screenRunnable,3000); // on redemande toutes les 3000ms
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth(), height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width, scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        // TURN INTO BLACK&WHITE
        for (int x = 0; x < resizedBitmap.getWidth()-1; ++x) {
            for (int y = 0; y < resizedBitmap.getHeight()-1; ++y) {
                // get one pixel color
                int pixel = resizedBitmap.getPixel(x, y);
                // retrieve color of all channels
                int A = Color.alpha(pixel), R = Color.red(pixel), G = Color.green(pixel), B = Color.blue(pixel);
                // take conversion up to one single value
                R = G = B = (int) (255 - (0.299 * R + 0.587 * G + 0.114 * B));
                // set new pixel color to output bitmap
                resizedBitmap.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return resizedBitmap;
    }


    //////////     IMAGE  button
    @SuppressLint("SetTextI18n")
    private void imageButton() throws IOException {
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = true; bitmapMode = false; graphicMode = false;

        final Glasses g = connectedGlasses;
        Button buttonimages = findViewById(R.id.image_command);
        Button buttonimage2 = findViewById(R.id.image2_command);

        g.clear();
        g.txt(new Point(300, 140), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Wait for the image...");
        g.cfgSet("cfgGolf");
        imageCount ++;
        if (imageCount==8) {imageCount = 1;}
//        g.cfgSet("cfgImage");
        if (imageCount==1) {
            buttonimages.setText("Lion");
            buttonimage2.setText("Lion");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("lion_304x256.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 0);}
//            g.imgDisplay((byte) 4, (short) 0, (short) 0);}
        if (imageCount==2) {
            buttonimages.setText("Zebra");
            buttonimage2.setText("Zebra");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("zebre_304x248.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 4);}
//            g.imgDisplay((byte) 5, (short) 0, (short) 4);}
        if (imageCount==3) {
            buttonimages.setText("Tiger");
            buttonimage2.setText("Tiger");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("tigre_304x256.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 0);}
//            g.imgDisplay((byte) 6, (short) 0, (short) 0);}
        if (imageCount==4) {
            buttonimages.setText("Chess");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("chess_304x171.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 42);}
//            g.imgDisplay((byte) 7, (short) 0, (short) 42);}
        if (imageCount==5) {
            buttonimages.setText("Castle");
            buttonimage2.setText("Castle");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("castle_304x256.jpg")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 0);}
//            g.imgDisplay((byte) 8, (short) 0, (short) 0);}
        if (imageCount==6) {
            buttonimages.setText("NewYork");
            buttonimage2.setText("NewYork");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("newyork_304x213.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 18);}
//            g.imgDisplay((byte) 9, (short) 0, (short) 18);}
        if (imageCount==7) {
            buttonimages.setText("ENGO");
            buttonimage2.setText("ENGO");
            g.imgStream(BitmapFactory.decodeStream(getAssets().open("engo_304x170.png")),
                    ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 43);}
//            g.imgDisplay((byte) 10, (short) 0, (short) 43);}

    }

    //////////     IMAGE2  button
    @SuppressLint("SetTextI18n")
    private void image2Button() throws IOException {
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = true; bitmapMode = false; graphicMode = false;

        final Glasses g = connectedGlasses;
        Button buttonimages = findViewById(R.id.image_command);
        Button buttonimage2 = findViewById(R.id.image2_command);

        g.clear();
        g.txt(new Point(300, 140), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Wait for the image...");
        g.cfgSet("cfgGolf");
        g.cfgSet("cfgImage");
        if (!imageSaved) { imageSaved = true; g.cfgSet("cfgImage");
            g.txt(new Point(240, 110), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "Please wait");
            g.txt(new Point(270, 50), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "for the images");
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "12%");
            g.imgSave((byte) 0x04, img1, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "25%");
            g.imgSave((byte) 0x05, img2, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "38%");
            g.imgSave((byte) 0x06, img3, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "50%");
            g.imgSave((byte) 0x07, img4, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "62%");
            g.imgSave((byte) 0x08, img5, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "75%");
            g.imgSave((byte) 0x09, img6, MONO_4BPP);
            g.txt(new Point(190, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "88%");
            g.imgSave((byte) 0x0A, img7, MONO_4BPP);
            g.txt(new Point(210, 180), Rotation.TOP_LR, (byte) 3, (byte) 0x0F, "100 %");
            g.clear();
        }
        imageCount ++;
        if (imageCount==8) {imageCount = 1;}
        g.cfgSet("cfgImage");
        if (imageCount==1) {
            buttonimages.setText("Lion");
            buttonimage2.setText("Lion");
            g.imgDisplay((byte) 4, (short) 0, (short) 0);}
        if (imageCount==2) {
            buttonimages.setText("Zebra");
            buttonimage2.setText("Zebra");
            g.imgDisplay((byte) 5, (short) 0, (short) 4);}
        if (imageCount==3) {
            buttonimages.setText("Tiger");
            buttonimage2.setText("Tiger");
            g.imgDisplay((byte) 6, (short) 0, (short) 0);}
        if (imageCount==4) {
            buttonimages.setText("Chess");
            buttonimage2.setText("Chess");
            g.imgDisplay((byte) 7, (short) 0, (short) 42);}
        if (imageCount==5) {
            buttonimages.setText("Castle");
            buttonimage2.setText("Castle");
            g.imgDisplay((byte) 8, (short) 0, (short) 0);}
        if (imageCount==6) {
            buttonimages.setText("NewYork");
            buttonimage2.setText("NewYork");
            g.imgDisplay((byte) 9, (short) 0, (short) 18);}
        if (imageCount==7) {
            buttonimages.setText("ENGO");
            buttonimage2.setText("ENGO");
            g.imgDisplay((byte) 10, (short) 0, (short) 43);}
    }

    //////////     BITMAP  button
    private void bitmapsButton() {
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = false; bitmapMode = true; graphicMode = false;

        final Glasses g = connectedGlasses;
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");

        g.clear();
        g.cfgSet("ALooK");

        bitmapRunnable = new Runnable() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void run() {
                if (bitmapMode) {
                    clockText.setText(sdf2.format(new Date()));
                    displayClock();
                    g.layoutDisplay((byte) 71, "GPS");//GPS
                    if (BattLevel !=0 ) {batteryTextView.setText(String.format("%d",BattLevel)+"%");}

                    if (counter == 1) {g.imgDisplay((byte) bitmapCount, (short) 250, (short) 70);}
                    if (counter == 2) {g.imgDisplay((byte) bitmapCount, (short) 170, (short) 140);}
                    if (counter == 3) {g.imgDisplay((byte) bitmapCount, (short)  80, (short) 140);}
                    if (counter == 4) {g.imgDisplay((byte) bitmapCount, (short)  20, (short) 70);}
                    if (counter == 5) {g.imgDisplay((byte) bitmapCount, (short)  80, (short) 0);}
                    if (counter == 6) {g.imgDisplay((byte) bitmapCount, (short) 170, (short) 0);}
                    counter++; if (counter==7) {counter=1;}
                    bitmapCount++; if (bitmapCount==49) {bitmapCount=1;}
                    if (bitmapCount==3) {bitmapCount=4;} if (bitmapCount==23) {bitmapCount=48;}
                    bitmapHandler.postDelayed(this,500);
                }
            }
        };
        bitmapHandler.removeCallbacks(bitmapRunnable);
        bitmapHandler.postDelayed(bitmapRunnable,500); // on redemande toutes les 500ms
    }

    //////////     ANIMATION  button
    @SuppressLint("SetTextI18n")
    private void animButton() {
        this.findViewById(R.id.text_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.demo_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.gest_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.graphic_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.image2_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.screen_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.bitmaps_command).setBackgroundColor(getResources().getColor(R.color.primary_200));
        this.findViewById(R.id.anim_command).setBackgroundColor(getResources().getColor(R.color.secondary_200));
        textMode = false; demoMode = false; gestMode = false; screenMode = false; imageMode = false; bitmapMode = false; graphicMode = false;

        final Glasses g = connectedGlasses;
        Button buttonanimations = findViewById(R.id.anim_command);

        g.clear();
        g.cfgSet("cfgGolf");
        animCount ++;
        if (animCount==5) {animCount = 1;}

        if (animCount==1) {buttonanimations.setText("golfBall");
            g.animDisplay((byte) 1, (byte) 1,(short) 200,(byte) 0xFF, (short) (150-76/2), (short) (127-76/2));}
        if (animCount==2) {buttonanimations.setText("wolf");
            g.animDisplay((byte) 1, (byte) 2,(short) 80,(byte) 0xFF, (short) (150-176/2), (short) (127-82/2));}
        if (animCount==3) {buttonanimations.setText("earth");
            g.animDisplay((byte) 1, (byte) 3,(short) 150,(byte) 0xFF, (short) (150-32), (short) (127-32));}
        if (animCount==4) {buttonanimations.setText("running");
            g.animDisplay((byte) 1, (byte) 4,(short) 180,(byte) 0xFF, (short) (150-114/2), (short) (127-134/2));}

    }

        
    /////////  LUMINANCE  bar and switch
    private void lumaButton(int luma){ connectedGlasses.luma((byte) luma); }
    private void sensorSwitch(boolean on){ connectedGlasses.sensor(on); }


    @SuppressLint("SetTextI18n")
    private void setUIGlassesInformations(){
        final Glasses g = connectedGlasses;
        glassesidTextView.setText(g.getName());
        DeviceInformation di = g.getDeviceInformation();
        serialnumberTextView.setText(di.getSerialNumber());
        fwTextView.setText(di.getFirmwareVersion());

        g.settings(r -> sensorSwitch.setChecked(r.isGestureEnable()));
        g.settings(r -> luminanceSeekBar.setProgress(r.getLuma()));
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == requestCode && requestCode == Activity.RESULT_FIRST_USER) {
            if (data != null && data.hasExtra("connectedGlasses")) {
                if (connectedGlasses != null) {
                    connectedGlasses = data.getExtras().getParcelable("connectedGlasses");
                    connectedGlasses.setOnDisconnected(glasses -> MainActivity.this.disconnect());
                }
                runOnUiThread(MainActivity.this::setUIGlassesInformations);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (connectedGlasses != null) { savedInstanceState.putParcelable("connectedGlasses", connectedGlasses); }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();
        }
        if (!((DemoApp) this.getApplication()).isConnected()) {
            connectedGlasses = null;
        }
        this.updateVisibility();
        imageSaved = false; demoMode = false; gestMode = false; imageMode = false;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();
            largeText.setText("Your BlueTooth is not open !!\n\n" +
                    "Please open BlueTooth and\n\n relaunch the application.");
            largeText.setTextColor(Color.parseColor("#FF0000"));
            largeText.setTypeface(largeText.getTypeface(), Typeface.BOLD);
        }
        if (!((DemoApp) this.getApplication()).isConnected()) { connectedGlasses = null; }
        this.updateVisibility();
        imageSaved = false;
    }

    protected void onPause() {
        super.onPause();
        if(textHandler != null)
            textHandler.removeCallbacks(textRunnable); // On arrete le callback
        if(golfHandler != null)
            golfHandler.removeCallbacks(golfRunnable); // On arrete le callback
        if(bitmapHandler != null)
            bitmapHandler.removeCallbacks(bitmapRunnable); // On arrete le callback
//        if(screenHandler != null)
//            screenHandler.removeCallbacks(screenRunnable); // On arrete le callback
    }

    protected void onStop() {
        super.onStop();
        if(textHandler != null)
            textHandler.removeCallbacks(textRunnable); // On arrete le callback
        if(golfHandler != null)
            golfHandler.removeCallbacks(golfRunnable); // On arrete le callback
        if(bitmapHandler != null)
            bitmapHandler.removeCallbacks(bitmapRunnable); // On arrete le callback
        if(screenHandler != null)
            screenHandler.removeCallbacks(screenRunnable); // On arrete le callback
        if (connectedGlasses!=null) {
            connectedGlasses.cfgDelete("DemoApp");
            connectedGlasses.cfgDelete("cfgImage");
            connectedGlasses.cfgDelete("cfgGolf");
            connectedGlasses.cfgDelete("cfgLaurent");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final Glasses g = connectedGlasses;

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_app) {Toast.makeText(this.getApplicationContext(),
                getString(R.string.app_name) + "\nVersion " + getString(R.string.app_version),
                Toast.LENGTH_LONG).show();
            return true;}
        if (id == R.id.about_glasses) {
            if( g!=null) {Toast.makeText(this.getApplicationContext(),
                    "Glasses Name : " + g.getName() + "\n"
                            + "Firmware : " + g.getDeviceInformation().getFirmwareVersion(),
                    Toast.LENGTH_LONG).show();}
            else {Toast.makeText(this.getApplicationContext(),
                    "No connected glasses found yet!",
                    Toast.LENGTH_LONG).show();}
            return true;}
        return super.onOptionsItemSelected(item);
    }

    private void disconnect() {
        runOnUiThread(() -> {
            ((DemoApp) this.getApplication()).onDisconnected();
            connectedGlasses = null;
            MainActivity.this.updateVisibility();
            MainActivity.this.snack("Disconnected");
        });
    }

    private Toast toast(Object data) {
        Log.d("MainActivity", data.toString());
        Toast toast = Toast.makeText(this, data.toString(), Toast.LENGTH_LONG);
        toast.show();
        return toast;
    }

    private Snackbar snack() { return this.snack(null, null); }

    private void snack(Object data) { this.snack(null, data); }

    private Snackbar snack(View snackView, Object data) {
        snackView = this.findViewById(R.id.toolbar);
        final String msg = data == null ? "" : data.toString();
        Snackbar snack = Snackbar.make(snackView, msg, Snackbar.LENGTH_LONG);
        snack.show();
        if (data != null) { Log.d("MainActivity", data.toString()); }
        else { snack.dismiss(); }
        return snack;
    }

}
