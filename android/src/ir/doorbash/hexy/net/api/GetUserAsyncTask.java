package ir.doorbash.hexy.net.api;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ir.doorbash.hexy.util.Constants;

/**
 * Created by Milad Doorbash on 10/21/2019.
 */
public class GetUserAsyncTask extends AsyncTask<Void, Integer, GetUserAsyncTask.Response> {
    public static final String TAG = "GetUserAsyncTask";
    private Listener whenDone;
    private String id;

    public interface Listener {
        void onResponse(Response result);
    }

    public class Response {
        public int statusCode;
        public String response;

        public Response(int status, String res) {
            this.statusCode = status;
            this.response = res;
        }
    }

    public GetUserAsyncTask(String id, Listener whenDone) {
        this.id = id;
        this.whenDone = whenDone;
    }

    protected Response doInBackground(Void... voids) {
        try {
            Log.d(TAG, "sending get request to server...");
            URL url = new URL(Constants.API_ENDPOINT + "/user/" + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
//                connection.setDoOutput(true);
            connection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            connection.setReadTimeout(Constants.CONNECT_TIMEOUT);
            connection.connect();
            int status = connection.getResponseCode();
            InputStream is;
            if (status != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else
                is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            return new Response(status, rd.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Response result) {
        Log.d(TAG, "response code is " + result.statusCode);
        Log.d(TAG, "response is " + result.response);
        this.whenDone.onResponse(result);
    }
}
