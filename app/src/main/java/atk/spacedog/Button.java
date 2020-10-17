package atk.spacedog;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class Button {
    public RectF rect;
    String text;
    public Bitmap bitmap;

    public boolean is_sliding;
    public int pointer_id;
    public float offset;
    public String button_id; //unique id given to each button to see which has been clicked
    public boolean visible;

    //how it can be modified
    boolean slidable = false; //default value
    boolean pressable = false; //default value

    Paint p;

    public Button(RectF area, String buttontext, Typeface font, String id){
        //goal is a bitmap that is transparent everywhere besides the outline and the text
        button_id = id;
        is_sliding=false; //starts by not being sliding
        pointer_id = -1; //no touch pointer assigned yet
        offset = 0; //no touchdown, so no offset yet
        visible = false; //defaults to not being visible


        rect = area;
        text = buttontext;

        Bitmap transparent = Bitmap.createBitmap((int)area.width(), (int)area.height(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(transparent);

        //drawing text to canvas

        p = new Paint();

        p.setTypeface(font);
        int font_size = (int)(area.height() * 3/4); //max font size, will shrink if necessary
        p.setTextSize(font_size); //apparently text height = text size in pixels

        //check if font is too big, if so, shrink by 10% until small enough
        boolean too_big = p.measureText(buttontext)> (area.width() * 0.8);
        while(too_big) {
            //Log.d("font", "Font was too big at (" + String.valueOf(font_size) + ")");
            font_size *= 0.9;
            p.setTextSize(font_size);
            //Log.d("font", "New font size:" + String.valueOf(font_size));
            too_big = p.measureText(buttontext) > (area.width() * 0.8);
        }
        //Log.d("font", "exited while(too_big) loop");
        p.setTextAlign(Paint.Align.CENTER);
        p.setARGB(255,255,255,255); //white
        int yPos = (int)((c.getHeight()/2) - ((p.descent() + p.ascent())/2));
        c.drawText(buttontext,c.getWidth()/2, yPos, p);


        //we also need to draw the outlining rectangle
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(10);
        RectF outline_rect = new RectF(0,0,c.getWidth(), c.getHeight());
        c.drawRect(outline_rect, p);
        bitmap = transparent; //now has had all this stuff drawn to it.
    }

    public boolean clicked(int x, int y){
        return rect.contains(x,y);
    }
    public float getLeft(){return rect.left;}
    public float getTop(){return rect.top;}
    public float midX(){return rect.centerX();}
    public float midY(){return rect.centerY();}
    public float getBottom(){return rect.bottom;}

    public void draw(Canvas canvas){
        canvas.drawBitmap(bitmap,rect.left, rect.top,p);
    }
}
