package com.uml.controller.strategy;

import com.uml.command.MoveObjectsCommand;
import com.uml.command.ResizeObjectCommand;
import com.uml.model.BasicObject;
import com.uml.model.UMLObject;
import com.uml.util.HitTestUtil;
import com.uml.view.CanvasPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Use Cases C (select/hover), E (move), F (resize) in SELECT mode.
 *
 * Internal sub-state machine:
 *   IDLE → DRAGGING_OBJECT | RESIZING | RUBBER_BANDING
 *
 * Resize axis constraints and anchor positions are fully delegated to each
 * BasicObject subclass via getResizeConstraint() and getResizeAnchor()
 * (OCP compliant — zero type-specific logic here).
 *
 * Undo/Redo integration:
 *   Move  — before-positions snapshotted at onPressed, MoveObjectsCommand pushed at onReleased.
 *   Resize — before-bounds snapshotted at onPressed, ResizeObjectCommand pushed at onReleased.
 *   Only pushes if the object actually moved/resized (avoids empty history entries on plain clicks).
 */
public class SelectStrategy implements CanvasMouseStrategy { // 選取策略，處理 Use Case C（選取/懸停）、E（移動）、F（縮放）

    private enum SubState { IDLE, DRAGGING_OBJECT, RESIZING, RUBBER_BANDING } // 選取模式內部的子狀態機

    private SubState  subState   = SubState.IDLE; // 目前的子狀態，初始為 IDLE
    private Point     pressPoint = null; // 記錄滑鼠按下的位置，用於計算拖曳位移
    private UMLObject dragTarget = null; // 正在被拖曳或縮放的目標物件
    private int       resizePort = -1; // 正在拖曳的 port 索引（-1 表示無）
    private int       rubberX1, rubberY1; // 框選矩形的起始角座標

    private Point fixedAnchor = null; // 縮放時固定不動的錨點（被拖曳 port 的對角 / 對邊中點）

    // ── Undo/Redo snapshots ───────────────────────────────
    private Map<UMLObject, Point> moveBefore = null; // 移動前各物件的位置快照（按下時記錄）
    private int resizeBX, resizeBY, resizeBW, resizeBH; // 縮放前目標物件的邊界快照（按下時記錄）

    // ── mousePressed ─────────────────────────────────────
    @Override
    public void onPressed(MouseEvent e, CanvasPanel canvas) { // 滑鼠按下：決定進入哪個子狀態
        pressPoint = e.getPoint(); // 記錄按下位置

        // 1. Port hit-test on selected BasicObject → resize
        for (UMLObject obj : canvas.getObjects()) { // 遍歷所有物件，優先檢查是否按到 port
            if (obj.isSelected() && obj instanceof BasicObject bo) { // 只對已選取的 BasicObject 做 port 測試
                int pi = bo.getNearestPortIndex(pressPoint); // 找出最近的 port 索引
                if (pi != -1) { // 若按下位置靠近某個 port
                    subState    = SubState.RESIZING; // 進入縮放子狀態
                    dragTarget  = bo; // 記錄縮放目標
                    resizePort  = pi; // 記錄被拖曳的 port 索引
                    fixedAnchor = bo.getResizeAnchor(pi); // 取得固定錨點（對角位置）
                    // Snapshot before-bounds for undo
                    resizeBX = bo.getX(); resizeBY = bo.getY(); // 記錄縮放前的 x、y
                    resizeBW = bo.getWidth(); resizeBH = bo.getHeight(); // 記錄縮放前的寬、高
                    return; // 找到 port 則直接返回，不繼續其他測試
                }
            }
        }

        // 2. Object hit-test → move
        UMLObject hit = canvas.findObjectAt(e.getX(), e.getY()); // 找出被點擊的物件
        if (hit != null) { // 若點擊到物件
            if (!hit.isSelected()) { // 若該物件尚未被選取
                canvas.clearSelection(); // 清除其他物件的選取
                hit.setSelected(true); // 選取被點擊的物件
                canvas.bringToFront(hit); // 將物件移到最上層
            }
            subState   = SubState.DRAGGING_OBJECT; // 進入拖曳子狀態
            dragTarget = hit; // 記錄被拖曳的物件
            // Snapshot before-positions of ALL selected objects for undo
            moveBefore = new LinkedHashMap<>(); // 建立移動前快照 Map
            for (UMLObject obj : canvas.getSelectedObjects()) { // 對所有已選取物件做快照
                Rectangle b = obj.getBounds(); // 取得物件邊界
                moveBefore.put(obj, new Point(b.x, b.y)); // 記錄按下時的位置
            }
            canvas.repaint(); // 重繪以顯示選取狀態
            return; // 找到物件則直接返回
        }

        // 3. Empty space → rubber-band
        canvas.clearSelection(); // 點擊空白處先清除所有選取
        subState = SubState.RUBBER_BANDING; // 進入框選子狀態
        rubberX1 = e.getX(); // 記錄框選起始 x
        rubberY1 = e.getY(); // 記錄框選起始 y
        canvas.setRubberBand(null); // 清除舊的框選矩形
        canvas.repaint(); // 重繪
    }

