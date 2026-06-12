# UML Editor — OO 設計與架構評價

## 總體評價

這份程式的水準明顯高於一般課程專案。分層清楚、Strategy / Command / Composite / Template Method / Factory / Registry 都用在對的地方而非硬塞,[PortOwner.java](src/main/java/com/uml/model/PortOwner.java) 用能力介面取代具體類別依賴是很成熟的判斷,Command 也確實做到可脫離 Swing 測試。[ARCHITECTURE.md](ARCHITECTURE.md) 對每個取捨的說明也誠實。

但這個系統有一個**核心未完成的抽象**拖累了它宣稱的所有擴充性優點,而且程式裡留下的死碼與 [ARCHITECTURE.md](ARCHITECTURE.md) 的聲稱有數處直接矛盾。以下按嚴重度排列。

---

## 1.〔最嚴重〕選取/hover 有兩個真相來源,而且程式自己也不信任任何一個

`selected` / `hovered` flag 同時存在於 [UMLObject.java:9-10](src/main/java/com/uml/model/UMLObject.java#L9-L10),又存在於 [DiagramSelectionModel](src/main/java/com/uml/model/DiagramSelectionModel.java) 和 [CanvasInteractionState](src/main/java/com/uml/view/CanvasInteractionState.java)。問題不只是重複,而是**程式碼明顯不信任任一來源**:

```java
// DiagramSelectionModel.java:54-58
if (selectedObjects.contains(obj) || obj.isSelected()) visibleSelected.add(obj);
```

```java
// clearSelection():43-47 — 兩個來源各清一次「以防萬一」
selectedObjects.forEach(o -> o.setSelected(false));
document.getObjects().forEach(o -> o.setSelected(false));
```

`||` 與「兩邊都清」是典型的雙真相來源警訊:當兩個欄位可能不一致時,你只能 OR 起來賭。更嚴重的是 [SelectStrategy](src/main/java/com/uml/controller/strategy/SelectStrategy.java) 在**同一個方法裡混用兩種 API**——[L57](src/main/java/com/uml/controller/strategy/SelectStrategy.java#L57) 用 `selectionModel.isSelected(obj)`,[L75](src/main/java/com/uml/controller/strategy/SelectStrategy.java#L75) 卻用 `hit.isSelected()`。

**我的意見**:這不只是技術債,它讓 11.2 宣稱的「未來多 view 各自有不同 selection」**根本無法成立**——因為 renderer 是透過 `object.draw()` 讀物件自身的 `isSelected()` 來畫 port 的([BasicObject.java:29](src/main/java/com/uml/model/BasicObject.java#L29)),選取狀態被綁死在 model 物件上,兩個 view 必然共享同一份選取。這個抽象只做了一半,而宣稱的好處一個都還沒兌現。要嘛就把 flag 從 `UMLObject` 真的拿掉、讓 renderer 改問 `CanvasRenderContext` 裡的 selectionModel/interactionState(那個 context 本來就傳進去了,只是 [UMLObjectRenderer](src/main/java/com/uml/view/renderer/UMLObjectRenderer.java) 沒用),要嘛就承認暫時不做多 view、把 model 當唯一來源、刪掉 `DiagramSelectionModel.selectedObjects` 那個 Set。現在這種兩者並存是最糟的狀態。

---

## 2.〔嚴重〕Strategy 拿到的是整個具體 `CanvasPanel`,Strategy 模式的解耦被自己抵銷

`CanvasMouseStrategy` 每個方法簽章都是 `(MouseEvent, CanvasPanel canvas)`([CanvasMouseStrategy.java:14-18](src/main/java/com/uml/controller/strategy/CanvasMouseStrategy.java#L14-L18))。於是 controller 層的每個 strategy 都能呼叫 [CanvasPanel](src/main/java/com/uml/view/CanvasPanel.java) 的**任何** public 方法,而那個 public 介面非常龐大(group/ungroup/undo/redo/execute/raw*/repaint/各種 accessor)。

這造成兩個問題:
- **依賴方向變成環狀**:`view → controller.strategy → view`。controller 反過來編譯依賴具體 view 類別。
- Strategy 模式的重點是把行為與 context 解耦,但這裡等於把整個 God-object 交給 strategy , strategy 想呼叫什麼都行,真正的契約完全沒被定義。

**我的意見**:抽一個 `EditorContext`(或 `CanvasController`)介面,只暴露 strategy 真正需要的東西——`getDocument()`、`getSelectionModel()`、`getInteractionState()`、`execute()`、`pushHistory()`、`repaint()`、`restorePreviousMode()`。strategy 依賴這個介面而非 `CanvasPanel`。這樣依賴方向才會乾淨,也才測得動 strategy。以現在的規模這不是過度設計,因為你已經有 4 個 strategy 在共用這個隱性契約了。

---

## 3.〔嚴重〕Port 的幾何知識散在三個 switch,靠魔術數字索引手動同步

看 [RectObject](src/main/java/com/uml/model/RectObject.java):port 佈局被編碼在**三個地方**——`computePorts()` 的順序([L21-33](src/main/java/com/uml/model/RectObject.java#L21-L33))、`getResizeConstraint()` 的 `switch`([L54-60](src/main/java/com/uml/model/RectObject.java#L54-L60))、`getResizeAnchor()` 的 `switch`([L70-83](src/main/java/com/uml/model/RectObject.java#L70-L83)),全部靠 `0..7` 這組 magic int 對齊,只用註解維繫。

只要有人改動 `computePorts()` 的順序,另外兩個 switch 會**靜默地**錯掉,不會有任何編譯錯誤,而是 resize 行為詭異。這正是 OO 應該消滅、而不是製造的那種重複。

**我的意見**:讓「一個 port」變成一個物件/enum,自己攜帶位置、constraint、anchor 三項資訊,`computePorts()` 回傳這個結構。三處 switch 收斂成一份定義,新增形狀時也只需描述自己的 port 集合,不必再記三張對照表。這對「新增圖形」這個最常見的擴充需求直接受益。

---

## 4.〔嚴重〕大量死碼,且與 ARCHITECTURE.md 的聲稱直接矛盾

我 grep 過全專案,以下定義了但**零呼叫點**:

- `rawAddObject` / `rawRemoveObject` / `rawAddLink` / `rawRemoveLink`([CanvasPanel.java:150-153](src/main/java/com/uml/view/CanvasPanel.java#L150-L153))。註解寫「保留相容 API」,但 11.6 又明說「Command 不再操作 `rawAddObject()`」——既然沒人用,留著只會誤導讀者以為還有 raw 路徑。
- `LinkObject(BasicObject, int, BasicObject, int)` 這組建構子,以及三個 link 子類對應的 `(BasicObject,...)` 建構子——**全部沒有呼叫點**。這直接打臉 11.5:該節說重構後 link 不再綁 `BasicObject`,但這些建構子正是把 `BasicObject` 拉回 link 層的耦合,而且是死的。
- `LinkObject.getSource()` / `getTarget()`([LinkObject.java:44-45](src/main/java/com/uml/model/link/LinkObject.java#L44-L45))、`findBasicObjectNearPort`([CanvasPanel.java:170](src/main/java/com/uml/view/CanvasPanel.java#L170))與其唯一依賴的 `DiagramDocument.findPortOwnerNearPort`、`setTempLinkEnd`([CanvasPanel.java:212](src/main/java/com/uml/view/CanvasPanel.java#L212))——皆無呼叫點。

**我的意見**:這次重構文件寫得很用心,但死碼讓文件變成不可信。一個資深 reviewer 看到「保留相容 API」卻沒人相容、看到 `BasicObject` 建構子卻宣稱已解耦,會開始懷疑整份文件。直接刪掉。`LinkObject` 只留 `(PortReference, PortReference)` 一組建構子,才對得起 11.5 的論述。(順帶一提 `simulateRelease` 是有用的——[ButtonPanel:74](src/main/java/com/uml/view/ButtonPanel.java#L74) 在用,它不是死碼。)

---

## 5.〔中等〕`getResizeAnchor` 的基底預設值是「錯的」而非「強制覆寫」

[BasicObject.java:85-87](src/main/java/com/uml/model/BasicObject.java#L85-L87):

```java
public Point getResizeAnchor(int portIndex) {
    return new Point(getX(), getY());   // 預設回傳左上角
}
```

`getResizeConstraint` 預設 `NONE`、`getResizeAnchor` 預設左上角。問題是:未來有人新增一個 `BasicObject` 子類別,忘了覆寫這兩個方法,**不會編譯失敗**,而是得到一個「角落以外的 port 全部以左上角為錨點」的詭異 resize——一個安靜的錯誤,比 crash 難抓。`computePorts` / `drawShape` 是 abstract 強制實作,但決定 resize 正確性的這兩個卻是有(錯誤)預設值的虛擬方法,標準不一致。

**我的意見**:如果 port resize 是每個 BasicObject 都必須正確的核心行為,就把這兩個也設成 abstract,逼子類面對。或者更好——承接第 3 點,讓 anchor 由 port 結構自動推導,根本不需要子類各寫一張 switch。

---

## 6.〔中等〕Command 的「執行」有兩套哲學,而 SetLabelCommand 沒有理由用第二套

[Command.java](src/main/java/com/uml/command/Command.java) 的 javadoc 明確定義「`redo()` 同時是初次執行」,[CreateObjectStrategy](src/main/java/com/uml/controller/strategy/CreateObjectStrategy.java) 也照做(`execute()` → `redo()` 做事)。但 [MainFrame.openLabelDialog](src/main/java/com/uml/view/MainFrame.java#L116-L123) 卻是**先在 view 裡手動改 model、repaint,再 `pushHistory` 一個已經發生過的 command**。

move/resize 用 `pushHistory` 我能接受——它們在拖曳過程中本來就必須即時改 model,onReleased 時補登記是合理的。但 `SetLabelCommand` **沒有這個藉口**:它持有 before/after 完整狀態,完全可以走 `execute()` 讓 `redo()` 自己套用。現在的寫法讓「redo == do」這條讀者賴以理解的不變式被打破了,而且把 model 修改邏輯漏進了 `MainFrame`。

**我的意見**:`SetLabelCommand` 改走 `canvas.execute(...)`,把 [MainFrame.java:116-117](src/main/java/com/uml/view/MainFrame.java#L116-L117) 那兩行直接改 model 的程式碼刪掉。讓 view 只負責「收集 before/after → 建 command → execute」,不負責動 model。

---

## 7.〔中等〕Link 是二等公民:能建立,但不能選取、不能刪除,也沒有參照完整性

連線可以畫、可以 undo 建立,但**沒有任何路徑能選取或刪除一條 link**,連 link 的 hit-test 都不存在。同時 `DiagramDocument.removeObject`([L32](src/main/java/com/uml/model/DiagramDocument.java#L32))移除物件時,**不會清掉以它為端點的 link**——`LinkObject` 透過 `PortReference` 強參考著 `PortOwner`,一旦真的支援刪除物件,就會留下指向已刪物件的懸空連線(`getPoint()` 仍會解析到舊座標)。

**我的意見**:這是最常見的擴充需求(刪除)即將踩到的坑。即使這次不做刪除 UI,我也會在 `DiagramDocument` 就把參照完整性建好:`removeObject` 連帶移除相關 links,或至少提供 `linksTouching(PortOwner)` 查詢。現在 link 完全沒有 hit-test/selection 能力,意味著它的生命週期管理是缺的,而不是「之後再加」就能無痛補上的——加上去會回頭要求 link 也進選取模型。早點決定它的地位比較省。

---

## 8.〔中等〕Model 仍依賴 `Graphics2D`,renderer 層目前只是空殼

[UMLObjectRenderer](src/main/java/com/uml/view/renderer/UMLObjectRenderer.java) 和 [LinkRenderer](src/main/java/com/uml/view/renderer/LinkRenderer.java) 現在都只是 `object.draw(g)` / `link.draw(g)` 的轉呼叫,真正的繪圖知識還在 model 裡(`BasicObject.drawShape`、`LinkObject.drawArrowHead`)。11.9 老實承認這是刻意的階段性取捨,我認同「先把依賴方向擺正、不急著搬」這個判斷。

**我的意見**:只提醒一點——只要 model 還 `import java.awt.Graphics2D`,11.10 想要的「把 model 變成純資料 / 輸出圖片 / 換繪圖後端」就都還沒解鎖,而且 renderer 層現在是**零價值的轉呼叫**,反而增加閱讀成本。這層的存在目前只是「佔位宣示意圖」。它值不值得留,取決於你是否真的近期要搬繪圖;若不搬,我會考慮先收掉這層、等真要動 rendering 時再引入,避免空殼誤導。這是判斷題不是錯誤,但要意識到目前它是淨負擔。

---

## 9.〔次要〕其餘

- **滑鼠熱路徑的 `System.out.println`**:[CanvasPanel.java:77/88/94/101](src/main/java/com/uml/view/CanvasPanel.java#L77) 每次 press/release/click/drag 都印。11.10 說刻意保留,但以「長期維護」的角度這是雜訊,應換成可關閉的 logger。
- **`ModeManager` 只有 `addListener` 沒有 `removeListener`**([ModeManager.java:30](src/main/java/com/uml/controller/ModeManager.java#L30)):以本程式生命週期無妨,但是 Observer 模式漏了一半。
- **選取順序是死的意圖**:`DiagramSelectionModel` 特地用 `LinkedHashSet`「保留選取順序」([L12](src/main/java/com/uml/model/DiagramSelectionModel.java#L12)),但 `getSelectedObjects()` 又改用 document 順序重建([L55](src/main/java/com/uml/model/DiagramSelectionModel.java#L55)),於是那份順序從沒被用到。要嘛就用,要嘛別宣稱保留。

---

## 一句話總結

架構骨架與 pattern 選用是資深水準,但**第 1 點(雙真相來源)+ 第 2 點(strategy 抓著整個具體 view)是兩個會持續滲血的結構問題**,加上第 4 點的死碼讓重構文件失去公信力。我會優先修這三項:它們花的力氣不大,但直接決定了 ARCHITECTURE.md 宣稱的擴充性是真的還是紙上的。
