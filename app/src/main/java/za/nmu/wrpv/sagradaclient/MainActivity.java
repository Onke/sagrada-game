package za.nmu.wrpv.sagradaclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.List;

import nmu.sagrada.core.Colour;
import nmu.sagrada.core.ScoreBreakdown;
import nmu.sagrada.core.ScoreCalculator;
import nmu.sagrada.core.WindowCard;
import nmu.sagrada.protocol.Difficulty;
import nmu.sagrada.protocol.EndGame;
import nmu.sagrada.protocol.ErrorMessage;
import nmu.sagrada.protocol.GameMode;
import nmu.sagrada.protocol.GameSnapshot;
import nmu.sagrada.protocol.Joined;
import nmu.sagrada.protocol.LobbyState;
import nmu.sagrada.protocol.Message;
import nmu.sagrada.protocol.MoveRejected;
import nmu.sagrada.protocol.PlayerScore;
import nmu.sagrada.protocol.PlayerSnapshot;
import nmu.sagrada.protocol.PlayerSummary;
import nmu.sagrada.protocol.SetupState;
import nmu.sagrada.protocol.TurnPaused;

public class MainActivity extends AppCompatActivity implements SagradaClient.Listener {

    private static final int SCREEN_HOME = 0;
    private static final int SCREEN_CONNECT = 1;
    private static final int SCREEN_LOBBY = 2;
    private static final int SCREEN_OBJECTIVES = 3;
    private static final int SCREEN_WINDOWS = 4;
    private static final int SCREEN_GAME = 5;
    private static final int SCREEN_ROUND = 6;
    private static final int SCREEN_STANDINGS = 7;

    private static final int MAX_DRAFT_DICE = 9;

    /**
     * Deliberate pacing. Snapshots arrive faster than a person can read them, so a landed die holds
     * a ring instead of flickering past. Neither delay gates input on your own turn — they only
     * slow down things you are watching, not doing.
     */
    private static final long PLACEMENT_FLASH_MS = 900L;
    /** How long a bot's finished move stays on screen before auto-advance releases it. */
    private static final long AUTO_ADVANCE_MS = 1600L;

    private static final String PREFS = "sagrada";
    private static final String PREF_AUTO_NEXT = "autoNext";

    private final Handler pacing = new Handler(Looper.getMainLooper());

    private SagradaClient client;
    private GameMode selectedMode = GameMode.VS_BOTS;
    private int botCount = 3;
    private Difficulty difficulty = Difficulty.MEDIUM;
    private boolean autoAdvance;

    private String selfPlayerId;
    private boolean host;
    private SetupState setup;
    private GameSnapshot snapshot;

    private String displayedPlayerId;
    private String pendingWindowId;
    private int selectedDraftIndex = -1;
    private boolean pendingMove;
    private boolean wasMyTurn;
    /** The server re-broadcasts the lobby right after dealing a rematch; that one is not a reset. */
    private boolean setupJustStarted;

    private String flashPlayerId;
    private int flashCellIndex = -1;
    /** Non-null while a bot's finished turn waits for the player to tap Next. */
    private String pausedBotId;
    private boolean pausedBotPlaced;
    /** True while the round-transition screen is holding, before the player starts the round. */
    private boolean awaitingRoundStart;

    private ViewFlipper flipper;
    private View optionBots, optionLan, groupBotCount;
    private Button btnBots1, btnBots2, btnBots3, btnEasy, btnMedium, btnHard, btnConnect,
            btnContinue, btnConfirmWindow, btnSkip, btnNext, btnStartRound, btnPlayAgain,
            btnLeftArrow, btnRightArrow;
    private EditText txtName, txtIPAddress;
    private TextView txtWaiting, txtRounds, txtTurn, txtScore, txtViewing, txtDifficultyDetail,
            txtPrivateObjectiveTitle, txtPrivateObjectiveBody,
            txtRoundClosing, txtRoundHeadline, txtRoundDetail, txtRoundStarter,
            txtWinner, txtOutcome, txtRematchHint;
    private ImageView imgPrivateObjective;
    private LinearLayout lobbyPlayers, publicObjectiveCards, windowChoices, objectiveStrip,
            standingsList;
    private GridLayout draftPoolGrid, roundDraftPool;
    private WindowGridView windowGrid;
    private SwitchCompat switchAutoNext;

