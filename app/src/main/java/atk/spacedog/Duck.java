package atk.spacedog;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;


public class Duck extends Boss {
    //will play a quacking noise to keep your dachshund up at the top of the screen
    //thus duck won't get hit until you mute your phone
    //good to pair with tunnels to make the dachshund get hurt at the top of the screen


    private int status; //movement status
    float xvector; float yvector;
    float speed;
    private final double SECONDS_PER_QUACK = 1;
    private double quack_timer=0;

    private final float X_MARGIN;


    SoundPool soundPool;
    int soundID = -1;

    public Duck(float swidth, float sheight, BitmapLoader b, Dachshund d, Context context){
        super(swidth, sheight, b, d);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        try{
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("quack1.ogg");
            soundID = soundPool.load(descriptor,0);
        } catch(IOException e){
            Log.e("error", "failed to load sound file");
        }
        //animation
        unhit_frames.add(bitmapLoader.getBitmap("duck"));
        unhit_frames.add(bitmapLoader.getBitmap("duckclosed"));

        hit_frames.add(bitmapLoader.getBitmap("duckhit"));
        hit_frames.add(bitmapLoader.getBitmap("duckhitclosed"));
        seconds_per_frame= 0.33f;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());

        cur_bitmap = unhit_frames.get(frame); //set default bitmap

        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 30;
        life = max_life;
        updateLifeBar();

        status = BEGIN;

        X_MARGIN = screenwidth * (float) 0.025;
        speed = screenwidth * (float) (0.6);

        name = "duck";

    }
    public void reset(){
        life=max_life;
        status=BEGIN;
        quack_timer=0;
        alive=true;
        paint.setAlpha(255);
    }

    @Override
    protected void updateHitRects() {
        hit_rects[0] = image_rect;
        //can make this more complicated for  particular bosses if you want

    }

    private static final int BEGIN = 0;
    private static final int LEFT = 1;
    private static final int RIGHT = 2;

    @Override
    public void update(double t) {
        super.update(t); //updates the hit status
        float dt = (float) t;
        //set animation frame
        frame_timer+=dt;
        if(frame_timer >=seconds_per_frame){
            frame_timer=0;
            frame++;
            if(frame>=unhit_frames.size()){frame=0;}
        }

        quack_timer+=t;
        if(quack_timer >=SECONDS_PER_QUACK){
            soundPool.play(soundID,1,1,0,0,1); //play quacking sound
            quack_timer=0;
        }

        //set movement vector based on status
        switch(status){
            case BEGIN:
                centerAt(image_rect,screenwidth + image_rect.width(), screenheight /2);
                status = LEFT;
                break;

            case LEFT:
                move(-1* speed * dt, 0);
                if(image_rect.left<= X_MARGIN){
                    status = RIGHT;
                }
                break;

            case RIGHT:
                move(speed * dt, 0);
                if(screenwidth-image_rect.right <=X_MARGIN){status =LEFT;}
                break;


        }

    }
}
