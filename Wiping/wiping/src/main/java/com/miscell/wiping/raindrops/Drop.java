package com.miscell.wiping.raindrops;

/**
 * Created by chenjishi on 15/3/17.
 */
public class Drop {
    public int x, y;
    public float speed;
    public float width, height;

    /** 0:falling, 1:scale */
    public int type;

    public int offset;

    public long startTime;
}
