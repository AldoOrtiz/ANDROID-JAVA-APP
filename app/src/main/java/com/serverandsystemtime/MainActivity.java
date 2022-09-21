package com.serverandsystemtime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.function.LongFunction;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView timeTv;
    String time = "Loading...";
    private Thread myThread = null;
    public static final String[] TIME_SERVER = {"2.android.pool.ntp.org", "time.nist.gov", "pool.ntp.org"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        timeTv = findViewById(R.id.time_tv);

        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();


        button.setOnClickListener(view -> {
            if (isNetworkAvailable()) {
                button.setText("Online");
            } else {
                button.setText("No internet");
            }

        });


    }

    public void startGettingTime() {
        runOnUiThread(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("hh-mm-ss a");
                time = sdf.format(getNTPTime());
                timeTv.setText(time);
            } catch (Exception ignored) {
            }
        });
    }

    class CountDownRunner implements Runnable {
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    startGettingTime();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                }
            }
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private Date getNTPTime() {
        if (isNetworkAvailable()) {
            button.setText("Online");
            Log.e("isOnline", "true");
            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.setDefaultTimeout(2000);
            TimeInfo timeInfo;
            try {
                timeClient.open();
                InetAddress inetAddress = InetAddress.getByName(getNTPServer());
                timeInfo = timeClient.getTime(inetAddress);
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
                Date time = new Date(returnTime);
                timeClient.close();
                return time;
            } catch (Exception e) {
                e.printStackTrace();
                return new Date();
            }
        } else {
            button.setText("Offline");
            Log.e("isOnline", "false");
            return new Date(System.currentTimeMillis());
        }
    }

    public static String getNTPServer() {
        int max = TIME_SERVER.length;

        Random r = new Random();
        int i1 = r.nextInt(max - 1);

        return TIME_SERVER[i1];
    }

}