package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;

public class BlackHole extends Boss {

    int stage;
    final float HORIZ_SPEED;
    private float radius;

    public BlackHole(float swidth, float sheight, BitmapLoader b, Dachshund d){
        super(swidth, sheight, b, d);

        radius = screenwidth * 0.07f;
        image_rect = new RectF(0,0,radius*2, radius*2);

        //animation
        /*
        unhit_frames.add(bitmapLoader.getBitmap("dachsboss"));
        hit_frames.add(bitmapLoader.getBitmap("dachsbosshit"));
        seconds_per_frame= 1;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());


        cur_bitmap = unhit_frames.get(frame); //set default bitmap
        */
        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 25;
        life = max_life;

        stage = BEGIN;

        HORIZ_SPEED = screenwidth * (float) 0.33;

        name = "blackhole";

        //maybe change so that, once it gets to half of the screen, it makes a fast dart for your dog?

    }

    private static final int BEGIN = 0;
    private static final int LEFT = 1;

    @Override
    public void update(double t){
        if(hit){
            hit_timer-=t;
            if(hit_timer<=0){
                hit=false;
                frame = 0; //reseting animation when boss no longer hit
            }
        }
        float dt = (float) t;

        switch(stage){
            case BEGIN:
                moveTo(screenwidth + image_rect.width(),Util.randFloat(0,screenheight));
                stage = LEFT; break;

            case LEFT:
                move(HORIZ_SPEED * dt * -1,0); //move to the left
                //make y the inverse of the actual dachshund
                //float inverseY = screenheight - dachs.image_rect.centerY();
                //moveTo(image_rect.centerX(), inverseY);

                if(image_rect.right<0){
                    stage = BEGIN;
                    hit=false;
                }

        }
    }

    @Override
    public void draw(Canvas canvas){
        if(hit){
            paint.setColor(Color.RED);
        } else{
            paint.setColor(Color.argb(255,20,20,20));
        }
        canvas.drawCircle(image_rect.centerX(), image_rect.centerY(),radius, paint);
    }

    @Override
    public void updateHitRects(){
        hit_rects[0] = image_rect;
    }


}
