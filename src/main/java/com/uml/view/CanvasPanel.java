package com.uml.view;

import com.uml.command.Command;
import com.uml.command.CommandHistory;
import com.uml.command.GroupCommand;
import com.uml.command.UngroupCommand;
import com.uml.controller.EditorMode;
import com.uml.controller.ModeManager;
import com.uml.controller.strategy.CanvasMouseStrategy;
import com.uml.controller.tool.EditorToolRegistry;
import com.uml.model.BasicObject;
import com.uml.model.CompositeObject;
import com.uml.model.DiagramDocument;
import com.uml.model.DiagramSelectionModel;
import com.uml.model.PortOwner;
import com.uml.model.UMLObject;
import com.uml.model.link.LinkObject;
import com.uml.util.UMLConstants;
import com.uml.view.renderer.CanvasOverlayRenderer;
import com.uml.view.renderer.CanvasRenderContext;
import com.uml.view.renderer.DiagramRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.*;
import java.util.List;

/**
 * The drawing canvas: owns Swing event routing, delegates model state to
 * DiagramDocument / DiagramSelectionModel, and paints through renderers.
 */
public class CanvasPanel extends JPanel { // 繼承 JPanel，作為可繪製的畫布元件

    // ── Model / state ─────────────────────────────────────
    private final DiagramDocument document = new DiagramDocument(); // diagram 結構資料（objects、links、z-order）
    private final DiagramSelectionModel selectionModel = new DiagramSelectionModel(document); // diagram 選取狀態
    private final CanvasInteractionState interactionState = new CanvasInteractionState(); // 畫布互動暫態

    private CanvasMouseStrategy currentStrategy;                    // 目前使用中的滑鼠策略
    private final Map<EditorMode, CanvasMouseStrategy> strategyMap; // 模式 → 策略的對應表

    // ── Rendering ─────────────────────────────────────────
    private final DiagramRenderer diagramRenderer = new DiagramRenderer(); // diagram renderer
    private final CanvasOverlayRenderer overlayRenderer = new CanvasOverlayRenderer(); // overlay renderer

    // ── Command history ───────────────────────────────────
    private final CommandHistory history = new CommandHistory(); // 命令歷史，支援 Undo/Redo

    public CanvasPanel(ModeManager modeManager) { // 建構子，接收模式管理器
        this(modeManager, EditorToolRegistry.createDefault());
    }

    public CanvasPanel(ModeManager modeManager, EditorToolRegistry toolRegistry) { // 建構子，接收模式管理器與工具註冊表
        setBackground(Color.WHITE); // 畫布背景設為白色
        setPreferredSize(new Dimension(UMLConstants.CANVAS_W, UMLConstants.CANVAS_H)); // 設定畫布大小

        strategyMap = toolRegistry.createStrategyMap(modeManager); // 由工具註冊表建立模式→策略的對應表
        currentStrategy = strategyMap.get(EditorMode.SELECT); // 預設為選取模式

        modeManager.addListener((newMode, prev) -> // 傳入臨時匿名物件，監聽模式切換事件
                currentStrategy = strategyMap.get(newMode)); // 切換時更新目前策略
        /* lambda 展開後等同的寫法：
            modeManager.addListener(new ModeChangeListener() {
                @Override
                public void onModeChanged(EditorMode newMode, EditorMode prevMode) {
                    currentStrategy = strategyMap.get(newMode);
                }
            });
        */

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

        CanvasRenderContext renderContext = new CanvasRenderContext(document, selectionModel, interactionState); // 建立 renderer 需要的上下文
        diagramRenderer.render(g2d, renderContext); // 繪製 diagram 內容
        overlayRenderer.render(g2d, renderContext, currentStrategy); // 繪製目前策略的 overlay

        g2d.dispose(); // 釋放 Graphics2D 副本資源
    }

    // ── Command API ───────────────────────────────────────
    public void execute(Command cmd) { // 執行並記錄命令（模型尚未更新時使用）
        cmd.redo(); // 呼叫 redo() 將命令效果套用到模型
        history.push(cmd); // 將命令推入歷史堆疊，供之後 undo 使用
        repaint(); // 統一由 Swing adapter 觸發重繪
    }

    public void pushHistory(Command cmd) { // 只記錄命令（模型已事先更新時使用）
        history.push(cmd); // 直接推入歷史，不再執行 redo()
        repaint(); // 統一由 Swing adapter 觸發重繪
    }

    public void undo() { history.undo(); repaint(); } // 呼叫歷史的 undo，還原上一個命令
    public void redo() { history.redo(); repaint(); } // 呼叫歷史的 redo，重做下一個命令
    public boolean canUndo() { return history.canUndo(); } // 回傳是否可 undo
    public boolean canRedo() { return history.canRedo(); } // 回傳是否可 redo

    public void simulateRelease(int x, int y) { // 合成 mouseReleased 並委派給目前策略（供 glass pane 使用）
        MouseEvent synthetic = new MouseEvent(this, MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(), 0, x, y, 1, false); // 建立合成的滑鼠放開事件
        currentStrategy.onReleased(synthetic, this); // 委派給目前策略處理
    }

