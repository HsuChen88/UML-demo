package com.uml.view;

import com.uml.controller.mode.EditorMode;
import com.uml.controller.mode.ModeChangeListener;
import com.uml.controller.mode.ModeManager;
import com.uml.controller.tool.EditorToolDefinition;
import com.uml.controller.tool.EditorToolRegistry;
import com.uml.util.UMLConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Left-side tool button panel.
 * Each row shows a right-aligned text label and a square icon button.
 * Implements ModeChangeListener to highlight the active button (Observer).
 *
 * Tool buttons are JLabel (not JButton) to avoid L&F pressed-state interference:
 * JButton's Look-and-Feel renders a "pressed" overlay during mousePressed, which
 * delays the visibility of our custom highlight until mouseReleased.  JLabel has
 * no such built-in state, so setBackground() takes effect immediately.
 */
public class ButtonPanel extends JPanel implements ModeChangeListener { // 左側工具按鈕面板，實作 Observer 介面以監聽模式切換

    private final Map<EditorMode, JLabel> buttons = new EnumMap<>(EditorMode.class); // 模式 → 圖示標籤的對應表（JLabel 無 L&F 按壓狀態）
    private final CanvasPanel canvas; // 拖曳至畫布放開時的目標（Use Case A）

    public ButtonPanel(ModeManager modeManager, CanvasPanel c) { // 建構子：建立所有工具按鈕並配置版面
        this(modeManager, c, EditorToolRegistry.createDefault());
    }

    public ButtonPanel(ModeManager modeManager, CanvasPanel c, EditorToolRegistry toolRegistry) { // 建構子：以工具註冊表建立所有工具按鈕並配置版面
        this.canvas = c;
        setLayout(new MigLayout("wrap 2, insets 12 8 12 8, gap 6 10", // 使用 MigLayout：每行 2 欄，設定邊距和間距
                "[grow, right][44!]", // 第一欄：向右對齊並自動擴展；第二欄：固定 44px 寬
                "")); // 列高度自動

        for (EditorToolDefinition def : toolRegistry.getDefinitions()) { // 遍歷所有工具定義，建立對應的文字標籤與圖示標籤
            String     labelText = def.label(); // 取得文字標籤內容
            EditorMode mode      = def.mode();  // 取得對應模式
            Icon       icon      = def.icon();  // 取得圖示物件

            JLabel lbl = new JLabel(labelText); // 建立工具名稱文字標籤
            lbl.setFont(lbl.getFont().deriveFont(UMLConstants.LABEL_FONT_SIZE)); // 設定字型大小

            JLabel btn = new JLabel(icon); // 建立圖示標籤（用 JLabel 取代 JButton，避免 L&F 按壓視覺干擾）
            btn.setOpaque(true);     // 設為不透明，使 setBackground 生效
            btn.setBackground(null); // 預設無背景色（未選取狀態）
            btn.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 1)); // 預設細灰邊框
            btn.setHorizontalAlignment(SwingConstants.CENTER); // 圖示水平置中

            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { // 按下時立刻切換模式（JLabel 無 L&F 按壓狀態，highlight 立即可見）
                    modeManager.setMode(mode); // 通知模式管理器切換模式 → 觸發 onModeChanged → highlightButton
                    canvas.clearSelection();   // 切換模式時清除畫布的選取狀態，消除 port 顯示
                    canvas.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) { // 放開時處理「從按鈕拖曳至畫布建立物件」（Use Case A）
                    if (!def.canvasDropCreatesObject()) return; // 只有支援拖放建立的工具需要此邏輯

                    // Swing mouse-grab：mousePressed 在 btn 上，mouseReleased 也送到 btn（即使滑鼠已移到畫布上）
                    // 將放開座標從 btn 的座標系轉換為 canvas 的座標系
                    Point canvasPoint = SwingUtilities.convertPoint(btn, e.getPoint(), canvas);
                    if (canvas.contains(canvasPoint)) { // 若放開點在畫布範圍內
                        canvas.simulateRelease(canvasPoint.x, canvasPoint.y); // 觸發物件建立
                    }
                }
            });

            buttons.put(mode, btn); // 將圖示標籤加入對應表，供之後 highlightButton 使用
            add(lbl, "");           // 將文字標籤加入版面（第一欄，向右對齊）
            add(btn, "w " + UMLConstants.BUTTON_SIZE + "!, h " + UMLConstants.BUTTON_SIZE + "!"); // 固定寬高
        }

        modeManager.addListener(this);      // 將自己註冊為模式切換監聽者（Observer Pattern）
        highlightButton(EditorMode.SELECT); // 初始化時高亮 SELECT 按鈕
    }

    // ── ModeChangeListener ────────────────────────────────
    @Override
    public void onModeChanged(EditorMode newMode, EditorMode prevMode) { // Observer 回呼：模式切換時更新按鈕高亮
        highlightButton(newMode); // 高亮新模式對應的按鈕
    }

    // ── ButtonPanel own methods ───────────────────────────
    private void highlightButton(EditorMode mode) { // 更新按鈕高亮狀態（清除舊的，設定新的）
        buttons.values().forEach(b -> { // 先清除所有按鈕的高亮樣式
            b.setBackground(null); // 清除背景色
            b.setBorder(BorderFactory.createLineBorder(new Color(160, 160, 160), 1)); // 還原預設邊框
        });
        JLabel active = buttons.get(mode); // 取得新模式對應的按鈕
        if (active != null) { // 確認按鈕存在
            active.setBackground(UMLConstants.COLOR_BUTTON_ACTIVE); // 設定高亮背景色
            active.setBorder(new LineBorder(UMLConstants.COLOR_BUTTON_BORDER, 2, true)); // 設定高亮邊框（圓角線框）
        }
    }

}
