package atk.spacedog;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;


public class Pig extends Boss {

    private int status; //movement status
    float xvector; float yvector;
    float speed;

    private final float X_MARGIN;
    private final float Y_MARGIN;



    public Pig(float swidth, float sheight, BitmapLoader b, Dachshund d){
        super(swidth, sheight, b, d);

        //animation
        unhit_frames.add(bitmapLoader.getBitmap("pigunhit"));
        //unhit_frames.add(bitmapLoader.getBitmap("pighit")); //for now, to check if animation works

        hit_frames.add(bitmapLoader.getBitmap("pighit"));
        seconds_per_frame= 1;
        image_rect = new RectF(0,0, unhit_frames.get(0).getWidth(),
                unhit_frames.get(0).getHeight());

        cur_bitmap = unhit_frames.get(frame); //set default bitmap

        //hitbox rectangles
        hit_rects = new RectF[1];
        updateHitRects();

        //lives
        max_life = 15;
        life = max_life;
        updateLifeBar();

        status = Pig.BEGIN;

        X_MARGIN = screenwidth * (float) 0.025;
        Y_MARGIN = X_MARGIN;
        speed = screenwidth * (float) (0.6);

        name = "pig";

    }

    @Override
    protected void updateHitRects() {
        hit_rects[0] = image_rect;
        //can make this more complicated for  particular bosses if you want

    }

    private static final int BEGIN = 0;
    private static final int CENTERLEFT = 1;
    private static final int CENTERRIGHT = 2;
    private static final int CENTERUP = 3;
    private static final int TOPLEFT = 4;
    private static final int TOPRIGHT = 5;
    private static final int CENTERDOWN = 6;
    private static final int BOTTOMLEFT = 7;
    private static final int BOTTOMRIGHT = 8;
    private static final int BOTTOMUP = 9;

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

        //set movement vector based on status
        switch(status){
            case BEGIN:
                centerAt(image_rect,screenwidth + image_rect.width(), screenheight/2);
                status = CENTERLEFT;
                break;

            case CENTERLEFT:
                move(-1* speed * dt, 0);
                if(image_rect.left<= X_MARGIN){status = CENTERRIGHT;}
                break;

            case CENTERRIGHT:
                move(speed * dt, 0);
                if(screenwidth-image_rect.right <=X_MARGIN){status =CENTERUP;}
                break;

            case CENTERUP:
                move(0, -1 * speed * dt);
                if(image_rect.top <= Y_MARGIN){status = TOPLEFT;}
                break;

            case TOPLEFT:
                move(-1*speed*dt,0);
                if(image_rect.left<= X_MARGIN){status = TOPRIGHT;}
                break;

            case TOPRIGHT:
                move(speed * dt, 0);
                if(screenwidth-image_rect.right <=X_MARGIN){status =CENTERDOWN;}
                break;

            case CENTERDOWN:
                move(0, speed*dt);
                if(screenheight - image_rect.bottom <=Y_MARGIN){status=BOTTOMLEFT;}
                break;

            case BOTTOMLEFT:
                move(-1* speed * dt, 0);
                if(image_rect.left<= X_MARGIN){status = BOTTOMRIGHT;}
                break;

            case BOTTOMRIGHT:
                move(speed * dt, 0);
                if(screenwidth-image_rect.right <=X_MARGIN){status =BOTTOMUP;}
                break;

            case BOTTOMUP:
                move(0, -1 * speed * dt);
                if(image_rect.centerY() <= screenheight/2){status = CENTERLEFT;}
                break;

        }

    }


}
