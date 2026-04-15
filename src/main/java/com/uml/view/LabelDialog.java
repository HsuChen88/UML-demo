package com.uml.view;

import com.uml.util.UMLConstants;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * Use Case G: modal dialog for customising a basic object's label name and color.
 */
public class LabelDialog extends JDialog { // 標籤設定對話框（Use Case G），模態對話框確保使用者完成操作才返回

    private final JTextField nameField;     // 標籤文字輸入框
    private final JButton    colorButton;   // 顏色選擇按鈕（背景色顯示目前選取的顏色）
    private Color   selectedColor;          // 目前選取的顏色
    private boolean confirmed = false;      // 是否點擊了 OK 按鈕（true 表示確認，false 表示取消）

    public LabelDialog(Frame owner, String currentName, Color currentColor) { // 建構子：接收父視窗、目前標籤文字與顏色
        super(owner, "Customize Label Style", true); // 建立模態對話框（true = modal，阻塞父視窗）

        selectedColor = (currentColor != null) ? currentColor : UMLConstants.COLOR_DEFAULT_LABEL; // 初始化顏色（若無則用預設灰色）

        nameField   = new JTextField(currentName != null ? currentName : "", 16); // 建立文字輸入框，帶入目前標籤文字
        colorButton = new JButton("Choose Color"); // 建立顏色選擇按鈕
        colorButton.setBackground(selectedColor);       // 按鈕背景色顯示目前選取的顏色
        colorButton.setOpaque(true);                    // 設為不透明，使背景色生效
        colorButton.addActionListener(e -> { // 點擊顏色按鈕時開啟顏色選擇器
            Color chosen = JColorChooser.showDialog(this, "Pick Color", selectedColor); // 顯示系統顏色選擇對話框
            if (chosen != null) { // 若使用者選擇了顏色（非取消）
                selectedColor = chosen;             // 更新選取的顏色
                colorButton.setBackground(chosen); // 更新按鈕背景色預覽
            }
        });

        JButton ok     = new JButton("OK"); // 建立確認按鈕
        JButton cancel = new JButton("Cancel"); // 建立取消按鈕
        ok.addActionListener    (e -> { confirmed = true; dispose(); }); // 點擊 OK：設定確認旗標並關閉對話框
        cancel.addActionListener(e -> dispose()); // 點擊 Cancel：直接關閉對話框（confirmed 保持 false）

        setLayout(new MigLayout("wrap 2, insets 16", "[][grow, fill]", "[]8[]16[]")); // 使用 MigLayout：2 欄，設定邊距和間距
        add(new JLabel("Name:"));   // 加入 Name 標籤
        add(nameField);                 // 加入文字輸入框
        add(new JLabel("Color:")); // 加入 Color 標籤
        add(colorButton);               // 加入顏色選擇按鈕
        add(cancel, "span 2, split 2, right, w 80!"); // 加入 Cancel 按鈕（跨兩欄，向右對齊，固定寬度）
        add(ok,     "w 80!"); // 加入 OK 按鈕（固定寬度，緊跟 Cancel 右側）

        getRootPane().setDefaultButton(ok); // 設定 OK 為預設按鈕（按 Enter 等同點擊 OK）
        pack(); // 根據內容自動調整對話框大小
        setResizable(false); // 禁止調整大小
        setLocationRelativeTo(owner); // 將對話框置中於父視窗
    }

    public boolean isConfirmed()   { return confirmed; }            // 回傳是否確認（OK 被點擊）
    public String  getLabelName()  { return nameField.getText(); }  // 回傳使用者輸入的標籤文字
    public Color   getLabelColor() { return selectedColor; }        // 回傳使用者選取的顏色
}
