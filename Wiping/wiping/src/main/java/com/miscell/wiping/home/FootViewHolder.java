package com.miscell.wiping.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.miscell.wiping.R;

/**
 * Created by chenjishi on 16/2/4.
 */
public class FootViewHolder extends RecyclerView.ViewHolder {

    public TextView mFootText;

    public FootViewHolder(View itemView) {
        super(itemView);
        mFootText = (TextView) itemView.findViewById(R.id.loading_text);
    }
}
