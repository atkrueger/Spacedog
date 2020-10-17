package atk.spacedog;

import android.graphics.Bitmap;

/**
 * Created by Alex on 3/12/2017.
 */

public class Bullet extends Obstacle {

    private final float HORIZ_SPEED;

    public Bullet(Bitmap b, double spd, float swidth, float sheight, float startx, float starty){
        super(b,999,1,spd,swidth,sheight);
        HORIZ_SPEED = swidth * (float) spd;

        moveTo(startx,starty);

    }

    @Override
    public void reset(){
        lives--;
    }

    @Override
    public void update(double dt){
        float t = (float) dt;
        move(-1 * HORIZ_SPEED * t, 0);
        if(image_rect.right <0){
            reset();}
    }


}
