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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;

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

    // এই অংশটি আমি আগে দিতে ভুলে গিয়েছিলাম (গেম ইঞ্জিন স্টার্ট করা)
    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    // গেম ইঞ্জিন স্টপ করা
    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    class GameView extends SurfaceView implements Runnable {
        private Thread thread;
        private boolean isPlaying = false;
        private SurfaceHolder holder;
        private Paint paint;
        private float roadOffset = 0;
        private ToneGenerator toneGen;
        private Bitmap boyBitmap, monsterBitmap;

        public GameView(Context context) {
            super(context);
            holder = getHolder();
            paint = new Paint();
            toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

            // ছবি লোড করা
            int boyResId = getResources().getIdentifier("boy", "drawable", getPackageName());
            int monsterResId = getResources().getIdentifier("monster", "drawable", getPackageName());

            if (boyResId != 0) {
                boyBitmap = BitmapFactory.decodeResource(getResources(), boyResId);
            }
            if (monsterResId != 0) {
                monsterBitmap = BitmapFactory.decodeResource(getResources(), monsterResId);
            }
        }

        @Override
        public void run() {
            int frameCount = 0;
            while (isPlaying) {
                if (!holder.getSurface().isValid()) continue;
                Canvas canvas = holder.lockCanvas();
                if (canvas != null) {
                    drawGame(canvas);
                    holder.unlockCanvasAndPost(canvas);
                }
                
                // দৌড়ানোর সাউন্ড
                if (frameCount % 20 == 0) {
                    try { toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50); } catch (Exception e) {}
                }
                
                roadOffset += 20;
                if (roadOffset > 200) roadOffset = 0;
                frameCount++;
                
                try { Thread.sleep(30); } catch (InterruptedException e) {}
            }
        }

        private void drawGame(Canvas canvas) {
            int w = canvas.getWidth();
            int h = canvas.getHeight();

            // আকাশ
            paint.setColor(Color.parseColor("#87CEEB"));
            canvas.drawRect(0, 0, w, h/2f, paint);

            // মাটি
            paint.setColor(Color.parseColor("#228B22"));
            canvas.drawRect(0, h/2f, w, h, paint);

            // 3D রাস্তা
            paint.setColor(Color.GRAY);
            Path path = new Path();
            path.moveTo(w/2f - 50, h/2f);
            path.lineTo(w/2f + 50, h/2f);
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.close();
            canvas.drawPath(path, paint);

            // ছেলেটি
            if (boyBitmap != null) {
                canvas.drawBitmap(boyBitmap, w/2f - boyBitmap.getWidth()/2f, h - 500, paint);
            } else {
                paint.setColor(Color.BLUE);
                canvas.drawCircle(w/2f, h - 400, 40, paint);
            }

            // রাক্ষসটি
            if (monsterBitmap != null) {
                canvas.drawBitmap(monsterBitmap, w/2f - monsterBitmap.getWidth()/2f, h - 300, paint);
            } else {
                paint.setColor(Color.RED);
                canvas.drawCircle(w/2f, h - 200, 60, paint);
            }
            
            // ওপরের টেক্সট
            paint.setColor(Color.WHITE);
            paint.setTextSize(55);
            String instructionText = getResources().getString(R.string.game_instruction);
            canvas.drawText(instructionText, 20, 100, paint);
        }

        public void resume() {
            isPlaying = true;
            thread = new Thread(this);
            thread.start();
        }

        public void pause() {
            isPlaying = false;
            boolean retry = true;
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
        }
    }
}
