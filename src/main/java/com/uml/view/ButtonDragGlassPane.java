package com.uml.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Transparent glass pane that intercepts the mouseReleased event after the
 * user presses a RECT/OVAL tool button and drags into the canvas.
 *
 * Problem it solves: Swing's mouse-grab delivers all dragged/released events
 * to the component where the press occurred (the button), so the canvas never
 * receives mouseReleased when the drag started on a button.
 *
 * Lifecycle:
 *   activate()  – called by ButtonPanel on RECT/OVAL mousePressed; makes the
 *                 pane visible so it intercepts all subsequent events.
 *   mouseReleased – converts the point to canvas coordinates; if it lands on
 *                   the canvas, forwards to canvas.simulateRelease(); then
 *                   deactivates itself.
 */
public class ButtonDragGlassPane extends JPanel { // 透明 glass pane，攔截按鈕拖曳至畫布的 mouseReleased 事件

    private final CanvasPanel canvas; // 目標畫布，放開後若在此範圍內即建立物件

    public ButtonDragGlassPane(CanvasPanel c) { // 建構子：注入畫布
        this.canvas = c;
        setOpaque(false); // 透明，不遮擋畫面

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { // 攔截 mouseReleased
                deactivate(); // 立刻關閉 glass pane，讓後續事件正常傳遞

                // 將 glass pane 座標轉換為 canvas 的本地座標
                Point canvasPoint = SwingUtilities.convertPoint(ButtonDragGlassPane.this, e.getPoint(), canvas);

                if (canvas.contains(canvasPoint)) { // 若放開點在畫布範圍內
                    canvas.simulateRelease(canvasPoint.x, canvasPoint.y); // 觸發物件建立
                }
            }
        });
    }

    /** 啟動攔截：在 RECT/OVAL 按鈕 mousePressed 時呼叫 */
    public void activate() {
        setVisible(true);
    }

    private void deactivate() {
        setVisible(false);
    }
}
