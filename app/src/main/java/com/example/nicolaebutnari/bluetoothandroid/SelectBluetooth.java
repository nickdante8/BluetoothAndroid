package com.example.nicolaebutnari.bluetoothandroid;

        import android.app.ProgressDialog;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.net.Uri;
        import android.os.Bundle;
        import android.support.v4.content.res.ResourcesCompat;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.util.ArrayList;
        import java.util.Set;

public class SelectBluetooth extends AppCompatActivity {

    private static final String TAG = "SelecBluetooth";
    Context SelectBluetoothContext = this;
    ListView devicelist;
    ArrayList mArrayAdapter = new ArrayList();
    int mArrayAdaptercounter;

    public TextView textView;
    public Button button;
    public boolean is_button_clicked = false;
    private ProgressDialog progress;

    private BluetoothAdapter mBluetoothAdapter = null;
    public final static String ADDRESS = "com.example.erwin.arobswirelessboard.SelectBluetooth.address";
    public final static String BTNAME = "com.example.erwin.arobswirelessboard.SelectBluetooth.bt_name";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_bluetooth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_BT_MESSAGE);

        textView = (TextView) findViewById(R.id.textView);
        Log.d(TAG, message);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!is_button_clicked) {
                    ScanforNewDevices();
                    is_button_clicked = true;
                    button.setText(R.string.cancel_discovery);
                    button.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorBTCancelSearch, null));
                    textView.setText(R.string.select_bluetooth_cancel_search);
                } else {
                    StopDiscovery();
                    is_button_clicked = false;
                    button.setText(R.string.start_discovery);
                    button.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorBTSearch, null));

                    if (mArrayAdaptercounter == 0) {
                        textView.setText(R.string.no_device_found);
                    }
                    else {
                        textView.setText(getString(R.string.choose_device) + " " + getString(R.string.select_bluetooth_start_search));
                    }
                }
            }
        });

        devicelist = (ListView) findViewById(R.id.listView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        queryingPairedDevices();
    }

    // List known devices
    public void queryingPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList mArrayAdapter = new ArrayList();

        // if there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            msg("No Paired BT Devices Found.");
        }

        final ArrayAdapter adapter = new ArrayAdapter(SelectBluetoothContext, android.R.layout.simple_list_item_1, mArrayAdapter);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(chooseBluetoothDevice);
    }

    private AdapterView.OnItemClickListener chooseBluetoothDevice = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            StopDiscovery();

            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            String bt_name = info.substring(0, info.length() - 17);

            // Pass data to the Main Activity.
            // First create a new Intent without parameters
            Intent intent = new Intent();

            // Pass it to intent.putExtra for ID transfer where ADDRESS it's seams to be some kind of ID
            intent.putExtra(ADDRESS, address);
            intent.putExtra(BTNAME, bt_name);
            // And set result. It must be RESULT_OK for the positive int or the Main Activity wouldn't get it
            setResult(RESULT_OK, intent);
            // To end the current Activity
            finish();
        }
    };

    //----------------------------------------------------------------------------------------------
    // Scan for new Devices
    public void ScanforNewDevices() {
        // if is discovering then stop it
        StopDiscovery();

        if (!mBluetoothAdapter.startDiscovery()) {
            Log.d(TAG, "Unable to start Discovery.");
            //msg("Unable to start Discovery.");
        } else {
            Log.d(TAG, "Able to start Discovery.");
            //msg("Able to start Discovery.");
        }

        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
    }


    // You need to unregister your receiver on stop of your activity
    protected final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ArrayAdapter adapter;

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "Discovery has started!");
                // msg("Discovery has started!");
                // discovery starts, we can show progress dialog or perform other tasks
                // progress = ProgressDialog.show(SelectBluetooth.this, "Searching...", "Please wait!!!");

                // Clear collected in ArrayList, else it's going to add the same devices to the list
                mArrayAdapter.clear();
                mArrayAdaptercounter = 0;
                adapter = new ArrayAdapter(SelectBluetoothContext, android.R.layout.simple_list_item_1, mArrayAdapter);
                devicelist.setAdapter(adapter);
            } else {
                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.d(TAG, "Discovery has finished!");
                    // msg("Discovery has finished!");
                    // discovery finishes, dismiss progress dialog
                    // progress.dismiss();

                    StopDiscovery();
                    is_button_clicked = false;
                    button.setText(R.string.start_discovery);
                    button.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorBTSearch, null));

                    if (mArrayAdaptercounter == 0) {
                        textView.setText(R.string.no_device_found);
                    }
                    else {
                        textView.setText(getString(R.string.choose_device) + " " + getString(R.string.select_bluetooth_start_search));
                    }

                    // unregister Receiver to prevent memory leak
                    unregisterReceiver(mReceiver);
                } else {
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                        mArrayAdaptercounter++;

                        adapter = new ArrayAdapter(SelectBluetoothContext, android.R.layout.simple_list_item_1, mArrayAdapter);
                        devicelist.setAdapter(adapter);
                    } else {
                        textView.setText(R.string.no_device_found);
                        Log.d(TAG, "No device found.");
                        msg("No device found.");
                    }
                }
            }
        }

    };

    public void StopDiscovery() {
        // if is discovering then stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void Disconnect() {
        StopDiscovery();

        // Pass data to the Main Activity.
        // First create a new Intent without parameters
        Intent intent = new Intent();

        // Pass it to intent.putExtra for ID transfer where ADDRESS it's seams to be some kind of ID
        intent.putExtra(ADDRESS, "disconnect");
        // And set result. It must be RESULT_OK for the positive int or the Main Activity wouldn't get it
        setResult(RESULT_OK, intent);
        // To end the current Activity
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        StopDiscovery();
    }


    //----------------------------------------------------------------------------------------------
    // Used in case of errors, will end the activity
    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    // Used for messages
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_menu_select_bluetooth_disconnect) {
            Disconnect();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
