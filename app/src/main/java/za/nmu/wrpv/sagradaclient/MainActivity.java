package za.nmu.wrpv.sagradaclient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import nmu.sagrada.messages.Message;
import nmu.sagrada.messages.server.DraftPool;
import nmu.sagrada.messages.server.EndGame;
import nmu.sagrada.messages.server.PlayerName;
import nmu.sagrada.messages.server.PlayerWindow;
import nmu.sagrada.messages.server.PlayerWindowState;
import nmu.sagrada.messages.server.PrivateObjectiveReceived;
import nmu.sagrada.messages.server.PublicObjectives;
import nmu.sagrada.messages.server.Round;
import nmu.sagrada.messages.server.StartGame;
import nmu.sagrada.messages.server.WindowList;

public class MainActivity extends AppCompatActivity {
    private static final String CLIENT = "client";
    private SagradaClient client;

    //Game play views
    private TextView txtRounds;
    private ImageView roundBackground;
    private ImageView objective1Background;
    private ImageView objective2Background;
    private ImageView objective3Background;
    private ImageView objective4Background;
    private ImageView imgPlayerWindow;
    private ImageView imgObjective1;
    private ImageView imgObjective2;
    private ImageView imgObjective3;
    private ImageView imgObjective4;
    private Button btnRightArrow;
    private Button btnPlayerName;
    private Button btnLeftArrow;
    private Button btnSkip;
    private ImageView grid1, grid2, grid3, grid4, grid5;
    private ImageView grid6, grid7, grid8, grid9, grid10;
    private ImageView grid11, grid12, grid13, grid14, grid15;
    private ImageView grid16, grid17, grid18, grid19, grid20;
    private ImageView imgDraftPool1, imgDraftPool2, imgDraftPool3, imgDraftPool4,
            imgDraftPool5, imgDraftPool6, imgDraftPool7, imgDraftPool8, imgDraftPool9;
    private ArrayList<ImageView> windowGrid, draftPool;
    private ImageView selectedDraftDie;


    private String[] windowChoices = new String[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_play);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //Game play views binding
        txtRounds = findViewById(R.id.txtRounds);
        roundBackground = findViewById(R.id.roundBackground);

        imgPlayerWindow = findViewById(R.id.imgPlayerWindow);
        objective1Background = findViewById(R.id.objective1Background);
        objective2Background = findViewById(R.id.objective2Background);
        objective3Background = findViewById(R.id.objective3Background);
        objective4Background = findViewById(R.id.objective4Background);
        imgObjective1 = findViewById(R.id.imgObjective1);
        imgObjective2 = findViewById(R.id.imgObjective2);
        imgObjective3 = findViewById(R.id.imgObjective3);
        imgObjective4 = findViewById(R.id.imgObjective4);
        btnRightArrow = findViewById(R.id.btnRightArrow);
        btnPlayerName = findViewById(R.id.btnPlayerName);
        btnLeftArrow = findViewById(R.id.btnLeftArrow);
        btnSkip = findViewById(R.id.btnSkip);

        grid1 = findViewById(R.id.grid1);
        grid2 = findViewById(R.id.grid2);
        grid3 = findViewById(R.id.grid3);
        grid4 = findViewById(R.id.grid4);
        grid5 = findViewById(R.id.grid5);
        grid6 = findViewById(R.id.grid6);
        grid7 = findViewById(R.id.grid7);
        grid8 = findViewById(R.id.grid8);
        grid9 = findViewById(R.id.grid9);
        grid10 = findViewById(R.id.grid10);
        grid11 = findViewById(R.id.grid11);
        grid12 = findViewById(R.id.grid12);
        grid13 = findViewById(R.id.grid13);
        grid14 = findViewById(R.id.grid14);
        grid15 = findViewById(R.id.grid15);
        grid16 = findViewById(R.id.grid16);
        grid17 = findViewById(R.id.grid17);
        grid18 = findViewById(R.id.grid18);
        grid19 = findViewById(R.id.grid19);
        grid20 = findViewById(R.id.grid20);

        windowGrid = new ArrayList<>();
        windowGrid.add(grid1);
        windowGrid.add(grid2);
        windowGrid.add(grid3);
        windowGrid.add(grid4);
        windowGrid.add(grid5);
        windowGrid.add(grid6);
        windowGrid.add(grid7);
        windowGrid.add(grid8);
        windowGrid.add(grid9);
        windowGrid.add(grid10);
        windowGrid.add(grid11);
        windowGrid.add(grid12);
        windowGrid.add(grid13);
        windowGrid.add(grid14);
        windowGrid.add(grid15);
        windowGrid.add(grid16);
        windowGrid.add(grid17);
        windowGrid.add(grid18);
        windowGrid.add(grid19);
        windowGrid.add(grid20);

