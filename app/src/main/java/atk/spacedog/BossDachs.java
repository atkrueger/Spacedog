package atk.spacedog;


import android.graphics.RectF;

/**
 * Created by Alex on 3/7/2017.
 */

public class BossDachs extends Boss {

    int stage;
    final float HORIZ_SPEED;

    public BossDachs(float swidth, float sheight, BitmapLoader b, Dachshund d){
        super(swidth, sheight, b, d);

        //animation
        unhit_frames.add(bitmapLoader.getBitmap("dachsboss"));
        hit_frames.add(bitmapLoader.getBitmap("dachsbosshit"));
        seconds_per_frame= 1;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());

        cur_bitmap = unhit_frames.get(frame); //set default bitmap

        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 25;
        life = max_life;
        updateLifeBar();

        stage = BEGIN;

        HORIZ_SPEED = screenwidth * (float) 0.8;

        name = "dachs";

        //maybe change so that, once it gets to half of the screen, it makes a fast dart for your dog?

    }

    private static final int BEGIN = 0;
    private static final int INVERSE = 1;

    @Override
    public void update(double t){
        super.update(t);
        float dt = (float) t;

        switch(stage){
            case BEGIN:
                moveTo(screenwidth + image_rect.width(),Util.randFloat(0,screenheight));
                stage = INVERSE; break;

            case INVERSE:
                move(HORIZ_SPEED * dt * -1,0); //move to the left
                //make y the inverse of the actual dachshund
                float inverseY = screenheight - dachs.image_rect.centerY();
                moveTo(image_rect.centerX(), inverseY);

                if(image_rect.right<0){
                    stage = BEGIN;
                    hit=false;
                }

        }
    }

    @Override
    public void updateHitRects(){
        hit_rects[0] = image_rect;
    }



}
