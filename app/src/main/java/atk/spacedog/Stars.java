package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;

/**
 * Created by Alex on 3/1/2017.
 */

public class Stars {

    //private float[] x; //list of x coordinates
    //private float[] y; //list of y coordinates
    public RectF[] rects;
    float width;
    int screenwidth;
    int screenheight;
    Paint paint;
    private int flipx;

    final static float pScreenPerSec = (float)0.11;

    public Stars(int num_stars, float star_width, int screenw, int screenh){
        //x = new float[num_stars];
        //y = new float[num_stars];
        rects = new RectF[num_stars];
        for (int i = 0; i <num_stars; i++){ rects[i] = new RectF(0,0,star_width,star_width);}
        screenwidth = screenw;
        screenheight = screenh;
        width = star_width;

        randomizeAll();
        paint = new Paint();
        paint.setColor(Color.argb(255,255,255,153)); //star yellow
        flipx = 1;

    }

    public void randomizeAll(){ //randomizes placement of stars on the screen
        for(int i =0 ; i < rects.length; i++){randomizeStar(i);}
    }

    public void randomizeStar(int i){
        float newx = (float)Math.random() * screenwidth;
        float newy = (float)Math.random() * screenheight;
        rects[i].offsetTo(newx, newy);
    }

    public void update(double dt){
        for (int i = 0; i<rects.length; i++){
            rects[i].offset(-pScreenPerSec * screenwidth * (float) dt * flipx, 0);

            //see if they have exited screen
            if (flipx ==1 && rects[i].right<0){
                rects[i].offsetTo(screenwidth+100, (float)Math.random() * screenheight);
            } else if (flipx == -1 && rects[i].left> screenwidth){
                rects[i].offsetTo(-100, (float) Math.random() * screenheight);
            }
        }

    }

    public void flipX(){
        flipx*=-1;
        for(int i = 0; i <rects.length; i++){
            Util.centerAt(rects[i], screenwidth - rects[i].centerX(), rects[i].centerY());
        }
    }

    public void flipY(){
        for(int i = 0; i <rects.length; i++){
            Util.centerAt(rects[i],rects[i].centerX(), screenheight - rects[i].centerY());
        }
    }

    public void draw(Canvas canvas){
        for(int i = 0; i<rects.length; i++){
            canvas.drawRect(rects[i],  paint);
        }
    }

}