    // ── mouseDragged ─────────────────────────────────────
    @Override
    public void onDragged(MouseEvent e, CanvasPanel canvas) { // 滑鼠拖曳：根據子狀態執行對應動作
        switch (subState) { // 依子狀態分派
            case DRAGGING_OBJECT -> { // 拖曳物件
                int dx = e.getX() - pressPoint.x; // 計算水平位移
                int dy = e.getY() - pressPoint.y; // 計算垂直位移
                for (UMLObject obj : canvas.getSelectedObjects()) { // 移動所有已選取物件
                    obj.move(dx, dy); // 以相對位移移動物件
                }
                pressPoint = e.getPoint(); // 更新按下點為目前位置（增量式位移）
                canvas.repaint(); // 重繪
            }
            case RESIZING -> { // 縮放物件
                if (dragTarget instanceof BasicObject bo) { // 確認目標是 BasicObject
                    applyResize(bo, e.getX(), e.getY()); // 套用縮放計算
                    canvas.repaint(); // 重繪
                }
            }
            case RUBBER_BANDING -> { // 框選
                Rectangle r = HitTestUtil.normalise(rubberX1, rubberY1, e.getX(), e.getY()); // 計算標準化矩形（確保寬高為正）
                canvas.setRubberBand(r); // 更新框選矩形並重繪
            }
        }
    }

