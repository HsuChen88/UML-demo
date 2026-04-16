# UML Editor — 架構圖

> 以下使用 Mermaid 圖表，可在 VSCode 安裝 **Markdown Preview Mermaid Support** 擴充套件後預覽。

---

## 1. 分層架構（Layered Architecture）

```mermaid
graph TB
    subgraph ENTRY["Entry Point"]
        MAIN[Main\nFlatLight 主題 + EDT 啟動]
    end

    subgraph UI["View 層 (com.uml.view)"]
        MF[MainFrame\nJFrame + 選單列]
        BP[ButtonPanel\n工具按鈕面板]
        CP[CanvasPanel\n畫布 + 事件路由 + 模型持有者]
        LD[LabelDialog\n標籤 / 顏色編輯對話框]
        BDG[ButtonDragGlassPane\n按鈕拖拽玻璃層]
    end

    subgraph CTL["Controller 層 (com.uml.controller)"]
        MM[ModeManager\n狀態機]
        EM[EditorMode\n列舉：6 種模式]
        MCL[ModeChangeListener\n觀察者介面]
        subgraph STR["strategy 子包 (com.uml.controller.strategy)"]
            CMS[CanvasMouseStrategy\n介面（default no-op）]
            SS[SelectStrategy\n選取 / 移動 / 縮放]
            COS[CreateObjectStrategy\n建立圖形]
            CLS[CreateLinkStrategy\n建立連線 + buildLink 工廠]
        end
    end

    subgraph CMD["Command 層 (com.uml.command)"]
        CI[Command 介面\nundo / redo]
        CH[CommandHistory\nmax 50，雙堆疊]
        COC[CreateObjectCommand]
        CLC[CreateLinkCommand]
        MOC[MoveObjectsCommand\nMap before / after]
        ROC[ResizeObjectCommand\n前後 bounds]
        GC[GroupCommand]
        UGC[UngroupCommand]
        SLC[SetLabelCommand\n名稱 + 顏色]
    end

    subgraph MDL["Model 層 (com.uml.model)"]
        UO[UMLObject\n抽象基底]
        BO[BasicObject\n抽象形狀 + ResizeConstraint]
        RO[RectObject\n矩形 8 端口]
        OO[OvalObject\n橢圓 4 端口]
        CO[CompositeObject\n群組容器]
        subgraph LNK["link 子包 (com.uml.model.link)"]
            LO[LinkObject\n抽象連線]
            AL[AssociationLink\n實心三角]
            GL[GeneralizationLink\n空心三角]
            CML[CompositionLink\n實心菱形]
        end
    end

    subgraph UTL["Util 層 (com.uml.util)"]
        UC[UMLConstants\n常數定義]
        HTU[HitTestUtil\n碰撞偵測]
    end

    MAIN --> MF
    MF --> BP
    MF --> CP
    MF --> LD
    MF --> BDG
    BP -.->|實作| MCL
    BDG -->|simulateRelease| CP
    MM -->|通知| MCL
    CP -->|委派滑鼠事件| CMS
    CMS -.->|實作| SS
    CMS -.->|實作| COS
    CMS -.->|實作| CLS
    CP -->|execute / pushHistory| CH
    CH -->|push / undo / redo| CI
    CI -.->|實作| COC & CLC & MOC & ROC & GC & UGC & SLC
    SS & COS & CLS -->|rawAdd / rawRemove / moveTo| MDL
    UO --> BO & CO
    BO --> RO & OO
    LO --> AL & GL & CML
    CP -->|查詢| HTU
```

---

## 2. 設計模式總覽

