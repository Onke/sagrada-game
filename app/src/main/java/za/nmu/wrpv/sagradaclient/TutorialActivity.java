package za.nmu.wrpv.sagradaclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nmu.sagrada.core.Colour;
import nmu.sagrada.core.Die;
import nmu.sagrada.core.PlacementValidator;
import nmu.sagrada.core.ValidationResult;
import nmu.sagrada.core.WindowCard;
import nmu.sagrada.core.WindowPatterns;

/**
 * Three guided placements that teach the edge rule, cell restrictions and the orthogonal colour
 * conflict. Uses the same board and dice views as a real match so nothing has to be relearned.
 */
public class TutorialActivity extends AppCompatActivity {

    private static final int STEPS = 3;
    private static final int CONFLICT_CELL = 9;

    private final PlacementValidator validator = new PlacementValidator();
    private final List<DieView> diceViews = new ArrayList<>();
    private final List<Die> tutorialDice = Arrays.asList(
            new Die(Colour.RED, 4), new Die(Colour.BLUE, 6), new Die(Colour.BLUE, 5));

    private WindowCard window;
    private WindowGridView grid;
    private TextView instruction;
    private TextView feedback;
    private Button finish;
    private int step;
    private int selectedDieIndex = -1;
    private boolean conflictSeen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        instruction = findViewById(R.id.tutorialInstruction);
        feedback = findViewById(R.id.tutorialFeedback);
        finish = findViewById(R.id.btnTutorialFinish);
        grid = findViewById(R.id.tutorialGrid);
        grid.setOnCellClickListener(this::attemptPlacement);
        createDice(findViewById(R.id.tutorialDice));
        findViewById(R.id.btnTutorialBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnTutorialRestart).setOnClickListener(view -> restart());
        finish.setOnClickListener(view -> finish());
        restart();
    }

    private void createDice(LinearLayout container) {
        int size = dp(62);
        for (int i = 0; i < tutorialDice.size(); i++) {
            DieView die = new DieView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dp(6), 0, dp(6), 0);
            die.setLayoutParams(params);
            die.setDie(tutorialDice.get(i).getId());
            final int index = i;
            die.setOnClickListener(view -> selectDie(index));
            container.addView(die);
            diceViews.add(die);
        }
    }

    private void restart() {
        window = new WindowCard(WindowPatterns.patternFor("WINDOW1"));
        step = 0;
        selectedDieIndex = -1;
        conflictSeen = false;
        feedback.setText("");
        finish.setVisibility(View.GONE);
        render();
    }

    private void selectDie(int index) {
        if (step >= STEPS || index != step) {
            feedback.setText(R.string.tutorial_pick_named);
            return;
        }
        selectedDieIndex = index;
        feedback.setText("");
        render();
    }

    private void attemptPlacement(int cellIndex) {
        if (step >= STEPS) return;
        if (selectedDieIndex != step) {
            feedback.setText(R.string.select_die_first);
            return;
        }
        Die die = tutorialDice.get(step);
        int target = targetCell();
        ValidationResult result = validator.validate(window, die, cellIndex);

        // Step three deliberately walks the player into an illegal orthogonal colour match first.
        if (step == 2 && !conflictSeen && cellIndex == CONFLICT_CELL) {
            conflictSeen = true;
            feedback.setText(result.getMessage());
            instruction.setText(R.string.tutorial_step_three_legal);
            renderGrid();
            return;
        }
        if (cellIndex != target) {
            feedback.setText(result.isLegal() ? getString(R.string.tutorial_use_highlighted) : result.getMessage());
            return;
        }
        if (!result.isLegal()) {
            feedback.setText(result.getMessage());
            return;
        }
        window.place(cellIndex, die);
        step++;
        selectedDieIndex = -1;
        feedback.setText("");
        render();
    }

    private int targetCell() {
        if (step == 0) return 3;
        if (step == 1) return 4;
        return conflictSeen ? 8 : CONFLICT_CELL;
    }

    private void render() {
        if (step == 0) instruction.setText(R.string.tutorial_step_one);
        else if (step == 1) instruction.setText(R.string.tutorial_step_two);
        else if (step == 2) instruction.setText(conflictSeen
                ? R.string.tutorial_step_three_legal : R.string.tutorial_step_three_conflict);
        else {
            instruction.setText(R.string.tutorial_complete);
            feedback.setText("");
            finish.setVisibility(View.VISIBLE);
        }
        renderGrid();
        renderDice();
    }

    private void renderGrid() {
        grid.setWindow(window);
        // Show the target from the start of each step — the instruction tells the player to place
        // the die "on the glowing cell", so something had better be glowing before they look.
        boolean[] legal = new boolean[WindowCard.SIZE];
        if (step < STEPS) legal[targetCell()] = true;
        grid.setLegalCells(legal);
    }

    private void renderDice() {
        for (int i = 0; i < diceViews.size(); i++) {
            DieView die = diceViews.get(i);
            boolean playable = step < STEPS && i == step;
            die.setPlayable(playable);
            die.setEnabled(playable);
            die.setChosen(i == selectedDieIndex);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
