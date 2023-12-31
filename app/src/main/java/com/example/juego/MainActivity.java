package com.example.juego;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView levelTextView;
    private TextView scoreTextView;
    private ImageView heart1ImageView;
    private ImageView heart2ImageView;
    private ImageView heart3ImageView;
    private TextView instructionTextView;
    private Button startButton;

    private MediaPlayer mediaPlayerBackground;
    private MediaPlayer mediaPlayerPress;
    private MediaPlayer mediaPlayerSwipe;
    private MediaPlayer mediaPlayerShake;
    private MediaPlayer mediaPlayerDefeat;
    private MediaPlayer mediaPlayerGame;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean shakeDetected;

    private int currentLevel;
    private int currentScore;
    private int lives;
    private boolean gameStarted;
    private boolean gameOver;

    private Random random;
    private List<String> instructions;
    private int currentInstructionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        levelTextView = findViewById(R.id.levelTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        heart1ImageView = findViewById(R.id.heart1ImageView);
        heart2ImageView = findViewById(R.id.heart2ImageView);
        heart3ImageView = findViewById(R.id.heart3ImageView);
        instructionTextView = findViewById(R.id.instructionTextView);
        startButton = findViewById(R.id.startButton);

        mediaPlayerBackground = MediaPlayer.create(this, R.raw.fondo);
        mediaPlayerPress = MediaPlayer.create(this, R.raw.apretar);
        mediaPlayerSwipe = MediaPlayer.create(this, R.raw.deslizar);
        mediaPlayerShake = MediaPlayer.create(this, R.raw.agita);
        mediaPlayerDefeat = MediaPlayer.create(this, R.raw.lose);
        mediaPlayerGame = MediaPlayer.create(this, R.raw.defeat);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeDetected = false;

        random = new Random();
        instructions = new ArrayList<>();
        instructions.add(getString(R.string.instruction_press));
        instructions.add(getString(R.string.instruction_swipe));
        instructions.add(getString(R.string.instruction_shake));

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameStarted) {
                    startGame();
                }
            }
        });
    }

    private void startGame() {
        currentLevel = 1;
        currentScore = 0;
        lives = 3;
        gameStarted = true;
        gameOver = false;

        updateLevelTextView();
        updateScoreTextView();
        updateLivesImageViews();

        startButton.setEnabled(false);

        playBackgroundMusic();

        startNewRound();
    }

    private void startNewRound() {
        if (!gameOver) {
            currentInstructionIndex = random.nextInt(instructions.size());
            String instruction = instructions.get(currentInstructionIndex);
            String instructionSound = getInstructionSound(instruction);

            instructionTextView.setText(instruction);
            playInstructionSound(instructionSound);
        }
    }

    private void checkAction(boolean actionCompleted) {
        if (!gameOver) {
            boolean correctAction = actionCompleted && isCorrectAction();

            if (correctAction) {
                currentScore++;
                updateScoreTextView();
            } else {
                lives--;
                updateLivesImageViews();

                if (lives <= 0) {
                    endGame();
                    return;
                }
            }

            currentLevel++;
            updateLevelTextView();

            startNewRound();
        }
    }

    private boolean isCorrectAction() {
        String currentInstruction = instructions.get(currentInstructionIndex);

        if (currentInstruction.equals(getString(R.string.instruction_press)) && !shakeDetected) {
            return true;
        } else if (currentInstruction.equals(getString(R.string.instruction_swipe)) && !shakeDetected) {
            return true;
        } else if (currentInstruction.equals(getString(R.string.instruction_shake)) && shakeDetected) {
            return true;
        }

        return false;
    }

    private void endGame() {
        gameStarted = false;
        gameOver = true;

        shakeDetected = false;

        mediaPlayerBackground.stop();
        mediaPlayerDefeat.start();

        instructionTextView.setText(getString(R.string.game_over));

        startButton.setEnabled(true);
        startButton.setText(getString(R.string.start));

        resetLivesImageViews();
    }

    private void updateLevelTextView() {
        levelTextView.setText(getString(R.string.level, currentLevel));
    }

    private void updateScoreTextView() {
        scoreTextView.setText(getString(R.string.score, currentScore));
    }

    private void updateLivesImageViews() {
        if (lives == 3) {
            heart1ImageView.setImageResource(R.drawable.heart_full);
            heart2ImageView.setImageResource(R.drawable.heart_full);
            heart3ImageView.setImageResource(R.drawable.heart_full);
        } else if (lives == 2) {
            heart1ImageView.setImageResource(R.drawable.heart_empty);
            heart2ImageView.setImageResource(R.drawable.heart_full);
            heart3ImageView.setImageResource(R.drawable.heart_full);
        } else if (lives == 1) {
            heart1ImageView.setImageResource(R.drawable.heart_empty);
            heart2ImageView.setImageResource(R.drawable.heart_empty);
            heart3ImageView.setImageResource(R.drawable.heart_full);
        } else if (lives == 0) {
            heart1ImageView.setImageResource(R.drawable.heart_empty);
            heart2ImageView.setImageResource(R.drawable.heart_empty);
            heart3ImageView.setImageResource(R.drawable.heart_empty);
        }
    }

    private void resetLivesImageViews() {
        heart1ImageView.setImageResource(R.drawable.heart_full);
        heart2ImageView.setImageResource(R.drawable.heart_full);
        heart3ImageView.setImageResource(R.drawable.heart_full);
    }

    private String getInstructionSound(String instruction) {
        if (instruction.equals(getString(R.string.instruction_press))) {
            return "click";
        } else if (instruction.equals(getString(R.string.instruction_swipe))) {
            return "swipe";
        } else if (instruction.equals(getString(R.string.instruction_shake))) {
            return "shake";
        }

        return "";
    }

    private void playInstructionSound(String sound) {
        if (sound.equals("click")) {
            mediaPlayerPress.start();
        } else if (sound.equals("swipe")) {
            mediaPlayerSwipe.start();
        } else if (sound.equals("shake")) {
            mediaPlayerShake.start();
        }
    }

    private void playBackgroundMusic() {
        mediaPlayerBackground.setLooping(true);
        mediaPlayerBackground.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameStarted && event.getAction() == MotionEvent.ACTION_DOWN) {
            checkAction(true);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gameStarted && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z);

            if (acceleration > 20) {
                shakeDetected = true;
                checkAction(true);
                shakeDetected = false;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
