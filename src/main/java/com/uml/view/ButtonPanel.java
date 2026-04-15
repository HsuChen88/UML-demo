package com.uml.view;

import com.uml.controller.EditorMode;
import com.uml.controller.ModeChangeListener;
import com.uml.controller.ModeManager;
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
        this.canvas = c;
        setLayout(new MigLayout("wrap 2, insets 12 8 12 8, gap 6 10", // 使用 MigLayout：每行 2 欄，設定邊距和間距
                "[grow, right][44!]", // 第一欄：向右對齊並自動擴展；第二欄：固定 44px 寬
                "")); // 列高度自動

        Object[][] defs = { // 工具按鈕定義陣列（標籤文字、對應模式、圖示物件）
            {"select",         EditorMode.SELECT,         new SelectIcon()        }, // 選取工具
            {"association",    EditorMode.ASSOCIATION,    new AssociationIcon()   }, // 關聯線工具
            {"generalization", EditorMode.GENERALIZATION, new GeneralizationIcon()}, // 繼承線工具
            {"composition",    EditorMode.COMPOSITION,    new CompositionIcon()   }, // 組合線工具
            {"rect",           EditorMode.RECT,           new RectIcon()          }, // 矩形工具
            {"oval",           EditorMode.OVAL,           new OvalIcon()          }, // 橢圓工具
        };

        for (Object[] def : defs) { // 遍歷所有工具定義，建立對應的文字標籤與圖示標籤
            String     labelText = (String)     def[0]; // 取得文字標籤內容
            EditorMode mode      = (EditorMode) def[1]; // 取得對應模式
            Icon       icon      = (Icon)       def[2]; // 取得圖示物件

            JLabel lbl = new JLabel(labelText); // 建立工具名稱文字標籤
            lbl.setFont(lbl.getFont().deriveFont(UMLConstants.LABEL_FONT_SIZE)); // 設定字型大小

            JLabel btn = new JLabel(icon); // 建立圖示標籤（用 JLabel 取代 JButton，避免 L&F 按壓視覺干擾）
            btn.setOpaque(true); // 設為不透明，使 setBackground 生效
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
                    if (!mode.isObjectCreation()) return; // 只有 RECT/OVAL 模式需要此邏輯

                    // Swing mouse-grab：mousePressed 在 btn 上，mouseReleased 也送到 btn（即使滑鼠已移到畫布上）
                    // 將放開座標從 btn 的座標系轉換為 canvas 的座標系
                    Point canvasPoint = SwingUtilities.convertPoint(btn, e.getPoint(), canvas);
                    if (canvas.contains(canvasPoint)) { // 若放開點在畫布範圍內
                        canvas.simulateRelease(canvasPoint.x, canvasPoint.y); // 觸發物件建立
                    }
                }
            });

            buttons.put(mode, btn); // 將圖示標籤加入對應表，供之後 highlightButton 使用
            add(lbl, ""); // 將文字標籤加入版面（第一欄，向右對齊）
            add(btn, "w " + UMLConstants.BUTTON_SIZE + "!, h " + UMLConstants.BUTTON_SIZE + "!"); // 固定寬高
        }

        modeManager.addListener(this); // 將自己註冊為模式切換監聽者（Observer Pattern）
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

    //
    // Icon implementations
    // --------------------------------------------------------------------------------------------------------------------------------------------------

    private static abstract class BaseIcon implements Icon { // 所有工具圖示的抽象基底類別，定義共用尺寸與畫筆準備方法
        @Override public int getIconWidth()  { return UMLConstants.ICON_SIZE; } // 圖示寬度（像素）
        @Override public int getIconHeight() { return UMLConstants.ICON_SIZE; } // 圖示高度（像素）

        Graphics2D prepare(Graphics g, int x, int y) { // 建立 Graphics2D 副本並平移原點到圖示左上角
            Graphics2D g2 = (Graphics2D) g.create(); // 建立獨立的 Graphics2D 副本
            g2.translate(x, y); // 平移座標系原點到圖示的左上角
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // 開啟反鋸齒
                                RenderingHints.VALUE_ANTIALIAS_ON);
            return g2; // 回傳已設定好的畫筆
        }
    }

    /** Cursor / pointer arrow */
    private static class SelectIcon extends BaseIcon { // 選取工具圖示（游標箭頭形狀）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製游標箭頭多邊形
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(UMLConstants.ICON_DARK); // 設定顏色為深灰
            int[] px = { 3,  3,  7, 10, 12,  9, 15}; // 箭頭多邊形的 x 座標陣列
            int[] py = { 2, 17, 13, 19, 18, 12, 12}; // 箭頭多邊形的 y 座標陣列
            g2.fillPolygon(px, py, px.length); // 填充游標箭頭形狀
            g2.dispose();   // 釋放暫時畫筆
        }
    }

    /** Solid left-pointing arrow  ← */
    private static class AssociationIcon extends BaseIcon { // 關聯線工具圖示（實心箭頭）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製直線 + 實心三角形箭頭
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(UMLConstants.ICON_DARK); // 設定顏色
            g2.setStroke(new BasicStroke(1.8f)); // 設定線寬
            g2.drawLine(19, 12, 8, 12); // 繪製水平直線（從右到左）
            int[] px = {8, 14, 14}; // 箭頭三角形的 x 座標陣列
            int[] py = {12, 8, 16}; // 箭頭三角形的 y 座標陣列
            g2.fillPolygon(px, py, px.length);  // fill 實心填滿
            g2.dispose();   // 釋放暫時畫筆
        }
    }

    /** Open/hollow triangle arrowhead  ⇐ */
    private static class GeneralizationIcon extends BaseIcon { // 繼承線工具圖示（空心三角形箭頭）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製直線 + 空心三角形箭頭
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(UMLConstants.ICON_DARK); // 設定顏色
            g2.setStroke(new BasicStroke(1.6f)); // 設定線寬
            g2.drawLine(19, 12, 13, 12); // 繪製水平直線（從右到三角形底部）
            int[] px = {8, 14, 14}; // 箭頭三角形的 x 座標陣列
            int[] py = {12, 8, 16}; // 箭頭三角形的 y 座標陣列
            g2.drawPolygon(px, py, px.length);  // draw 只有線條（空心效果）
            g2.dispose();   // 釋放暫時畫筆
        }
    }

    /**
     * Hollow diamond + right line  ◇-
     */
    private static class CompositionIcon extends BaseIcon { // 組合線工具圖示（菱形 + 直線）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製菱形 + 水平直線
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(UMLConstants.ICON_DARK); // 設定顏色
            g2.setStroke(new BasicStroke(1.6f)); // 設定線寬
            int[] dpx = {2, 7, 12, 7}; // 菱形四個頂點的 x 座標（左、上、右、下）
            int[] dpy = {12, 8, 12, 16}; // 菱形四個頂點的 y 座標
            g2.drawPolygon(dpx, dpy, dpx.length); // 繪製菱形外框（空心）
            g2.drawLine(12, 12, 22, 12); // 繪製菱形右側的水平直線
            g2.dispose();   // 釋放暫時畫筆
        }
    }

    /** Filled gray rectangle */
    private static class RectIcon extends BaseIcon { // 矩形工具圖示（填色矩形）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製填色矩形
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(new Color(130, 130, 130)); // 設定填色為中灰
            g2.fillRect(3, 5, 18, 14); // 繪製填色矩形（位置、尺寸）
            g2.dispose();   // 釋放暫時畫筆
        }
    }

    /** Filled gray ellipse */
    private static class OvalIcon extends BaseIcon { // 橢圓工具圖示（填色橢圓）
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) { // 繪製填色橢圓
            Graphics2D g2 = prepare(g, x, y); // 準備畫筆
            g2.setColor(new Color(150, 150, 150)); // 設定填色為淺灰
            g2.fillOval(2, 2, 20, 20); // 繪製填色橢圓（位置、尺寸）
            g2.dispose();   // 釋放暫時畫筆
        }
    }
}
