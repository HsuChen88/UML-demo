package com.uml.model.link;

import com.uml.model.BasicObject;

import java.awt.*;

/** Composition: solid line + filled diamond at target. */
public class CompositionLink extends LinkObject { // 組合連線：實線 + 目標端實心菱形

    private static final int DIAMOND_LEN = 14;                   // 菱形半長（從頂點到中心的距離，像素）
    private static final double HALF_ANGLE = Math.toRadians(20); // 菱形半角（弧度），控制菱形的寬窄

    public CompositionLink(BasicObject source, int srcPort, // 建構子：接收起點與終點的物件及 port 索引
                           BasicObject target, int tgtPort) {
        super(source, srcPort, target, tgtPort);            // 委派給父類別建構子
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point from, Point to) { // 繪製實心菱形
        double theta = angle(from, to); // 計算連線方向角

        // tip at 'to', back vertex at distance 2*DIAMOND_LEN
        int bx = (int) (to.x - 2 * DIAMOND_LEN * Math.cos(theta)); // 計算菱形後頂點 x（與 to 相距 2倍菱形長）
        int by = (int) (to.y - 2 * DIAMOND_LEN * Math.sin(theta)); // 計算菱形後頂點 y

        // side vertices
        int lx = (int) (to.x - DIAMOND_LEN * Math.cos(theta - HALF_ANGLE)); // 計算菱形左側頂點 x
        int ly = (int) (to.y - DIAMOND_LEN * Math.sin(theta - HALF_ANGLE)); // 計算菱形左側頂點 y
        int rx = (int) (to.x - DIAMOND_LEN * Math.cos(theta + HALF_ANGLE)); // 計算菱形右側頂點 x
        int ry = (int) (to.y - DIAMOND_LEN * Math.sin(theta + HALF_ANGLE)); // 計算菱形右側頂點 y

        int[] xs = {to.x, lx, bx, rx}; // 菱形四個頂點的 x 座標（前頂、左側、後頂、右側）
        int[] ys = {to.y, ly, by, ry}; // 菱形四個頂點的 y 座標

        g.setColor(Color.BLACK);  // 設定填色為黑色
        g.fillPolygon(xs, ys, 4); // 填充實心菱形（4 個頂點）
    }
}
