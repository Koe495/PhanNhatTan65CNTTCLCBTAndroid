package cuoiki.helloworldgame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

import cuoiki.helloworldgame.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GameView gameView;
    private RecognitionManager recognitionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvHelloWorld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOpeningAnimation();
            }
        });

        // Khởi tạo AI
        recognitionManager = new RecognitionManager();
        recognitionManager.downloadModel();
    }

    private void startOpeningAnimation() {
        // Ẩn chữ gốc
        binding.tvHelloWorld.setVisibility(View.INVISIBLE);

        String text = "hello world";
        int[] location = new int[2];
        binding.tvHelloWorld.getLocationOnScreen(location);

        int currentX = location[0];
        final int startY = location[1];

        final Random random = new Random();

        // Loop tạo từng chữ cái giả
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                currentX += 13; // Skip dấu cách
                continue;
            }

            final TextView charView = new TextView(this);
            charView.setText(String.valueOf(c));
            charView.setTextSize(20);
            charView.setTextColor(Color.BLACK);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = currentX;
            params.topMargin = startY;
            params.gravity = Gravity.TOP | Gravity.START;

            binding.rootContainer.addView(charView, params);

            // Đo độ rộng từng chữ
            charView.measure(0, 0);
            currentX += charView.getMeasuredWidth(); // Tăng x bằng với độ rộng chữ vừa đo

            float bounceOffsetY = -50f;

            // 1. Animation nảy
            ViewPropertyAnimator animator = charView.animate()
                    .translationY(bounceOffsetY)
                    .setDuration(150)
                    .setListener(null);

            // 2. Animation rơi
            final int finalI = i;

            animator.withEndAction(new Runnable() {
                @Override
                public void run() {
                    charView.animate()
                            .translationY(binding.rootContainer.getHeight() - startY)
                            .rotation(random.nextInt(360))
                            .alpha(0)
                            .setDuration(850 + random.nextInt(500))
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (finalI == text.length() - 1) {
                                        initGame();
                                    }
                                    // Tiêu huỷ hàng giả sau khi dùng
                                    binding.rootContainer.removeView(charView);
                                }
                            });
                }
            });
        }
    }

    // UI GAME
    private void initGame() {
        binding.gameContainer.setVisibility(View.VISIBLE);
        binding.tvScore.setVisibility(View.VISIBLE);
        binding.tvDiff.setVisibility(View.VISIBLE);

        gameView = new GameView(this);
        // Truyền AI vào GameView
        gameView.setRecognitionManager(recognitionManager);
        binding.gameContainer.addView(gameView);

        gameView.setGameOverListener(new GameView.GameOverListener() {
            @Override
            public void onScoreUpdate(final int score) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvScore.setText("Score: " + score);
                    }
                });
            }

            @Override
            public void onDiffUpdate(final int diff) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvDiff.setText("Diff: " + diff);
                    }
                });
            }

            @Override
            public void onGameOver() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "GAME OVER!", Toast.LENGTH_LONG).show();
                        binding.gameContainer.removeAllViews();
                        binding.tvHelloWorld.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onGameWin() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "YOU WIN!", Toast.LENGTH_LONG).show();
                        binding.gameContainer.removeAllViews();
                        binding.tvHelloWorld.setVisibility(View.VISIBLE);
                        binding.tvHelloWorld.setText("YOU WIN!");
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recognitionManager != null) {
            recognitionManager.close();
        }
    }
}