```mermaid
graph LR
    ROOT["設計模式"]

    ROOT --> P1["Composite\n組合模式"]
    P1 --> P1A["UMLObject 抽象基底"]
    P1 --> P1B["BasicObject 葉節點\nRectObject / OvalObject"]
    P1 --> P1C["CompositeObject 容器\n遞迴 draw / move / contains"]

    ROOT --> P2["Strategy\n策略模式"]
    P2 --> P2A["CanvasMouseStrategy 介面\n(default no-op 方法)"]
    P2 --> P2B["SelectStrategy\nIDLE / DRAGGING_OBJECT / RESIZING / RUBBER_BANDING"]
    P2 --> P2C["CreateObjectStrategy"]
    P2 --> P2D["CreateLinkStrategy"]
    P2 --> P2E["CanvasPanel.strategyMap\n依模式動態切換"]

    ROOT --> P3["Command\n命令模式"]
    P3 --> P3A["Command 介面 undo / redo"]
    P3 --> P3B["CommandHistory 雙堆疊 max 50"]
    P3 --> P3C["execute(cmd) — 呼叫 redo 後 push"]
    P3 --> P3D["pushHistory(cmd) — 直接 push（漸進操作）"]

    ROOT --> P4["Observer\n觀察者模式"]
    P4 --> P4A["ModeManager 發佈者"]
    P4 --> P4B["ModeChangeListener 介面"]
    P4 --> P4C["ButtonPanel 訂閱者\n高亮現用模式按鈕"]

    ROOT --> P5["Template Method\n樣板方法"]
    P5 --> P5A["BasicObject.draw 骨架\ndrawShape / drawPorts / drawLabel"]
    P5 --> P5B["LinkObject.draw 骨架\ndrawArrowHead 子類實作"]

    ROOT --> P6["State\n狀態模式"]
    P6 --> P6A["ModeManager\n全域 6 種模式"]
    P6 --> P6B["SelectStrategy 子狀態\nIDLE / DRAGGING_OBJECT / RESIZING / RUBBER_BANDING"]

    ROOT --> P7["Factory Method\n工廠方法"]
    P7 --> P7A["CreateLinkStrategy.buildLink()"]
    P7 --> P7B["依 EditorMode 建立\nAssociationLink / GeneralizationLink / CompositionLink"]
```

---

## 3. 類別關係圖（Class Diagram）

```mermaid
classDiagram
    class UMLObject {
        <<abstract>>
        -int depth
        -boolean selected
        -boolean hovered
        -String labelName
        -Color labelColor
        +draw(Graphics2D)*
        +contains(int,int) boolean*
        +getBounds() Rectangle*
        +move(int,int)*
        +moveTo(int,int)*
        +getters/setters()
    }

    class BasicObject {
        <<abstract>>
        -int x
        -int y
        -int width
        -int height
        -List~Point~ portsCache
        +getPorts() List~Point~
        +getPort(int) Point
        +getNearestPortIndex(Point) int
        +getResizeConstraint(int) ResizeConstraint
        +getResizeAnchor(int) Point
        +setBounds(int,int,int,int)
        +draw(Graphics2D)
        #drawShape(Graphics2D)*
        #computePorts() List~Point~*
        -drawPorts(Graphics2D)
        -drawLabel(Graphics2D)
    }

    class ResizeConstraint {
        <<enumeration>>
        NONE
        LOCK_WIDTH
        LOCK_HEIGHT
    }

    class RectObject {
        +computePorts() List~Point~
        +getResizeConstraint(int) ResizeConstraint
        +getResizeAnchor(int) Point
        +drawShape(Graphics2D)
    }

    class OvalObject {
        +computePorts() List~Point~
        +contains(int,int) boolean
        +getResizeConstraint(int) ResizeConstraint
        +getResizeAnchor(int) Point
        +drawShape(Graphics2D)
    }

    class CompositeObject {
        -List~UMLObject~ children
        +getDirectChildren() List~UMLObject~
        +getBounds() Rectangle
        +draw(Graphics2D)
        +move(int,int)
        +moveTo(int,int)
        +addChild(UMLObject)
        +removeChild(UMLObject)
    }

    class LinkObject {
        <<abstract>>
        #BasicObject source
        #int sourcePortIndex
        #BasicObject target
        #int targetPortIndex
        +draw(Graphics2D)
        #drawArrowHead(Graphics2D,Point,Point)*
        -angle(Point,Point) double
        +getSource() BasicObject
        +getTarget() BasicObject
    }

    class AssociationLink {
        +drawArrowHead() 實心三角
    }

    class GeneralizationLink {
        +drawArrowHead() 空心三角
    }

    class CompositionLink {
        +drawArrowHead() 實心菱形
    }

    class CanvasMouseStrategy {
        <<interface>>
        +onPressed(MouseEvent, CanvasPanel)
        +onDragged(MouseEvent, CanvasPanel)
        +onReleased(MouseEvent, CanvasPanel)
        +onMoved(MouseEvent, CanvasPanel)
        +onClicked(MouseEvent, CanvasPanel)
    }

    class SelectStrategy {
        -SubState subState
        -Point pressPoint
        -UMLObject dragTarget
        -int resizePort
        -Point fixedAnchor
        -Map~UMLObject,Point~ moveBefore
    }

    class CreateObjectStrategy {
        -EditorMode objectMode
        -ModeManager modeManager
    }

    class CreateLinkStrategy {
        -EditorMode linkMode
        -BasicObject sourceObject
        -int sourcePortIndex
        -Point tempEnd
        -buildLink(BasicObject,int,BasicObject,int) LinkObject
    }

    class Command {
        <<interface>>
        +undo()
        +redo()
    }

    class CommandHistory {
        -Deque~Command~ undoStack
        -Deque~Command~ redoStack
        -MAX_HISTORY = 50
        +push(Command)
        +undo()
        +redo()
        +canUndo() boolean
        +canRedo() boolean
    }

    class ModeManager {
        -EditorMode currentMode
        -EditorMode previousMode
        -List~ModeChangeListener~ listeners
        +setMode(EditorMode)
        +restorePreviousMode()
        +getCurrentMode() EditorMode
        +addListener(ModeChangeListener)
    }

    class ModeChangeListener {
        <<interface>>
        +onModeChanged(EditorMode, EditorMode)
    }

    class CanvasPanel {
        -List~UMLObject~ objects
        -List~LinkObject~ links
        -CanvasMouseStrategy currentStrategy
        -Map~EditorMode,CanvasMouseStrategy~ strategyMap
        -CommandHistory history
        -Rectangle rubberBand
        +execute(Command)
        +pushHistory(Command)
        +rawAddObject(UMLObject)
        +rawRemoveObject(UMLObject)
        +rawAddLink(LinkObject)
        +rawRemoveLink(LinkObject)
        +group()
        +ungroup()
        +findObjectAt(int,int) UMLObject
        +findBasicObjectNearPort(int,int) BasicObject
        +simulateRelease(int,int)
    }

    class ButtonPanel {
        -Map~EditorMode,JLabel~ buttons
        +onModeChanged(EditorMode, EditorMode)
    }

    class ButtonDragGlassPane {
        -CanvasPanel canvas
        +activate()
        -deactivate()
    }

    UMLObject <|-- BasicObject
    UMLObject <|-- CompositeObject
    BasicObject <|-- RectObject
    BasicObject <|-- OvalObject
    BasicObject ..> ResizeConstraint : uses
    LinkObject --> BasicObject : source / target
    LinkObject <|-- AssociationLink
    LinkObject <|-- GeneralizationLink
    LinkObject <|-- CompositionLink
    CanvasMouseStrategy <|.. SelectStrategy
    CanvasMouseStrategy <|.. CreateObjectStrategy
    CanvasMouseStrategy <|.. CreateLinkStrategy
    Command <|.. CreateObjectCommand
    Command <|.. CreateLinkCommand
    Command <|.. MoveObjectsCommand
    Command <|.. ResizeObjectCommand
    Command <|.. GroupCommand
    Command <|.. UngroupCommand
    Command <|.. SetLabelCommand
    CommandHistory --> Command : manages
    ModeManager --> ModeChangeListener : notifies
    ModeChangeListener <|.. ButtonPanel
    CanvasPanel --> CommandHistory : owns
    CanvasPanel --> CanvasMouseStrategy : delegates
    ButtonDragGlassPane --> CanvasPanel : simulateRelease
```

