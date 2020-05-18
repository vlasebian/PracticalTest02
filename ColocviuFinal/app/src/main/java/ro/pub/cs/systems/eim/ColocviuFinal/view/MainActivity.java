package ro.pub.cs.systems.eim.ColocviuFinal.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ro.pub.cs.systems.eim.ColocviuFinal.R;
import ro.pub.cs.systems.eim.ColocviuFinal.general.Constants;
import ro.pub.cs.systems.eim.ColocviuFinal.network.ClientThread;
import ro.pub.cs.systems.eim.ColocviuFinal.network.ServerThread;

public class MainActivity extends AppCompatActivity {
    private ServerThread serverThread;
    private ClientThread clientThread;

    private Integer  port;

    private EditText portEditText;
    private Button   startServerButton;
    private EditText currencyEditText;
    private Button   makeRequestButton;
    private TextView resultTextView;

    private StartServerButtonClickListener startServerButtonClickListener = new StartServerButtonClickListener();
    private class StartServerButtonClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if (portEditText.getText().toString().isEmpty()) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Port was not specified!");
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Port was not specified!!", Toast.LENGTH_SHORT).show();
                return;
            }

            port = Integer.parseInt(portEditText.getText().toString());

            /* start server thread */
            serverThread = new ServerThread(port);
            if (serverThread.getServerSocket() == null) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Could not create server thread!!", Toast.LENGTH_SHORT).show();
                return;
            }
            serverThread.start();
        }
    }

    private MakeRequestButtonClickListener makeRequestButtonClickListener = new MakeRequestButtonClickListener();
    private class MakeRequestButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currencyEditText.getText().toString().isEmpty()) {
                Log.e(Constants.TAG, "[MAIN ACTIVITY] Currency was not specified!");
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] Currency was not specified!!", Toast.LENGTH_SHORT).show();
                return;
            }

            String currency = currencyEditText.getText().toString().toUpperCase();
            resultTextView.setText("");

            clientThread = new ClientThread("localhost", port, resultTextView, currency);
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_main);

        portEditText      = findViewById(R.id.portEditText);
        startServerButton = findViewById(R.id.startServerButton);
        currencyEditText  = findViewById(R.id.currencyEditText);
        makeRequestButton = findViewById(R.id.makeRequestButton);
        resultTextView    = findViewById(R.id.dataTextView);

        startServerButton.setOnClickListener(startServerButtonClickListener);
        makeRequestButton.setOnClickListener(makeRequestButtonClickListener);
    }


    @Override
    protected void onDestroy() {
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onDestroy() callback method has been invoked");
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}
