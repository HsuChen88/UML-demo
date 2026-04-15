package com.uml.model;

import com.uml.util.HitTestUtil;
import com.uml.util.UMLConstants;

import java.awt.*;
import java.util.List;

public abstract class BasicObject extends UMLObject { // 具有位置、尺寸與 port 的基本 UML 物件（RectObject 與 OvalObject 的共同父類別）

    // 縮放軸鎖定規則：NONE 兩軸均可、LOCK_WIDTH 鎖定寬度、LOCK_HEIGHT 鎖定高度
    public enum ResizeConstraint { NONE, LOCK_WIDTH, LOCK_HEIGHT }

    private int x, y, width, height; // 物件的左上角座標 (x, y) 與尺寸 (width, height)

    private List<Point> portsCache = null;  // port 位置的快取（位置或尺寸改變時失效）

    public BasicObject(int x, int y, int width, int height) { // 建構子：設定初始位置與尺寸
        this.x      = x;
        this.y      = y;
        this.width  = Math.max(width,  UMLConstants.MIN_SIZE); // 確保寬度不低於最小值
        this.height = Math.max(height, UMLConstants.MIN_SIZE); // 確保高度不低於最小值
    }

    // ── UMLObject contract ────────────────────────────────
    @Override
    public void draw(Graphics2D g) { // Template Method：定義繪製流程，子類別實作各步驟
        drawShape(g);                                   // 1. 繪製物件形狀（矩形或橢圓）
        if (isSelected() || isHovered()) drawPorts(g);  // 2. 若選取或懸停則繪製 port 把手
        String name = getLabelName();
        if (name != null && !name.isBlank()) drawLabel(g); // 3. 若有標籤文字則繪製標籤
    }

    @Override
    public boolean contains(int px, int py) { // 預設點擊測試：使用包圍矩形（OvalObject 會覆寫為橢圓方程式）
        return getBounds().contains(px, py);  // 利用 AWT Rectangle 的 contains 方法判斷
    }

    @Override
    public Rectangle getBounds() { // 回傳物件的包圍矩形
        return new Rectangle(x, y, width, height); // 建立並回傳新的 Rectangle 物件
    }

    @Override
    public void move(int dx, int dy) { // 以相對位移移動物件（拖曳時使用）
        x += dx; // 更新 x 座標
        y += dy; // 更新 y 座標
        portsCache = null; // 使 port 快取失效，下次存取時重新計算
    }

    @Override
    public void moveTo(int nx, int ny) { // 移動到絕對座標（Undo/Redo 時使用）
        x = nx; // 直接設定 x 座標
        y = ny; // 直接設定 y 座標
        portsCache = null; // 使 port 快取失效
    }

    // ── Port system ───────────────────────────────────────
    protected abstract List<Point> computePorts(); // 抽象方法：計算所有 port 的絕對座標

    public final List<Point> getPorts() { // 取得 port 清單（使用快取，避免每次都重算）
        if (portsCache == null) {
            portsCache = computePorts();
        }
        return portsCache;
    }

    public Point getPort(int index) { // 取得指定索引的 port 座標
        return getPorts().get(index); // 從快取清單取得指定 port
    }

    public int getNearestPortIndex(Point mouse) { // 找出距離滑鼠最近且在容差範圍內的 port 索引
        List<Point> ports = getPorts();
        for (int i = 0; i < ports.size(); i++) { // 遍歷所有 port
            if (HitTestUtil.isNearPort(mouse, ports.get(i))) return i; // 若滑鼠靠近此 port 則回傳索引
        }
        return -1; // 沒有靠近的 port 則回傳 -1
    }

    // ── Resize constraint API (UC-F) ─────────────────────
    public ResizeConstraint getResizeConstraint(int portIndex) { // 取得指定 port 的縮放軸鎖定規則（預設兩軸均可）
        return ResizeConstraint.NONE;                            // 預設不鎖定任何軸（子類別可覆寫）
    }

    public Point getResizeAnchor(int portIndex) { // 取得指定 port 的縮放錨點（預設左上角）
        return new Point(getX(), getY());         // 預設回傳左上角（子類別應覆寫以回傳正確的對角位置）
    }

    // ── Drawing helpers ───────────────────────────────────
    protected abstract void drawShape(Graphics2D g); // 抽象方法：由子類別實作具體形狀的繪製

    protected void drawPorts(Graphics2D g) { // 繪製所有 port 把手（小黑色方塊）
        g.setColor(Color.BLACK); // 設定 port 顏色為黑色
        int half = UMLConstants.PORT_SIZE / 2; // 計算 port 方塊的半邊長
        for (Point p : getPorts()) { // 遍歷所有 port
            g.fillRect(p.x - half, p.y - half, UMLConstants.PORT_SIZE, UMLConstants.PORT_SIZE); // 以 port 座標為中心繪製方塊
        }
    }

    protected void drawLabel(Graphics2D g) { // 繪製物件標籤文字（置中對齊）
        String name = getLabelName(); // 取得標籤文字
        g.setColor(Color.BLACK); // 設定文字顏色為黑色
        FontMetrics fm = g.getFontMetrics(); // 取得字型度量，用於計算文字寬高
        int tw = fm.stringWidth(name); // 計算文字寬度（像素）
        int th = (fm.getAscent() - fm.getDescent()) / 2; // 計算文字高度修正值（垂直置中）
        int cx = getX() + getWidth()  / 2 - tw / 2; // 計算文字繪製的 x 座標（水平置中）
        int cy = getY() + getHeight() / 2 + th; // 計算文字繪製的 y 座標（垂直置中）
        g.drawString(name, cx, cy); // 在物件中央繪製標籤文字
    }

    public void setBounds(int nx, int ny, int nw, int nh) { // 設定物件的新邊界（縮放時使用）
        x      = nx; // 更新 x 座標
        y      = ny; // 更新 y 座標
        width  = Math.max(nw, UMLConstants.MIN_SIZE); // 更新寬度，確保不低於最小值
        height = Math.max(nh, UMLConstants.MIN_SIZE); // 更新高度，確保不低於最小值
        portsCache = null; // 使 port 快取失效
    }

    // ── Accessors ─────────────────────────────────────────
    public int getX()      { return x; } // 取得物件左邊界的 x 座標
    public int getY()      { return y; } // 取得物件上邊界的 y 座標
    public int getWidth()  { return width; } // 取得物件寬度
    public int getHeight() { return height; } // 取得物件高度
}
