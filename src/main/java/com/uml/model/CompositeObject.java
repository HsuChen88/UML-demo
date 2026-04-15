package com.uml.model;

import com.uml.util.UMLConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CompositeObject extends UMLObject { // 複合物件（群組容器），Composite Pattern 的 Composite 角色

    private final List<UMLObject> children = new ArrayList<>(); // 儲存此群組直接包含的子物件（可以是 BasicObject 或另一個 CompositeObject）

    public CompositeObject(List<UMLObject> members) { // 建構子：以指定物件清單建立群組
        children.addAll(members); // 將所有成員加入子物件清單
    }

    // ── UMLObject contract ────────────────────────────────
    @Override
    public Rectangle getBounds() { // 計算包含「所有」子物件的最小包圍矩形
        if (children.isEmpty()) return new Rectangle(); // 若無子物件則回傳空矩形
        Rectangle r = children.get(0).getBounds(); // 從第一個子物件的邊界開始
        for (int i = 1; i < children.size(); i++) { // 遍歷其餘子物件
            r = r.union(children.get(i).getBounds()); // 將目前矩形與子物件邊界取聯集（擴展包圍矩形）
        }
        return r; // 回傳最終的包圍矩形
    }

    @Override
    public void draw(Graphics2D g) { // 繪製群組：先繪製所有子物件，選取/hover 時再繪製群組外框
        children.forEach(c -> c.draw(g)); // 依序繪製每個子物件（子物件依自身 selected/hovered 決定是否顯示 port）

        if (isSelected() || isHovered()) { // 若群組被選取或懸停：僅顯示外框，不顯示任何 ports
            Rectangle b   = getBounds();
            int       pad = UMLConstants.COMPOSITE_PAD;
            float[]   dash = UMLConstants.DASH_COMPOSITE;
            g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g.setColor(Color.DARK_GRAY);
            g.drawRect(b.x - pad, b.y - pad, b.width + pad * 2, b.height + pad * 2);
            g.setStroke(new BasicStroke(UMLConstants.STROKE_THIN)); // 重置畫筆，避免影響後續繪製
        }
    }

    @Override
    public boolean contains(int px, int py) {   // 點擊測試：使用包圍矩形判斷
        return getBounds().contains(px, py);    // 利用 AWT Rectangle 判斷點是否在包圍矩形內
    }

    @Override
    public void move(int dx, int dy) {          // 移動群組：將位移傳遞給所有子物件
        children.forEach(c -> c.move(dx, dy));  // 每個子物件各自移動（包含巢狀群組會遞迴傳遞）
    }

    @Override
    public void moveTo(int nx, int ny) { // 移動到絕對座標（Undo/Redo 時使用）
        Rectangle current = getBounds(); // 取得目前包圍矩形
        move(nx - current.x, ny - current.y); // 計算相對位移後呼叫 move（避免重複實作）
    }

    // ── CompositeObject own methods ───────────────────────
    public void addChild(UMLObject obj)    { children.add(obj); }    // 新增一個子物件到群組
    public void removeChild(UMLObject obj) { children.remove(obj); } // 從群組移除一個子物件

    public List<UMLObject> getDirectChildren() {       // 取得直接子物件清單（唯讀，防止外部修改）
        return Collections.unmodifiableList(children); // 包裝為不可修改的 List 回傳
    }

}
