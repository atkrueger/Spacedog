package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.ArrayList;

/**
 * Created by Alex on 3/2/2017.
 */

public class Farts {
    public ArrayList<RectF> rects; //all there is to it, right?
    //public ArrayList<Integer> xflips;
    private ArrayList<Fart> farts;
    private int xflip;
    private int yflip;

    public final static int FART_COLOR = Color.argb(125,200, 100, 0); //slightly transparent brown
    private final float xvector; //in percentage of screen per second
    private final float yvector;

    private int screenwidth;
    private int screenheight;

    private static final double FARTS_PER_SEC = 20; //max number of farts allowed per  second
    private  final float MAX_FART_WIDTH;
    private double sec_since_fart=0; //time since last additional fart added

    Dachshund dachs;

    Paint paint;

    public Farts(int swidth, int sheight, Dachshund d){
        screenwidth = swidth;
        screenheight = sheight;
        MAX_FART_WIDTH = (float)(sheight * 0.03);
        xvector = (float)(-0.18 * screenwidth);
        yvector = (float)(0.05 * screenheight);
        farts = new ArrayList<Fart>(300);
        rects = new ArrayList<RectF>(300);

        paint = new Paint();
        paint.setColor(FART_COLOR);
        dachs = d;
        xflip = 1;
        yflip = 1;
    }

    public void flipX(){
        xflip *=-1;
        for(RectF rect: rects){
            centerAt(rect,screenwidth-rect.centerX(),rect.centerY());
        }
    }

    public void flipY(){
        yflip*=-1;
        for(RectF rect: rects){
            centerAt(rect,rect.centerX(),screenheight - rect.centerY());
        }
    }

    private void addFart(float x, float y, float width){
        RectF fart_rect = new RectF(x,y,x+width, y+width);
        rects.add(fart_rect);
        farts.add(new Fart(fart_rect,dachs.flippedX, dachs.flippedY));
    }

    private float newFartX(float fart_width){
        //returns appropriate new X position for new fart depending on dachshund orientation
        RectF dachs_rect = dachs.image_rect;
        float newx;
        if(!dachs.flippedX){
            newx =  (float)(dachs_rect.left - dachs_rect.width()*0.1 - fart_width); //a little to the left of the dachshund
        } else{
             newx = (float)(dachs_rect.right + dachs_rect.width()*0.1 - fart_width); // alittle to the right
        }
        return newx;
    }

    private float newFartY(float fart_width){
        RectF dachs_rect = dachs.image_rect;
        float newy;
        if(!dachs.flippedY){
            newy = (float)(dachs_rect.bottom - dachs_rect.height()*0.1); //towards the bottom of the dachshund
        } else{
            newy = (float)(dachs_rect.top + dachs_rect.height()*0.1); //towards the bottom of the dachshund
        }
        return newy;
    }

    public void update(float dt, int avg_volume, int vol_threshold, float sensitivity){

        //add new farts if appropriate
        sec_since_fart+=dt;
        if(avg_volume > vol_threshold){//loud enough
            if(sec_since_fart >= 1/FARTS_PER_SEC){//enough time has elapsed
                RectF dachs_rect = dachs.image_rect;
                sec_since_fart = 0; //reset sec_since fart
                float fart_width = (avg_volume - vol_threshold) * sensitivity  /150; //higher volume = larger fart
                fart_width = Math.min(fart_width, MAX_FART_WIDTH);

                addFart(newFartX(fart_width), newFartY(fart_width), fart_width);
            }
        }

        //move all farts and remove if they have exited the screen
        //float xflip = dachs.flippedX? -1:1;
        //Fart f;
        for (int i = rects.size()-1; i>=0; i--){
            //f = farts.get(i);
            //f.rect.offset(xvector * dt * f.xflip, yvector * dt * f.yflip);
            rects.get(i).offset(xvector * dt *xflip, yvector * dt * yflip);
            //deleted farts that have exited the screen
            if(rects.get(i).right<0 || rects.get(i).left > screenwidth){
                rects.remove(i);
            }
        }
    }

    public void clear(){
        farts.clear();
        rects.clear();
    }

    public void draw(Canvas canvas){
        for(RectF rect:rects){
            canvas.drawRect(rect,paint);
        }

    }

    private void centerAt(RectF rect, float x, float y){
        float width = rect.width();
        float height = rect.height();

        float new_left = x - width/2;
        float new_top = y - height/2;

        rect.left = new_left;
        rect.right = new_left + width;
        rect.top = new_top;
        rect.bottom = new_top + height;

    }


}