    // ── Compatibility raw API ─────────────────────────────
    public void rawAddObject(UMLObject obj)    { document.addObject(obj); } // 直接新增物件（保留相容 API）
    public void rawRemoveObject(UMLObject obj) { document.removeObject(obj); } // 直接移除物件（保留相容 API）
    public void rawAddLink(LinkObject lnk)     { document.addLink(lnk); } // 直接新增連線（保留相容 API）
    public void rawRemoveLink(LinkObject lnk)  { document.removeLink(lnk); } // 直接移除連線（保留相容 API）

    // ── Public object/link API (used by Strategies) ──────
    public void addObject(UMLObject obj)   { document.addObject(obj); repaint(); } // 新增物件並重繪畫布
    public void addLink  (LinkObject lnk)  { document.addLink(lnk); repaint(); } // 新增連線並重繪畫布
    public void removeObject(UMLObject obj){ document.removeObject(obj); repaint(); } // 移除物件並重繪畫布

    /** Move object to end of list so it renders on top. */
    public void bringToFront(UMLObject obj) { // 將物件移到清單末端，使其渲染在最上層
        document.bringToFront(obj); // 委派給 DiagramDocument 維護 z-order
    }

    /** Hit-test from topmost object downward (reverse list order). */
    public UMLObject findObjectAt(int x, int y) { // 從最上層往下做點擊測試，回傳第一個命中的物件
        return document.findObjectAt(x, y);
    }

    public BasicObject findBasicObjectNearPort(int x, int y) { // 找出 port 靠近指定座標的 BasicObject（保留相容 API）
        PortOwner owner = document.findPortOwnerNearPort(x, y);
        return (owner instanceof BasicObject bo) ? bo : null;
    }

    public List<UMLObject> getSelectedObjects() { // 回傳所有已選取的物件清單
        return selectionModel.getSelectedObjects();
    }

    public void clearSelection() { // 清除所有物件的選取狀態
        selectionModel.clearSelection();
    }

    // ── Use Case D (Group / Ungroup) ──────────────────────
    public void group() { // 將目前選取的物件群組化
        List<UMLObject> selected = getSelectedObjects(); // 取得所有已選取的物件
        if (selected.size() < 2) return; // 群組需要至少 2 個物件，否則直接返回

        CompositeObject composite = new CompositeObject(selected); // 建立複合物件，包含所有選取物件
        List<Integer> childIndexes = selected.stream().map(document::indexOfObject).toList(); // 記錄每個子物件的原始 z-order
        int compositeIndex = childIndexes.stream().mapToInt(Integer::intValue).max().orElse(document.getObjects().size()); // composite 放到最高層成員的位置
        execute(new GroupCommand(document, selectionModel, selected, composite, childIndexes, compositeIndex)); // 執行並記錄群組命令
    }

    public void ungroup() { // 將選取的群組解散，還原為個別物件
        List<UMLObject> selected = getSelectedObjects(); // 取得所有已選取的物件
        if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject composite)) return; // 必須恰好選取一個複合物件

        List<UMLObject> children = new ArrayList<>(composite.getDirectChildren()); // 取得群組的直接子物件清單
        int compositeIndex = document.indexOfObject(composite); // 記錄 composite 原始 z-order
        execute(new UngroupCommand(document, selectionModel, composite, children, compositeIndex)); // 執行並記錄解散命令
    }

    // ── Accessors used by strategies ─────────────────────
    public List<UMLObject> getObjects() { return document.getObjects(); } // 回傳唯讀物件清單，防止外部直接修改
    public UMLObject getHoveredObject() { return interactionState.getHoveredObject(); } // 回傳目前懸停的物件
    public DiagramDocument getDocument() { return document; } // 回傳 diagram document
    public DiagramSelectionModel getSelectionModel() { return selectionModel; } // 回傳 diagram selection model
    public CanvasInteractionState getInteractionState() { return interactionState; } // 回傳畫布互動暫態

    public void setHoveredObject(UMLObject obj)  { interactionState.setHoveredObject(obj); } // 設定懸停物件（由策略在 mouseMoved 時呼叫）
    public void setRubberBand(Rectangle r)       { interactionState.setRubberBand(r); repaint(); } // 設定框選矩形並重繪（null 表示結束框選）
    public void setTempLinkEnd(Point p)          { interactionState.setTemporaryLink(interactionState.getTemporaryLinkSource(), p); repaint(); } // 設定拉線終點並重繪

    /** Updates hovered object by hit-testing current mouse position. */
    public void updateHoverAt(int x, int y) {
        UMLObject hovered = document.findObjectAt(x, y);
        if (hovered == interactionState.getHoveredObject()) return;

        interactionState.setHoveredObject(hovered);
        repaint();
    }

    /** Clears current hover state, e.g. when mouse leaves the canvas. */
    public void clearHover() {
        if (interactionState.getHoveredObject() == null) return;
        interactionState.clearHover();
        repaint();
    }
}
