package com.uml.command;

/**
 * Command interface for undo/redo.
 *
 * {@code redo()} doubles as the initial execution action:
 * {@link com.uml.view.CanvasPanel#execute} calls {@code redo()} then pushes to history.
 * This avoids duplicate execution paths for "do" vs "redo".
 */
public interface Command { // Command Pattern 的命令介面；undo 還原操作，redo 重做（同時也是初次執行）
    void undo(); // 還原此命令的效果（回到執行前的狀態）
    void redo(); // 重做此命令的效果（也用作初次執行，避免 do/redo 兩段重複邏輯）
}