---

## 4. 核心流程：滑鼠事件路由

```mermaid
sequenceDiagram
    participant User
    participant CanvasPanel
    participant ModeManager
    participant Strategy as 當前 Strategy
    participant Command
    participant Model

    User->>CanvasPanel: MouseEvent (按下/拖曳/放開)
    CanvasPanel->>ModeManager: getCurrentMode()
    ModeManager-->>CanvasPanel: EditorMode
    CanvasPanel->>Strategy: onPressed / onDragged / onReleased
    Strategy->>Model: 讀取 / 修改物件
    Strategy->>Command: new XxxCommand(before, after)
    alt 建立操作（Create）
        Strategy->>CanvasPanel: execute(cmd)
        CanvasPanel->>Command: redo()
        Command->>Model: 套用變更
        CanvasPanel->>CanvasPanel: history.push(cmd)
    else 漸進操作（Move / Resize）
        Strategy->>CanvasPanel: pushHistory(cmd)
        CanvasPanel->>CanvasPanel: history.push(cmd)
    end
    CanvasPanel->>CanvasPanel: repaint()
```

---

## 5. 按鈕拖拽建立物件（ButtonDragGlassPane 流程）

```mermaid
sequenceDiagram
    participant User
    participant ButtonPanel
    participant ModeManager
    participant ButtonDragGlassPane
    participant CanvasPanel
    participant CreateObjectStrategy

    User->>ButtonPanel: mousePressed on tool button
    ButtonPanel->>ModeManager: setMode(RECT / OVAL)
    ModeManager-->>ButtonPanel: onModeChanged (highlight)
    ButtonPanel->>ButtonDragGlassPane: activate()
    Note over ButtonDragGlassPane: 透明覆蓋層截取全視窗事件
    User->>ButtonDragGlassPane: mouseReleased (on canvas area)
    ButtonDragGlassPane->>CanvasPanel: simulateRelease(x, y)
    CanvasPanel->>CreateObjectStrategy: onReleased(e, canvas)
    CreateObjectStrategy->>CanvasPanel: execute(CreateObjectCommand)
    CreateObjectStrategy->>ModeManager: restorePreviousMode()
    CanvasPanel->>CanvasPanel: repaint()
```

