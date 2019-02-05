package com.example.redes.paul;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AsyncTaskActivity";
    public final static String path = "https://restleccionandroid.herokuapp.com/guardarUsuario";
    ServicioWeb servicio;
    String respuesta;
    private Button btnPlay;
    private Button btnHilo;
    private MediaPlayer mediaplayer;
    public TextView nombregui;
    public TextView correogui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Inicializamos la clase MediaPlayer asociandole el fichero de Audio
        mediaplayer = MediaPlayer.create(this, R.raw.vidaextra);
        nombregui = (TextView) findViewById(R.id.txtNombre);
        correogui = (TextView) findViewById(R.id.txtCorreo);
        //Obtenemos los tres botones de la interfaz
        btnHilo= (Button)findViewById(R.id.btnAgregar);
        btnPlay= (Button)findViewById(R.id.btnSonido);
        //Y les asignamos el controlador de eventos
        btnPlay.setOnClickListener(this);
        btnHilo.setOnClickListener(this);
    }

    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnSonido:
                //Iniciamos el audio
                mediaplayer.start();
                break;
            case R.id.btnAgregar:

                servicio = (ServicioWeb) new ServicioWeb().execute();

                break;
        }
    }

    private class ServicioWeb extends AsyncTask<Integer, Integer, String> {
        @Override
        protected String doInBackground(Integer... params) {
            return getWebServiceResponseData();
        }

        protected String getWebServiceResponseData() {
            respuesta="";
            HttpURLConnection urlConnection = null;
            Map<String, String> stringMap = new HashMap<>();
            stringMap.put("nombre", String.valueOf(nombregui.getText()));
            stringMap.put("correo", String.valueOf(correogui.getText()));

            String requestBody = Utils.buildPostParameters(stringMap);
            try {
                urlConnection = (HttpURLConnection) Utils.makeRequest("POST", path, null, "application/x-www-form-urlencoded", requestBody);
                InputStream inputStream;
                Log.d(TAG, requestBody);
                Log.d(TAG, "por aqui entra 2");
                // get stream
                if (urlConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                    inputStream = urlConnection.getInputStream();
                } else {
                    inputStream = urlConnection.getErrorStream();
                }
                // parse stream
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String temp, response = "";
                while ((temp = bufferedReader.readLine()) != null) {
                    response += temp;
                }
                respuesta="correcto";
                return respuesta;
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String respuesta) {
            super.onPostExecute(respuesta);

            Log.d(TAG, "onPostExecute");
            if (respuesta=="correcto"){

                Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "registro exitoso", Toast.LENGTH_SHORT);

                toast1.show();
            }else{
                Toast toast1 =
                        Toast.makeText(getApplicationContext(),
                                "no se registro", Toast.LENGTH_SHORT);

                toast1.show();
            }

        }
    }

    public static class Utils{
        public static String buildPostParameters(Object content) {
            String output = null;
            if ((content instanceof String) ||
                    (content instanceof JSONObject) ||
                    (content instanceof JSONArray)) {
                output = content.toString();
            } else if (content instanceof Map) {
                Uri.Builder builder = new Uri.Builder();
                HashMap hashMap = (HashMap) content;
                if (hashMap != null) {
                    Iterator entries = hashMap.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry entry = (Map.Entry) entries.next();
                        builder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
                        entries.remove(); // avoids a ConcurrentModificationException
                    }
                    output = builder.build().getEncodedQuery();
                }
            }

            return output;
        }

        public static URLConnection makeRequest(String method, String apiAddress, String accessToken, String mimeType, String requestBody) throws IOException {
            URL url = null;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(apiAddress);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(!method.equals("GET"));
                urlConnection.setRequestMethod(method);
                urlConnection.setRequestProperty("Authorization", "Bearer " + accessToken);
                urlConnection.setRequestProperty("Content-Type", mimeType);
                OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "utf-8"));
                writer.write(requestBody);
                writer.flush();
                writer.close();
                outputStream.close();

                urlConnection.connect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return urlConnection;
        }
    }











}