package com.example.finalconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.mukesh.tinydb.TinyDB;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int RESULT_LOAD_IMG = 2;
    public static final int LAUNCH_SECOND_ACTIVITY = 1;
    private static final int STATE_MESSAGE_RECEIVED = 3;
    TextView connectionStatus, messageTextView, positionView;
    View view;
    Button aSwitch, discoverButton;
    ListView listView;
    EditText typeMsg;
    ImageButton goImageButton, settingsBtn;
    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;
    Socket socket;
    ServerClass serverClass;
    TinyDB tinyDB;
    ClientClass clientClass;
    boolean isHost;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initalWork();
        exqListener();
    }

    private void initalWork(){
        connectionStatus = findViewById(R.id.connection_status);
        messageTextView = findViewById(R.id.messageTextView);
        positionView = findViewById(R.id.positionView);
        aSwitch = findViewById(R.id.switch1);
        discoverButton = findViewById(R.id.buttonDiscover);
        listView = findViewById(R.id.listView);
        typeMsg = findViewById(R.id.editTextTypeMsg);
        imageView = findViewById(R.id.imageView);
        settingsBtn = findViewById(R.id.settingsBtn);
        goImageButton = findViewById(R.id.goImageBtn);
        view = findViewById(R.id.view);
        tinyDB = new TinyDB(getApplicationContext());

        manager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if(!wifiP2pDeviceList.equals(peers)){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;
                for(WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);

                if(peers.size() == 0){
                    connectionStatus.setText("No Device Found");
                    return;
                }
            }
        }
    };

    private void exqListener(){
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
                startActivityForResult(intent, 1);
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discovery started");
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Discovery not started");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Connected: " + device.deviceAddress);
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Not Connected");
                    }
                });
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                i.putExtra("statusText", positionView.getText().toString());
                startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    imageView.setImageBitmap(selectedImage);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.PNG, 50, stream);
                    byte[] imageBytes = stream.toByteArray();

                    goImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(MainActivity.this, ImageViewActivity.class);
                            i.putExtra("imageBitmap", stream.toByteArray());
                            i.putExtra("statusText", positionView.getText().toString());
                            startActivity(i);
                        }
                    });

                    int subArraySize = 400;

                    executor.execute((new Runnable() {
                        @Override
                        public void run() {
                            if(isHost){
                                //serverClass.write(img.getBytes());
                                serverClass.write(String.valueOf(imageBytes.length).getBytes());
                                for(int i = 0; i < imageBytes.length; i += subArraySize){
                                    byte[] tempArray;
                                    tempArray = Arrays.copyOfRange(imageBytes, i, Math.min(imageBytes.length, i + subArraySize));
                                    serverClass.write(tempArray);

                                }
                            }else if(!isHost){
                                //clientClass.write(img.getBytes());
                                clientClass.write(String.valueOf(imageBytes.length).getBytes());
                                for(int i = 0; i < imageBytes.length; i += subArraySize){
                                    byte[] tempArray;
                                    tempArray = Arrays.copyOfRange(imageBytes, i, Math.min(imageBytes.length, i + subArraySize));
                                    clientClass.write(tempArray);
                                    if(clientClass.Touched()){
                                        System.out.println("Touched the client");
                                    }
                                }
                            }
                        }
                    }));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                Bundle bundle = data.getExtras();
                String result = bundle.getString("statusText");

                positionView.setText(result);
            }
        }
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText("Host");
                isHost = true;
                serverClass = new ServerClass();
                serverClass.start();
            }else if(wifiP2pInfo.groupFormed){
                connectionStatus.setText("Client");
                isHost = false;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    public class ClientClass extends Thread {
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;
        private boolean isTouch = false;

        public ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        public boolean Touched(){
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int X = (int) event.getX();
                    int Y = (int) event.getY();

                    int eventaction = event.getAction();
                    switch (eventaction) {
                        case MotionEvent.ACTION_DOWN:
                            //Toast.makeText(MainActivity.this, "ACTION_DOWN AT COORDS "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();

                            isTouch = true;
                            break;

                        case MotionEvent.ACTION_MOVE:
                            //Toast.makeText(MainActivity.this, "MOVE "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                            break;

                        case MotionEvent.ACTION_UP:
                            //Toast.makeText(MainActivity.this, "ACTION_UP "+"X: "+X+" Y: "+Y, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    return true;
                }
            });
            return true;
        }

        @Override
        public void run(){
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper()){
              @Override
              public void handleMessage(Message msg){
                  switch(msg.what){
                      case STATE_MESSAGE_RECEIVED:
                          byte[] readBuff = (byte[]) msg.obj;
                          Bitmap bitmap = BitmapFactory.decodeByteArray(readBuff, 0, msg.arg1);

                          ByteArrayOutputStream stream = new ByteArrayOutputStream();
                          bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                          goImageButton.setOnClickListener(new View.OnClickListener() {
                              @Override
                              public void onClick(View v) {
                                  Intent i = new Intent(MainActivity.this, ImageViewActivity.class);
                                  i.putExtra("imageBitmap", stream.toByteArray());
                                  i.putExtra("statusText", positionView.getText().toString());
                                  startActivity(i);
                              }
                          });
                          imageView.setImageBitmap(bitmap);
                  }
              }
            };

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //for image sending
                    byte[] buff = null;
                    int numberOfbytes = 0;
                    int index = 0;
                    boolean flag = true;

                    if(socket != null) {
                        while (true) {
                            if (flag) {
                                try {
                                    //figure out where its failing

                                    byte[] temp = new byte[0];
                                    if (inputStream != null) {
                                        temp = new byte[inputStream.available()];
                                    }
                                    if (inputStream != null && inputStream.read(temp) > 0) {
                                        numberOfbytes = Integer.parseInt(new String(temp, StandardCharsets.UTF_8));
                                        buff = new byte[numberOfbytes];
                                        flag = false;
                                    }
                                } catch(SocketException e){
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    byte[] data = new byte[inputStream.available()];
                                    int numbers = inputStream.read(data);

                                    System.arraycopy(data, 0, buff, index, numbers);
                                    index = index + numbers;

                                    if (index == numberOfbytes) {
                                        handler.obtainMessage(STATE_MESSAGE_RECEIVED, numberOfbytes, -1, buff).sendToTarget();
                                        flag = true;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }
        public void write(byte[] bytes){
            try {
                if(outputStream == null){
                    socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                    outputStream = socket.getOutputStream();
                }
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServerClass extends Thread implements Serializable{
        ServerSocket serverSocket;
        InputStream inputStream;
        OutputStream outputStream;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                serverSocket.setReuseAddress(true);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper()){
                @Override
                public void handleMessage(Message msg){
                    switch(msg.what){
                        case STATE_MESSAGE_RECEIVED:
                            byte[] readBuff = (byte[]) msg.obj;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(readBuff, 0, msg.arg1);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            goImageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(MainActivity.this, ImageViewActivity.class);
                                    i.putExtra("imageBitmap", stream.toByteArray());
                                    i.putExtra("statusText", positionView.getText().toString());
                                    startActivity(i);
                                }
                            });
                            imageView.setImageBitmap(bitmap);
                    }
                }
            };

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    //for image sending
                    byte[] buff = null;
                    int numberOfbytes = 0;
                    int index = 0;
                    boolean flag = true;

                    while(socket != null) {
                        while (true) {
                            if (flag) {
                                try {
                                    if(inputStream == null){
                                        inputStream = socket.getInputStream();
                                    }
                                    byte[] temp = new byte[inputStream.available()];
                                    if (inputStream.read(temp) > 0) {
                                        numberOfbytes = Integer.parseInt(new String(temp, StandardCharsets.UTF_8));
                                        buff = new byte[numberOfbytes];
                                        flag = false;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    byte[] data = new byte[inputStream.available()];
                                    int numbers = inputStream.read(data);

                                    System.arraycopy(data, 0, buff, index, numbers);
                                    index = index + numbers;

                                    if (index == numberOfbytes) {
                                        handler.obtainMessage(STATE_MESSAGE_RECEIVED, numberOfbytes, -1, buff).sendToTarget();
                                        flag = true;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }
        public void write(byte[] bytes){
            try {
                if(socket == null || outputStream == null){
                    socket = serverSocket.accept();
                    serverSocket.setReuseAddress(true);
                    outputStream = socket.getOutputStream();
                }
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
