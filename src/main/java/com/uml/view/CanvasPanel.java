package com.uml.view;

import com.uml.command.Command;
import com.uml.command.CommandHistory;
import com.uml.command.GroupCommand;
import com.uml.command.UngroupCommand;
import com.uml.controller.EditorMode;
import com.uml.controller.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;
import com.uml.controller.strategy.CreateLinkStrategy;
import com.uml.controller.strategy.CreateObjectStrategy;
import com.uml.controller.strategy.SelectStrategy;
import com.uml.model.BasicObject;
import com.uml.model.CompositeObject;
import com.uml.model.UMLObject;
import com.uml.model.link.LinkObject;
import com.uml.util.UMLConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The drawing canvas: owns the object/link lists, delegates all mouse
 * events to the active CanvasMouseStrategy, and paints everything.
 */
public class CanvasPanel extends JPanel { // 繼承 JPanel，作為可繪製的畫布元件

    // ── State ─────────────────────────────────────────────
    /** Render order: higher index = top layer (drawn last / hit-tested first). */
    private final List<UMLObject>  objects = new ArrayList<>(); // 儲存所有 UML 物件（矩形、橢圓、群組）
    private final List<LinkObject> links   = new ArrayList<>(); // 儲存所有連線物件

    private CanvasMouseStrategy currentStrategy;                    // 目前使用中的滑鼠策略
    private final Map<EditorMode, CanvasMouseStrategy> strategyMap; // 模式 → 策略的對應表

    private UMLObject hoveredObject = null; // 滑鼠目前懸停的物件（用於顯示 port）
    private Rectangle rubberBand    = null; // 框選矩形（null 表示未框選）
    private Point     tempLinkEnd   = null; // 拉線時的暫時終點（用於繪製預覽虛線）

    // ── Command history ───────────────────────────────────
    private final CommandHistory history = new CommandHistory(); // 命令歷史，支援 Undo/Redo

