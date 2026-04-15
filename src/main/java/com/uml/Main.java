package com.uml; // 宣告此檔案所在的根套件

import com.formdev.flatlaf.FlatLightLaf;
import com.uml.view.MainFrame;

import javax.swing.*;

public class Main { // 程式進入點類別
    public static void main(String[] args) { // Java 程式的主方法
        FlatLightLaf.setup(); // 套用 FlatLight 主題（必須在任何 Swing 元件建立前呼叫）
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true)); // 在 Event Dispatch Thread 上建立並顯示主視窗（Swing 執行緒安全規則）
    }
}
