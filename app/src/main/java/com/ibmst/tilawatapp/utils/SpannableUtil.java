package com.ibmst.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imranbaig on 25/06/2017.
 */

public class SpannableUtil {

    public static void applyAyatNumbering(final Context context, String text, SpannableStringBuilder sb , int color)
    {

        char start = '﴿';
        char end = '﴾';

        colorChar(text, sb, color, start, 1);
        colorChar(text, sb, color, end, 1);

    }

    public static void colorChar(String text, SpannableStringBuilder sb, int color, char character, int len) {
        List<Integer> finds = new ArrayList<Integer>();

        int find  = text.indexOf( character   , 0);
        while (find!=-1)
        {
            finds.add(find);

            if(find+1<text.length()) {
                find = text.indexOf(character, find + 1);
            }else{
                find = -1;
            }
        }

        for (int i = 0; i < finds.size() ; i++) {
            sb.setSpan(new ForegroundColorSpan(color), finds.get(i), finds.get(i)+len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