    public CanvasPanel(ModeManager modeManager) { // 建構子，接收模式管理器
        setBackground(Color.WHITE); // 畫布背景設為白色
        setPreferredSize(new Dimension(UMLConstants.CANVAS_W, UMLConstants.CANVAS_H)); // 設定畫布大小

        strategyMap = new EnumMap<>(EditorMode.class); // 用 EnumMap 建立模式→策略的對應表（效能優於 HashMap）
        strategyMap.put(EditorMode.SELECT,         new SelectStrategy()); // 選取模式對應選取策略
        strategyMap.put(EditorMode.RECT,           new CreateObjectStrategy(EditorMode.RECT, modeManager)); // 矩形模式
        strategyMap.put(EditorMode.OVAL,           new CreateObjectStrategy(EditorMode.OVAL, modeManager)); // 橢圓模式
        strategyMap.put(EditorMode.ASSOCIATION,    new CreateLinkStrategy(EditorMode.ASSOCIATION));         // 關聯線模式
        strategyMap.put(EditorMode.GENERALIZATION, new CreateLinkStrategy(EditorMode.GENERALIZATION));      // 繼承線模式
        strategyMap.put(EditorMode.COMPOSITION,    new CreateLinkStrategy(EditorMode.COMPOSITION));         // 組合線模式

        currentStrategy = strategyMap.get(EditorMode.SELECT); // 預設為選取模式

        modeManager.addListener((newMode, prev) -> // 監聽模式切換事件
                currentStrategy = strategyMap.get(newMode)); // 切換時更新目前策略

        addMouseListener(new MouseAdapter() { // 註冊滑鼠事件監聽器
            @Override
            public void mousePressed(MouseEvent e) { // 滑鼠按下
                System.out.println("[Event] mousePressed  @ " + e.getPoint()); // debug：印出事件名稱與座標
                currentStrategy.onPressed(e, CanvasPanel.this); // 委派給目前策略處理
            }

            @Override
            public void mouseExited(MouseEvent e) { // 滑鼠離開畫布時清除懸停狀態
                clearHover();
            }

            @Override
            public void mouseReleased(MouseEvent e) { // 滑鼠放開
                System.out.println("[Event] mouseReleased @ " + e.getPoint()); // debug：印出事件名稱與座標
                currentStrategy.onReleased(e, CanvasPanel.this); // 委派給目前策略處理
            }

            @Override
            public void mouseClicked(MouseEvent e) { // 滑鼠點擊（pressed + released 在同一位置）
                System.out.println("[Event] mouseClicked  @ " + e.getPoint()); // debug：印出事件名稱與座標
                currentStrategy.onClicked(e, CanvasPanel.this); // 委派給目前策略處理
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() { // 註冊滑鼠移動事件監聽器
            @Override
            public void mouseDragged(MouseEvent e) { // 滑鼠拖曳（按住移動）
                System.out.println("[Event] mouseDragged  @ " + e.getPoint()); // debug：印出事件名稱與座標
                currentStrategy.onDragged(e, CanvasPanel.this); // 委派給目前策略處理
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoverAt(e.getX(), e.getY()); // 由畫布統一維護懸停狀態
                currentStrategy.onMoved(e, CanvasPanel.this); // 再委派給策略做模式專屬邏輯
            }
        });
    }

    // ── Painting ──────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) { // 覆寫 Swing 繪製方法，每次 repaint() 時呼叫
        super.paintComponent(g); // 先呼叫父類別清除背景
        Graphics2D g2d = (Graphics2D) g.create(); // 建立 Graphics2D 副本（避免影響原始 context）
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 開啟反鋸齒，讓線條更平滑

        objects.forEach(obj -> obj.draw(g2d)); // 依序繪製所有 UML 物件（索引越大越在上層）
        links.forEach(lnk -> lnk.draw(g2d)); // 繪製所有連線（畫在物件之後，避免被遮擋）

        if (tempLinkEnd != null && currentStrategy instanceof CreateLinkStrategy cls) { // 若正在拉線且有起點
            BasicObject src = cls.getSourceObject(); // 取得連線的起點物件
            if (src != null) { // 確認起點存在
                Point p1 = src.getPort(cls.getSourcePortIndex()); // 取得起點 port 的座標
                g2d.setStroke(new BasicStroke(UMLConstants.STROKE_PREVIEW, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, UMLConstants.DASH_PREVIEW, 0f)); // 設定虛線筆刷
                g2d.setColor(Color.GRAY); // 預覽線用灰色
                g2d.drawLine(p1.x, p1.y, tempLinkEnd.x, tempLinkEnd.y); // 畫出起點到滑鼠位置的預覽線
            }
        }

        if (rubberBand != null) { // 若框選矩形存在
            g2d.setStroke(new BasicStroke(UMLConstants.STROKE_PREVIEW, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10f, UMLConstants.DASH_PREVIEW, 0f)); // 設定虛線筆刷
            g2d.setColor(Color.BLUE); // 框選矩形用藍色
            g2d.drawRect(rubberBand.x, rubberBand.y, rubberBand.width, rubberBand.height); // 繪製框選矩形
        }

        g2d.dispose(); // 釋放 Graphics2D 副本資源
    }

    // ── Command API ───────────────────────────────────────
    public void execute(Command cmd) { // 執行並記錄命令（模型尚未更新時使用）
        cmd.redo(); // 呼叫 redo() 將命令效果套用到模型
        history.push(cmd); // 將命令推入歷史堆疊，供之後 undo 使用
    }

    public void pushHistory(Command cmd) { // 只記錄命令（模型已事先更新時使用）
        history.push(cmd); // 直接推入歷史，不再執行 redo()
    }

    public void undo() { history.undo(); } // 呼叫歷史的 undo，還原上一個命令
    public void redo() { history.redo(); } // 呼叫歷史的 redo，重做下一個命令
    public boolean canUndo() { return history.canUndo(); } // 回傳是否可 undo
    public boolean canRedo() { return history.canRedo(); } // 回傳是否可 redo

    public void simulateRelease(int x, int y) { // 合成 mouseReleased 並委派給目前策略（供 glass pane 使用）
        MouseEvent synthetic = new MouseEvent(this, MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(), 0, x, y, 1, false); // 建立合成的滑鼠放開事件
        currentStrategy.onReleased(synthetic, this); // 委派給目前策略處理
    }

    // ── Raw API (used ONLY by Command.undo/redo) ─────────────
    public void rawAddObject(UMLObject obj)    { objects.add(obj); } // 直接新增物件（不 repaint，給 Command 內部使用）
    public void rawRemoveObject(UMLObject obj) { objects.remove(obj); } // 直接移除物件（不 repaint，給 Command 內部使用）
    public void rawAddLink(LinkObject lnk)     { links.add(lnk); } // 直接新增連線（不 repaint，給 Command 內部使用）
    public void rawRemoveLink(LinkObject lnk)  { links.remove(lnk); } // 直接移除連線（不 repaint，給 Command 內部使用）

    // ── Public object/link API (used by Strategies) ──────
    public void addObject(UMLObject obj)   { objects.add(obj);    repaint(); } // 新增物件並重繪畫布
    public void addLink  (LinkObject lnk)  { links.add(lnk);      repaint(); } // 新增連線並重繪畫布
    public void removeObject(UMLObject obj){ objects.remove(obj); repaint(); } // 移除物件並重繪畫布

    /** Move object to end of list so it renders on top. */
    public void bringToFront(UMLObject obj) { // 將物件移到清單末端，使其渲染在最上層
        objects.remove(obj); // 先從原位置移除
        objects.add(obj); // 再加回清單末端（最上層）
    }

    /** Hit-test from topmost object downward (reverse list order). */
    public UMLObject findObjectAt(int x, int y) { // 從最上層往下做點擊測試，回傳第一個命中的物件
        for (int i = objects.size() - 1; i >= 0; i--) { // 從清單末端（最上層）往前遍歷
            if (objects.get(i).contains(x, y)) return objects.get(i); // 若物件包含該點則回傳
        }
        return null; // 沒有物件被點到則回傳 null
    }

    /**
     * Find a BasicObject (not Composite) whose port is near (x, y).
     * Returns null if none found.
     */
    public BasicObject findBasicObjectNearPort(int x, int y) { // 找出 port 靠近指定座標的 BasicObject
        Point mouse = new Point(x, y); // 將座標包裝成 Point 物件
        for (int i = objects.size() - 1; i >= 0; i--) { // 從最上層往下遍歷
            UMLObject obj = objects.get(i); // 取得當前物件
            if (obj instanceof BasicObject bo && bo.getNearestPortIndex(mouse) != -1) { // 若是 BasicObject 且有靠近的 port
                return bo; // 回傳該物件
            }
        }
        return null; // 沒有符合的物件則回傳 null
    }

    public List<UMLObject> getSelectedObjects() { // 回傳所有已選取的物件清單
        return objects.stream().filter(UMLObject::isSelected).collect(Collectors.toList()); // 過濾出 isSelected == true 的物件
    }

    public void clearSelection() { // 清除所有物件的選取狀態
        objects.forEach(o -> o.setSelected(false)); // 對每個物件呼叫 setSelected(false)
    }

    // ── Use Case D (Group / Ungroup) ──────────────────────
    public void group() { // 將目前選取的物件群組化
        List<UMLObject> selected = getSelectedObjects(); // 取得所有已選取的物件
        if (selected.size() < 2) return; // 群組需要至少 2 個物件，否則直接返回

        CompositeObject composite = new CompositeObject(selected); // 建立複合物件，包含所有選取物件
        // Apply manually (model first, then record)
        objects.removeAll(selected); // 從清單移除原本的個別物件
        objects.add(composite); // 將複合物件加入清單
        clearSelection(); // 清除 canvas.objects 中的選取狀態
        selected.forEach(o -> o.setSelected(false)); // 清除子物件的選取旗標（子物件已不在 objects 清單，clearSelection 無法觸及）
        composite.setSelected(true); // 選取新建立的群組

        pushHistory(new GroupCommand(this, selected, composite)); // 記錄群組命令，供 Undo 使用
        repaint(); // 重繪畫布
    }

    public void ungroup() { // 將選取的群組解散，還原為個別物件
        List<UMLObject> selected = getSelectedObjects(); // 取得所有已選取的物件
        if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject composite)) return; // 必須恰好選取一個複合物件

