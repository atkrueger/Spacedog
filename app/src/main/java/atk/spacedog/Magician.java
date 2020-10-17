package atk.spacedog;

import android.graphics.RectF;

/**
 * Created by Alex on 3/8/2017.
 */

public class Magician extends Boss {

    int stage;
    final float DASH_SPEED;
    final float RETREAT_SPEED;
    float xvector;
    float yvector;

    public Magician(float swidth, float sheight, BitmapLoader b, Dachshund d){
        super(swidth, sheight, b, d);

        //animation
        unhit_frames.add(bitmapLoader.getBitmap("rabbit"));
        //unhit_frames.add(bitmapLoader.getBitmap("pighit")); //for now, to check if animation works

        hit_frames.add(bitmapLoader.getBitmap("rabbithit"));
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

        DASH_SPEED = screenwidth * 2f;
        RETREAT_SPEED = screenheight * 0.5f;

        name = "magician";

        //maybe change so that, once it gets to half of the screen, it makes a fast dart for your dog?

    }

    private static final int BEGIN = 0;
    private static final int DASH = 1;
    private static final int RETREAT = 2;

    @Override
    public void update(double t){
        super.update(t);
        float dt = (float) t;
        float x; float y;
        switch(stage){
            case BEGIN:
                x = screenwidth + image_rect.width();
                y = Util.randFloat(0,screenheight);
                moveTo(x,y);

                double pi = 3.14;
                double min_angle = pi - Math.atan(y/x);
                double max_angle = pi + Math.atan((screenheight - y)/x);
                double rand = Math.random();
                double new_angle = rand * (max_angle - min_angle) + min_angle;
                xvector = (float)(DASH_SPEED  * Math.cos(new_angle));
                yvector = (float)(-1 * DASH_SPEED  * Math.sin(new_angle)); // bc up is negative

                stage = DASH;
                break;

            case DASH:
                move(xvector * dt, yvector * dt);

                if(image_rect.right < -1 * image_rect.width()*4){
                    xvector *=-0.2f;
                    yvector *=-0.2f; //reverse dashing path, slowly
                    stage = RETREAT;
                }
                break;

            case RETREAT:
                move(xvector * dt, yvector * dt);
                if(image_rect.left > screenwidth){
                    stage = BEGIN;
                }
                break;

        }
    }

    @Override
    public void updateHitRects(){
        hit_rects[0] = image_rect;
    }


}
