package com.serverandsystemtime;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button button; // button to get time
    TextView timeTv; // text view to show time
    String time = "Loading...";  // time string
    private Thread myThread = null; // thread to get time
    public static final String[] TIME_SERVER = {"2.android.pool.ntp.org", "time.nist.gov", "pool.ntp.org"}; // time server


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // call super class
        setContentView(R.layout.activity_main); // set layout

        button = findViewById(R.id.button);// get button
        timeTv = findViewById(R.id.time_tv);// get text view

        Runnable runnable = new CountDownRunner();// create runnable
        myThread = new Thread(runnable);// create thread
        myThread.start();// start thread


        button.setOnClickListener(view -> { // set on click listener
            if (isNetworkAvailable()) { // check if network is available
                button.setText("Online"); // set button text
            } else {
                button.setText("No internet");
            }

        });


    }

    public void startGettingTime() { // method to get time
        runOnUiThread(() -> { // run on ui thread
            try { //    try catch block
                SimpleDateFormat sdf = new SimpleDateFormat("hh-mm-ss a"); // create simple date format
                time = sdf.format(getNTPTime()); // get time from ntp server
                timeTv.setText(time);// set text view text
            } catch (Exception ignored) { // catch exception
            }
        });
    }

    class CountDownRunner implements Runnable { // runnable class
        @Override
        public void run() { // run method
            while (!Thread.currentThread().isInterrupted()) { // while thread is not interrupted
                try {
                    startGettingTime(); // get time
                    Thread.sleep(1000); //  sleep for 1 second
                } catch (InterruptedException e) { // catch exception
                    Thread.currentThread().interrupt(); // interrupt thread
                } catch (Exception e) {
                }
            }
        }
    }


    private boolean isNetworkAvailable() { // method to check if network is available
        ConnectivityManager connectivityManager // create connectivity manager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); // get connectivity manager
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo(); // get active network info
        return activeNetworkInfo != null && activeNetworkInfo.isConnected(); // return true if network is available
    }

    private Date getNTPTime() { // method to get time from ntp server
        if (isNetworkAvailable()) { // check if network is available
            button.setText("Online"); // set button text
            Log.e("isOnline", "true"); //   log
            NTPUDPClient timeClient = new NTPUDPClient(); // create ntp client
            timeClient.setDefaultTimeout(2000); // set default timeout
            TimeInfo timeInfo; // create time info
            try {
                timeClient.open(); // open client
                InetAddress inetAddress = InetAddress.getByName(getNTPServer()); // get inet address
                timeInfo = timeClient.getTime(inetAddress); // get time info
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime(); // get time
                Date time = new Date(returnTime); // create date
                timeClient.close(); // close client
                return time;// return time
            } catch (Exception e) {
                e.printStackTrace(); // print stack trace
                return new Date(); // return current time
            }
        } else {
            button.setText("Offline");
            Log.e("isOnline", "false");
            return new Date(System.currentTimeMillis()); // return current time
        }
    }

    public static String getNTPServer() { // method to get ntp server
        int max = TIME_SERVER.length; // get max length

        Random r = new Random(); // create random
        int i1 = r.nextInt(max - 1); // get random number

        return TIME_SERVER[i1]; // return random server
    }

}