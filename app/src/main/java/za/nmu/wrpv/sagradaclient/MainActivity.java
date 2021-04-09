package za.nmu.wrpv.sagradaclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import nmu.sagrada.messages.Message;
import nmu.sagrada.messages.server.*;

public class MainActivity extends AppCompatActivity {

    private SagradaClient client;

    //Connection Views
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

    //Setup Views
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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Connection views binding
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

        //Setup view binding
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


        //Set initial visibilities
        btnPlay.setVisibility(View.VISIBLE);
        btnHelp.setVisibility(View.VISIBLE);
        sagradaBanner.setVisibility(View.VISIBLE);

        //Lobby list adapter and setup
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
            for(int i = 1; i <= 25; i++) windowImages.add("WINDOW" + i);

            imgWindow1.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[0]);
                startGame();

            });
            imgWindow2.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[1]);
                startGame();

            });
            imgWindow3.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[2]);
                startGame();


            });
            imgWindow4.setOnClickListener(view1 -> {
                client.sendWindowSelected(windowChoices[3]);
                startGame();

            });
        }
    }

    private void startGame() {
        btnWindows.setVisibility(View.GONE);
        imgWindow1.setVisibility(View.GONE);
        imgWindow2.setVisibility(View.GONE);
        imgWindow3.setVisibility(View.GONE);
        imgWindow4.setVisibility(View.GONE);
        btnContinue.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);
        client.sendSetupDone();
    }

    public void onMessageReceived(Message message) {

        if (message instanceof LobbyPlayersListed) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            LobbyPlayersListed list = (LobbyPlayersListed) message;
            adapter.addAll(list.playerNames);
            adapter.notifyDataSetChanged();
        }
        else if (message instanceof GameSetup) {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
            txtWaiting.setVisibility(View.GONE);
            sagradaBanner.setVisibility(View.GONE);
            imgBack.setVisibility(View.GONE);

            btnPrivateObjective.setVisibility(View.VISIBLE);
            imgPrivateObjective.setVisibility(View.VISIBLE);
            btnContinue.setVisibility(View.VISIBLE);

            client.sendPrivateObjectiveRequest();
            continueClicked = 0;

        } else if (message instanceof PrivateObjectiveReceived) {
            PrivateObjectiveReceived privateObjective = (PrivateObjectiveReceived) message;
            if (privateObjective.colour != null) {
                if (imgPrivateObjective.getVisibility() == View.VISIBLE) {
                    imgPrivateObjective.setImageResource(getImage(privateObjective.colour));

                }
            } else {
                Toast.makeText(this, "Private objective is NULL", Toast.LENGTH_LONG).show();
            }

        } else if (message instanceof PublicObjectives) {
            PublicObjectives publicObjectives = (PublicObjectives) message;
            if (publicObjectives.publicObjectives.size() == 3) {
                if (imgPublicObjective1.getVisibility() == View.VISIBLE && imgPublicObjective2.getVisibility() == View.VISIBLE && imgPublicObjective3.getVisibility() == View.VISIBLE) {
                    imgPublicObjective1.setImageResource(getImage(publicObjectives.publicObjectives.get(0)));
                    imgPublicObjective2.setImageResource(getImage(publicObjectives.publicObjectives.get(1)));
                    imgPublicObjective3.setImageResource(getImage(publicObjectives.publicObjectives.get(2)));

                    imgObjective2.setImageResource(getImage(publicObjectives.publicObjectives.get(0)));
                    imgObjective3.setImageResource(getImage(publicObjectives.publicObjectives.get(1)));
                    imgObjective4.setImageResource(getImage(publicObjectives.publicObjectives.get(2)));
                }
            }

        } else if (message instanceof WindowList) {
            WindowList windowList = (WindowList) message;
            if (windowList.windows.size() == 4) {
                imgWindow1.setImageResource(getImage(windowList.windows.get(0)));
                imgWindow2.setImageResource(getImage(windowList.windows.get(1)));
                imgWindow3.setImageResource(getImage(windowList.windows.get(2)));
                imgWindow4.setImageResource(getImage(windowList.windows.get(3)));

                windowChoices[0] = windowList.windows.get(0);
                windowChoices[1] = windowList.windows.get(1);
                windowChoices[2] = windowList.windows.get(2);
                windowChoices[3] = windowList.windows.get(3);

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
            progressBar.setVisibility(View.GONE);

            roundBackground.setVisibility(View.VISIBLE);
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
            btnRightArrow.setVisibility(View.VISIBLE);

        } else if (message instanceof Round) {
            Round round = (Round) message;
            txtRounds.setText(round.round);

        } else if (message instanceof PlayerWindow) {
            PlayerWindow window = (PlayerWindow) message;
            Log.i("GGG", "Window ID ---> " + window.windowNumber);
            imgPlayerWindow.setImageResource(getImage(window.windowNumber));

        } else if (message instanceof DraftPool) {
            DraftPool receivedDraftPool = (DraftPool) message;
            int i = 0;
            for (String die : receivedDraftPool.draftPool) {
                if (draftPool.get(i).getVisibility() == View.VISIBLE) {
                    draftPool.get(i).setImageResource(getImage(die));
                    i++;
                }
            }

        } else if (message instanceof PlayerName) {
            String name = ((PlayerName) message).name;
            btnPlayerName.setText(name);
            if(txtName.getText().toString().equals(name)){

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
                    windowGrid.get(i).setImageResource(getImage(dicePosition));
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


    public void onBackClicked(View view) {
        btnPlay.setVisibility(View.VISIBLE);
        btnHelp.setVisibility(View.VISIBLE);
        btnConnect.setVisibility(View.GONE);
        txtName.setVisibility(View.GONE);
        txtIPAddress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        imgBack.setVisibility(View.GONE);
        txtWaiting.setVisibility(View.GONE);
        listView.setVisibility(View.GONE);
        client.disconnect();
    }


    public void onSkipClicked(View view) {
        client.sendSkipTurn();
    }

    public void onLeftArrowClicked(View view) {
    }

    public void onRightArrowClicked(View view) {

    }




    private int getImage(String id) {
        if(id == null)
            id = "";
        switch (id) {
            //Private Objectives
            case "BLUE":
                return R.drawable.blue;
            case "GREEN":
                return R.drawable.green;
            case "RED":
                return R.drawable.red;
            case "YELLOW":
                return R.drawable.yellow;
            case "PURPLE":
                return R.drawable.purple;

            //Public Objectives
            case "UPSIDE_DOWN_NUMBER_PYRAMID":
                return R.drawable.public_objective_card1;
            case "THREE_FOUR_PAIR":
                return R.drawable.public_objective_card2;
            case "DIFFERENT_NUMBERS_COLUMN":
                return R.drawable.public_objective_card3;
            case "DIFFERENT_COLOURS_COLUMN":
                return R.drawable.public_objective_card4;
            case "FIVE_SIX_PAIR":
                return R.drawable.public_objective_card5;
            case "JAGGED_DIFFERENT_COLOURS ":
                return R.drawable.public_objective_card6;
            case "DIFFERENT_COLOURS_ROW":
                return R.drawable.public_objective_card7;
            case "LIGHT_SHADES":
                return R.drawable.public_objective_card8;
            case "ONE_TWO":
                return R.drawable.public_objective_card9;
            case "DIFFERENT_NUMBERS_ROW":
                return R.drawable.public_objective_card10;

            //Windows
            case "WINDOW1":
                return R.drawable.window1;
            case "WINDOW2":
                return R.drawable.window2;
            case "WINDOW3":
                return R.drawable.window3;
            case "WINDOW4":
                return R.drawable.window4;
            case "WINDOW5":
                return R.drawable.window5;
            case "WINDOW6":
                return R.drawable.window6;
            case "WINDOW7":
                return R.drawable.window7;
            case "WINDOW8":
                return R.drawable.window8;
            case "WINDOW9":
                return R.drawable.window9;
            case "WINDOW10":
                return R.drawable.window10;
            case "WINDOW11":
                return R.drawable.window11;
            case "WINDOW12":
                return R.drawable.window12;
            case "WINDOW13":
                return R.drawable.window13;
            case "WINDOW14":
                return R.drawable.window14;
            case "WINDOW15":
                return R.drawable.window15;
            case "WINDOW16":
                return R.drawable.window16;
            case "WINDOW17":
                return R.drawable.window17;
            case "WINDOW18":
                return R.drawable.window18;
            case "WINDOW19":
                return R.drawable.window19;
            case "WINDOW20":
                return R.drawable.window20;
            case "WINDOW21":
                return R.drawable.window21;
            case "WINDOW22":
                return R.drawable.window22;
            case "WINDOW23":
                return R.drawable.window23;
            case "WINDOW24":
                return R.drawable.window24;
            case "WINDOW25":
                return R.drawable.window25;

            //Dice
            case "BLUE1":
                return R.drawable.blue_1;
            case "BLUE2":
                return R.drawable.blue_2;
            case "BLUE3":
                return R.drawable.blue_3;
            case "BLUE4":
                return R.drawable.blue_4;
            case "BLUE5":
                return R.drawable.blue_5;
            case "BLUE6":
                return R.drawable.blue_6;
            case "GREEN1":
                return R.drawable.green_1;
            case "GREEN2":
                return R.drawable.green_2;
            case "GREEN3":
                return R.drawable.green_3;
            case "GREEN4":
                return R.drawable.green_4;
            case "GREEN5":
                return R.drawable.green_5;
            case "GREEN6":
                return R.drawable.green_6;
            case "RED1":
                return R.drawable.red_1;
            case "RED2":
                return R.drawable.red_2;
            case "RED3":
                return R.drawable.red_3;
            case "RED4":
                return R.drawable.red_4;
            case "RED5":
                return R.drawable.red_5;
            case "RED6":
                return R.drawable.red_6;
            case "PURPLE1":
                return R.drawable.purple_1;
            case "PURPLE2":
                return R.drawable.purple_2;
            case "PURPLE3":
                return R.drawable.purple_3;
            case "PURPLE4":
                return R.drawable.purple_4;
            case "PURPLE5":
                return R.drawable.purple_5;
            case "PURPLE6":
                return R.drawable.purple_6;
            case "YELLOW1":
                return R.drawable.yellow_1;
            case "YELLOW2":
                return R.drawable.yellow_2;
            case "YELLOW3":
                return R.drawable.yellow_3;
            case "YELLOW4":
                return R.drawable.yellow_4;
            case "YELLOW5":
                return R.drawable.yellow_5;
            case "YELLOW6":
                return R.drawable.yellow_6;

            default:
                return R.drawable.empty;
        }

    }


}
