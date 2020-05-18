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

    private EditText ipEditText;
    private Button getDataButton;
    private TextView resultTextView;

    private ButtonClickListener getDataButtonListener = new ButtonClickListener();
    private class ButtonClickListener implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            String address = null;

            if (ipEditText.getText() != null) {
                address = ipEditText.getText().toString();
            }

            if (serverThread == null || !serverThread.isAlive()) {
                Toast.makeText(getApplicationContext(), "[MAIN ACTIVITY] There is no server to connect to!", Toast.LENGTH_SHORT).show();
                return;
            }

            resultTextView.setText("");
            clientThread = new ClientThread("localhost", 1234, resultTextView, address);
            clientThread.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, "[MAIN ACTIVITY] onCreate() callback method has been invoked");
        setContentView(R.layout.activity_main);

        /* TODO */

        ipEditText = findViewById(R.id.portEditText);
        getDataButton = findViewById(R.id.getDataButton);
        resultTextView = findViewById(R.id.dataTextView);

        getDataButton.setOnClickListener(getDataButtonListener);

        /* start server thread */
        serverThread = new ServerThread(1234);
        if (serverThread.getServerSocket() == null) {
            Log.e(Constants.TAG, "[MAIN ACTIVITY] Could not create server thread!");
            return;
        }
        serverThread.start();
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
