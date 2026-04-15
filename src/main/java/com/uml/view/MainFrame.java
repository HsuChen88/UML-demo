package com.uml.view;

import com.uml.command.SetLabelCommand;
import com.uml.controller.ModeManager;
import com.uml.model.BasicObject;
import com.uml.model.UMLObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Top-level window: assembles ButtonPanel + CanvasPanel and owns the menu bar.
 */
public class MainFrame extends JFrame { // 頂層視窗，負責組裝所有 UI 元件並擁有選單列

    private final ModeManager modeManager = new ModeManager(); // 建立模式管理器（由 ButtonPanel 與 CanvasPanel 共用）
    private final CanvasPanel canvasPanel; // 畫布面板
    private final ButtonPanel buttonPanel; // 左側工具按鈕面板

    public MainFrame() { // 建構子：組裝視窗
        super("Oops UML Editor"); // 設定視窗標題

        canvasPanel = new CanvasPanel(modeManager); // 建立畫布，注入模式管理器
        buttonPanel = new ButtonPanel(modeManager, canvasPanel); // 建立按鈕面板，注入模式管理器與畫布

        setLayout(new BorderLayout()); // 使用 BorderLayout 進行版面配置
        add(buttonPanel,              BorderLayout.WEST); // 工具按鈕面板放在左側
        add(new JScrollPane(canvasPanel), BorderLayout.CENTER); // 畫布加上捲軸放在中央
        setJMenuBar(buildMenuBar()); // 設定選單列

        pack(); // 根據內容自動調整視窗大小
        setDefaultCloseOperation(EXIT_ON_CLOSE); // 關閉視窗時結束程式
        setLocationRelativeTo(null); // 將視窗置中於螢幕
    }

    private JMenuBar buildMenuBar() { // 建立並回傳選單列
        JMenuBar bar = new JMenuBar(); // 建立選單列

        JMenu file = new JMenu("File"); // 建立 File 選單（目前為空，預留）
        bar.add(file); // 加入選單列

        JMenu edit = new JMenu("Edit"); // 建立 Edit 選單

        JMenuItem undo    = new JMenuItem("Undo"); // 建立 Undo 選單項目
        JMenuItem redo    = new JMenuItem("Redo"); // 建立 Redo 選單項目
        JMenuItem group   = new JMenuItem("Group"); // 建立 Group 選單項目
        JMenuItem ungroup = new JMenuItem("Ungroup"); // 建立 Ungroup 選單項目
        JMenuItem label   = new JMenuItem("Label"); // 建立 Label 選單項目

        refreshUndoRedoState(undo, redo); // 依照歷史狀態初始化 Undo/Redo 的可用性

        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK)); // 設定 Ctrl+Z 為 Undo 快捷鍵
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK)); // 設定 Ctrl+Y 為 Redo 快捷鍵
        group.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK)); // 設定 Ctrl+G 為 Group 快捷鍵
        ungroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)); // 設定 Ctrl+Shift+G 為 Ungroup 快捷鍵

        undo.addActionListener(e -> { // Undo 點擊時先防護，再更新按鈕狀態
            if (canvasPanel.canUndo()) canvasPanel.undo();
            refreshUndoRedoState(undo, redo);
        });
        redo.addActionListener(e -> { // Redo 點擊時先防護，再更新按鈕狀態
            if (canvasPanel.canRedo()) canvasPanel.redo();
            refreshUndoRedoState(undo, redo);
        });
        group.addActionListener(e -> { // Group 後同步更新 Undo/Redo 可用性
            canvasPanel.group();
            refreshUndoRedoState(undo, redo);
        });
        ungroup.addActionListener(e -> { // Undo/Redo 可用性
            canvasPanel.ungroup();
            refreshUndoRedoState(undo, redo);
        });
        label.addActionListener(e -> { // Label 變更後同步更新 Undo/Redo 可用性
            openLabelDialog();
            refreshUndoRedoState(undo, redo);
        });

        edit.add(undo);     // 加入 Undo 選單項目
        edit.add(redo);     // 加入 Redo 選單項目
        edit.addSeparator(); // 加入分隔線
        edit.add(group);    // 加入 Group 選單項目
        edit.add(ungroup);  // 加入 Ungroup 選單項目
        edit.addSeparator(); // 加入分隔線
        edit.add(label);    // 加入 Label 選單項目
        bar.add(edit);      // 將 Edit 選單加入選單列

        return bar; // 回傳完整選單列
    }

    private void refreshUndoRedoState(JMenuItem undoButton, JMenuItem redoButton) { // 依照歷史紀錄切換 Undo/Redo 可用性
        undoButton.setEnabled(canvasPanel.canUndo());
        redoButton.setEnabled(canvasPanel.canRedo());
    }

    private void openLabelDialog() { // 開啟標籤設定對話框（Use Case G）
        List<UMLObject> selected = canvasPanel.getSelectedObjects(); // 取得目前選取的物件清單
        if (selected.size() != 1 || !(selected.get(0) instanceof BasicObject bo)) return; // 必須恰好選取一個 BasicObject 才開啟對話框

        // Snapshot before-state for undo
        String beforeName  = bo.getLabelName(); // 記錄修改前的標籤文字
        Color  beforeColor = bo.getLabelColor(); // 記錄修改前的標籤顏色

        LabelDialog dlg = new LabelDialog(this, beforeName, beforeColor); // 建立標籤對話框，帶入目前值
        dlg.setVisible(true); // 顯示對話框（模態，阻塞直到關閉）

        if (dlg.isConfirmed()) { // 若使用者點擊 OK
            String afterName  = dlg.getLabelName(); // 取得使用者輸入的新標籤文字
            Color  afterColor = dlg.getLabelColor(); // 取得使用者選擇的新顏色

            // Apply change to model
            bo.setLabelName(afterName.isBlank() ? null : afterName); // 套用新標籤文字（空白則設為 null 清除標籤）
            bo.setLabelColor(afterColor); // 套用新顏色
            canvasPanel.repaint(); // 重繪畫布

            // Record for undo (use the same normalised name stored in model)
            canvasPanel.pushHistory(new SetLabelCommand( // 推入標籤命令到歷史（供 Undo 使用）
                    canvasPanel, bo, beforeName, beforeColor, // 傳入前後狀態
                    bo.getLabelName(), afterColor)); // 使用 model 中已正規化的名稱（null 而非空字串）
        }
    }
}
