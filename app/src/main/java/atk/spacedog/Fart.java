package atk.spacedog;

import android.graphics.RectF;

/**
 * Created by Alex on 3/11/2017.
 */

public class Fart {

    RectF rect;
    int xflip;
    int yflip;

    public Fart(RectF r, boolean xflipped, boolean yflipped){
        rect = r;
        xflip = xflipped? -1:1;
        yflip = yflipped? -1:1;
    }
}
