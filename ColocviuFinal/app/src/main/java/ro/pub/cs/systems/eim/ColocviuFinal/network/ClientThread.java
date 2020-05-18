package ro.pub.cs.systems.eim.ColocviuFinal.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.ColocviuFinal.general.Constants;
import ro.pub.cs.systems.eim.ColocviuFinal.general.Utilities;

import static ro.pub.cs.systems.eim.ColocviuFinal.general.Constants.TAG;

public class ClientThread extends Thread {
    private String  address;
    private Integer port;

    private Socket socket;
    private TextView resultTextView;

    private String currency;
    private String requestType;

    public ClientThread(String address, int port, TextView resultTextView, String ...otherInfo) {
        this.address = address;
        this.port = port;

        this.resultTextView = resultTextView;

        currency = otherInfo[0];
        requestType = otherInfo[1];
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            /* send search parameters to CommunicationThread */
            printWriter.println(currency);
            printWriter.flush();
            printWriter.println(requestType);
            printWriter.flush();

            Log.i(TAG, "[CLIENT THREAD] Waiting for response");

            if (requestType == Constants.UPDATE) {
                /* do not update the ui */
                return;
            }

            /* wait for results to be sent */
            String information;
            while ((information = bufferedReader.readLine()) != null) {
                /* update UI */
                Log.i(TAG, information);
                final String finalizedWeateherInformation = information;
                resultTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        resultTextView.setText(finalizedWeateherInformation);
                    }
                });
            }
        } catch (IOException ioException) {
            Log.e(TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
