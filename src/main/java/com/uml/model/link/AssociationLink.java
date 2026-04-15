package com.uml.model.link;

import com.uml.model.BasicObject;

import java.awt.*;

/** Association: solid line + filled arrowhead at target. */
public class AssociationLink extends LinkObject { // 關聯連線：實線 + 目標端實心箭頭

    private static final int ARROW_LEN   = 14;                    // 箭頭長度（像素）
    private static final double ARROW_ANGLE = Math.toRadians(25); // 箭頭半角（弧度），控制箭頭的張開程度

    public AssociationLink(BasicObject source, int srcPort, // 建構子：接收起點與終點的物件及 port 索引
                           BasicObject target, int tgtPort) {
        super(source, srcPort, target, tgtPort);            // 委派給父類別建構子
    }

    @Override
    protected void drawArrowHead(Graphics2D g, Point from, Point to) { // 繪製實心填充箭頭（三角形）
        double theta = angle(from, to); // 計算連線方向角

        int x1 = (int) (to.x - ARROW_LEN * Math.cos(theta - ARROW_ANGLE)); // 計算箭頭左翼端點 x
        int y1 = (int) (to.y - ARROW_LEN * Math.sin(theta - ARROW_ANGLE)); // 計算箭頭左翼端點 y
        int x2 = (int) (to.x - ARROW_LEN * Math.cos(theta + ARROW_ANGLE)); // 計算箭頭右翼端點 x
        int y2 = (int) (to.y - ARROW_LEN * Math.sin(theta + ARROW_ANGLE)); // 計算箭頭右翼端點 y

        int[] xs = {to.x, x1, x2}; // 箭頭三角形的 x 座標陣列（頂點、左翼、右翼）
        int[] ys = {to.y, y1, y2}; // 箭頭三角形的 y 座標陣列

        g.setColor(Color.BLACK);    // 設定填色為黑色
        g.fillPolygon(xs, ys, 3);   // 填充實心三角形箭頭
    }
}
