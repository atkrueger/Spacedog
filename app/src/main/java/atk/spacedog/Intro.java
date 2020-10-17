package atk.spacedog;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

public class Intro {

    Label title;
    Button start;
    boolean active;
    Paint paint;

    public static final int INACTIVE = 0;
    public static final int ACTIVE =1;
    public static final int START=2;

    public Intro(int screenwidth, int screenheight, Typeface font){
        //intro label
        float title_width = screenwidth * 7/10;
        float title_height = screenheight * 3/10;
        float title_top = screenheight * 1/5;
        float title_left = screenwidth/2 - title_width/2;
        RectF title_rect = new RectF(title_left, title_top, title_left + title_width, title_top + title_height);
        title = new Label(title_rect, "Space Dog Warrior", font);

        //intro start button
        float sens_height = screenheight /6;
        float sens_width = screenwidth * 1/5;
        float sens_left = screenwidth * 1/8 - sens_width/2;
        float sens_top = screenheight/2 - sens_height/2;
        RectF sens_rect = new RectF(sens_left, sens_top, sens_left+sens_width, sens_top+sens_height);

        RectF start_rect = new RectF(sens_rect);
        start_rect.offsetTo(screenwidth/2 - sens_width/2, screenheight*3/4 - sens_height/2);
        start = new Button(start_rect,"Start",font,"startgame");

        active = false;
        paint = new Paint();
    }

    public void draw(Canvas canvas){
        if(!active){return;} //draw only if active
        canvas.drawBitmap(start.bitmap, start.getLeft(), start.getTop(),paint);
        canvas.drawBitmap(title.bitmap, title.getLeft(), title.getTop(), paint);

    }

    public int update(MotionEvent m){
        if(!active){return INACTIVE;}
        int status = ACTIVE; //default status

        int action =  m.getAction() & MotionEvent.ACTION_MASK;
        int cur_pointer_index = m.getActionIndex();
        int cur_pointer_id = m.getPointerId(cur_pointer_index);
        int x; int y;

        switch (action){

            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                x = (int)m.getX(cur_pointer_index);
                y = (int)m.getY(cur_pointer_index);

                if (start.clicked(x,y)){
                    status = START;
                }
                break;
            //end action down events
        }
        return status;
    }

}
