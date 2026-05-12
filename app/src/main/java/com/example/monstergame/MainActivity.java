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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FrameLayout container = findViewById(R.id.game_container);
        container.addView(new GameView(this));
    }

    class GameView extends SurfaceView implements Runnable {
        private Thread thread;
        private boolean isPlaying = true;
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
                drawGame(canvas);
                holder.unlockCanvasAndPost(canvas);
                
                if (frameCount % 20 == 0) {
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
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

            paint.setColor(Color.parseColor("#87CEEB"));
            canvas.drawRect(0, 0, w, h/2, paint);

            paint.setColor(Color.parseColor("#228B22"));
            canvas.drawRect(0, h/2, w, h, paint);

            paint.setColor(Color.GRAY);
            Path path = new Path();
            path.moveTo(w/2 - 50, h/2);
            path.lineTo(w/2 + 50, h/2);
            path.lineTo(w, h);
            path.lineTo(0, h);
            path.close();
            canvas.drawPath(path, paint);

            if (boyBitmap != null) {
                canvas.drawBitmap(boyBitmap, w/2 - boyBitmap.getWidth()/2, h - 300, paint);
            } else {
                paint.setColor(Color.BLUE);
                canvas.drawCircle(w/2, h - 300, 40, paint);
            }

            if (monsterBitmap != null) {
                canvas.drawBitmap(monsterBitmap, w/2 - monsterBitmap.getWidth()/2, h - 100, paint);
            } else {
                paint.setColor(Color.RED);
                canvas.drawCircle(w/2, h - 100, 60, paint);
            }
            
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            String instructionText = getResources().getString(R.string.game_instruction);
            canvas.drawText(instructionText, 100, 100, paint);
        }

        public void resume() {
            isPlaying = true;
            thread = new Thread(this);
            thread.start();
        }

        public void pause() {
            isPlaying = false;
            try { thread.join(); } catch (InterruptedException e) {}
        }
    }
}