        imgDraftPool1 = findViewById(R.id.imgDraftPool1);
        imgDraftPool2 = findViewById(R.id.imgDraftPool2);
        imgDraftPool3 = findViewById(R.id.imgDraftPool3);
        imgDraftPool4 = findViewById(R.id.imgDraftPool4);
        imgDraftPool5 = findViewById(R.id.imgDraftPool5);
        imgDraftPool6 = findViewById(R.id.imgDraftPool6);
        imgDraftPool7 = findViewById(R.id.imgDraftPool7);
        imgDraftPool8 = findViewById(R.id.imgDraftPool8);
        imgDraftPool9 = findViewById(R.id.imgDraftPool9);

        draftPool = new ArrayList<>();
        draftPool.add(imgDraftPool1);
        draftPool.add(imgDraftPool2);
        draftPool.add(imgDraftPool3);
        draftPool.add(imgDraftPool4);
        draftPool.add(imgDraftPool5);
        draftPool.add(imgDraftPool6);
        draftPool.add(imgDraftPool7);
        draftPool.add(imgDraftPool8);
        draftPool.add(imgDraftPool9);


        client = (SagradaClient) getIntent().getSerializableExtra(CLIENT);
        client.setMessageReceiver(
                message -> runOnUiThread(
                        () -> onMessageReceived(message)
                ));

