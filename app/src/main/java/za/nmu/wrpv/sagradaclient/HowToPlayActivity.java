package za.nmu.wrpv.sagradaclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HowToPlayActivity extends AppCompatActivity {
    private static final int PAGE_COUNT = 7;
    private ViewPager2 pager;
    private TextView pageCount;
    private Button previous;
    private Button next;
    private Button practice;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        pager = findViewById(R.id.helpPager);
        pageCount = findViewById(R.id.helpPageCount);
        previous = findViewById(R.id.btnPrevious);
        next = findViewById(R.id.btnNext);
        practice = findViewById(R.id.btnPractice);
        pager.setAdapter(new HelpAdapter());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { updateControls(position); }
        });
        previous.setOnClickListener(view -> pager.setCurrentItem(Math.max(0, pager.getCurrentItem() - 1), true));
        next.setOnClickListener(view -> pager.setCurrentItem(Math.min(PAGE_COUNT - 1, pager.getCurrentItem() + 1), true));
        findViewById(R.id.btnCloseHelp).setOnClickListener(view -> finish());
        practice.setOnClickListener(view -> startActivity(new Intent(this, TutorialActivity.class)));
        updateControls(0);
    }

    private void updateControls(int position) {
        pageCount.setText(getString(R.string.page_count, position + 1, PAGE_COUNT));
        previous.setEnabled(position > 0);
        next.setEnabled(position < PAGE_COUNT - 1);
        practice.setVisibility(position == PAGE_COUNT - 1 ? View.VISIBLE : View.GONE);
    }

    private final class HelpAdapter extends RecyclerView.Adapter<HelpViewHolder> {
        private final int[] titles = {R.string.help_goal_title, R.string.help_setup_title,
                R.string.help_round_title, R.string.help_first_die_title,
                R.string.help_restrictions_title, R.string.help_scoring_title,
                R.string.help_controls_title};
        private final int[] bodies = {R.string.help_goal_body, R.string.help_setup_body,
                R.string.help_round_body, R.string.help_first_die_body,
                R.string.help_restrictions_body, R.string.help_scoring_body,
                R.string.help_controls_body};
        private final int[] images = {R.drawable.sagrada_logo, R.drawable.objectives,
                R.drawable.label_rounds, R.drawable.window1, R.drawable.window15,
                R.drawable.public_objective_card7, R.drawable.sagrada_board_game};

        @NonNull @Override public HelpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_help_page, parent, false);
            return new HelpViewHolder(view);
        }

        @Override public void onBindViewHolder(@NonNull HelpViewHolder holder, int position) {
            holder.title.setText(titles[position]);
            holder.body.setText(bodies[position]);
            holder.image.setImageResource(images[position]);
            holder.image.setContentDescription(getString(titles[position]));
        }
        @Override public int getItemCount() { return PAGE_COUNT; }
    }

    private static final class HelpViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView body;
        HelpViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.helpImage);
            title = itemView.findViewById(R.id.helpPageTitle);
            body = itemView.findViewById(R.id.helpPageBody);
        }
    }
}