    private final List<DieView> draftDice = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        autoAdvance = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_AUTO_NEXT, false);
        bindViews();
        flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.screen_in));
        flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.screen_out));
        wireListeners();
        buildDraftPool();
        applyModeSelection();
        showScreen(SCREEN_HOME);
    }

    private void bindViews() {
        flipper = findViewById(R.id.flipper);
        optionBots = findViewById(R.id.optionBots);
        optionLan = findViewById(R.id.optionLan);
        groupBotCount = findViewById(R.id.groupBotCount);
        btnBots1 = findViewById(R.id.btnBots1);
        btnBots2 = findViewById(R.id.btnBots2);
        btnBots3 = findViewById(R.id.btnBots3);
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);
        txtDifficultyDetail = findViewById(R.id.txtDifficultyDetail);
        btnConnect = findViewById(R.id.btnConnect);
        btnContinue = findViewById(R.id.btnContinue);
        btnConfirmWindow = findViewById(R.id.btnConfirmWindow);
        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);
        btnLeftArrow = findViewById(R.id.btnLeftArrow);
        btnRightArrow = findViewById(R.id.btnRightArrow);
        txtName = findViewById(R.id.txtName);
        txtIPAddress = findViewById(R.id.txtIPAddress);
        txtWaiting = findViewById(R.id.txtWaiting);
        txtRounds = findViewById(R.id.txtRounds);
        txtTurn = findViewById(R.id.txtTurn);
        txtScore = findViewById(R.id.txtScore);
        txtViewing = findViewById(R.id.txtViewing);
        txtPrivateObjectiveTitle = findViewById(R.id.txtPrivateObjectiveTitle);
        txtPrivateObjectiveBody = findViewById(R.id.txtPrivateObjectiveBody);
        imgPrivateObjective = findViewById(R.id.imgPrivateObjective);
        lobbyPlayers = findViewById(R.id.lobbyPlayers);
        publicObjectiveCards = findViewById(R.id.publicObjectiveCards);
        windowChoices = findViewById(R.id.windowChoices);
        objectiveStrip = findViewById(R.id.objectiveStrip);
        draftPoolGrid = findViewById(R.id.draftPool);
        windowGrid = findViewById(R.id.windowGrid);
        switchAutoNext = findViewById(R.id.switchAutoNext);
        btnStartRound = findViewById(R.id.btnStartRound);
        roundDraftPool = findViewById(R.id.roundDraftPool);
        txtRoundClosing = findViewById(R.id.txtRoundClosing);
        txtRoundHeadline = findViewById(R.id.txtRoundHeadline);
        txtRoundDetail = findViewById(R.id.txtRoundDetail);
        txtRoundStarter = findViewById(R.id.txtRoundStarter);
        txtWinner = findViewById(R.id.txtWinner);
        txtOutcome = findViewById(R.id.txtOutcome);
        txtRematchHint = findViewById(R.id.txtRematchHint);
        standingsList = findViewById(R.id.standingsList);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
    }

    private void wireListeners() {
        findViewById(R.id.btnPlay).setOnClickListener(view -> showScreen(SCREEN_CONNECT));
        findViewById(R.id.btnHelp).setOnClickListener(view -> openHelp());
        findViewById(R.id.btnHomePractice).setOnClickListener(view ->
                startActivity(new Intent(this, PracticeActivity.class)));
        findViewById(R.id.btnConnectBack).setOnClickListener(view -> showScreen(SCREEN_HOME));
        findViewById(R.id.btnLobbyLeave).setOnClickListener(view -> leaveMatch());
        findViewById(R.id.btnGameHelp).setOnClickListener(view -> openHelp());
        findViewById(R.id.btnGameLeave).setOnClickListener(view -> confirmLeave());

        optionBots.setOnClickListener(view -> { selectedMode = GameMode.VS_BOTS; applyModeSelection(); });
        optionLan.setOnClickListener(view -> { selectedMode = GameMode.LAN_MULTIPLAYER; applyModeSelection(); });
        btnBots1.setOnClickListener(view -> { botCount = 1; applyModeSelection(); });
        btnBots2.setOnClickListener(view -> { botCount = 2; applyModeSelection(); });
        btnBots3.setOnClickListener(view -> { botCount = 3; applyModeSelection(); });
        btnEasy.setOnClickListener(view -> { difficulty = Difficulty.EASY; applyModeSelection(); });
        btnMedium.setOnClickListener(view -> { difficulty = Difficulty.MEDIUM; applyModeSelection(); });
        btnHard.setOnClickListener(view -> { difficulty = Difficulty.HARD; applyModeSelection(); });

        btnConnect.setOnClickListener(view -> connect());
        btnContinue.setOnClickListener(view -> showWindowChoices());
        btnConfirmWindow.setOnClickListener(view -> confirmWindow());
        btnSkip.setOnClickListener(view -> skipTurn());
        btnNext.setOnClickListener(view -> continueTurn());
        btnStartRound.setOnClickListener(view -> startRound());
        btnPlayAgain.setOnClickListener(view -> requestRematch());
        findViewById(R.id.btnExitMatch).setOnClickListener(view -> leaveMatch());
        switchAutoNext.setChecked(autoAdvance);
        switchAutoNext.setOnCheckedChangeListener((button, checked) -> setAutoAdvance(checked));
        btnLeftArrow.setOnClickListener(view -> browsePlayer(-1));
        btnRightArrow.setOnClickListener(view -> browsePlayer(1));
        windowGrid.setOnCellClickListener(this::onWindowCellClicked);
    }

    private void buildDraftPool() {
        int size = dp(52);
        int margin = dp(4);
        for (int i = 0; i < MAX_DRAFT_DICE; i++) {
            DieView die = new DieView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            die.setLayoutParams(params);
            final int index = i;
            die.setOnClickListener(view -> selectDraftDie(index));
            draftPoolGrid.addView(die);
            draftDice.add(die);
        }
    }

    // ---------------------------------------------------------------- screen routing

    private void showScreen(int screen) {
        if (flipper.getDisplayedChild() != screen) flipper.setDisplayedChild(screen);
    }

    private int currentScreen() {
        return flipper.getDisplayedChild();
    }

    private void openHelp() {
        startActivity(new Intent(this, HowToPlayActivity.class));
    }

    // ---------------------------------------------------------------- connect

    private void applyModeSelection() {
        boolean bots = selectedMode == GameMode.VS_BOTS;
        optionBots.setSelected(bots);
        optionLan.setSelected(!bots);
        groupBotCount.setVisibility(bots ? View.VISIBLE : View.GONE);
        btnBots1.setSelected(botCount == 1);
        btnBots2.setSelected(botCount == 2);
        btnBots3.setSelected(botCount == 3);
        btnEasy.setSelected(difficulty == Difficulty.EASY);
        btnMedium.setSelected(difficulty == Difficulty.MEDIUM);
        btnHard.setSelected(difficulty == Difficulty.HARD);
        txtDifficultyDetail.setText(difficultyDetail());
    }

    private int difficultyDetail() {
        switch (difficulty) {
            case EASY: return R.string.difficulty_easy_detail;
            case HARD: return R.string.difficulty_hard_detail;
            default: return R.string.difficulty_medium_detail;
        }
    }

    private void setAutoAdvance(boolean enabled) {
        autoAdvance = enabled;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_AUTO_NEXT, enabled).apply();
        // Flipping it on mid-pause should release the turn already waiting, not just the next one.
        if (enabled && pausedBotId != null) scheduleAutoAdvance();
        else if (!enabled) pacing.removeCallbacks(autoContinue);
    }

    private void connect() {
        String name = txtName.getText().toString().trim();
        String address = txtIPAddress.getText().toString().trim();
        if (name.isEmpty()) {
            txtName.setError(getString(R.string.enter_name));
            txtName.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            txtIPAddress.setError(getString(R.string.enter_address));
            txtIPAddress.requestFocus();
            return;
        }
        hideKeyboard();
        resetMatchState();
        lobbyPlayers.removeAllViews();
        txtWaiting.setText(getString(R.string.connecting, address));
        showScreen(SCREEN_LOBBY);
        client = new SagradaClient(this);
        client.connect(address, name, selectedMode,
                selectedMode == GameMode.VS_BOTS ? botCount : 0, difficulty);
    }

    private void hideKeyboard() {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focus = getCurrentFocus();
        if (manager != null && focus != null) {
            manager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }

    // ---------------------------------------------------------------- networking

    @Override
    public void onMessage(Message message) {
        runOnUiThread(() -> handleMessage(message));
    }

    @Override
    public void onConnectionError(String message) {
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.connection_error)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> goHome())
                    .show();
        });
    }

    private void handleMessage(Message message) {
        if (message instanceof Joined) {
            selfPlayerId = ((Joined) message).playerId;
            host = ((Joined) message).host;
        } else if (message instanceof LobbyState) {
            handleLobby((LobbyState) message);
        } else if (message instanceof SetupState) {
            handleSetup((SetupState) message);
        } else if (message instanceof GameSnapshot) {
            renderSnapshot((GameSnapshot) message);
        } else if (message instanceof TurnPaused) {
            handleTurnPaused((TurnPaused) message);
        } else if (message instanceof MoveRejected) {
            MoveRejected rejected = (MoveRejected) message;
            pendingMove = false;
            if (rejected.snapshot != null) renderSnapshot(rejected.snapshot);
            Toast.makeText(this, rejected.message, Toast.LENGTH_LONG).show();
        } else if (message instanceof EndGame) {
            showStandings((EndGame) message);
        } else if (message instanceof ErrorMessage) {
            showServerError((ErrorMessage) message);
        }
    }

    private void handleLobby(LobbyState lobby) {
        renderLobby(lobby);
        if (setupJustStarted) {
            // The lobby broadcast that trails a rematch deal — stay on the setup screens.
            setupJustStarted = false;
            return;
        }
        if (currentScreen() == SCREEN_OBJECTIVES || currentScreen() == SCREEN_WINDOWS
                || currentScreen() == SCREEN_GAME || currentScreen() == SCREEN_ROUND) {
            // A LAN player dropped and the server tore the match down; follow it back to the lobby.
            pacing.removeCallbacksAndMessages(null);
            setup = null;
            snapshot = null;
            pausedBotId = null;
            awaitingRoundStart = false;
            showScreen(SCREEN_LOBBY);
        }
    }

    private void renderLobby(LobbyState lobby) {
        lobbyPlayers.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (PlayerSummary player : lobby.players) {
            View row = inflater.inflate(R.layout.item_lobby_player, lobbyPlayers, false);
            ((TextView) row.findViewById(R.id.playerName)).setText(player.name);
            TextView tag = row.findViewById(R.id.playerTag);
            if (player.bot) tag.setText(R.string.bot_suffix);
            else if (player.id.equals(selfPlayerId)) tag.setText(R.string.you_suffix);
            else tag.setText("");
            lobbyPlayers.addView(row);
        }
        txtWaiting.setText(lobby.mode == GameMode.LAN_MULTIPLAYER
                ? getString(R.string.waiting_players, lobby.players.size(), lobby.requiredHumanPlayers)
                : getString(R.string.preparing_bot_match));
    }

    // ---------------------------------------------------------------- setup

    private void handleSetup(SetupState state) {
        pacing.removeCallbacksAndMessages(null);
        flashPlayerId = null;
        flashCellIndex = -1;
        pausedBotId = null;
        awaitingRoundStart = false;
        objectiveStrip.removeAllViews();
        setup = state;
        snapshot = null;
        pendingWindowId = null;
        pendingMove = false;
        wasMyTurn = false;
        displayedPlayerId = null;
        setupJustStarted = true;
        renderObjectives();
        showScreen(SCREEN_OBJECTIVES);
    }

    private void renderObjectives() {
        Colour privateColour = Objectives.privateColour(setup.privateObjective);
        imgPrivateObjective.setImageResource(privateColourCard(privateColour));
        txtPrivateObjectiveTitle.setText(Objectives.privateTitle(this, setup.privateObjective));
        txtPrivateObjectiveBody.setText(Objectives.privateBody(this, setup.privateObjective));

        publicObjectiveCards.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String objective : setup.publicObjectives) {
            View card = inflater.inflate(R.layout.item_objective_card, publicObjectiveCards, false);
            ((ImageView) card.findViewById(R.id.objectiveImage)).setImageResource(Objectives.image(objective));
            ((TextView) card.findViewById(R.id.objectiveTitle)).setText(Objectives.title(this, objective));
            ((TextView) card.findViewById(R.id.objectiveBody)).setText(Objectives.body(this, objective));
            ((TextView) card.findViewById(R.id.objectivePoints))
                    .setText(getString(R.string.objective_points, Objectives.points(this, objective)));
            publicObjectiveCards.addView(card);
        }
    }

    private int privateColourCard(Colour colour) {
        if (colour == null) return R.drawable.empty;
        switch (colour) {
            case BLUE: return R.drawable.blue;
            case GREEN: return R.drawable.green;
            case PURPLE: return R.drawable.purple;
            case RED: return R.drawable.red;
            case YELLOW: return R.drawable.yellow;
            default: return R.drawable.empty;
        }
    }

    private void showWindowChoices() {
        if (setup == null) return;
        pendingWindowId = null;
        btnConfirmWindow.setEnabled(false);
        windowChoices.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String windowId : setup.windowChoices) {
            View row = inflater.inflate(R.layout.item_window_choice, windowChoices, false);
            WindowCard card = Windows.pattern(windowId);
            WindowGridView grid = row.findViewById(R.id.choiceGrid);
            grid.setWindow(card);
            ((TextView) row.findViewById(R.id.choiceName)).setText(prettyWindowName(windowId));
            TextView stats = row.findViewById(R.id.choiceStats);
            if (card != null) {
                stats.setText(getString(R.string.window_stats, Windows.countColourCells(card),
                        Windows.countValueCells(card), Windows.countOpenCells(card)));
            }
            row.setOnClickListener(view -> selectWindowChoice(windowId));
            windowChoices.addView(row);
        }
        showScreen(SCREEN_WINDOWS);
    }

    private void selectWindowChoice(String windowId) {
        pendingWindowId = windowId;
        for (int i = 0; i < windowChoices.getChildCount(); i++) {
            windowChoices.getChildAt(i).setSelected(setup.windowChoices.get(i).equals(windowId));
        }
        btnConfirmWindow.setEnabled(true);
    }

    private void confirmWindow() {
        if (setup == null || pendingWindowId == null || client == null) {
            Toast.makeText(this, R.string.select_window_first, Toast.LENGTH_SHORT).show();
            return;
        }
        txtWaiting.setText(R.string.waiting_for_setup);
        showScreen(SCREEN_LOBBY);
        client.selectWindow(pendingWindowId);
    }

    private String prettyWindowName(String windowId) {
        return windowId != null && windowId.startsWith("WINDOW")
                ? "Window " + windowId.substring(6) : String.valueOf(windowId);
    }

    // ---------------------------------------------------------------- play

    private void renderSnapshot(GameSnapshot update) {
        GameSnapshot previous = snapshot;
        snapshot = update;
        pendingMove = false;
        selectedDraftIndex = -1;
        // A TurnPaused for this move, if any, arrives immediately after this snapshot.
        pausedBotId = null;

        boolean myTurn = isMyTurn();
        if (displayedPlayerId == null || playerById(displayedPlayerId) == null) {
            displayedPlayerId = selfPlayerId;
        }
        if (myTurn && !wasMyTurn) displayedPlayerId = selfPlayerId;
        wasMyTurn = myTurn;

        notePlacement(previous, update);

        boolean newRound = previous == null || previous.round != update.round;
        if (newRound) {
            awaitingRoundStart = true;
            renderRoundTransition(previous, update);
        }
        showScreen(awaitingRoundStart ? SCREEN_ROUND : SCREEN_GAME);

        txtRounds.setText(getString(R.string.round_of, update.round));
        renderObjectiveStrip();
        renderDraftPool();
        renderDisplayedPlayer();
        renderTurnBanner();
        renderActions();
        renderScore();

        boolean multiplePlayers = update.players.size() > 1;
        btnLeftArrow.setVisibility(multiplePlayers ? View.VISIBLE : View.INVISIBLE);
        btnRightArrow.setVisibility(multiplePlayers ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Finds the die that just landed and holds a ring on it, so a bot's move is something you see
     * happen rather than a board that silently differs from the one you were looking at.
     */
    private void notePlacement(GameSnapshot previous, GameSnapshot update) {
        if (previous == null || previous.round != update.round) return;
        for (PlayerSnapshot after : update.players) {
            PlayerSnapshot before = findIn(previous, after.id);
            if (before == null) continue;
            int placed = singleNewDie(before.dicePositions, after.dicePositions);
            if (placed < 0) continue;
            flashPlayerId = after.id;
            flashCellIndex = placed;
            pacing.removeCallbacks(clearFlash);
            pacing.postDelayed(clearFlash, PLACEMENT_FLASH_MS);
            return;
        }
    }

    private final Runnable clearFlash = new Runnable() {
        @Override public void run() {
            flashPlayerId = null;
            flashCellIndex = -1;
            if (snapshot != null) renderDisplayedPlayer();
        }
    };

    private PlayerSnapshot findIn(GameSnapshot source, String id) {
        for (PlayerSnapshot player : source.players) if (player.id.equals(id)) return player;
        return null;
    }

    /** Returns the index of the one newly filled cell, or -1 if zero or several changed. */
    private int singleNewDie(List<String> before, List<String> after) {
        int found = -1;
        int limit = Math.min(before.size(), after.size());
        for (int i = 0; i < limit; i++) {
            if (!Dice.isDie(before.get(i)) && Dice.isDie(after.get(i))) {
                if (found >= 0) return -1;
                found = i;
            }
        }
        return found;
    }

    /**
     * A round boundary is the one moment the whole board changes at once — leftover dice are swept
     * away and a new pool is rolled. It gets its own screen so it cannot be mistaken for a normal
     * turn, and it waits for the player rather than passing on a timer.
     */
    private void renderRoundTransition(GameSnapshot previous, GameSnapshot update) {
        boolean firstRound = previous == null;
        txtRoundClosing.setText(firstRound ? getString(R.string.match_ready)
                : getString(R.string.round_complete, previous.round));
        txtRoundHeadline.setText(getString(R.string.round_headline, update.round));
        txtRoundDetail.setText(R.string.round_detail);

        roundDraftPool.removeAllViews();
        int size = dp(52);
        int margin = dp(4);
        for (String dieId : update.draftPool) {
            DieView die = new DieView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            die.setLayoutParams(params);
            die.setDie(dieId);
            roundDraftPool.addView(die);
        }

        PlayerSnapshot starter = null;
        for (PlayerSnapshot player : update.players) {
            if (player.id.equals(update.activePlayerId)) starter = player;
        }
        if (starter == null) {
            txtRoundStarter.setText("");
        } else if (starter.id.equals(selfPlayerId)) {
            txtRoundStarter.setText(R.string.round_starter_you);
        } else {
            txtRoundStarter.setText(getString(R.string.round_starter_other, starter.name));
        }
    }

    private void startRound() {
        awaitingRoundStart = false;
        showScreen(SCREEN_GAME);
        // A bot may have finished its move behind the transition screen; honour auto-advance now.
        if (pausedBotId != null && autoAdvance) scheduleAutoAdvance();
    }

    private void renderObjectiveStrip() {
        if (setup == null || objectiveStrip.getChildCount() > 0) return;
        LayoutInflater inflater = LayoutInflater.from(this);
        addObjectiveChip(inflater, privateColourCard(Objectives.privateColour(setup.privateObjective)),
                Objectives.privateTitle(this, setup.privateObjective),
                Objectives.privateTitle(this, setup.privateObjective),
                Objectives.privateBody(this, setup.privateObjective), null);
        for (String objective : setup.publicObjectives) {
            addObjectiveChip(inflater, Objectives.image(objective), Objectives.title(this, objective),
                    Objectives.title(this, objective), Objectives.body(this, objective),
                    Objectives.points(this, objective));
        }
    }

    private void addObjectiveChip(LayoutInflater inflater, int image, String label,
                                  String title, String body, String points) {
        View chip = inflater.inflate(R.layout.item_objective_chip, objectiveStrip, false);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) chip.getLayoutParams();
        params.weight = 1;
        params.setMarginEnd(objectiveStrip.getChildCount() < 3 ? dp(6) : 0);
        chip.setLayoutParams(params);
        ((ImageView) chip.findViewById(R.id.chipImage)).setImageResource(image);
        ((TextView) chip.findViewById(R.id.chipLabel)).setText(label);
        chip.setOnClickListener(view -> showObjectiveDetail(title, body, points));
        objectiveStrip.addView(chip);
    }

    private void showObjectiveDetail(String title, String body, String points) {
        String message = points == null || points.isEmpty()
                ? body : body + "\n\n" + getString(R.string.objective_points, points);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void renderDraftPool() {
        boolean myTurn = isMyTurn();
        for (int i = 0; i < draftDice.size(); i++) {
            DieView die = draftDice.get(i);
            if (snapshot != null && i < snapshot.draftPool.size()) {
                die.setVisibility(View.VISIBLE);
                die.setDie(snapshot.draftPool.get(i));
                die.setChosen(i == selectedDraftIndex);
                die.setPlayable(myTurn);
                die.setEnabled(myTurn);
            } else {
                die.setVisibility(View.GONE);
            }
        }
    }

    /**
     * A held bot turn swaps Pass for Next, so the bottom of the screen always offers exactly the
     * one action that makes sense right now.
     */
    private void renderActions() {
        boolean paused = pausedBotId != null;
        boolean botThinking = !paused && snapshot != null && !isMyTurn() && activeIsBot();
        // While a bot deliberates, Next is present but dead — the player can see what is coming and
        // that it has not happened yet.
        btnNext.setVisibility(paused || botThinking ? View.VISIBLE : View.GONE);
        btnNext.setEnabled(paused);
        btnSkip.setVisibility(paused || botThinking ? View.GONE : View.VISIBLE);
        btnSkip.setEnabled(isMyTurn() && !pendingMove);
        switchAutoNext.setVisibility(paused || botThinking ? View.VISIBLE : View.GONE);
    }

    private boolean activeIsBot() {
        PlayerSnapshot active = playerById(snapshot.activePlayerId);
        return active != null && active.bot;
    }

    private void handleTurnPaused(TurnPaused paused) {
        pausedBotId = paused.playerId;
        pausedBotPlaced = paused.placed;
        // Hold the ring on the die that just landed until the player moves on, and show them the
        // window it landed in — otherwise pausing tells them nothing.
        pacing.removeCallbacks(clearFlash);
        displayedPlayerId = paused.playerId;
        renderDisplayedPlayer();
        renderTurnBanner();
        renderActions();
        if (autoAdvance && !awaitingRoundStart) scheduleAutoAdvance();
    }

    private void scheduleAutoAdvance() {
        pacing.removeCallbacks(autoContinue);
        pacing.postDelayed(autoContinue, AUTO_ADVANCE_MS);
    }

    private final Runnable autoContinue = new Runnable() {
        @Override public void run() {
            if (autoAdvance) continueTurn();
        }
    };

    private void continueTurn() {
        if (client == null || pausedBotId == null) return;
        pacing.removeCallbacks(autoContinue);
        pausedBotId = null;
        flashPlayerId = null;
        flashCellIndex = -1;
        renderActions();
        client.continueTurn();
    }

    private void renderDisplayedPlayer() {
        PlayerSnapshot player = playerById(displayedPlayerId);
        if (player == null) return;
        boolean own = player.id.equals(selfPlayerId);
        boolean active = player.id.equals(snapshot.activePlayerId);
        String label = own ? getString(R.string.viewing_own) : getString(R.string.viewing_other, player.name);
        txtViewing.setText(active ? label + "  " + getString(R.string.turn_marker) : label);

        WindowCard window = Windows.build(player.windowId, player.dicePositions);
        windowGrid.setWindow(window);
        windowGrid.setEnabled(own && isMyTurn() && !pendingMove);
        if (selectedDraftIndex >= 0 && own && isMyTurn() && snapshot != null
                && selectedDraftIndex < snapshot.draftPool.size()) {
            windowGrid.setLegalCells(Windows.legalCells(window, snapshot.draftPool.get(selectedDraftIndex)));
        } else {
            windowGrid.clearHighlights();
        }
        if (flashCellIndex >= 0 && player.id.equals(flashPlayerId)) {
            windowGrid.setSelectedCell(flashCellIndex);
        }
    }

    private void renderTurnBanner() {
        if (snapshot == null) return;
        if (pausedBotId != null) {
            PlayerSnapshot bot = playerById(pausedBotId);
            txtTurn.setText(getString(pausedBotPlaced ? R.string.bot_placed : R.string.bot_passed,
                    bot == null ? "" : bot.name));
            txtTurn.setTextColor(getResources().getColor(R.color.textPrimary, null));
            return;
        }
        if (isMyTurn()) {
            WindowCard window = ownWindow();
            if (Windows.stuck(window, snapshot.draftPool)) {
                txtTurn.setText(R.string.your_turn_stuck);
            } else if (selectedDraftIndex >= 0) {
                txtTurn.setText(R.string.your_turn_selected);
            } else {
                txtTurn.setText(R.string.your_turn);
            }
            txtTurn.setTextColor(getResources().getColor(R.color.gold, null));
            return;
        }
        PlayerSnapshot active = playerById(snapshot.activePlayerId);
        txtTurn.setText(active == null ? getString(R.string.waiting_turn)
                : getString(active.bot ? R.string.bot_thinking : R.string.other_turn, active.name));
        txtTurn.setTextColor(getResources().getColor(R.color.textSecondary, null));
    }

    private void renderScore() {
        WindowCard window = ownWindow();
        Colour privateColour = setup == null ? null : Objectives.privateColour(setup.privateObjective);
        if (window == null || privateColour == null) {
            txtScore.setVisibility(View.GONE);
            return;
        }
        ScoreBreakdown breakdown = ScoreCalculator.score(window, privateColour, setup.publicObjectives);
        txtScore.setVisibility(View.VISIBLE);
        txtScore.setText(getString(R.string.score_projection, breakdown.getTotal()));
    }

    private WindowCard ownWindow() {
        PlayerSnapshot self = playerById(selfPlayerId);
        return self == null ? null : Windows.build(self.windowId, self.dicePositions);
    }

    private void selectDraftDie(int index) {
        if (snapshot == null || pendingMove || !isMyTurn() || index >= snapshot.draftPool.size()) return;
        selectedDraftIndex = selectedDraftIndex == index ? -1 : index;
        // Placing means looking at your own window, so follow the player there automatically.
        if (selectedDraftIndex >= 0) displayedPlayerId = selfPlayerId;
        renderDraftPool();
        renderDisplayedPlayer();
        renderTurnBanner();
    }

    private void onWindowCellClicked(int cellIndex) {
        if (snapshot == null || pendingMove || !isMyTurn()) return;
        if (!selfPlayerId.equals(displayedPlayerId)) return;
        if (selectedDraftIndex < 0 || selectedDraftIndex >= snapshot.draftPool.size()) {
            Toast.makeText(this, R.string.select_die_first, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean[] legal = Windows.legalCells(ownWindow(), snapshot.draftPool.get(selectedDraftIndex));
        if (cellIndex < legal.length && !legal[cellIndex]) {
            Toast.makeText(this, R.string.cell_not_legal, Toast.LENGTH_SHORT).show();
            return;
        }
        pendingMove = true;
        windowGrid.setSelectedCell(cellIndex);
        windowGrid.setEnabled(false);
        client.placeDie(selectedDraftIndex, cellIndex);
    }

    private void skipTurn() {
        if (snapshot == null || pendingMove || !isMyTurn() || client == null) return;
        pendingMove = true;
        btnSkip.setEnabled(false);
        client.skipTurn();
    }

    private void browsePlayer(int step) {
        if (snapshot == null || snapshot.players.isEmpty()) return;
        int index = indexOf(displayedPlayerId);
        if (index < 0) index = 0;
        index = (index + step + snapshot.players.size()) % snapshot.players.size();
        displayedPlayerId = snapshot.players.get(index).id;
        renderDisplayedPlayer();
    }

    private boolean isMyTurn() {
        return snapshot != null && selfPlayerId != null && selfPlayerId.equals(snapshot.activePlayerId);
    }

    private PlayerSnapshot playerById(String id) {
        if (snapshot == null || id == null) return null;
        for (PlayerSnapshot player : snapshot.players) if (player.id.equals(id)) return player;
        return null;
    }

    private int indexOf(String id) {
        if (snapshot == null || id == null) return -1;
        for (int i = 0; i < snapshot.players.size(); i++) {
            if (snapshot.players.get(i).id.equals(id)) return i;
        }
        return -1;
    }

    // ---------------------------------------------------------------- end of match

    /**
     * The end of a match is the part worth looking at, so it gets a screen rather than a dialog:
     * every player's final window, their revealed private colour and how the points broke down.
     */
    private void showStandings(EndGame endGame) {
        pacing.removeCallbacksAndMessages(null);
        pausedBotId = null;
        awaitingRoundStart = false;

        PlayerScore self = null;
        int selfRank = 0;
        for (int i = 0; i < endGame.standings.size(); i++) {
            if (endGame.standings.get(i).playerId.equals(selfPlayerId)) {
                self = endGame.standings.get(i);
                selfRank = i;
            }
        }
        boolean won = self != null && selfRank == 0;
        if (!endGame.standings.isEmpty()) {
            txtWinner.setText(won ? getString(R.string.you_win)
                    : getString(R.string.winner_is, endGame.standings.get(0).name));
        }
        if (self == null) {
            txtOutcome.setText("");
        } else {
            txtOutcome.setText(won ? getString(R.string.outcome_won)
                    : getString(R.string.outcome_lost, ordinal(selfRank), self.total));
        }

        standingsList.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < endGame.standings.size(); i++) {
            standingsList.addView(buildStandingRow(inflater, endGame.standings.get(i), i));
        }

        btnPlayAgain.setEnabled(host);
        txtRematchHint.setVisibility(host ? View.GONE : View.VISIBLE);
        txtRematchHint.setText(R.string.waiting_for_host);
        showScreen(SCREEN_STANDINGS);
    }

    private View buildStandingRow(LayoutInflater inflater, PlayerScore score, int index) {
        View row = inflater.inflate(R.layout.item_standing, standingsList, false);
        ((TextView) row.findViewById(R.id.standingRank)).setText(ordinal(index));
        String name = score.playerId.equals(selfPlayerId) ? getString(R.string.standing_you, score.name)
                : score.bot ? getString(R.string.standing_bot, score.name) : score.name;
        ((TextView) row.findViewById(R.id.standingName)).setText(name);
        ((TextView) row.findViewById(R.id.standingTotal)).setText(String.valueOf(score.total));
        ((TextView) row.findViewById(R.id.standingBreakdown)).setText(getString(R.string.score_line,
                score.publicScore, score.privateScore, score.emptyPenalty));

        Colour privateColour = Objectives.privateColour(score.privateObjective);
        TextView privateLabel = row.findViewById(R.id.standingPrivate);
        View swatch = row.findViewById(R.id.standingPrivateSwatch);
        if (privateColour == null) {
            privateLabel.setText(R.string.standing_private_unknown);
            swatch.setVisibility(View.GONE);
        } else {
            privateLabel.setText(getString(R.string.standing_private, Dice.name(privateColour)));
            swatch.setVisibility(View.VISIBLE);
            swatch.setBackgroundColor(Dice.colour(this, privateColour));
        }

        // The last snapshot before EndGame already carries every finished window.
        PlayerSnapshot finished = playerById(score.playerId);
        WindowGridView board = row.findViewById(R.id.standingWindow);
        board.setWindow(finished == null ? null
                : Windows.build(finished.windowId, finished.dicePositions));
        return row;
    }

    private String ordinal(int index) {
        switch (index) {
            case 0: return getString(R.string.rank_first);
            case 1: return getString(R.string.rank_second);
            case 2: return getString(R.string.rank_third);
            default: return getString(R.string.rank_fourth);
        }
    }

    private void requestRematch() {
        if (client == null) return;
        if (selectedMode == GameMode.LAN_MULTIPLAYER) {
            client.requestRematch(0, difficulty);
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.section_opponents)
                .setItems(new String[]{"1", "2", "3"}, (dialog, which) -> {
                    botCount = which + 1;
                    applyModeSelection();
                    client.requestRematch(botCount, difficulty);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showServerError(ErrorMessage error) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.server_message)
                .setMessage(error.message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (error.fatal) {
                        if (client != null) client.closeImmediately();
                        goHome();
                    }
                })
                .show();
    }

    // ---------------------------------------------------------------- leaving

    private void confirmLeave() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.quit_match)
                .setMessage(R.string.quit_match_detail)
                .setNegativeButton(R.string.stay, null)
                .setPositiveButton(R.string.quit_confirm, (dialog, which) -> leaveMatch())
                .show();
    }

    private void leaveMatch() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        goHome();
    }

    private void goHome() {
        resetMatchState();
        showScreen(SCREEN_HOME);
    }

    private void resetMatchState() {
        pacing.removeCallbacksAndMessages(null);
        flashPlayerId = null;
        flashCellIndex = -1;
        pausedBotId = null;
        awaitingRoundStart = false;
        selfPlayerId = null;
        host = false;
        setup = null;
        snapshot = null;
        displayedPlayerId = null;
        pendingWindowId = null;
        selectedDraftIndex = -1;
        pendingMove = false;
        wasMyTurn = false;
        setupJustStarted = false;
        objectiveStrip.removeAllViews();
        windowGrid.setWindow(null);
        windowGrid.clearHighlights();
    }

    @Override
    public void onBackPressed() {
        switch (currentScreen()) {
            case SCREEN_HOME:
                super.onBackPressed();
                return;
            case SCREEN_CONNECT:
                showScreen(SCREEN_HOME);
                return;
            case SCREEN_WINDOWS:
                showScreen(SCREEN_OBJECTIVES);
                return;
            case SCREEN_STANDINGS:
                leaveMatch();
                return;
            default:
                confirmLeave();
        }
    }

    @Override
    protected void onDestroy() {
        pacing.removeCallbacksAndMessages(null);
        if (isFinishing() && client != null) client.closeImmediately();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