        List<UMLObject> children = new ArrayList<>(composite.getDirectChildren()); // 取得群組的直接子物件清單
        // Apply manually (model first, then record)
        objects.remove(composite); // 從清單移除複合物件
        objects.addAll(children); // 將子物件逐一加回清單
        clearSelection(); // 清除所有選取狀態

        pushHistory(new UngroupCommand(this, composite, children)); // 記錄解散命令，供 Undo 使用
        repaint(); // 重繪畫布
    }

    // ── Accessors used by strategies ─────────────────────
    public List<UMLObject> getObjects()          { return Collections.unmodifiableList(objects); } // 回傳唯讀物件清單，防止外部直接修改
    public UMLObject       getHoveredObject()    { return hoveredObject; } // 回傳目前懸停的物件

    public void setHoveredObject(UMLObject obj)  { hoveredObject = obj; } // 設定懸停物件（由策略在 mouseMoved 時呼叫）
    public void setRubberBand(Rectangle r)       { rubberBand = r; repaint(); } // 設定框選矩形並重繪（null 表示結束框選）
    public void setTempLinkEnd(Point p)          { tempLinkEnd = p; repaint(); } // 設定拉線終點並重繪（null 表示結束拉線）

    /** Updates hovered object by hit-testing current mouse position. */
    public void updateHoverAt(int x, int y) {
        UMLObject hovered = findObjectAt(x, y);
        if (hovered == hoveredObject) return;

        if (hoveredObject != null) hoveredObject.setHovered(false);
        if (hovered != null) hovered.setHovered(true);
        hoveredObject = hovered;
        repaint();
    }

    /** Clears current hover state, e.g. when mouse leaves the canvas. */
    public void clearHover() {
        if (hoveredObject == null) return;
        hoveredObject.setHovered(false);
        hoveredObject = null;
        repaint();
    }
}
