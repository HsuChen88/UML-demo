package com.uml.model;

import com.uml.util.UMLConstants;

import java.awt.*;
import java.util.List;

public class RectObject extends BasicObject { // 矩形 UML 物件，繼承自 BasicObject

    public RectObject(int x, int y, int width, int height) { // 建構子：設定初始位置與尺寸
        super(x, y, width, height); // 委派給父類別建構子處理
    }

    /**
     * Port layout (index map):
     *   0(TL)  1(TM)  2(TR)
     *   7(ML)         3(MR)
     *   6(BL)  5(BM)  4(BR)
     */
    @Override
    protected List<Point> computePorts() { // 計算矩形的 8 個 port 絕對座標
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        return List.of( // 回傳不可變的 port 清單（順序固定，索引 0~7）
            new Point(x,         y        ),  // 0 左上
            new Point(x + w / 2, y        ),  // 1 上中
            new Point(x + w,     y        ),  // 2 右上
            new Point(x + w,     y + h / 2),  // 3 右中
            new Point(x + w,     y + h    ),  // 4 右下
            new Point(x + w / 2, y + h    ),  // 5 下中
            new Point(x,         y + h    ),  // 6 左下
            new Point(x,         y + h / 2)   // 7 左中
        );
    }

    @Override
    protected void drawShape(Graphics2D g) {                         // 繪製（填色 + 外框）
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        g.setColor(getLabelColor());                                 // 設定填色（使用物件的 labelColor 欄位）
        g.fillRect(x, y, w, h);                                      // 填充矩形
        g.setColor(Color.BLACK);                                     // 設定外框顏色為黑色
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL));    // 設定外框線寬
        g.drawRect(x, y, w, h);                                      // 繪製矩形外框
    }

    /**
     * Port layout:
     *   0=TL  1=TM  2=TR
     *   7=ML        3=MR
     *   6=BL  5=BM  4=BR
     *
     * Edge-midpoint ports lock one axis during resize.
     */
    @Override
    public ResizeConstraint getResizeConstraint(int portIndex) { // 回傳指定 port 的縮放軸鎖定規則
        return switch (portIndex) { // 根據 port 索引決定鎖定規則
            case 1, 5 -> ResizeConstraint.LOCK_WIDTH;   // TM、BM：拖曳時只改變高度（寬度鎖定）
            case 3, 7 -> ResizeConstraint.LOCK_HEIGHT;  // RM、LM：拖曳時只改變寬度（高度鎖定）
            default   -> ResizeConstraint.NONE; // 角落 port：兩軸均可自由縮放
        };
    }

    /**
     * Returns the fixed anchor point opposite to the dragged port.
     * Port layout (for reference):
     *   0=TL → anchor BR   1=TM → anchor BM   2=TR → anchor BL
     *   7=ML → anchor MR   3=MR → anchor ML
     *   6=BL → anchor TR   5=BM → anchor TM   4=BR → anchor TL
     */
    @Override
    public Point getResizeAnchor(int portIndex) { // 回傳指定 port 縮放時的固定錨點座標
        int x = getX(), y = getY(), w = getWidth(), h = getHeight(); // 取得目前位置與尺寸
        return switch (portIndex) { // 每個 port 對應其對角或對邊中點
            case 0 -> new Point(x + w, y + h);  // TL（左上）→ 固定 BR（右下）
            case 1 -> new Point(x,     y + h);  // TM（上中）→ 固定 BM（下中，x 由 LOCK_WIDTH 鎖定）
            case 2 -> new Point(x,     y + h);  // TR（右上）→ 固定 BL（左下）
            case 3 -> new Point(x,     y    );  // MR（右中）→ 固定 ML（左中，y 由 LOCK_HEIGHT 鎖定）
            case 4 -> new Point(x,     y    );  // BR（右下）→ 固定 TL（左上）
            case 5 -> new Point(x,     y    );  // BM（下中）→ 固定 TM（上中，x 由 LOCK_WIDTH 鎖定）
            case 6 -> new Point(x + w, y    );  // BL（左下）→ 固定 TR（右上）
            case 7 -> new Point(x + w, y    );  // ML（左中）→ 固定 MR（右中，y 由 LOCK_HEIGHT 鎖定）
            default -> new Point(x,     y    ); // 不應發生，預設回傳左上角
        };
    }
}
