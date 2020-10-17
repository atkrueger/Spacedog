package atk.spacedog;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

public class Tunnel {

    private ArrayList<RectF>  rects;

    float screenwidth; float screenheight;

    private final float SPEED;
    private final float WIDTH;
    private final float MAXSHIFT;

    Paint paint;

    public Tunnel(float swidth, float sheight){
        screenwidth = swidth;
        screenheight = sheight;

        rects= new ArrayList<>();

        SPEED = swidth * (float) 0.75;
        WIDTH = swidth * (float) 0.01; //width of each column
        MAXSHIFT =  (float) 0.02; //maximum difference in column heights from one to the next
        paint = new Paint();
        paint.setARGB(255,125,125,125); //grey, I think
    }

    public void addSection(double secondslong, double margin){
        addSection(secondslong, margin, MAXSHIFT);
    }

    public void addSection(double secondslong, double margin, float maxshift){

        double screenlengths = SPEED * secondslong / screenwidth;
        int num_columns = (int)(screenlengths * screenwidth / WIDTH);
        RectF[] top = new RectF[num_columns];
        RectF[] bottom = new RectF[num_columns];


        //randomized tunnel
        float curx = screenwidth+ WIDTH; //start just off the edge of the screen

        //create the first columns, with tunnel starting dead centered
        //make tunnel start random instead
        float min_tops_bottom = 0;
        float max_tops_bottom = (float)(1-margin)*screenheight;
        float tops_bottom = randFloat(min_tops_bottom, max_tops_bottom);
        float bottoms_top = (float)(tops_bottom + screenheight * margin);

        top[0] = new RectF(curx, 0,curx+WIDTH, tops_bottom);
        bottom[0] = new RectF(curx, bottoms_top,curx+WIDTH, screenheight);
        curx+=WIDTH;
        float change;
        for(int i = 1; i < num_columns; i++){
            change = randFloat(-1 * maxshift * screenheight, maxshift * screenheight);
            tops_bottom+=change; //tunnel undulation

            //restrict to top and bottom
            if(tops_bottom<=0){tops_bottom=0;}
            if(tops_bottom + screenheight*margin > screenheight){tops_bottom = max_tops_bottom;}

            bottoms_top = (float)(tops_bottom + screenheight * margin);
            top[i] = new RectF(curx,0, curx+WIDTH, tops_bottom);
            bottom[i] = new RectF(curx,bottoms_top, curx+WIDTH, screenheight);

            curx+=WIDTH;
        }

        //load into your list
        for(int i = 0; i <num_columns; i++){
            rects.add(top[i]);
            rects.add(bottom[i]);
        }
    }

    public void update(double dt){
        for(int i = rects.size()-1; i>=0; i--){
            rects.get(i).offset(SPEED * -1 * (float)dt,0);
            if(rects.get(i).right<0){
                rects.remove(i);
            }
        }

    }

    private float randFloat(float min, float max){
        return min + (float) Math.random() * (max-min);
    }

    public void draw (Canvas canvas){
        for(RectF r: rects){
            if(r.left<screenwidth) {
                canvas.drawRect(r, paint);
            }
        }

    }

    public void clear(){
        rects.clear();
    }

    public boolean hitDachs(Dachshund dachs){
        //should be going from left to right - only need to check the left-most
        for (RectF r : rects) {
            if (r.left > dachs.hit_rects[0].right) {
                return false; //the rest of the tunnel is too far to the right
            }
            if(RectF.intersects(r, dachs.hit_rects[0])){return true;}
        }

        return false;//shouldn't ever be reached, but there just in case
    }
}
