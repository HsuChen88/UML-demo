package com.uml.controller.strategy;

import com.uml.command.MoveObjectsCommand;
import com.uml.model.UMLObject;
import com.uml.view.CanvasPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles Use Cases C (select/hover) and E (move) in SELECT mode.
 *
 * Internal sub-state machine:
 *   IDLE → DRAGGING_OBJECT
 *
 * Undo/Redo integration:
 *   Move — before-positions snapshotted at onPressed,
 *           MoveObjectsCommand pushed at onReleased.
 *   Only pushed if something actually moved.
 */
public class SelectStrategy implements CanvasMouseStrategy { // 選取策略，處理 Use Case C（選取/懸停）、E（移動）

    private enum SubState { IDLE, DRAGGING_OBJECT } // 選取模式內部的子狀態機

    private SubState  subState   = SubState.IDLE; // 目前的子狀態，初始為 IDLE
    private Point     pressPoint = null; // 記錄滑鼠按下的位置，用於計算拖曳位移
    private UMLObject dragTarget = null; // 正在被拖曳的目標物件

    // ── Undo/Redo snapshots ───────────────────────────────
    private Map<UMLObject, Point> moveBefore = null; // 移動前各物件的位置快照（按下時記錄）

    // ── mousePressed ─────────────────────────────────────
    @Override
    public void onPressed(MouseEvent e, CanvasPanel canvas) { // 滑鼠按下：決定進入哪個子狀態
        pressPoint = e.getPoint(); // 記錄按下位置

        // Object hit-test → move
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

        // Empty space → clear selection
        canvas.clearSelection(); // 點擊空白處清除所有選取
        subState = SubState.IDLE; // 保持 IDLE（rubber-band 功能在後續版本加入）
        canvas.repaint(); // 重繪
    }

    // ── mouseDragged ─────────────────────────────────────
    @Override
    public void onDragged(MouseEvent e, CanvasPanel canvas) { // 滑鼠拖曳：根據子狀態執行對應動作
        if (subState == SubState.DRAGGING_OBJECT) { // 拖曳物件
            int dx = e.getX() - pressPoint.x; // 計算水平位移
            int dy = e.getY() - pressPoint.y; // 計算垂直位移
            for (UMLObject obj : canvas.getSelectedObjects()) { // 移動所有已選取物件
                obj.move(dx, dy); // 以相對位移移動物件
            }
            pressPoint = e.getPoint(); // 更新按下點為目前位置（增量式位移）
            canvas.repaint(); // 重繪
        }
    }

    // ── mouseReleased ─────────────────────────────────────
    @Override
    public void onReleased(MouseEvent e, CanvasPanel canvas) { // 滑鼠放開：完成操作並推入命令歷史
        if (subState == SubState.DRAGGING_OBJECT && moveBefore != null) { // 若是拖曳完成
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

        subState    = SubState.IDLE; // 重置子狀態為 IDLE
        dragTarget  = null; // 清除拖曳目標
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
}
