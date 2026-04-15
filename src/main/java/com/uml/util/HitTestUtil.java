package com.uml.util;

import java.awt.*;

public final class HitTestUtil { // 點擊測試工具類別；final 防止繼承，所有方法皆為靜態

    private HitTestUtil() {} // 私有建構子，禁止實例化（工具類別慣例）

    /** True when {@code mouse} is within PORT_HIT_RADIUS of {@code port} (Chebyshev distance). */
    public static boolean isNearPort(Point mouse, Point port) { // 判斷滑鼠是否靠近指定 port（使用 Chebyshev 距離）
        return Math.abs(mouse.x - port.x) <= UMLConstants.PORT_HIT_RADIUS && // 水平距離在容差範圍內
               Math.abs(mouse.y - port.y) <= UMLConstants.PORT_HIT_RADIUS; // 且垂直距離也在容差範圍內
    }

    /** True when {@code obj} is completely enclosed by {@code selection}. */
    public static boolean isCompletelyInside(Rectangle obj, Rectangle selection) { // 判斷物件是否完全在框選範圍內
        return selection.contains(obj); // 使用 AWT Rectangle.contains(Rectangle) 做包含判斷
    }

    /** Normalise a rectangle so width/height are always positive. */
    public static Rectangle normalise(int x1, int y1, int x2, int y2) { // 將任意兩點轉換為正規化矩形（寬高皆為正）
        int nx = Math.min(x1, x2);  // 取兩點的最小 x 作為矩形左邊界
        int ny = Math.min(y1, y2);  // 取兩點的最小 y 作為矩形上邊界
        int nw = Math.abs(x2 - x1); // 計算寬度（絕對值確保為正）
        int nh = Math.abs(y2 - y1); // 計算高度（絕對值確保為正）
        return new Rectangle(nx, ny, nw, nh); // 回傳正規化後的矩形
    }
}
