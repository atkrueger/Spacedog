package atk.spacedog;

import android.graphics.Canvas;

/**
 * Created by Alex on 3/8/2017.
 */

public class Ball {

    int ballType; //health, invincibility, etc.
    public static final int HEALTH = 0;
    public static final int INVINC = 1;

    float radius;
    float x;
    float y;

    float xvector;
    float yvector;

    float speed;

    public Ball(float rad, float screenwidth, float screenheight , float spd, int btype){
        radius = rad;
        ballType = btype;

        speed = spd;

        //set random x and y location off to the side of the screen
        x = screenwidth + radius*2;
        y = Util.randFloat(0,screenheight);

        //set random diagonal angle, ensuring it reaches the left edge of the screen
        double pi = 3.14;
        double min_angle = pi - Math.atan(y/x);
        double max_angle = pi + Math.atan((screenheight - y)/x);
        double rand = Math.random();
        double new_angle = rand * (max_angle - min_angle) + min_angle;
        xvector = (float)(speed * screenwidth * Math.cos(new_angle));
        yvector = (float)(-1 * speed * screenwidth * Math.sin(new_angle)); // bc up is negative
    }

    public void update(double t){
        float dt = (float) t;
        x+= xvector * dt;
        y+= yvector * dt;
    }

}
