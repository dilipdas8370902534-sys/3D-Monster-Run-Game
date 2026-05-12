package com.example.monstergame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout container = findViewById(R.id.game_container);
        gameView = new GameView(this);
        container.addView(gameView);
    }

    @Override
    protected void onResume() { super.onResume(); if(gameView != null) gameView.resume(); }

    @Override
    protected void onPause() { super.onPause(); if(gameView != null) gameView.pause(); }

    class GameView extends SurfaceView implements Runnable {
        private Thread thread;
        private boolean isPlaying = false;
        private SurfaceHolder holder;
        private Paint paint;
        private Bitmap boyBitmap, monsterBitmap;
        private float boyX, goldX, goldY;
        private int score = 0, timeLeft = 60;
        private boolean isGameOver = false;
        private ToneGenerator toneGen;
        private Random random = new Random();

        public GameView(Context context) {
            super(context);
            holder = getHolder();
            paint = new Paint();
            toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            
            // ছবি লোড এবং সাইজ ঠিক করা (Scaling)
            Bitmap rawBoy = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("boy", "drawable", getPackageName()));
            Bitmap rawMonster = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("monster", "drawable", getPackageName()));
            
            if(rawBoy != null) boyBitmap = Bitmap.createScaledBitmap(rawBoy, 250, 250, true);
            if(rawMonster != null) monsterBitmap = Bitmap.createScaledBitmap(rawMonster, 350, 350, true);
            
            resetGold();
        }

        private void resetGold() {
            goldX = random.nextInt(600) + 200;
            goldY = 0;
        }

        @Override
        public void run() {
            long lastTime = System.currentTimeMillis();
            while (isPlaying) {
                if (!holder.getSurface().isValid()) continue;
                
                // টাইমার লজিক
                if (System.currentTimeMillis() - lastTime > 1000 && !isGameOver) {
                    timeLeft--;
                    if (timeLeft <= 0) isGameOver = true;
                    lastTime = System.currentTimeMillis();
                }

                update();
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    draw(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
                try { Thread.sleep(30); } catch (InterruptedException e) {}
            }
        }

        private void update() {
            if (isGameOver) return;
            
            goldY += 25; // সোনা নিচের দিকে আসবে
            if (goldY > getHeight()) resetGold();

            // সংঘর্ষ সনাক্তকরণ (Collision Detection)
            if (Math.abs(boyX - goldX) < 150 && Math.abs((getHeight() - 500) - goldY) < 150) {
                score += 10;
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                resetGold();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            int w = canvas.getWidth();
            int h = canvas.getHeight();
            if (boyX == 0) boyX = w / 2f;

            // পরিবেশ আঁকা
            paint.setColor(Color.parseColor("#87CEEB")); // আকাশ
            canvas.drawRect(0, 0, w, h / 2f, paint);
            paint.setColor(Color.parseColor("#228B22")); // ঘাস
            canvas.drawRect(0, h / 2f, w, h, paint);

            // রাস্তা
            paint.setColor(Color.GRAY);
            Path path = new Path();
            path.moveTo(w / 2f - 100, h / 2f);
            path.lineTo(w / 2f + 100, h / 2f);
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.close();
            canvas.drawPath(path, paint);

            if (!isGameOver) {
                // সোনা (সোনা হিসেবে হলুদ বৃত্ত)
                paint.setColor(Color.parseColor("#FFD700"));
                canvas.drawCircle(goldX, goldY, 40, paint);

                // ছেলে
                if (boyBitmap != null) canvas.drawBitmap(boyBitmap, boyX - 125, h - 550, paint);
                // রাক্ষস
                if (monsterBitmap != null) canvas.drawBitmap(monsterBitmap, w / 2f - 175, h - 350, paint);

                // স্কোর ও সময়
                paint.setColor(Color.WHITE);
                paint.setTextSize(60);
                canvas.drawText("Score: " + score, 50, 100, paint);
                canvas.drawText("Time: " + timeLeft, 50, 180, paint);
            } else {
                paint.setColor(Color.RED);
                paint.setTextSize(80);
                canvas.drawText("GAME OVER!", w / 4f, h / 2f, paint);
                paint.setTextSize(60);
                canvas.drawText("Final Score: " + score, w / 4f, h / 2f + 100, paint);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                boyX = event.getX(); // আঙুল যেখানে ছেলেটি সেখানে যাবে
            }
            return true;
        }

        public void resume() { isPlaying = true; thread = new Thread(this); thread.start(); }
        public void pause() { isPlaying = false; try { thread.join(); } catch (InterruptedException e) {} }
    }
}
