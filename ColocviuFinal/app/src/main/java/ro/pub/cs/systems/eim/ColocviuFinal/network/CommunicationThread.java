package ro.pub.cs.systems.eim.ColocviuFinal.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
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
            String firstKey = bufferedReader.readLine();
            /*
            String secondKey = bufferedReader.readLine();
            if (firstKey == null || firstKey.isEmpty() || secondKey == null || secondKey.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client");
                return;
            }
            */
            if (firstKey == null || firstKey.isEmpty()) {
                Log.e(TAG, "[COMMUNICATION THREAD] Error receiving parameters from client");
                return;
            }

            /* get cached data from server, if no data is cached, make a request */
            HashMap<String, DataModel> data = serverThread.getData();
            DataModel dataModel = null;

            if (data.containsKey(firstKey)) {

                Log.i(TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                dataModel = data.get(firstKey);

            } else {
                Log.i(TAG, "[COMMUNICATION THREAD] Getting the information from the web service...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

                switch (requestType) {
                    case POST_REQUEST:
                        HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);

                        /* create request body */
                        List<NameValuePair> params = new ArrayList<>();
                        params.add(new BasicNameValuePair("firstKey", firstKey));

                        /* set request body data type and add it to the request */
                        UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                        httpPost.setEntity(urlEncodedFormEntity);

                        /* make request and wait for response */
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        pageSourceCode = httpClient.execute(httpPost, responseHandler);

                        break;
                    case GET_REQUEST:
                        HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + firstKey);

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
                        Document document = Jsoup.parse(pageSourceCode);
                        Element element = document.child(0);

                        /*
                        Elements elements = element.getElementsByTag(Constants.SCRIPT_TAG);
                        for (Element script : elements) {
                            String scriptData = script.data();
                            if (scriptData.contains(Constants.SEARCH_KEY)) {
                                int position = scriptData.indexOf(Constants.SEARCH_KEY) + Constants.SEARCH_KEY.length();
                                scriptData = scriptData.substring(position);
                                JSONObject content = new JSONObject(scriptData);
                                JSONObject currentObservation = content.getJSONObject(Constants.CURRENT_OBSERVATION);
                                String temperature = currentObservation.getString(Constants.TEMPERATURE);
                                String windSpeed = currentObservation.getString(Constants.WIND_SPEED);
                                String condition = currentObservation.getString(Constants.CONDITION);
                                String pressure = currentObservation.getString(Constants.PRESSURE);
                                String humidity = currentObservation.getString(Constants.HUMIDITY);
                                weatherForecastInformation = new WeatherForecastInformation(
                                        temperature, windSpeed, condition, pressure, humidity
                                );
                                serverThread.setData(city, weatherForecastInformation);
                                break;
                            }
                        }
                        */

                        break;
                    case GET_REQUEST:

                        JSONObject content = new JSONObject(pageSourceCode);
                        String country = content.getString("name");
                        String countryCode = content.getString("alpha2");
                        String continent = content.getString("continent");

                        JSONObject geo = content.getJSONObject("geo");

                        Double latitude = geo.getDouble("latitude");
                        Double longitude = geo.getDouble("longitude");

                        dataModel = new DataModel(country, countryCode, continent, latitude, longitude);
                        data.put(firstKey, dataModel);
                        serverThread.setData(data);

                        /*
                        JSONArray weatherArray = content.getJSONArray(Constants.WEATHER);
                        JSONObject weather;
                        String condition = "";
                        for (int i = 0; i < weatherArray.length(); i++) {
                            weather = weatherArray.getJSONObject(i);
                            condition += weather.getString(Constants.MAIN) + " : " + weather.getString(Constants.DESCRIPTION);

                            if (i < weatherArray.length() - 1) {
                                condition += ";";
                            }
                        }

                        JSONObject main = content.getJSONObject(Constants.MAIN);
                        String temperature = main.getString(Constants.TEMP);
                        String pressure = main.getString(Constants.PRESSURE);
                        String humidity = main.getString(Constants.HUMIDITY);

                        JSONObject wind = content.getJSONObject(Constants.WIND);
                        String windSpeed = wind.getString(Constants.SPEED);

                        weatherForecastInformation = new WeatherForecastInformation(
                                temperature, windSpeed, condition, pressure, humidity
                        );
                        serverThread.setData(city, weatherForecastInformation);
                        */

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

            /*
            switch(informationType) {
                case Constants.ALL:
                    result = weatherForecastInformation.toString();
                    break;
                case Constants.TEMPERATURE:
                    result = weatherForecastInformation.getTemperature();
                    break;
                case Constants.WIND_SPEED:
                    result = weatherForecastInformation.getWindSpeed();
                    break;
                case Constants.CONDITION:
                    result = weatherForecastInformation.getCondition();
                    break;
                case Constants.HUMIDITY:
                    result = weatherForecastInformation.getHumidity();
                    break;
                case Constants.PRESSURE:
                    result = weatherForecastInformation.getPressure();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (all / temperature / wind_speed / condition / humidity / pressure)!";
            }
        */

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
