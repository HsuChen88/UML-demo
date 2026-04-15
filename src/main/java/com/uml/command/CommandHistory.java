package com.uml.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Bounded undo/redo stack.
 *
 * Pushing a new command clears the redo stack (standard editor behaviour:
 * a new action discards the undone branch).
 */
public class CommandHistory { // 命令歷史管理器：維護 undo/redo 雙層堆疊

    private static final int MAX_HISTORY = 50; // 最大歷史紀錄筆數（超過則移除最舊的）

    private final Deque<Command> undoStack = new ArrayDeque<>(); // Undo 堆疊：存放已執行的命令（最新的在頂部）
    private final Deque<Command> redoStack = new ArrayDeque<>(); // Redo 堆疊：存放已 undo 的命令（最新還原的在頂部）

    public void push(Command cmd) { // 推入一個已執行的命令到歷史
        undoStack.push(cmd); // 推入 undo 堆疊頂端
        if (undoStack.size() > MAX_HISTORY) { // 若超過上限
            ((ArrayDeque<Command>) undoStack).removeLast(); // 移除最舊的命令（堆疊底部）
        }
        redoStack.clear(); // 清空 redo 堆疊（執行新命令後 redo 分支失效，這是標準編輯器行為）
    }

    public void undo() { // 還原最近一次命令
        if (undoStack.isEmpty()) return; // 若 undo 堆疊為空則不執行
        Command cmd = undoStack.pop(); // 從 undo 堆疊取出最新命令
        cmd.undo(); // 執行還原操作
        redoStack.push(cmd); // 將命令推入 redo 堆疊，供之後重做
    }

    public void redo() { // 重做最近一次已還原的命令
        if (redoStack.isEmpty()) return; // 若 redo 堆疊為空則不執行
        Command cmd = redoStack.pop(); // 從 redo 堆疊取出最近還原的命令
        cmd.redo(); // 執行重做操作
        undoStack.push(cmd); // 將命令重新推入 undo 堆疊
    }

    public boolean canUndo() { return !undoStack.isEmpty(); } // 判斷是否有可還原的命令
    public boolean canRedo() { return !redoStack.isEmpty(); } // 判斷是否有可重做的命令
}
