package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class Homing extends Boss{

    int stage;
    final float HORIZ_SPEED;
    final float VERT_SPEED;

    public Homing(float swidth, float sheight, BitmapLoader b, Dachshund d){
        super(swidth, sheight, b, d);

        //animation
        unhit_frames.add(bitmapLoader.getBitmap("catunhit"));
        //unhit_frames.add(bitmapLoader.getBitmap("pighit")); //for now, to check if animation works

        hit_frames.add(bitmapLoader.getBitmap("cathit"));
        seconds_per_frame= 1;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());

        cur_bitmap = unhit_frames.get(frame); //set default bitmap

        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 20;
        life = max_life;
        updateLifeBar();

        stage = BEGIN;

        HORIZ_SPEED = screenwidth * (float) 0.8;
        VERT_SPEED = screenheight * (float) 0.4;

        name = "homing";

    }

    private static final int BEGIN = 0;
    private static final int HOMING = 1;

    @Override
    public void update(double t){
        super.update(t);
        float dt = (float) t;

        switch(stage){
            case BEGIN:
                moveTo(screenwidth + image_rect.width(),randFloat(0,screenheight));
                stage = HOMING; break;

            case HOMING:
                boolean dachs_started_above_boss = dachs.image_rect.centerY() < image_rect.centerY();
                int dir = dachs_started_above_boss? -1:1;

                float dx = -1 * HORIZ_SPEED * dt;
                float dy = dir * VERT_SPEED * dt;
                move(dx, dy);

                //check if overshot, if so, center with the dachshund
                boolean dachs_ended_above_boss = dachs.image_rect.centerY() < image_rect.centerY();
                if(dachs_started_above_boss != dachs_ended_above_boss){
                    moveTo(image_rect.centerX(), dachs.image_rect.centerY());
                }

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

    private float randFloat(float min, float max){
        return min + (float) Math.random() * (max-min);
    }

}
