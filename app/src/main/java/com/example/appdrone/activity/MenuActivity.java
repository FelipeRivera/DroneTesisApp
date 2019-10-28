package com.example.appdrone.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.appdrone.R;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;


public class MenuActivity extends AppCompatActivity {

    private Button automatico;
    private Button manual;
    private Intent intents;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        intents =null;
        automatico = findViewById(R.id.automatico);
        manual = findViewById(R.id.manual);


        Intent intent = getIntent();
        final ARDiscoveryDeviceService service = intent.getParcelableExtra(ReconocimientoActivity.EXTRA_DEVICE_SERVICE);

        // Eventos
        manual.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                intents = new Intent(MenuActivity.this, BebopActivity.class);
                intents.putExtra(ReconocimientoActivity.EXTRA_DEVICE_SERVICE, service);
                startActivity(intents);
            }
        });

        automatico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intents = new Intent(MenuActivity.this, AutoActivity.class);
                intents.putExtra(ReconocimientoActivity.EXTRA_DEVICE_SERVICE, service);
                startActivity(intents);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
}
