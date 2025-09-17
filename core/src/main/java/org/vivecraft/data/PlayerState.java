package org.vivecraft.data;

public class PlayerState {
    public double x;
    public double y;
    public double z;

    public double prevX;
    public double prevY;
    public double prevZ;

    public float xRot;
    public float yRot;
    public float prevXRot;
    public float prevYRot;

    public float yHeadRot;
    public float prevYHeadRot;

    public float eyeHeight;

    public PlayerState(
        double x, double y, double z, double prevX, double prevY, double prevZ, float xRot, float yRot,
        float prevXRot, float prevYRot, float yHeadRot, float prevYHeadRot, float eyeHeight)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = prevX;
        this.prevY = prevY;
        this.prevZ = prevZ;
        this.xRot = xRot;
        this.yRot = yRot;
        this.yHeadRot = yHeadRot;
        this.prevXRot = prevXRot;
        this.prevYRot = prevYRot;
        this.prevYHeadRot = prevYHeadRot;
        this.eyeHeight = eyeHeight;
    }
}
