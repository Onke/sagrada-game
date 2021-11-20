package za.nmu.wrpv.sagradaclient;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;
import nmu.sagrada.messages.server.PrivateObjectiveReceived;
import nmu.sagrada.messages.server.PublicObjectives;
import nmu.sagrada.messages.server.WindowList;

public class ObjectivesActivity extends AppCompatActivity {

    private static final String CLIENT = "client";
    private SagradaClient client;

    private Button btnPrivateObjective;
    private Button btnWindows;
    private Button btnPublicObjective;
    private Button btnContinue;
    private ImageView imgPrivateObjective;
    private ImageView imgPublicObjective1;
    private ImageView imgPublicObjective2;
    private ImageView imgPublicObjective3;
    private ImageView imgWindow1;
    private ImageView imgWindow2;
    private ImageView imgWindow3;
    private ImageView imgWindow4;
    private int continueClicked;

    private String[] windowChoices = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objectives);


        btnPrivateObjective = findViewById(R.id.btnPrivateObjective);
        btnWindows = findViewById(R.id.btnWindows);
        btnPublicObjective = findViewById(R.id.btnPublicObjective);
        btnContinue = findViewById(R.id.btnContinue);
        imgPrivateObjective = findViewById(R.id.imgPrivateObjective);
        imgPublicObjective1 = findViewById(R.id.imgPublicObjective1);
        imgPublicObjective2 = findViewById(R.id.imgPublicObjective2);
        imgPublicObjective3 = findViewById(R.id.imgPublicObjective3);
        imgWindow1 = findViewById(R.id.imgWindow1);
        imgWindow2 = findViewById(R.id.imgWindow2);
        imgWindow3 = findViewById(R.id.imgWindow3);
        imgWindow4 = findViewById(R.id.imgWindow4);

        btnPrivateObjective.setVisibility(View.VISIBLE);
        imgPrivateObjective.setVisibility(View.VISIBLE);
        btnContinue.setVisibility(View.VISIBLE);
        continueClicked = 0;

        client = (SagradaClient) getIntent().getSerializableExtra(CLIENT);
        client.setMessageReceiver(
                message -> runOnUiThread(
                        () -> onMessageReceived(message)
                ));

        client.sendPrivateObjectiveRequest();
    }

    private void onMessageReceived(Message message) {
        if (message instanceof PrivateObjectiveReceived) {
            PrivateObjectiveReceived privateObjective = (PrivateObjectiveReceived) message;
            if (privateObjective.colour != null) {
                if (imgPrivateObjective.getVisibility() == View.VISIBLE) {
                    imgPrivateObjective.setImageResource(Images.getImage(privateObjective.colour));
                }
            } else {
                Toast.makeText(this, "Private objective is NULL", Toast.LENGTH_LONG).show();
            }

        } else if (message instanceof PublicObjectives) {
            PublicObjectives publicObjectives = (PublicObjectives) message;
            if (publicObjectives.publicObjectives.size() == 3) {
                if (imgPublicObjective1.getVisibility() == View.VISIBLE && imgPublicObjective2.getVisibility() == View.VISIBLE && imgPublicObjective3.getVisibility() == View.VISIBLE) {
                    imgPublicObjective1.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(0)));
                    imgPublicObjective2.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(1)));
                    imgPublicObjective3.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(2)));

                }
            }

        } else if (message instanceof WindowList) {
            WindowList windowList = (WindowList) message;
            if (windowList.windows.size() == 4) {
                imgWindow1.setImageResource(Images.getImage(windowList.windows.get(0)));
                imgWindow2.setImageResource(Images.getImage(windowList.windows.get(1)));
                imgWindow3.setImageResource(Images.getImage(windowList.windows.get(2)));
                imgWindow4.setImageResource(Images.getImage(windowList.windows.get(3)));

                windowChoices[0] = windowList.windows.get(0);
                windowChoices[1] = windowList.windows.get(1);
                windowChoices[2] = windowList.windows.get(2);
                windowChoices[3] = windowList.windows.get(3);

            }

        }


    }


    public void onContinueClicked(View view) {
        //SetVisibility
        if (continueClicked == 0) {
            btnPrivateObjective.setVisibility(View.GONE);
            imgPrivateObjective.setVisibility(View.GONE);

            imgPublicObjective1.setVisibility(View.VISIBLE);
            imgPublicObjective2.setVisibility(View.VISIBLE);
            imgPublicObjective3.setVisibility(View.VISIBLE);
            btnPublicObjective.setVisibility(View.VISIBLE);

            client.sendPublicObjectiveRequest();
            continueClicked++;
        } else if (continueClicked == 1) {
            btnPublicObjective.setVisibility(View.GONE);
            imgPublicObjective1.setVisibility(View.GONE);
            imgPublicObjective2.setVisibility(View.GONE);
            imgPublicObjective3.setVisibility(View.GONE);
            btnContinue.setVisibility(View.GONE);

            btnWindows.setVisibility(View.VISIBLE);
            imgWindow1.setVisibility(View.VISIBLE);
            imgWindow2.setVisibility(View.VISIBLE);
            imgWindow3.setVisibility(View.VISIBLE);
            imgWindow4.setVisibility(View.VISIBLE);

            client.sendWindowListRequest();
            continueClicked++;

            ArrayList<String> windowImages = new ArrayList<>();
            for (int i = 1; i <= 25; i++) windowImages.add("WINDOW" + i);

            imgWindow1.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[0]);
            });
            imgWindow2.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[1]);

            });
            imgWindow3.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[2]);


            });
            imgWindow4.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[3]);

            });

            Intent intent = new Intent(this, ObjectivesActivity.class);
            intent.putExtra(CLIENT, client);
            startActivity(intent);
        }
    }
}