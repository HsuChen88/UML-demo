package com.uml.view;

import com.uml.controller.ModeManager;

import javax.swing.*;

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

        setLayout(new java.awt.BorderLayout()); // 使用 BorderLayout 進行版面配置
        add(buttonPanel,              java.awt.BorderLayout.WEST); // 工具按鈕面板放在左側
        add(new JScrollPane(canvasPanel), java.awt.BorderLayout.CENTER); // 畫布加上捲軸放在中央
        setJMenuBar(buildMenuBar()); // 設定選單列

        pack(); // 根據內容自動調整視窗大小
        setDefaultCloseOperation(EXIT_ON_CLOSE); // 關閉視窗時結束程式
        setLocationRelativeTo(null); // 將視窗置中於螢幕
    }

    private JMenuBar buildMenuBar() { // 建立並回傳選單列
        JMenuBar bar = new JMenuBar(); // 建立選單列

        JMenu file = new JMenu("File"); // 建立 File 選單（目前為空，預留）
        bar.add(file); // 加入選單列

        JMenu edit = new JMenu("Edit"); // 建立 Edit 選單（項目將在下一個 commit 加入）
        bar.add(edit); // 加入選單列

        return bar; // 回傳完整選單列
    }
}