    // ── mouseReleased ─────────────────────────────────────
    @Override
    public void onReleased(MouseEvent e, CanvasPanel canvas) { // 滑鼠放開：完成操作並推入命令歷史
        if (subState == SubState.DRAGGING_OBJECT && moveBefore != null) { // 若是拖曳完成
            // Build after-snapshot and push only if something actually moved
            Map<UMLObject, Point> moveAfter = new LinkedHashMap<>(); // 建立移動後快照 Map
            moveBefore.keySet().forEach(obj -> { // 對每個已移動物件記錄新位置
                Rectangle b = obj.getBounds(); // 取得目前邊界
                moveAfter.put(obj, new Point(b.x, b.y)); // 記錄放開時的位置
            });
            boolean moved = moveBefore.entrySet().stream() // 比較前後位置，判斷是否真的移動過
                    .anyMatch(en -> !en.getValue().equals(moveAfter.get(en.getKey()))); // 若任何物件位置改變
            if (moved) { // 只有真正移動才推入歷史（避免空記錄）
                canvas.pushHistory(new MoveObjectsCommand(canvas, moveBefore, moveAfter)); // 推入移動命令
            }
            moveBefore = null; // 清除快照
        }

        if (subState == SubState.RESIZING && dragTarget instanceof BasicObject bo) { // 若是縮放完成
            int ax = bo.getX(), ay = bo.getY(), aw = bo.getWidth(), ah = bo.getHeight(); // 取得縮放後的邊界
            if (ax != resizeBX || ay != resizeBY || aw != resizeBW || ah != resizeBH) { // 只有真正縮放才推入歷史
                canvas.pushHistory(new ResizeObjectCommand(canvas, bo, // 推入縮放命令（含前後邊界快照）
                        resizeBX, resizeBY, resizeBW, resizeBH, // 縮放前的邊界
                        ax, ay, aw, ah)); // 縮放後的邊界
            }
        }

        if (subState == SubState.RUBBER_BANDING) { // 若是框選完成
            Rectangle sel = HitTestUtil.normalise(rubberX1, rubberY1, e.getX(), e.getY()); // 計算最終框選矩形
            canvas.setRubberBand(null); // 清除框選矩形顯示
            for (UMLObject obj : canvas.getObjects()) { // 遍歷所有物件
                if (HitTestUtil.isCompletelyInside(obj.getBounds(), sel)) { // 若物件完全在框選範圍內
                    obj.setSelected(true); // 選取該物件
                }
            }
            canvas.repaint(); // 重繪以顯示選取狀態
        }

        subState    = SubState.IDLE; // 重置子狀態為 IDLE
        dragTarget  = null; // 清除拖曳目標
        resizePort  = -1; // 清除 port 索引
        fixedAnchor = null; // 清除錨點
    }

    // ── mouseMoved (hover) ────────────────────────────────
    @Override
    public void onMoved(MouseEvent e, CanvasPanel canvas) { // 滑鼠移動（未按下）：更新懸停狀態（Use Case C）
        canvas.updateHoverAt(e.getX(), e.getY()); // 委派畫布統一維護懸停狀態與重繪
    }

    // ── mouseClicked (single-click select) ───────────────
    @Override
    public void onClicked(MouseEvent e, CanvasPanel canvas) { // 滑鼠點擊（Use Case C：單點選取）
        UMLObject hit = canvas.findObjectAt(e.getX(), e.getY()); // 找出被點擊的物件
        if (hit == null) return; // 若點擊空白處則忽略
        canvas.clearSelection(); // 清除所有選取
        hit.setSelected(true); // 選取被點擊的物件
        canvas.bringToFront(hit); // 將物件移到最上層
        canvas.repaint(); // 重繪
    }

    // ── Resize helper ─────────────────────────────────────
    /**
     * Applies resize to {@code bo} given the current mouse position.
     * Uses {@link BasicObject#getResizeConstraint} to lock axes for
     * edge-midpoint ports — no type-specific checks needed here.
     */
    private void applyResize(BasicObject bo, int mx, int my) { // 根據滑鼠位置計算並套用縮放（Use Case F）
        int newX = Math.min(fixedAnchor.x, mx); // 新的左邊界（取錨點與滑鼠的較小 x）
        int newY = Math.min(fixedAnchor.y, my); // 新的上邊界（取錨點與滑鼠的較小 y）
        int newW = Math.abs(mx - fixedAnchor.x); // 新的寬度（錨點與滑鼠的 x 距離）
        int newH = Math.abs(my - fixedAnchor.y); // 新的高度（錨點與滑鼠的 y 距離）

        switch (bo.getResizeConstraint(resizePort)) { // 依物件的軸鎖定規則調整
            case LOCK_WIDTH  -> { newX = bo.getX(); newW = bo.getWidth(); } // 鎖定寬度（只有高度改變）
            case LOCK_HEIGHT -> { newY = bo.getY(); newH = bo.getHeight(); } // 鎖定高度（只有寬度改變）
            case NONE        -> { /* both axes resize freely */ } // 兩軸均可自由縮放
        }

        bo.setBounds(newX, newY, newW, newH); // 套用新的邊界到物件
    }
}
