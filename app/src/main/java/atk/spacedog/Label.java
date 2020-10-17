package atk.spacedog;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class Label {
    public RectF rect;
    String text;
    public Bitmap bitmap;
    public boolean visible;
    private Paint paint;

    public Label(RectF area, String labeltext, Typeface font){
        //goal is a bitmap that is transparent everywhere besides the outline and the text
        visible = false; //defaults to not being visible

        rect = area;
        text = labeltext;

        Bitmap transparent = Bitmap.createBitmap((int)area.width(), (int)area.height(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(transparent);

        //drawing text to canvas

        paint = new Paint();

        paint.setTypeface(font);
        int font_size = (int)(area.height()*0.9f); //max font size, will shrink if necessary
        paint.setTextSize(font_size); //apparently text height = text size in pixels

        //check if font is too big, if so, shrink by 10% until small enough
        boolean too_big = paint.measureText(labeltext)> area.width();
        while(too_big) {
            font_size *= 0.9;
            paint.setTextSize(font_size);
            too_big = paint.measureText(labeltext) > area.width();
        }
        //Log.d("font", "exited while(too_big) loop");
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setARGB(255,255,255,255); //white
        int yPos = (int)((c.getHeight()/2) - ((paint.descent() + paint.ascent())/2));
        c.drawText(labeltext,c.getWidth()/2, yPos, paint);
        bitmap=transparent;
    }

    public float getLeft(){return rect.left;}
    public float getTop(){return rect.top;}

    public void draw(Canvas canvas){
        canvas.drawBitmap(bitmap,rect.left,rect.top,paint);
    }
}

