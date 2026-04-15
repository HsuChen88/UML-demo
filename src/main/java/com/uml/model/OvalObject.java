package com.uml.model;

import com.uml.util.UMLConstants;

import java.awt.*;
import java.util.List;

public class OvalObject extends BasicObject { // 橢圓 UML 物件，繼承自 BasicObject

    public OvalObject(int x, int y, int width, int height) { // 建構子：設定初始位置與尺寸
        super(x, y, width, height); // 委派給父類別建構子處理
    }

    /** 4 ports: top, right, bottom, left */
    @Override
    protected List<Point> computePorts() { // 計算橢圓的 4 個基點 port 座標（上、右、下、左）
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        return List.of( // 回傳不可變的 port 清單（順序固定，索引 0~3）
            new Point(x + w / 2, y        ),  // 0 上（橢圓頂端）
            new Point(x + w,     y + h / 2),  // 1 右（橢圓最右端）
            new Point(x + w / 2, y + h    ),  // 2 下（橢圓底端）
            new Point(x,         y + h / 2)   // 3 左（橢圓最左端）
        );
    }

    @Override
    protected void drawShape(Graphics2D g) { // 繪製橢圓形狀（填色 + 外框）
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        g.setColor(getLabelColor());    // 設定填色（使用物件的 labelColor 欄位）
        g.fillOval(x, y, w, h);         // 填充橢圓
        g.setColor(Color.BLACK);        // 設定外框顏色為黑色
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL));    // 設定外框線寬
        g.drawOval(x, y, w, h);         // 繪製橢圓外框
    }

    /** Oval hit-test uses the ellipse equation. */
    @Override
    public boolean contains(int px, int py) { // 覆寫點擊測試：使用橢圓方程式（比矩形包圍框更精確）
        double cx = getX() + getWidth()  / 2.0; // 橢圓中心 x
        double cy = getY() + getHeight() / 2.0; // 橢圓中心 y
        double rx = getWidth()  / 2.0; // 橢圓水平半徑
        double ry = getHeight() / 2.0; // 橢圓垂直半徑
        double dx = (px - cx) / rx; // 點相對於橢圓的標準化 x 距離
        double dy = (py - cy) / ry; // 點相對於橢圓的標準化 y 距離
        return dx * dx + dy * dy <= 1.0; // 橢圓方程式：(dx²+dy² ≤ 1) 表示點在橢圓內
    }

    /**
     * Port layout: 
     *      0=T, 
     * 3=L,     1=R,
     *      2=B
     * Cardinal ports each lock one axis during resize.
     */
    @Override
    public ResizeConstraint getResizeConstraint(int portIndex) { // 回傳指定 port 的縮放軸鎖定規則
        return switch (portIndex) { // 根據 port 索引決定鎖定規則
            case 0, 2 -> ResizeConstraint.LOCK_WIDTH;   // T、B：拖曳時只改變高度
            case 1, 3 -> ResizeConstraint.LOCK_HEIGHT;  // R、L：拖曳時只改變寬度
            default   -> ResizeConstraint.NONE;         // 不應發生（橢圓只有 4 個 port）
        };
    }

    /**
     * Returns the fixed anchor opposite to the dragged port.
     *   0=T → anchor B   1=R → anchor L
     *   2=B → anchor T   3=L → anchor R
     */
    @Override
    public Point getResizeAnchor(int portIndex) { // 回傳指定 port 縮放時的固定錨點座標
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        return switch (portIndex) { // 每個 port 對應其對邊中點
            case 0 -> new Point(x + w / 2, y + h    );  // T（上）→ 固定 B（下）
            case 1 -> new Point(x,         y + h / 2);  // R（右）→ 固定 L（左）
            case 2 -> new Point(x + w / 2, y        );  // B（下）→ 固定 T（上）
            case 3 -> new Point(x + w,     y + h / 2);  // L（左）→ 固定 R（右）
            default -> new Point(x,        y        ); // 不應發生，預設回傳左上角
        };
    }
}
