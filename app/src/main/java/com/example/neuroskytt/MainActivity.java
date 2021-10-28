package com.example.neuroskytt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.neurosky.library.NeuroSky;
import com.neurosky.library.exception.BluetoothNotEnabledException;
import com.neurosky.library.listener.ExtendedDeviceMessageListener;
import com.neurosky.library.message.enums.BrainWave;
import com.neurosky.library.message.enums.Signal;
import com.neurosky.library.message.enums.State;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.annotations.NonNull;


public class MainActivity extends AppCompatActivity {
    private final static String LOG_TAG = "NeuroSky";
    private NeuroSky neuroSky;


    TextView tvState;
    TextView tvAttention ;
    TextView tvMeditation ;
    TextView tvBlink ;
    Button btn_connect;
    Button btn_disconnect;
    Button btn_start_monitoring;
    Button btn_stop_monitoring;
    final int TIPO_CONTENTO = 1;
    final int TIPO_TRISTE = 0;
    int tipo_seleccionado = -1;
    boolean clasificar = false;
    boolean estrendada = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        neuroSky = createNeuroSky();
        tvState = (TextView)this.findViewById(R.id.tv_state);
        tvAttention = (TextView)this.findViewById(R.id.tv_attention) ;
        tvMeditation = (TextView)this.findViewById(R.id.tv_meditation) ;
        tvBlink = (TextView)this.findViewById(R.id.tv_blink) ;
        btn_connect = (Button)this.findViewById(R.id.btn_connect);
        btn_disconnect = (Button)this.findViewById(R.id.btn_disconnect);
        btn_start_monitoring = (Button)this.findViewById(R.id.btn_start_monitoring);
        btn_stop_monitoring = (Button)this.findViewById(R.id.btn_stop_monitoring);



        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    neuroSky.connect();
                } catch (BluetoothNotEnabledException e) {
                    showToast(e.getMessage(), Toast.LENGTH_SHORT);
                    Log.d(LOG_TAG, e.getMessage());
                }
            }
        });


        btn_start_monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neuroSky.start();
            }
        });

        btn_stop_monitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                neuroSky.stop();
            }
        });


    }

/*    @Override protected void onResume() {
        super.onResume();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.start();
        }
    }

    @Override protected void onPause() {
        super.onPause();
        if (neuroSky != null && neuroSky.isConnected()) {
            neuroSky.stop();
        }
    }*/

    @NonNull
    private NeuroSky createNeuroSky() {
        return new NeuroSky(new ExtendedDeviceMessageListener() {
            @Override public void onStateChange(State state) {
                handleStateChange(state);
            }

            @Override public void onSignalChange(Signal signal) {
                handleSignalChange(signal);
            }

            @Override public void onBrainWavesChange(Set<BrainWave> brainWaves) {
                handleBrainWavesChange(brainWaves);
            }
        });
    }

    private void handleStateChange(final State state) {
        /*if (neuroSky != null && state.equals(State.CONNECTED)) {
            neuroSky.start();
        }*/

        tvState.setText(state.toString());
        Log.d(LOG_TAG, state.toString());
    }

    private void handleSignalChange(final Signal signal) {
        switch (signal) {
            case ATTENTION:
                tvAttention.setText(getFormattedMessage("attention: %d", signal));
                break;
            case MEDITATION:
                tvMeditation.setText(getFormattedMessage("meditation: %d", signal));
                break;
            case BLINK:
                tvBlink.setText(getFormattedMessage("blink: %d", signal));
                break;
        }

        Log.d(LOG_TAG, String.format("%s: %d", signal.toString(), signal.getValue()));
    }

    private String getFormattedMessage(String messageFormat, Signal signal) {
        return String.format(Locale.getDefault(), messageFormat, signal.getValue());
    }

    private void handleBrainWavesChange(final Set<BrainWave> brainWaves) {





        // Request a string response from the provided URL.
        HashMap<String, String> params = new HashMap<String, String>();

        for (BrainWave brainWave : brainWaves) {
            params.put(brainWave.toString(), String.valueOf(brainWave.getValue()));
            //Log.d(LOG_TAG, String.format("brain: %s: %d", brainWave.toString(), brainWave.getValue()));
            //Log.d(LOG_TAG, String.format("brain: %s: %d", brainWave.toString(), brainWave.getValue()));
        }

        if((tipo_seleccionado!=-1 && !estrendada) || clasificar){
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://192.168.3.20:8000/polls?tipo="+tipo_seleccionado;
            if(clasificar){
                url ="http://192.168.3.20:8000/polls/clasifica";
                System.out.println(params);
            }

            JsonObjectRequest stringRequest = null;


            stringRequest = new JsonObjectRequest(Request.Method.POST, url,new JSONObject(params),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Display the first 500 characters of the response string.
                            tvAttention.setText("Response is: "+ response.toString());

                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    tvAttention.setText("That didn't work!");
                    System.out.println(error);
                }
            });

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }


    }

    public void showToast(final String msg, final int timeStyle) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }

    @OnClick(R.id.btn_disconnect) void disconnect() {
        neuroSky.disconnect();

    }

    @OnClick(R.id.btn_contento) void enviarWavesTipoContento() {
        tipo_seleccionado = TIPO_CONTENTO;

    }

    @OnClick(R.id.btn_triste) void enviarWavesTipoTriste() {
        tipo_seleccionado = TIPO_TRISTE;

    }

    @OnClick(R.id.btn_entrenar) void solicitarEntrenamiento() {
        tipo_seleccionado = -1;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("", "");

        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.3.20:8000/polls/entrena";
        JsonObjectRequest stringRequest = null;




        stringRequest = new JsonObjectRequest(Request.Method.POST, url,new JSONObject(params),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        tvAttention.setText("Response is: "+ response.toString());
                        estrendada = true;
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                tvAttention.setText("That didn't work!");
                System.out.println(error);
            }
        });

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    @OnClick(R.id.btn_clasificar) void enviarWaveIdentificar() {
        tipo_seleccionado = -1;
         clasificar = true;

    }

    @OnClick(R.id.btn_stopSending) void stopSendingWaves() {
        tipo_seleccionado = -1;
        clasificar = false;

    }


}
