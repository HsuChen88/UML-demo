package com.uml.model.link;

import com.uml.model.BasicObject;
import com.uml.util.UMLConstants;

import java.awt.*;

/** Generalization: solid line + hollow triangle arrowhead at target. */
public class GeneralizationLink extends LinkObject { // 繼承連線：實線 + 目標端空心三角形箭頭

    private static final int ARROW_LEN   = 16;                    // 箭頭長度（像素），比關聯線稍長以區分
    private static final double ARROW_ANGLE = Math.toRadians(22); // 箭頭半角（弧度），比關聯線稍窄

    public GeneralizationLink(BasicObject source, int srcPort, // 建構子：接收起點與終點的物件及 port 索引
                              BasicObject target, int tgtPort) {
        super(source, srcPort, target, tgtPort);               // 委派給父類別建構子
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point from, Point to) { // 繪製空心三角形箭頭
        double theta = angle(from, to); // 計算連線方向角

        int x1 = (int) (to.x - ARROW_LEN * Math.cos(theta - ARROW_ANGLE)); // 計算箭頭左翼端點 x
        int y1 = (int) (to.y - ARROW_LEN * Math.sin(theta - ARROW_ANGLE)); // 計算箭頭左翼端點 y
        int x2 = (int) (to.x - ARROW_LEN * Math.cos(theta + ARROW_ANGLE)); // 計算箭頭右翼端點 x
        int y2 = (int) (to.y - ARROW_LEN * Math.sin(theta + ARROW_ANGLE)); // 計算箭頭右翼端點 y

        int[] xs = {to.x, x1, x2}; // 箭頭三角形的 x 座標陣列
        int[] ys = {to.y, y1, y2}; // 箭頭三角形的 y 座標陣列

        g.setColor(Color.WHITE);    // 先填白色（遮住底下的連線，製造空心效果）
        g.fillPolygon(xs, ys, 3);   // 填充白色三角形
        g.setColor(Color.BLACK);    // 切換為黑色繪製外框
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL)); // 設定外框線寬
        g.drawPolygon(xs, ys, 3);   // 繪製三角形外框（空心箭頭效果）
    }
}
