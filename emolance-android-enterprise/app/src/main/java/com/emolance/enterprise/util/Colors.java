package com.emolance.enterprise.util;

import android.graphics.Color;

/**
 * Created by user on 2/25/18.
 */

public class Colors {
    public static int colorPicker(int val){
        if(val <= 2 && val > 0){
            return Color.GREEN;
        }
        else if(val <= 4 && val > 2){
            return Color.YELLOW;
        }
        else if(val <= 6 && val > 4){
            return Color.RED;
        }
        else{
            return Color.GRAY;
        }
    }
}
