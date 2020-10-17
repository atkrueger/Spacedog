package atk.spacedog;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;


public class Balls {

    Paint paint;
    private ArrayList<Ball> balls;

    private static final int YELLOW = Color.argb(255,255,255,153);
    private static final int WHITE = Color.argb(255,255,255,255);

    float screenwidth; float screenheight;

    public float radius;

    public  Balls(float swidth, float sheight){
        screenwidth = swidth;
        screenheight = sheight;
        radius = swidth * (float) 0.015;
        balls = new ArrayList<>();
        paint = new Paint();
    }

    public void addBall(float speed, int ballType){
        balls.add(new Ball(radius,screenwidth,screenheight,speed,ballType));
    }

    public void update(double t, double hballs_per_sec, double iballs_per_sec){

        //add new balls as appropriate
        double h_chance = hballs_per_sec * t;
        if(Math.random() <= h_chance){
            addBall((float) 0.7, Ball.HEALTH);
        }
        double i_chance = iballs_per_sec *t;
        if(Math.random() <=i_chance){
            addBall((float) 0.8, Ball.INVINC);
        }

        //update balls and remove if necessary
        for(int i = balls.size()-1; i>=0; i--){
            balls.get(i).update(t);
            if(balls.get(i).x + radius <0){ //exited left side of the screen
                balls.remove(i);
            }
        }
    }


    public void draw(Canvas canvas) {

        for(Ball b:balls){
            if (b.ballType ==Ball.HEALTH){
                paint.setColor(WHITE);
            } else if (b.ballType == Ball.INVINC){
                paint.setColor(Util.randColor());
            }
            canvas.drawCircle(b.x,b.y, b.radius, paint);
        }
    }

    public int healthBallsCaught(Dachshund dachs){
        //returns number of health balls caught and removes any caught ones
        int caught = 0;
        float distance;
        Ball b;
        for(int i = balls.size()-1; i>=0; i--){
            b = balls.get(i);
            if(b.ballType==Ball.HEALTH){
                distance = Util.distance(b.x,b.y,dachs.image_rect.centerX(), dachs.image_rect.centerY());
                if(distance<radius + dachs.image_rect.width()/2){
                    caught+=1;
                    balls.remove(i);
                }
            }
        }

        return caught;
    }

    public int invincBallsCaught(Dachshund dachs){
        //returns number of health balls caught and removes any caught ones
        int caught = 0;
        float distance;
        Ball b;
        for(int i = balls.size()-1; i>=0; i--){
            b = balls.get(i);
            if(b.ballType==Ball.INVINC){
                distance = Util.distance(b.x,b.y,dachs.image_rect.centerX(), dachs.image_rect.centerY());
                if(distance<radius + dachs.image_rect.width()/2){
                    caught+=1;
                    balls.remove(i);
                }
            }
        }

        return caught;
    }
}
