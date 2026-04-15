package com.uml.model.link;

import com.uml.model.BasicObject;
import com.uml.util.UMLConstants;

import java.awt.*;

public abstract class LinkObject { // 所有連線物件的抽象基底類別

    protected final BasicObject source;          // 連線的起點物件（儲存參考而非座標，確保物件移動後連線跟著更新）
    protected final int         sourcePortIndex; // 起點物件的 port 索引
    protected final BasicObject target;          // 連線的終點物件
    protected final int         targetPortIndex; // 終點物件的 port 索引

    public LinkObject(BasicObject source, int sourcePortIndex, // 建構子：接收起點與終點的物件及 port 索引
                      BasicObject target, int targetPortIndex) {
        this.source          = source;
        this.sourcePortIndex = sourcePortIndex;
        this.target          = target;
        this.targetPortIndex = targetPortIndex;
    }

    // ── Template methods ─────────────────────────────────
    public final void draw(Graphics2D g) { // Template Method：繪製連線主體（直線），箭頭委派給子類別
        Point p1 = source.getPort(sourcePortIndex); // 取得起點 port 的目前座標（物件移動後自動更新）
        Point p2 = target.getPort(targetPortIndex); // 取得終點 port 的目前座標

        g.setColor(Color.BLACK); // 設定連線顏色為黑色
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL)); // 設定連線線寬
        g.drawLine(p1.x, p1.y, p2.x, p2.y); // 繪製從起點到終點的直線

        drawArrowHead(g, p1, p2); // 呼叫子類別方法繪製對應的箭頭（Template Method hook）
    }

    protected abstract void drawArrowHead(Graphics2D g, Point from, Point to); // 抽象方法：由子類別實作各自的箭頭繪製

    // ── helpers ──────────────────────────────────────────
    protected double angle(Point from, Point to) { // 計算從 from 到 to 的方向角（弧度）
        return Math.atan2(to.y - from.y, to.x - from.x); // 使用 atan2 計算向量角度（考慮四個象限）
    }

    public BasicObject getSource() { return source; } // 取得連線起點物件
    public BasicObject getTarget() { return target; } // 取得連線終點物件
}
