package com.uml.util;

import java.awt.*;

/**
 * Central repository for all shared constant values.
 *
 * Sections:
 *   1. Object geometry
 *   2. Hit-test
 *   3. Drawing — stroke widths
 *   4. Drawing — dash patterns
 *   5. Colours
 *   6. UI — ButtonPanel
 *   7. Canvas
 */
public final class UMLConstants { // 全域常數類別；final 防止繼承，私有建構子防止實例化

    private UMLConstants() {} // 私有建構子，禁止外部建立實例（工具類別慣例）

    // ── 1. Object geometry ───────────────────────────────
    /** Pixel side-length of each port square handle. */
    public static final int   PORT_SIZE      = 8; // 每個 port 方形把手的邊長（像素）

    /** Minimum allowed width or height of any BasicObject. */
    public static final int   MIN_SIZE       = 20; // BasicObject 允許的最小寬度或高度（防止縮到看不見）

    /** Default width when creating a new object. */
    public static final int   DEFAULT_W      = 100; // 建立新物件時的預設寬度

    /** Default height when creating a new object. */
    public static final int   DEFAULT_H      = 80; // 建立新物件時的預設高度

    /** Padding (pixels) around children when drawing CompositeObject border. */
    public static final int   COMPOSITE_PAD  = 6; // 繪製群組邊框時，子物件外的內縮留白（像素）

    // ── 2. Hit-test ──────────────────────────────────────
    /** Chebyshev-distance threshold for port hit-testing. */
    public static final int   PORT_HIT_RADIUS = 5; // port 點擊偵測的容差半徑（使用 Chebyshev 距離，即棋盤距離）

    // ── 3. Drawing — stroke widths ────────────────────────
    /** Standard outline stroke used for shapes and links. */
    public static final float STROKE_NORMAL  = 1.5f; // 標準線寬，用於物件外框與連線

    /** Thin stroke used to reset graphics state after composites. */
    public static final float STROKE_THIN    = 1.0f; // 細線寬，用於繪製群組後重置畫筆狀態

    /** Stroke width for the temp-link preview line and rubber-band rectangle. */
    public static final float STROKE_PREVIEW = 1.2f; // 預覽線（拖曳連線、框選矩形）的線寬

    // ── 4. Drawing — dash patterns ────────────────────────
    /** Dash pattern for the temp-link preview line and rubber-band rectangle. */
    public static final float[] DASH_PREVIEW   = {5f, 4f}; // 虛線樣式：實線 5px，空白 4px（用於預覽線與框選）

    /** Dash pattern for the CompositeObject selection border. */
    public static final float[] DASH_COMPOSITE = {6f, 4f}; // 虛線樣式：實線 6px，空白 4px（用於群組邊框）

    // ── 5. Colours ───────────────────────────────────────
    /** Default fill / label colour for newly created objects. */
    public static final Color COLOR_DEFAULT_LABEL = new Color(180, 180, 180); // 新建物件的預設填色（淺灰）

    /** Background colour of the currently active tool button. */
    public static final Color COLOR_BUTTON_ACTIVE = new Color(210, 210, 210); // 已啟用工具按鈕的背景色（較淺灰）

    /** Border colour for the active tool button highlight. */
    public static final Color COLOR_BUTTON_BORDER = new Color(120, 120, 120); // 已啟用工具按鈕的邊框色（深灰）

    // ── 6. UI — ButtonPanel ──────────────────────────────
    /** Pixel size of the square painted area inside each tool button icon. */
    public static final int   ICON_SIZE       = 24; // 工具按鈕圖示的繪製區域邊長（像素）

    /** Dark ink colour used in all tool button icon drawings. */
    public static final Color ICON_DARK       = new Color(60, 60, 60); // 工具按鈕圖示的墨色（深灰接近黑）

    /** Side length (pixels) of the square tool buttons. */
    public static final int   BUTTON_SIZE     = 40; // 工具按鈕的邊長（像素）

    /** Font size (pt) for the tool button text labels. */
    public static final float LABEL_FONT_SIZE = 13f; // 工具按鈕旁文字標籤的字型大小（點數）

    // ── 7. Canvas ─────────────────────────────────────────
    /** Preferred canvas width in pixels. */
    public static final int CANVAS_W = 900; // 畫布的偏好寬度（像素）

    /** Preferred canvas height in pixels. */
    public static final int CANVAS_H = 700; // 畫布的偏好高度（像素）
}
