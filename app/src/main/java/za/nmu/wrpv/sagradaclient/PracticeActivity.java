package za.nmu.wrpv.sagradaclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import nmu.sagrada.core.Colour;
import nmu.sagrada.core.Die;
import nmu.sagrada.core.ObjectiveScorer;
import nmu.sagrada.core.PlacementValidator;
import nmu.sagrada.core.ScoreBreakdown;
import nmu.sagrada.core.ScoreCalculator;
import nmu.sagrada.core.ValidationResult;
import nmu.sagrada.core.WindowCard;
import nmu.sagrada.core.WindowPatterns;

/**
 * Free-play sandbox: a random window, a random private colour and a random pool, with the real
 * validator and scorer behind it. Distinct from {@link TutorialActivity}, which teaches a fixed
 * scripted lesson at the end of the How to Play deck.
 */
public class PracticeActivity extends AppCompatActivity {

    private static final int POOL_SIZE = 9;
    private static final int PUBLIC_OBJECTIVES = 3;

    private final PlacementValidator validator = new PlacementValidator();
    private final Random random = new Random();
    private final List<DieView> diceViews = new ArrayList<>();
    private final List<String> pool = new ArrayList<>();
    private final List<String> publicObjectives = new ArrayList<>();

    private WindowCard window;
    private Colour privateColour;
    private String windowId;
    private int selectedDieIndex = -1;

    private WindowGridView grid;
    private GridLayout diceContainer;
    private TextView windowName;
    private TextView privateLabel;
    private TextView scoreLabel;
    private TextView feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);
        grid = findViewById(R.id.practiceGrid);
        diceContainer = findViewById(R.id.practiceDice);
        windowName = findViewById(R.id.practiceWindowName);
        privateLabel = findViewById(R.id.practicePrivate);
        scoreLabel = findViewById(R.id.practiceScore);
        feedback = findViewById(R.id.practiceFeedback);
        grid.setOnCellClickListener(this::attemptPlacement);
        findViewById(R.id.btnPracticeBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnPracticeNew).setOnClickListener(view -> deal());
        deal();
    }

    /** Rolls a fresh scenario, so no two visits practise the same placement. */
    private void deal() {
        List<String> windows = new ArrayList<>(WindowPatterns.ids());
        windowId = windows.get(random.nextInt(windows.size()));
        window = new WindowCard(WindowPatterns.patternFor(windowId));

        Colour[] colours = Colour.values();
        privateColour = colours[random.nextInt(colours.length)];

        publicObjectives.clear();
        List<String> objectives = new ArrayList<>(Arrays.asList(ObjectiveScorer.ALL));
        Collections.shuffle(objectives, random);
        publicObjectives.addAll(objectives.subList(0, PUBLIC_OBJECTIVES));

        pool.clear();
        for (int i = 0; i < POOL_SIZE; i++) {
            pool.add(new Die(colours[random.nextInt(colours.length)], random.nextInt(6) + 1).getId());
        }

        selectedDieIndex = -1;
        windowName.setText(prettyWindowName(windowId));
        privateLabel.setText(getString(R.string.practice_private, Dice.name(privateColour)));
        feedback.setText(R.string.practice_prompt);
        buildDice();
        render();
    }

    private void buildDice() {
        diceContainer.removeAllViews();
        diceViews.clear();
        int size = dp(52);
        int margin = dp(4);
        for (int i = 0; i < pool.size(); i++) {
            DieView die = new DieView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            die.setLayoutParams(params);
            die.setDie(pool.get(i));
            final int index = i;
            die.setOnClickListener(view -> selectDie(index));
            diceContainer.addView(die);
            diceViews.add(die);
        }
    }

    private void selectDie(int index) {
        selectedDieIndex = selectedDieIndex == index ? -1 : index;
        render();
    }

    private void attemptPlacement(int cellIndex) {
        if (selectedDieIndex < 0) {
            feedback.setText(R.string.select_die_first);
            return;
        }
        Die die = Die.fromId(pool.get(selectedDieIndex));
        ValidationResult result = validator.validate(window, die, cellIndex);
        if (!result.isLegal()) {
            feedback.setText(result.getMessage());
            return;
        }
        window.place(cellIndex, die);
        pool.remove(selectedDieIndex);
        selectedDieIndex = -1;
        feedback.setText(getString(R.string.practice_placed, Dice.describe(this, die.getId())));
        buildDice();
        render();
    }

    private void render() {
        grid.setWindow(window);
        if (selectedDieIndex >= 0 && selectedDieIndex < pool.size()) {
            grid.setLegalCells(Windows.legalCells(window, pool.get(selectedDieIndex)));
        } else {
            grid.clearHighlights();
        }
        for (int i = 0; i < diceViews.size(); i++) {
            diceViews.get(i).setChosen(i == selectedDieIndex);
        }

        ScoreBreakdown breakdown = ScoreCalculator.score(window, privateColour, publicObjectives);
        scoreLabel.setText(getString(R.string.practice_score, breakdown.getTotal(),
                breakdown.getPublicScore(), breakdown.getPrivateScore(), breakdown.getEmptyPenalty()));

        if (window.emptyCount() == 0) {
            feedback.setText(R.string.practice_full);
        } else if (!pool.isEmpty() && Windows.stuck(window, pool)) {
            feedback.setText(R.string.practice_no_moves);
        }
    }

    private String prettyWindowName(String id) {
        return id != null && id.startsWith("WINDOW") ? "Window " + id.substring(6) : String.valueOf(id);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
