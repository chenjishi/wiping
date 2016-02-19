package com.miscell.wiping.home;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by chenjishi on 16/1/12.
 */
public class MarginDecoration extends RecyclerView.ItemDecoration {

    private float density;

    public MarginDecoration(Context context) {
        density = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        if (position == RecyclerView.NO_POSITION) return;

        int margin = (int) (density * 8);

        outRect.set(position % 2 == 0 ? margin : margin / 2, margin / 2, position % 2 == 0 ? margin / 2 : margin, margin / 2);
    }
}
