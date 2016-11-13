package com.example.dhananjay.RControl;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Set;


public class Activity2 extends Activity {
    BluetoothAdapter mBluetoothAdapter;
    TextView textView;
    EditText message, deviceName;
    Button sendButton;
    Button forwardButton, reverseButton, leftButton, rightButton;
    BluetoothSocket mmSocket;
    BluetoothDevice device;
    ScrollView scrollView;
    Button toggleLogButton, connectButton;
    private OutputStream mmOutStream;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity2);


        toggleLogButton = (Button) findViewById(R.id.toggleLogButton);
        textView = (TextView) findViewById(R.id.textView);
        deviceName = (EditText) findViewById(R.id.device_name);
        message = (EditText) findViewById(R.id.message);
        sendButton = (Button) findViewById(R.id.send_message);
        forwardButton = (Button) findViewById(R.id.forwardButton);
        reverseButton = (Button) findViewById(R.id.reverseButton);
        rightButton = (Button) findViewById(R.id.rightButton);
        leftButton = (Button) findViewById(R.id.leftButton);
        connectButton = (Button) findViewById(R.id.connect_button);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setVisibility(View.GONE);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        OnTouchListener forwardListener = new OnTouchListener('f');
        OnTouchListener reverseListener = new OnTouchListener('q');
        OnTouchListener leftListener = new OnTouchListener('l');
        OnTouchListener rightListener = new OnTouchListener('r');


        forwardButton.setOnTouchListener(forwardListener);
        reverseButton.setOnTouchListener(reverseListener);
        leftButton.setOnTouchListener(leftListener);
        rightButton.setOnTouchListener(rightListener);
    }

    public String connect() {
        String log = new String();

        try {
            if (mmOutStream != null) {
                closeStreams();
                mmOutStream = null;
            }

            if (!mBluetoothAdapter.isEnabled()) {

                runOnUiThread(new MyRunnable("Turning On Bluetooth"));
                mBluetoothAdapter.enable();
                while (!mBluetoothAdapter.isEnabled()) {
                }

            }

            final String bModule = deviceName.getText().toString();
            runOnUiThread(new MyRunnable("Connecting To " + bModule + "..."));


            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            device = null;
            for (BluetoothDevice device1 : pairedDevices) {
                Log.e("abcd", "device : " + device1.getName());
                if (device1.getName().equals(bModule)) {
                    device = device1;
                    log = log + device1.getName() + " Found \n";
                    break;
                }

            }


            if (device == null) {
                log = log + bModule + " not found in paired devices.Pair with " + bModule + " before trying again.";

                return log;
            }


            log = log + "Connecting to " + device.getName() + "...\n";
            mmSocket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
            mmSocket.connect();
            log = log + "Connected to " + device.getName() + "\n";
            Log.e("test", "Socket connected");


            OutputStream tmpOut = null;

            tmpOut = mmSocket.getOutputStream();
            Log.e("test", "Obtained Output Stream");


            mmOutStream = tmpOut;
        } catch (Exception e) {
            log = log + "Couldn't establish Bluetooth connection!";
            Log.e("test", "exception", e);
        }

        return log;

    }

    public void onClickConnect(View v) {
        new ConnectTask().execute();

    }

    public void onClickSend(View v) {
        byte[] msg = message.getText().toString().getBytes(Charset.forName("UTF-8"));
        write(msg);
        message.setText("");

    }

    @Override
    protected void onStop() {
        super.onPause();
        closeStreams();
    }

    protected void onDestroy() {
        super.onDestroy();
        closeStreams();
    }

    public void closeStreams() {
        try {
            if (mmOutStream != null)
                mmOutStream.close();

            if (mmSocket != null)
                mmSocket.close();

        } catch (IOException e) {
            Log.e("test", "exception", e);
        }
    }

    public void onClickStopButton(View view) {
        write('s');
    }

    public void onClickToggleLogButton(View view) {

        new AlertDialog.Builder(this)
                .setTitle("Log")
                .setMessage(textView.getText().toString())
                .setPositiveButton("Clear Log", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText("");
                        // continue with delete
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();



    }

    void write(char a) {
        try {
            if (mmOutStream == null) {
                textView.append("No Device Connected \n");
                return;
            }
            mmOutStream.write(a);
            textView.append("Sent " + a + "     ");
        } catch (IOException e) {
            textView.append("Error" + e.toString());
        }

    }

    void write(byte[] a) {
        try {
            if (mmOutStream == null) {
                textView.append("No Device Connected \n");
                return;
            }
            mmOutStream.write(a);
            textView.append("Sent " + Arrays.toString(a) + "     ");
        } catch (IOException e) {
            textView.append("Error" + e.toString());
        }

    }

    class OnTouchListener implements View.OnTouchListener {
        char a;

        OnTouchListener(char a) {
            this.a = a;
        }

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    write(a);
                    break;
                case MotionEvent.ACTION_UP:
                    write('s');
                    break;
            }
            return false;
        }
    }

    public class MyRunnable implements Runnable {
        String message;

        public MyRunnable(String message) {
            this.message = message;
        }

        public void run() {
            pDialog.setMessage(message);
        }
    }

    class ConnectTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Activity2.this);
            pDialog.setMessage("Connecting...");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String a) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            textView.setText(a);

            super.onPostExecute(a);
        }

        @Override
        protected String doInBackground(Void... params) {
            return connect();
        }
    }


}







