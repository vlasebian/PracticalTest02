package ro.pub.cs.systems.eim.ColocviuFinal.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;

import ro.pub.cs.systems.eim.ColocviuFinal.general.Constants;
import ro.pub.cs.systems.eim.ColocviuFinal.general.Utilities;
import ro.pub.cs.systems.eim.ColocviuFinal.model.DataModel;

import static ro.pub.cs.systems.eim.ColocviuFinal.general.Constants.GET_REQUEST;
import static ro.pub.cs.systems.eim.ColocviuFinal.general.Constants.POST_REQUEST;
import static ro.pub.cs.systems.eim.ColocviuFinal.general.Constants.TAG;

public class CommunicationThread extends Thread {
    private Socket socket;
    private ServerThread serverThread;

    private static final String requestType = GET_REQUEST;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }

        try {
            /* get reader and writer */
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            if (bufferedReader == null || printWriter == null) {
                Log.e(TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }

            Log.i(TAG, "[COMMUNICATION THREAD] Waiting for parameters from client");
            String currency = bufferedReader.readLine();
            String request  = bufferedReader.readLine();

            if (currency == null || currency.isEmpty()) {
                Log.e(TAG, "[COMMUNICATION THREAD] Error receiving parameters from client");
                return;
            }

            /* get cached data from server, if no data is cached, make a request */
            HashMap<String, DataModel> data = serverThread.getData();
            DataModel dataModel = null;

            if (request == Constants.UI_REQUEST && data.containsKey(currency)) {

                Log.e(TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                dataModel = data.get(currency);

            } else {
                Log.i(TAG, "[COMMUNICATION THREAD] Getting the information from the web service...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                switch (requestType) {
                    case POST_REQUEST:
                        break;
                    case GET_REQUEST:
                        HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + currency +
                                                                        Constants.WEB_SERVICE_MODE);

                        HttpResponse httpGetResponse = httpClient.execute(httpGet);
                        HttpEntity httpGetEntity = httpGetResponse.getEntity();
                        if (httpGetEntity != null) {
                            pageSourceCode = EntityUtils.toString(httpGetEntity);
                        }

                        break;
                }

                if (pageSourceCode == null) {
                    Log.e(TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else {
                    Log.i(TAG, pageSourceCode);
                }

                switch (requestType) {
                    case POST_REQUEST:
                        break;
                    case GET_REQUEST:

                        JSONObject content = new JSONObject(pageSourceCode);
                        JSONObject time    = content.getJSONObject("time");
                        JSONObject bpi     = content.getJSONObject("bpi");
                        JSONObject curr    = bpi.getJSONObject(currency);

                        String updateTimestamp = time.getString("updated");
                        String rate = curr.getString("rate");

                        dataModel = new DataModel(rate, updateTimestamp);

                        data.put(currency, dataModel);
                        serverThread.setData(data);

                        break;
                }

            }

            /* no data found */
            if (data == null) {
                Log.e(TAG, "[COMMUNICATION THREAD] Data is null!");
                return;
            }

            /* update UI: create result string and send it to the socket */
            String result = dataModel.toString();

            printWriter.println(result);
            printWriter.flush();

        } catch (IOException ioException) {
            Log.e(TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }
}