---

## 6. 兩種 Command 執行路徑

```mermaid
flowchart LR
    subgraph P1["路徑 A：建立操作（Create）"]
        A1[Strategy 建立 Command] --> A2[canvas.execute cmd]
        A2 --> A3[cmd.redo 套用變更]
        A3 --> A4[history.push 推入 undoStack]
    end

    subgraph P2["路徑 B：漸進操作（Move / Resize）"]
        B1[onPressed 快照 before\n記錄 Map / bounds] --> B2[onDragged 即時更新 Model]
        B2 --> B3[onReleased 快照 after]
        B3 --> B4{有實際變化?}
        B4 -->|是| B5[canvas.pushHistory cmd]
        B5 --> B6[history.push 推入 undoStack\n不重複 redo]
        B4 -->|否| B7[捨棄，不記錄]
    end

    style P1 fill:#e8f4e8
    style P2 fill:#e8f0f8
```

---

## 7. 端口（Port）系統與連線動態追蹤

```mermaid
flowchart TD
    subgraph PORT["BasicObject 端口系統"]
        P1["computePorts()\n計算絕對座標"]
        P2[portsCache 快取]
        P3["move() / setBounds()\n使快取失效 portsCache=null"]
        P4["getNearestPortIndex()\nChebyshev 距離命中測試"]
        P1 --> P2 --> P3 --> P1
        P2 --> P4
    end

    subgraph CON["ResizeConstraint 軸鎖定"]
        RC1["RectObject 邊中點\nLOCK_WIDTH / LOCK_HEIGHT"]
        RC2["OvalObject 上下端口\nLOCK_WIDTH"]
        RC3["OvalObject 左右端口\nLOCK_HEIGHT"]
        RC4["四角端口\nNONE（自由縮放）"]
    end

    subgraph LINK["LinkObject 動態繪製"]
        L1[儲存 source 物件參考 + sourcePortIndex]
        L2[儲存 target 物件參考 + targetPortIndex]
        L3["draw() 呼叫\nsource.getPort(sourcePortIndex)"]
        L4["draw() 呼叫\ntarget.getPort(targetPortIndex)"]
        L5[繪製連線 + 箭頭]
        L1 --> L3 --> L5
        L2 --> L4 --> L5
    end

    PORT -->|物件移動後端口自動更新| LINK
    CON -->|約束 SelectStrategy.applyResize| PORT
```

---

## 8. Use Case 對應實作

```mermaid
flowchart LR
    UC_A["UC-A\n建立形狀"] --> BDG_A[ButtonDragGlassPane\nsimulateRelease]
    BDG_A --> COS[CreateObjectStrategy\n.onReleased]
    UC_B["UC-B\n建立連線"] --> CLS[CreateLinkStrategy\n.onPressed/Dragged/Released\n+ buildLink 工廠]
    UC_C["UC-C\n選取物件"] --> SS_Click[SelectStrategy\n.onClicked / onMoved hover]
    UC_D_G["UC-D\n群組"] --> GP["CanvasPanel.group()\nMainFrame 選單"]
    UC_D_UG["UC-D\n解群組"] --> UGP["CanvasPanel.ungroup()\nMainFrame 選單"]
    UC_E["UC-E\n移動物件"] --> SS_Drag[SelectStrategy\n.onDragged DRAGGING_OBJECT]
    UC_F["UC-F\n縮放形狀"] --> SS_Resize[SelectStrategy\n.onDragged RESIZING\napplyResize + ResizeConstraint]
    UC_G["UC-G\n設定標籤"] --> LD_G[LabelDialog\n名稱 + 顏色]

    COS --> COC[CreateObjectCommand]
    CLS --> CLC[CreateLinkCommand]
    SS_Drag --> MOC[MoveObjectsCommand\nMap before/after]
    SS_Resize --> ROC[ResizeObjectCommand\nbounds before/after]
    GP --> GCM[GroupCommand]
    UGP --> UGCM[UngroupCommand]
    LD_G --> SLCM[SetLabelCommand\n名稱 + Color]

    COC & CLC & MOC & ROC & GCM & UGCM & SLCM --> CH[CommandHistory\npush / undo / redo\nmax 50]
```
