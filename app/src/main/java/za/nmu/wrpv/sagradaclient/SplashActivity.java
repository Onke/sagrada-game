package za.nmu.wrpv.sagradaclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;
import nmu.sagrada.messages.server.LobbyPlayersListed;

public class SplashActivity extends AppCompatActivity {

    private static final String CLIENT = "client";
    private SagradaClient client;

    private Button btnPlay;
    private Button btnHelp;
    private Button btnConnect;
    private ImageView sagradaBanner;
    private EditText txtName;
    private EditText txtIPAddress;
    private ProgressBar progressBar;
    private ImageView imgBack;
    private TextView txtWaiting;
    private ListView listView;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        btnPlay = findViewById(R.id.btnPlay);
        btnHelp = findViewById(R.id.btnHelp);
        btnConnect = findViewById(R.id.btnConnect);
        sagradaBanner = findViewById(R.id.sagradaBanner);
        txtName = findViewById(R.id.txtName);
        txtIPAddress = findViewById(R.id.txtIPAddress);
        progressBar = findViewById(R.id.progressBar);
        imgBack = findViewById(R.id.imgBack);
        txtWaiting = findViewById(R.id.txtWaiting);
        listView = findViewById(R.id.listView);


        adapter = new ArrayAdapter<>(this, R.layout.my_text_view, new ArrayList<>());
        listView.setAdapter(adapter);
    }

    public void onPlayOnlineClicked(View view) {
        btnPlay.setVisibility(View.GONE);
        btnHelp.setVisibility(View.GONE);
        btnConnect.setVisibility(View.VISIBLE);
        txtName.setVisibility(View.VISIBLE);
        txtIPAddress.setVisibility(View.VISIBLE);
        imgBack.setVisibility(View.VISIBLE);

    }

    public void onConnectClicked(View view) {
        btnConnect.setVisibility(View.GONE);
        txtName.setVisibility(View.GONE);
        txtIPAddress.setVisibility(View.GONE);

        String serverAddress = txtIPAddress.getText().toString();
        String playerName = txtName.getText().toString();

        if (!playerName.equals("")) {
            if (!serverAddress.equals("")) {
                Log.i("ChatClient", "Connecting to " + serverAddress + " as " + playerName);
                client = new SagradaClient(
                        message -> runOnUiThread(
                                () -> onMessageReceived(message)
                        ));
                client.connect(serverAddress, playerName);

            } else {
                Toast.makeText(this, "Enter Sever IP Address", Toast.LENGTH_LONG).show();
                btnConnect.setVisibility(View.VISIBLE);
                txtName.setVisibility(View.VISIBLE);
                txtIPAddress.setVisibility(View.VISIBLE);
                return;
            }

        } else {
            Toast.makeText(this, "Enter both Name and Server IP Address", Toast.LENGTH_LONG).show();
            btnConnect.setVisibility(View.VISIBLE);
            txtName.setVisibility(View.VISIBLE);
            txtIPAddress.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        txtWaiting.setVisibility(View.VISIBLE);

    }

    private void onMessageReceived(Message message) {
        if (message instanceof LobbyPlayersListed) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            LobbyPlayersListed list = (LobbyPlayersListed) message;
            adapter.addAll(list.playerNames);
            adapter.notifyDataSetChanged();

            if(list.playerNames.size() == 4){
                Intent intent = new Intent(this, ObjectivesActivity.class);
                intent.putExtra(CLIENT, client);
                startActivity(intent);
            }

        }
    }
}