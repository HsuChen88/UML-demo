# 軟體工程教學筆記

> 這份筆記是給接手這個專案的你準備的，
> 收錄了在這個專案中用到的 Java 語言特性、Swing 技術、Maven 建置工具，
> 以及幾個關鍵設計模式的深入解說。
>
> 每個主題都有「在哪裡用到」的對應，讓你可以直接在程式碼中看到實例。

---

## 目錄

1. [Java 物件導向核心觀念](#一java-物件導向核心觀念)
2. [Java 17 新語法特性](#二java-17-新語法特性)
3. [Java Collections Framework](#三java-collections-framework)
4. [Java Swing 深入解析](#四java-swing-深入解析)
5. [Maven 建置工具](#五maven-建置工具)
6. [七大設計模式詳解](#六七大設計模式詳解)
7. [軟體設計原則 SOLID](#七軟體設計原則-solid)
8. [除錯技巧](#八除錯技巧)

---

## 一、Java 物件導向核心觀念

### 1.1 抽象類別 vs 介面：什麼時候用哪個？

這個專案同時使用了兩者，理解差異很重要。

**抽象類別（`abstract class`）：**
```java
// BasicObject.java
public abstract class BasicObject extends UMLObject {
    private int x, y, width, height;  // ← 有狀態（欄位）
    
    public abstract List<Point> computePorts();  // ← 子類別「必須」實作
    public abstract void drawShape(Graphics2D g);
    
    // 有具體實作的方法（子類別可以繼承直接使用）
    public final List<Point> getPorts() {
        if (portsCache == null) portsCache = computePorts();
        return portsCache;
    }
}
```

**介面（`interface`）：**
```java
// CanvasMouseStrategy.java
public interface CanvasMouseStrategy {
    // Java 8+ 的 default 方法：提供預設（空）實作
    // 實作類別可以選擇性覆寫，不是全部都要實作
    default void onPressed (MouseEvent e, CanvasPanel c) {}
    default void onDragged (MouseEvent e, CanvasPanel c) {}
    default void onReleased(MouseEvent e, CanvasPanel c) {}
    default void onMoved   (MouseEvent e, CanvasPanel c) {}
    default void onClicked (MouseEvent e, CanvasPanel c) {}
}
```

**選擇規則：**
| 用抽象類別 | 用介面 |
|-----------|--------|
| 子類別之間有共同的**狀態**（欄位） | 多個不相關的類別共用相同的**行為合約** |
| 子類別有共同的具體方法 | 需要模擬「多重繼承」（Java 類別只能單繼承）|
| 有「是一種」的關係（`is-a`）| 有「可以做某事」的關係（`can-do`）|

這個專案中：
- `UMLObject` 是抽象類別：所有 UML 物件都有 `depth`、`selected` 等狀態
- `CanvasMouseStrategy` 是介面：三種策略本質上是完全不同的類別，只是都能「回應滑鼠事件」

---

### 1.2 封裝性（Encapsulation）：為什麼欄位要 `private`

這個專案在程式碼審核中改善的最重要一點就是封裝性。

```java
// 壞的做法（審核前的程式碼）
public abstract class UMLObject {
    protected int depth = 50;  // ← protected：子類別和同套件都可以直接存取
    protected boolean selected;
}

// 在 CompositeObject.java（子類別）
public void draw(Graphics2D g) {
    if (selected) { ... }  // ← 直接存取，繞過任何封裝
}
```

```java
// 好的做法（審核後）
public abstract class UMLObject {
    private int depth = 50;  // ← private：只有 UMLObject 自己能存取
    private boolean selected;
    
    public boolean isSelected() { return selected; }  // ← 統一透過 getter
    public void setSelected(boolean s) { this.selected = s; }
}

// 在 CompositeObject.java
public void draw(Graphics2D g) {
    if (isSelected()) { ... }  // ← 透過 getter，清晰且安全
}
```

**為什麼要這樣做？**

1. **防止意外修改**：如果 `depth` 是 `protected`，任何子類別都可以 `depth = -999`，沒有任何保護。
2. **便於加入驗證邏輯**：如果未來需要 `depth` 只能是 0~99，只需修改 `setDepth()` 一個地方。
3. **可讀性**：`isSelected()` 比 `selected` 更清楚表達「詢問是否被選取」的語意。

---

### 1.3 Template Method Pattern（以程式碼說明）

`BasicObject.draw()` 是最好的 Template Method 範例：

```java
// BasicObject.java — 定義「畫一個物件」的步驟骨架
@Override
public void draw(Graphics2D g) {
    drawShape(g);                              // Step 1：畫形狀（由子類別實作）
    if (isSelected() || isHovered()) {
        drawPorts(g);                          // Step 2：畫 port 點（已有預設實作）
    }
    String name = getLabelName();
    if (name != null && !name.isBlank()) {
        drawLabel(g);                          // Step 3：畫標籤（已有預設實作）
    }
}

protected abstract void drawShape(Graphics2D g);  // 這步驟由子類別決定

// ↓ ↓ ↓ 子類別只需要實作 drawShape
public class RectObject extends BasicObject {
    @Override
    protected void drawShape(Graphics2D g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(getX(), getY(), getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(UMLConstants.STROKE_NORMAL));
        g.drawRect(getX(), getY(), getWidth(), getHeight());
    }
}
```

**好處：**
- 不管 Rect 還是 Oval，「畫 port」「畫標籤」的邏輯**只寫一次**（在 `BasicObject`）
- 子類別只需要實作「畫形狀」這一步，其餘步驟自動繼承
- 未來加入菱形，只需實作 `drawShape()`，port 和標籤的繪製自動沿用

---

### 1.4 多型（Polymorphism）的實際應用

```java
// CanvasPanel.java — 畫布繪圖
private final List<UMLObject> objects = new ArrayList<>();

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g.create();
    
    // 這一行同時繪製 RectObject、OvalObject、CompositeObject
    // 不需要 if-else 或 switch，多型幫你分派到正確的 draw()
    objects.forEach(obj -> obj.draw(g2d));
}
```

`objects` 裡可能有 `RectObject`、`OvalObject`、`CompositeObject`。
呼叫 `obj.draw(g2d)` 時，Java 自動呼叫「正確的那個子類別的 draw 方法」。
這就是多型——**對外統一介面，對內各自實作**。

---

## 二、Java 17 新語法特性

### 2.1 Pattern Matching for instanceof（Java 16+）

```java
// 舊的寫法（Java 14 以前）
if (obj instanceof BasicObject) {
    BasicObject bo = (BasicObject) obj;  // 需要強制型別轉換
    bo.getNearestPortIndex(pressPoint);
}

// 新的寫法（Java 16+）
// 使用位置：SelectStrategy.java:57
if (obj instanceof BasicObject bo) {  // 型別判斷 + 宣告變數一步完成
    bo.getNearestPortIndex(pressPoint);
}
```

另一個例子：
```java
// CanvasPanel.java:196
if (selected.size() != 1 || !(selected.get(0) instanceof CompositeObject composite)) return;
// ↑ 如果不是 CompositeObject 就提前返回，同時宣告 composite 變數
List<UMLObject> children = new ArrayList<>(composite.getDirectChildren());
```

---

### 2.2 Switch Expressions（Java 14+）

**舊的 switch 語句：**
```java
// 繁瑣，每個 case 要 break，容易忘記
String result;
switch (mode) {
    case RECT:
        result = "矩形";
        break;
    case OVAL:
        result = "橢圓";
        break;
    default:
        result = "未知";
}
```

**新的 switch 表達式：**
```java
// CreateLinkStrategy.java:66 — 根據模式建立對應的連線物件
LinkObject link = switch (linkMode) {
    case ASSOCIATION    -> new AssociationLink(src, sp, tgt, tp);
    case GENERALIZATION -> new GeneralizationLink(src, sp, tgt, tp);
    case COMPOSITION    -> new CompositionLink(src, sp, tgt, tp);
    default -> throw new IllegalStateException("Unexpected link mode: " + linkMode);
};
```

**也用於 SelectStrategy 中的 Resize 邏輯：**
```java
// SelectStrategy.java:207
switch (bo.getResizeConstraint(resizePort)) {
    case LOCK_WIDTH  -> { newX = bo.getX(); newW = bo.getWidth(); }
    case LOCK_HEIGHT -> { newY = bo.getY(); newH = bo.getHeight(); }
    case NONE        -> { /* both axes resize freely */ }
}
```

---

### 2.3 var（Local Variable Type Inference）

雖然這個專案沒有大量使用，但 Java 10+ 支援：
```java
// 不用寫出完整的型別名稱
var objects = new ArrayList<UMLObject>();  // 等同 ArrayList<UMLObject> objects = ...
var strategy = new SelectStrategy();       // 等同 SelectStrategy strategy = ...
```

使用時機：型別在右側已經很明顯時。
不建議用於：方法參數、回傳型別、介面型別宣告。

---

### 2.4 文字區塊（Text Blocks，Java 15+）

```java
// 這個專案沒有用到，但知道有這個很有用
// 適合寫 JSON、SQL、HTML 等多行字串
String json = """
    {
        "type": "RectObject",
        "x": 100,
        "y": 200
    }
    """;
```

---

## 三、Java Collections Framework

### 3.1 List 的選擇

| 實作 | 優勢 | 使用場景 |
|------|------|---------|
| `ArrayList` | 隨機存取快 (O(1)) | 大多數情況，頻繁讀取 |
| `LinkedList` | 頭尾插入快 (O(1)) | 當作 Queue 或 Deque 使用時 |
| `ArrayDeque` | 作為 Stack 效能最好 | 取代 `Stack` 類別（已過時）|

**這個專案中的選擇：**
```java
// CanvasPanel.java — objects 列表，頻繁讀取和遍歷
private final List<UMLObject> objects = new ArrayList<>();

// CommandHistory.java — undo/redo 堆疊，只需要頭尾操作
private final Deque<Command> undoStack = new ArrayDeque<>();
private final Deque<Command> redoStack = new ArrayDeque<>();
```

---

### 3.2 Map 的選擇

```java
// 一般用途：HashMap（無序）
Map<String, Object> generalMap = new HashMap<>();

// 當 Key 是 Enum 時，永遠用 EnumMap！效能比 HashMap 好
// 使用位置：CanvasPanel.java:39
Map<EditorMode, CanvasMouseStrategy> strategyMap = new EnumMap<>(EditorMode.class);

// 當需要保持插入順序時：LinkedHashMap
// 使用位置：SelectStrategy.java（記錄物件移動前的座標）
Map<UMLObject, Point> moveBefore = new LinkedHashMap<>();
```

**`EnumMap` 為什麼比 `HashMap` 快？**
因為 Enum 的值是整數序號，`EnumMap` 內部用陣列存儲，直接用序號當索引，
比 `HashMap` 計算 hash code 快得多。

---

### 3.3 不可修改的集合視圖

```java
// CanvasPanel.java:209 — 返回不可修改的視圖
public List<UMLObject> getObjects() {
    return Collections.unmodifiableList(objects);
}
```

**為什麼要這樣做？**
```java
// 如果直接返回 objects：
List<UMLObject> list = canvas.getObjects();
list.add(new RectObject(0, 0, 100, 100));  // 哇！外部程式碼直接修改了 canvas 的內部狀態！
// canvas 不知道有物件被加入，也沒有呼叫 repaint()

// 用 unmodifiableList 包裝後：
List<UMLObject> list = canvas.getObjects();
list.add(new RectObject(0, 0, 100, 100));  // ← 拋出 UnsupportedOperationException
// 強制所有修改都透過 canvas.addObject()，確保 repaint() 被呼叫
```

---

### 3.4 Stream API 基礎

這個專案有少量使用，了解基本用法很有幫助：

```java
// 過濾：取得所有被選取的物件
// 使用位置：CanvasPanel.java:170
List<UMLObject> selected = objects.stream()
    .filter(UMLObject::isSelected)        // 只保留 isSelected() == true 的
    .collect(Collectors.toList());        // 收集成 List

// 任意一個滿足條件（anyMatch）
// 使用位置：SelectStrategy.java:137
boolean moved = moveBefore.entrySet().stream()
    .anyMatch(en -> !en.getValue().equals(moveAfter.get(en.getKey())));

// forEach（代替 for 迴圈）
objects.forEach(obj -> obj.draw(g2d));    // 等同 for (UMLObject obj : objects) obj.draw(g2d);
```

---

## 四、Java Swing 深入解析

### 4.1 Event Dispatch Thread（EDT）— 最重要的 Swing 概念

Swing 是**非執行緒安全**的（Thread-unsafe）。
所有 Swing 元件的建立和修改，都必須在 **Event Dispatch Thread** 上執行。

```java
// Main.java — 程式進入點的正確寫法
public static void main(String[] args) {
    FlatLightLaf.setup();
    SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    // ↑ invokeLater 將 Lambda 排入 EDT 的執行佇列
    // ↑ main() 執行完後，EDT 才開始執行 UI 的建立
}
```

**如果不這樣做會怎樣？**
- 在 `main()` 執行緒直接 `new JFrame()` 通常不會立刻出問題
- 但在多執行緒環境（例如有背景執行緒更新 UI 時），會出現難以重現的奇怪 bug

**黃金法則：**
- 任何時候要更新 UI（呼叫 `repaint()`、修改元件屬性），如果你在非 EDT 執行緒，用 `SwingUtilities.invokeLater()`。
- 所有滑鼠事件本身就在 EDT 上，不需要額外包裝。

---

### 4.2 paintComponent 的正確用法

```java
// CanvasPanel.java
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);  // ❗ 必須呼叫！清除背景，避免殘影
    
    // ❗ 建立新的 Graphics2D 副本，不要直接修改傳入的 g
    Graphics2D g2d = (Graphics2D) g.create();
    
    // 開啟抗鋸齒，讓曲線更平滑
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    
    // 繪製邏輯...
    
    // ❗ 一定要 dispose！釋放 g2d 副本持有的資源
    g2d.dispose();
}
```

**為什麼要 `g.create()` 而不是直接用 `g`？**
傳入的 `g` 是 Swing 系統管理的繪圖上下文，如果你修改了它的狀態（顏色、筆觸、座標變換），
可能會影響其他元件的繪圖。`g.create()` 建立獨立副本，`dispose()` 後副本的修改不影響原始 `g`。

**為什麼 `super.paintComponent(g)` 必須呼叫？**
父類別會用背景色填滿元件，清除上一幀的內容。
如果不呼叫，上一幀畫的東西會殘留，造成視覺混亂。

---

### 4.3 repaint() 的工作原理

```java
canvas.repaint();  // ← 這不是立即重繪！
```

`repaint()` 只是告訴 Swing「這個元件需要重繪」，
Swing 會在 EDT 的空閒時間排程一次 `paintComponent()` 呼叫。

**重要推論：**
1. 呼叫 `repaint()` 後，畫面不會立即更新
2. 多次 `repaint()` 可能會被合併成一次繪製（效能最佳化）
3. **絕對不要**在 `paintComponent()` 裡修改資料（會造成無限迴圈）

```java
// 錯誤示範：不要在 paintComponent 裡改資料
@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    objects.add(new RectObject(...));  // ← 觸發 repaint → 再次進 paintComponent → 無限迴圈！
}
```

---

### 4.4 MouseListener vs MouseMotionListener

這兩個介面各自監聽不同類型的滑鼠事件：

```
MouseListener（5 個事件）：
├── mousePressed   — 按下滑鼠按鈕（任何按鈕）
├── mouseReleased  — 放開滑鼠按鈕
├── mouseClicked   — 按下並放開（且中間沒有移動）
├── mouseEntered   — 滑鼠進入元件範圍
└── mouseExited    — 滑鼠離開元件範圍

MouseMotionListener（2 個事件）：
├── mouseDragged   — 按住按鈕同時移動
└── mouseMoved     — 沒有按鈕的移動（hover）
```

**常見問題：`mouseClicked` 的陷阱**

`mouseClicked` 要求「按下後放開，且中間沒有任何移動」才會觸發。
使用者操作時，即使是輕微的 1-2 像素滑動，也不會觸發 `mouseClicked`。
這就是為什麼很多功能（特別是拖曳建立物件）用 `mousePressed` + `mouseReleased` 而不是 `mouseClicked`。

---

### 4.5 JPanel 的繼承慣例

```java
public class CanvasPanel extends JPanel {
    
    public CanvasPanel() {
        setBackground(Color.WHITE);         // 設定背景色
        setPreferredSize(new Dimension(1200, 800));  // 設定偏好大小
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // 自訂繪圖邏輯
    }
}
```

`JPanel` 提供：
- 雙緩衝（Double buffering）防止閃爍
- `setBackground()` / `getBackground()`
- 偏好大小（`setPreferredSize`），影響佈局管理器的計算

---

### 4.6 佈局管理器

**BorderLayout（主視窗用）**
```java
// MainFrame.java
setLayout(new BorderLayout());
add(buttonPanel, BorderLayout.WEST);    // 左側固定寬度
add(scrollPane,  BorderLayout.CENTER);  // 中央佔用剩餘空間
```

**MigLayout（按鈕列用）**
```java
// ButtonPanel.java
setLayout(new MigLayout("wrap 2, insets 12 8 12 8, gap 6 10"));
// wrap 2   → 每行 2 個元件後換行
// insets   → 上右下左的邊距（像 CSS 的 padding）
// gap      → 元件間距（水平 6px，垂直 10px）
```

MigLayout 比內建的 BoxLayout 或 GridLayout 更靈活，是 Swing 中最常用的第三方佈局管理器。

---

### 4.7 JDialog（彈跳視窗）

```java
// LabelDialog.java
public class LabelDialog extends JDialog {
    
    public LabelDialog(Frame owner, ...) {
        super(owner, "Customize Label Style", true);
        //                                    ↑ true = modal（阻擋主視窗）
        
        // 設定佈局、加入元件...
        
        pack();   // 自動計算視窗大小，剛好包住所有元件
        setLocationRelativeTo(owner);  // 置中在父視窗上
    }
}

// 使用方式：
LabelDialog dialog = new LabelDialog(mainFrame, ...);
dialog.setVisible(true);
// ← 在 modal dialog 關閉前，這行之後的程式碼不會執行

// dialog 關閉後才到這裡
if (dialog.isConfirmed()) {
    // 套用使用者的修改
}
```

---

## 五、Maven 建置工具

### 5.1 Maven 是什麼？

Maven 是 Java 世界最常用的**建置工具**和**依賴管理器**，類似 Node.js 的 npm 或 Python 的 pip。

它解決三個問題：
1. **依賴管理**：宣告你需要哪些套件，Maven 自動從網路下載
2. **建置標準化**：`mvn compile`、`mvn test`、`mvn package` 指令在任何機器上都一樣
3. **插件生態系**：`maven-shade-plugin` 可以打包成一個含有所有依賴的「fat JAR」

### 5.2 pom.xml 解析

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <!-- 專案身分識別 -->
    <groupId>com.uml</groupId>         <!-- 組織/公司 ID（通常是反轉的網域名）-->
    <artifactId>uml-editor</artifactId> <!-- 專案名稱 -->
    <version>1.0-SNAPSHOT</version>     <!-- SNAPSHOT = 開發中版本 -->
    
    <!-- 編譯設定 -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>  <!-- 用 Java 17 語法 -->
        <maven.compiler.target>17</maven.compiler.target>  <!-- 編譯到 Java 17 bytecode -->
    </properties>
    
    <!-- 依賴 — Maven 會自動下載這些 JAR -->
    <dependencies>
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.2</version>  <!-- 指定版本，確保可重現的建置 -->
        </dependency>
        <!-- 更多依賴... -->
    </dependencies>
    
    <!-- 建置插件 -->
    <build>
        <plugins>
            <!-- maven-shade-plugin：把所有依賴打包進一個 JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <configuration>
                    <transformers>
                        <!-- 設定 JAR 的主類別（雙擊 JAR 時執行哪個 class）-->
                        <transformer ...>
                            <mainClass>com.uml.Main</mainClass>
                        </transformer>
                    </transformers>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 5.3 依賴是怎麼找到的？

Maven 依賴從 **Maven Central** 倉庫（https://search.maven.org/）下載，
下載後快取在你的家目錄 `~/.m2/repository/`。

你可以在 Maven Central 搜尋任何 Java 套件，複製 pom.xml 片段，加入 `<dependencies>` 即可。

### 5.4 常用 Maven 指令

```bash
mvn clean               # 刪除 target/ 目錄
mvn compile             # 編譯 src/main/java/ 下的所有 Java 檔
mvn test                # 執行測試（src/test/java/ 下的 JUnit 測試）
mvn package             # 編譯 + 打包成 target/ 下的 JAR
mvn clean package       # 完整重建

# 不打包直接執行（開發時最方便）
mvn exec:java -Dexec.mainClass="com.uml.Main"

# 查看專案的完整依賴樹（找版本衝突時很有用）
mvn dependency:tree
```

---

## 六、七大設計模式詳解

### 6.1 Strategy Pattern（策略模式）

**用途：** 把一系列「可互換的演算法」封裝成獨立的物件。

**在本專案的應用：**
```
問題：滑鼠點擊畫布，根據不同模式要做完全不同的事：
  - SELECT 模式：點擊 = 選取物件
  - RECT 模式：點擊 = 在那裡建立矩形
  - ASSOCIATION 模式：點擊 port = 開始畫連線

沒有 Strategy：
  void mousePressed(MouseEvent e) {
      if (mode == SELECT) { ... 50 行選取邏輯 ... }
      else if (mode == RECT) { ... 30 行建立矩形邏輯 ... }
      else if (mode == ASSOCIATION) { ... 40 行連線邏輯 ... }
      // 每新增一種工具就要進來改這個方法
  }

有 Strategy：
  // CanvasPanel 只知道「呼叫當前策略的方法」
  void mousePressed(MouseEvent e) {
      currentStrategy.onPressed(e, this);
  }
  // 換模式就換 strategy，不動 CanvasPanel
  modeManager.addListener((newMode, prev) ->
      currentStrategy = strategyMap.get(newMode));
```

**識別 Strategy Pattern 的特徵：**
- 一個介面定義統一的方法簽名
- 多個實作類別各自實作不同的邏輯
- 一個「使用者」（Context）持有介面引用，可以在執行期換掉實作

---

### 6.2 Command Pattern（命令模式）

**用途：** 把「操作」封裝成物件，讓操作可以被儲存、傳遞、撤銷。

**在本專案的應用：**
```java
// 每個使用者操作都被封裝成 Command
interface Command {
    void undo();  // 撤銷這個操作
    void redo();  // 重做這個操作
}

// 例如：建立矩形
class CreateObjectCommand implements Command {
    private final CanvasPanel canvas;
    private final UMLObject   obj;
    
    @Override public void undo() { canvas.rawRemoveObject(obj); canvas.repaint(); }
    @Override public void redo() { canvas.rawAddObject(obj);    canvas.repaint(); }
}
```

**命令歷史（Command History）雙堆疊設計：**
```
執行操作：push 到 undoStack
Ctrl+Z  ：pop from undoStack, call undo(), push to redoStack
Ctrl+Y  ：pop from redoStack, call redo(), push to undoStack
```

**好處：**
- Undo 可以套用到任何操作，只要把操作封裝成 Command
- Command 可以序列化，甚至可以透過網路傳送（協作編輯）
- 可以實作 Macro（把一系列 Command 包成一個 MacroCommand）

---

### 6.3 Composite Pattern（組合模式）

**用途：** 讓「單一物件」和「物件的集合」可以被一視同仁地對待。

**在本專案的應用：**
```java
// UMLObject 是共同介面
abstract class UMLObject {
    abstract void draw(Graphics2D g);
    abstract boolean contains(int x, int y);
}

// RectObject — 葉節點（Leaf）
class RectObject extends BasicObject {
    void draw(Graphics2D g) { /* 畫一個矩形 */ }
}

// CompositeObject — 容器節點（Composite）
class CompositeObject extends UMLObject {
    List<UMLObject> children;
    
    void draw(Graphics2D g) {
        children.forEach(child -> child.draw(g));  // 遞迴委派給子物件
        // 額外畫群組的虛線外框
    }
}
```

**關鍵洞察：** `CanvasPanel` 在呼叫 `obj.draw()` 時，
不需要知道 `obj` 是單一矩形還是包含 10 個物件的群組——多型幫你處理。

```
CanvasPanel 的 objects 列表：
[RectObject, OvalObject, CompositeObject([RectObject, OvalObject])]

draw() 呼叫：
  ├── RectObject.draw()
  ├── OvalObject.draw()
  └── CompositeObject.draw()
       ├── RectObject.draw()
       └── OvalObject.draw()
```

---

### 6.4 Observer Pattern（觀察者模式）

**用途：** 當一個物件的狀態改變時，自動通知所有「觀察者」。

**在本專案的應用：**
```java
// 主題（Subject）：ModeManager
class ModeManager {
    private List<ModeChangeListener> listeners = new ArrayList<>();
    
    public void addListener(ModeChangeListener l) { listeners.add(l); }
    
    public void setMode(EditorMode newMode) {
        this.currentMode = newMode;
        listeners.forEach(l -> l.onModeChanged(newMode, previousMode));
        // 通知所有觀察者！
    }
}

// 觀察者 1：ButtonPanel（更新按鈕高亮）
modeManager.addListener((newMode, prev) -> updateButtonHighlight(newMode));

// 觀察者 2：CanvasPanel（切換 Strategy）
modeManager.addListener((newMode, prev) -> currentStrategy = strategyMap.get(newMode));
```

**好處：**
- `ModeManager` 不需要知道有哪些觀察者存在
- 新增一個「觀察模式變化的元件」，只需呼叫 `addListener()`，不修改 ModeManager

**Java 標準庫中的 Observer 應用：**
- `ActionListener` / `MouseListener` — 這就是 Observer Pattern！
- `java.util.Observable` / `java.util.Observer`（已過時）
- `PropertyChangeListener`

---

### 6.5 Template Method Pattern（模板方法模式）

**用途：** 在父類別定義演算法的骨架，讓子類別填入具體步驟。

**在本專案的應用：** 見 1.3 節（BasicObject.draw()）。

---

### 6.6 State Pattern（狀態模式）

**用途：** 把物件的不同「狀態」封裝成獨立的類別，讓物件在不同狀態下有不同行為。

**在本專案的應用：**

`SelectStrategy` 內部的子狀態機：
```java
enum SubState { IDLE, DRAGGING_OBJECT, RESIZING, RUBBER_BANDING }
private SubState subState = SubState.IDLE;

@Override
public void onDragged(MouseEvent e, CanvasPanel canvas) {
    switch (subState) {  // 根據狀態決定行為
        case DRAGGING_OBJECT -> { /* 移動物件 */ }
        case RESIZING        -> { /* Resize */ }
        case RUBBER_BANDING  -> { /* 更新框選矩形 */ }
    }
}
```

---

### 6.7 Factory Method Pattern（工廠方法模式）

**用途：** 把「建立物件」的邏輯集中在一個方法，讓呼叫端不需要知道建立哪個子類別。

**在本專案的應用：**
```java
// CreateLinkStrategy.java
private LinkObject buildLink(BasicObject src, int sp, BasicObject tgt, int tp) {
    return switch (linkMode) {
        case ASSOCIATION    -> new AssociationLink(src, sp, tgt, tp);
        case GENERALIZATION -> new GeneralizationLink(src, sp, tgt, tp);
        case COMPOSITION    -> new CompositionLink(src, sp, tgt, tp);
        default -> throw new IllegalStateException("Unexpected link mode: " + linkMode);
    };
}
```

呼叫端（`onReleased`）只需要呼叫 `buildLink()`，不需要知道實際建立的是哪個子類別：
```java
LinkObject link = buildLink(sourceObject, sourcePortIndex, targetObject, targetPortIndex);
canvas.execute(new CreateLinkCommand(canvas, link));
```

---

## 七、軟體設計原則 SOLID

SOLID 是五個物件導向設計原則的縮寫，這個專案很好地體現了它們。

### S — Single Responsibility Principle（單一職責原則）

一個類別只做一件事。

| 類別 | 唯一職責 |
|------|---------|
| `HitTestUtil` | 只做幾何命中測試計算 |
| `UMLConstants` | 只儲存常數 |
| `ModeManager` | 只管理編輯模式的狀態 |
| `CommandHistory` | 只管理 Undo/Redo 歷史 |

---

### O — Open/Closed Principle（開放封閉原則）

**對擴充開放，對修改封閉。** 新增功能時應該加新程式碼，而不是修改現有的。

```java
// 新增一種形狀（例如菱形），只需要：
class DiamondObject extends BasicObject {
    @Override protected void drawShape(Graphics2D g) { /* 新實作 */ }
    @Override protected List<Point> computePorts() { /* 新實作 */ }
}

// SelectStrategy 完全不需要修改，因為：
private void applyResize(BasicObject bo, int mx, int my) {
    switch (bo.getResizeConstraint(resizePort)) {  // ← 多型！不管什麼形狀都能用
        case LOCK_WIDTH  -> ...
        case LOCK_HEIGHT -> ...
        case NONE        -> ...
    }
}
```

---

### L — Liskov Substitution Principle（里氏替換原則）

子類別必須可以替換父類別使用，而不破壞程式正確性。

```java
List<UMLObject> objects = ...;  // 包含 RectObject、OvalObject、CompositeObject

// 這段程式碼對任何 UMLObject 子類別都必須正確運作
objects.forEach(obj -> {
    obj.draw(g2d);          // 每個子類別都要能正確繪製自己
    boolean hit = obj.contains(x, y);  // 每個子類別都要正確判斷是否命中
});
```

---

### I — Interface Segregation Principle（介面隔離原則）

不要強迫一個類別實作它不需要的方法。

```java
// CanvasMouseStrategy 的 default 方法 — 這就是 ISP 的應用
interface CanvasMouseStrategy {
    default void onPressed (MouseEvent e, CanvasPanel c) {}  // 預設空方法
    default void onDragged (MouseEvent e, CanvasPanel c) {}
    default void onReleased(MouseEvent e, CanvasPanel c) {}
    default void onMoved   (MouseEvent e, CanvasPanel c) {}
    default void onClicked (MouseEvent e, CanvasPanel c) {}
}

// CreateObjectStrategy 只需要覆寫它實際用到的方法
class CreateObjectStrategy implements CanvasMouseStrategy {
    @Override public void onReleased(MouseEvent e, CanvasPanel canvas) { /* 建立物件 */ }
    // 其他方法使用預設空實作，不需要寫
}
```

---

### D — Dependency Inversion Principle（依賴反轉原則）

高層模組不應依賴低層模組；兩者都應依賴**抽象**。

```java
// CanvasPanel（高層）依賴介面（抽象）
private CanvasMouseStrategy currentStrategy;  // ← 介面型別
// 不是 private SelectStrategy currentStrategy;  ← 具體型別（錯誤）

// 執行期動態替換具體實作
currentStrategy = strategyMap.get(newMode);  // SelectStrategy 或 CreateObjectStrategy
```

---

## 八、除錯技巧

### 8.1 Swing 除錯的特殊挑戰

Swing 的錯誤有時候不會馬上炸出來，而是以「畫面閃爍」或「元件不更新」等視覺異常呈現。

**常見問題 1：`repaint()` 沒有反應**

```java
// 最常見的原因：資料改了，但沒有呼叫 repaint()
public void moveObject(UMLObject obj, int dx, int dy) {
    obj.move(dx, dy);
    // repaint();  ← 忘記呼叫這行，畫面不會更新
}
```

**常見問題 2：NullPointerException 在 paintComponent**

```java
// 危險的寫法：如果 hoveredObject 為 null 且你沒有處理
if (hoveredObject.isSelected()) { ... }  // ← NullPointerException

// 安全的寫法
if (hoveredObject != null && hoveredObject.isSelected()) { ... }
```

**常見問題 3：`mouseClicked` 觸發後 `mouseReleased` 也會觸發**

`mouseClicked` = 快速點擊（pressed + released 且沒有移動）
`mouseReleased` 在 clicked 之後也會觸發。
不要在兩個地方處理相同的邏輯！

---

### 8.2 在 IntelliJ 中設定斷點偵錯

1. 在你想暫停的那行程式碼左側點一下，出現紅色圓點
2. 用 Debug 模式啟動（而不是 Run）：點綠色蟲子圖示
3. 程式執行到斷點時會暫停，右下角的 Variables 視窗顯示所有變數的當前值
4. 點「Step Over」（F8）逐行執行，「Step Into」（F7）進入方法內部

**對 Swing 特別有用的斷點技巧：**
在 `paintComponent` 第一行設斷點，可以看到每次重繪時的物件狀態。
在 Strategy 的 `onPressed` / `onDragged` 設斷點，可以追蹤滑鼠事件流程。

---

### 8.3 System.out.println 除錯

簡單但有效，快速確認某段程式碼是否被執行：

```java
@Override
public void onPressed(MouseEvent e, CanvasPanel canvas) {
    System.out.println("[SelectStrategy] onPressed at (" + e.getX() + ", " + e.getY() + ")");
    System.out.println("[SelectStrategy] hit object: " + canvas.findObjectAt(e.getX(), e.getY()));
    // 正式版本記得移除這些 println
}
```

---

### 8.4 驗證幾何計算正確性

幾何計算（Port 位置、Resize 邏輯）很容易有 off-by-one 或邊界條件錯誤。

快速驗證方式：
```java
// 在 drawShape 加入暫時的視覺輔助
@Override
protected void drawShape(Graphics2D g) {
    // 正常的繪圖邏輯...
    
    // DEBUG：畫出 Resize 錨點（紅色大圓）
    for (int i = 0; i < getPorts().size(); i++) {
        Point anchor = getResizeAnchor(i);
        g.setColor(Color.RED);
        g.fillOval(anchor.x - 5, anchor.y - 5, 10, 10);
        g.setColor(Color.BLUE);
        g.drawString(String.valueOf(i), anchor.x + 5, anchor.y);
    }
}
```

---

*文件最後更新：2026-04-15*
