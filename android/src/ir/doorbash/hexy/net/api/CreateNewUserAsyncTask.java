package ir.doorbash.hexy.net.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import ir.doorbash.hexy.dialogs.CustomizeDialog;
import ir.doorbash.hexy.util.Constants;
import ir.doorbash.hexy.util.Shared;

/**
 * Created by Milad Doorbash on 10/21/2019.
 */
public class CreateNewUserAsyncTask extends AsyncTask<Void, Integer, String> {
    public static final String TAG = "CreateNewUserAsyncTask";

    private Listener whenDone;
    private String name;

    public interface Listener {
        void onResponse(String result);
    }

    public CreateNewUserAsyncTask(String name, Listener whenDone) {
        this.whenDone = whenDone;
        this.name = name;
    }

    protected String doInBackground(Void... voids) {
        try {
            Log.d(TAG, "sending get request to server...");
            URL url = new URL(Constants.API_ENDPOINT + "/user/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            LinkedHashMap<String, String> httpHeaders = new LinkedHashMap<>();
            httpHeaders.put("Accept", "application/json");
            httpHeaders.put("Content-Type", "application/json");
            for (Map.Entry<String, String> header : httpHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.READ_TIMEOUT);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            String body = new JSONObject().put("name", name).toString();
            System.out.println("body is " + body);
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            int status = connection.getResponseCode();
            InputStream is;
            if (status != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else
                is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            return rd.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        Log.d(TAG, "result is " + result);
        whenDone.onResponse(result);
    }
}