        client.sendPrivateObjectiveRequest();
        client.sendPublicObjectiveRequest();


    }

    public void onMessageReceived(Message message) {

        if (message instanceof PrivateObjectiveReceived) {
            PrivateObjectiveReceived privateObjective = (PrivateObjectiveReceived) message;
            if (privateObjective.colour != null) {
                if (imgObjective1.getVisibility() == View.VISIBLE) {
                    imgObjective1.setImageResource(Images.getImage(privateObjective.colour));
                }
            } else {
                Toast.makeText(this, "Private objective is NULL", Toast.LENGTH_LONG).show();
            }

        } else if (message instanceof PublicObjectives) {
            PublicObjectives publicObjectives = (PublicObjectives) message;
            if (publicObjectives.publicObjectives.size() == 3) {
                imgObjective2.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(0)));
                imgObjective3.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(1)));
                imgObjective4.setImageResource(Images.getImage(publicObjectives.publicObjectives.get(2)));

            }

        } else if (message instanceof StartGame) {

            //Set die placement visual
            for (final ImageView draftDie : draftPool) {
                draftDie.setOnClickListener(v -> selectedDraftDie = draftDie);
            }
            for (int i = 0; i < windowGrid.size(); i++) {
                ImageView windowBox = windowGrid.get(i);
                onClickWindowBox(windowBox);
            }

            //Show game play views
            /*roundBackground.setVisibility(View.VISIBLE);
            txtRounds.setVisibility(View.VISIBLE);

            objective1Background.setVisibility(View.VISIBLE);
            imgObjective1.setVisibility(View.VISIBLE);
            objective2Background.setVisibility(View.VISIBLE);
            imgObjective2.setVisibility(View.VISIBLE);
            objective3Background.setVisibility(View.VISIBLE);
            imgObjective3.setVisibility(View.VISIBLE);
            objective4Background.setVisibility(View.VISIBLE);
            imgObjective4.setVisibility(View.VISIBLE);

            imgPlayerWindow.setVisibility(View.VISIBLE);

            grid1.setVisibility(View.VISIBLE);
            grid2.setVisibility(View.VISIBLE);
            grid3.setVisibility(View.VISIBLE);
            grid4.setVisibility(View.VISIBLE);
            grid5.setVisibility(View.VISIBLE);
            grid6.setVisibility(View.VISIBLE);
            grid7.setVisibility(View.VISIBLE);
            grid8.setVisibility(View.VISIBLE);
            grid9.setVisibility(View.VISIBLE);
            grid10.setVisibility(View.VISIBLE);
            grid11.setVisibility(View.VISIBLE);
            grid12.setVisibility(View.VISIBLE);
            grid13.setVisibility(View.VISIBLE);
            grid14.setVisibility(View.VISIBLE);
            grid15.setVisibility(View.VISIBLE);
            grid16.setVisibility(View.VISIBLE);
            grid17.setVisibility(View.VISIBLE);
            grid18.setVisibility(View.VISIBLE);
            grid19.setVisibility(View.VISIBLE);
            grid20.setVisibility(View.VISIBLE);

            imgDraftPool1.setVisibility(View.VISIBLE);
            imgDraftPool2.setVisibility(View.VISIBLE);
            imgDraftPool3.setVisibility(View.VISIBLE);
            imgDraftPool4.setVisibility(View.VISIBLE);
            imgDraftPool5.setVisibility(View.VISIBLE);
            imgDraftPool6.setVisibility(View.VISIBLE);
            imgDraftPool7.setVisibility(View.VISIBLE);
            imgDraftPool8.setVisibility(View.VISIBLE);
            imgDraftPool9.setVisibility(View.VISIBLE);

            btnLeftArrow.setVisibility(View.VISIBLE);
            btnPlayerName.setVisibility(View.VISIBLE);
            btnRightArrow.setVisibility(View.VISIBLE);*/

        } else if (message instanceof Round) {
            Round round = (Round) message;
            txtRounds.setText(round.round);

        } else if (message instanceof PlayerWindow) {
            PlayerWindow window = (PlayerWindow) message;
            Log.i("GGG", "Window ID ---> " + window.windowNumber);
            imgPlayerWindow.setImageResource(Images.getImage(window.windowNumber));

        } else if (message instanceof DraftPool) {
            DraftPool receivedDraftPool = (DraftPool) message;
            int i = 0;
            for (String die : receivedDraftPool.draftPool) {
                if (draftPool.get(i).getVisibility() == View.VISIBLE) {
                    draftPool.get(i).setImageResource(Images.getImage(die));
                    i++;
                }
            }

        } else if (message instanceof PlayerName) {
            String name = ((PlayerName) message).name;
            btnPlayerName.setText(name);
            if(client.getPlayerName().equals(name)){

                btnPlayerName.setVisibility(View.GONE);
                btnLeftArrow.setVisibility(View.GONE);
                btnRightArrow.setVisibility(View.GONE);

                objective1Background.setVisibility(View.VISIBLE);
                imgObjective1.setVisibility(View.VISIBLE);
                for(ImageView draftDie : draftPool)
                    draftDie.setVisibility(View.VISIBLE);
                for(ImageView gridPosition : windowGrid)
                    gridPosition.setClickable(true);
            }
            else{
                btnPlayerName.setVisibility(View.VISIBLE);
                btnLeftArrow.setVisibility(View.VISIBLE);
                btnRightArrow.setVisibility(View.VISIBLE);

                objective1Background.setVisibility(View.GONE);
                imgObjective1.setVisibility(View.GONE);


                for(ImageView draftDie : draftPool)
                    draftDie.setVisibility(View.GONE);
                for(ImageView gridPosition : windowGrid)
                    gridPosition.setClickable(false);
            }

        } else if (message instanceof PlayerWindowState) {
            PlayerWindowState windowState = (PlayerWindowState) message;
            int i = 0;
            for (String dicePosition : windowState.dicePostions) {
                if (dicePosition.equals("NULL")) {
                    windowGrid.get(i).setImageResource(R.drawable.empty);
                } else {
                    windowGrid.get(i).setImageResource(Images.getImage(dicePosition));
                }
                i++;
            }
        }
        else if( message instanceof EndGame){
            EndGame endGame = (EndGame)message;

        }
    }


    public void onClickWindowBox(final ImageView windowBox) {

        windowBox.setOnClickListener(view -> {
            if (selectedDraftDie != null) {
                windowBox.setImageDrawable(selectedDraftDie.getDrawable());
                int draftPoolSelection = -1;
                for (int i = 0; i < draftPool.size(); i++) {
                    if (selectedDraftDie.getDrawable().equals(draftPool.get(i).getDrawable())) {
                        draftPoolSelection = i;
                        break;
                    }
                }
                int windowSelection = -1;
                for (int i = 0; i < windowGrid.size(); i++) {
                    if (windowBox.equals(windowGrid.get(i)) && windowBox.getVisibility() != View.VISIBLE) {
                        windowSelection = i;
                        break;
                    }
                }
                if(draftPoolSelection == -1 || windowSelection == -1){
                    Toast.makeText(this, "Selection error", Toast.LENGTH_LONG).show();
                    draftPoolSelection = 0;
                    windowSelection = 0;
                }

                client.sendPlacedDieMessage(draftPoolSelection, windowSelection);
                windowBox.setVisibility(View.VISIBLE);
                selectedDraftDie.setVisibility(View.GONE);
                selectedDraftDie = null;
            } else {
                Toast.makeText(this, "Select die from draft pool", Toast.LENGTH_LONG).show();
            }
        });

    }


    public void onSkipClicked(View view) {
        client.sendSkipTurn();
    }

    public void onLeftArrowClicked(View view) {
    }

    public void onRightArrowClicked(View view) {

    }


}
