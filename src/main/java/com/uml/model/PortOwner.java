package com.uml.model;

import java.awt.*;

public interface PortOwner { // 擁有可連線 port 的物件能力介面（未來新圖形只要實作此介面即可成為連線端點）

    Point getPort(int index); // 取得指定 port 的絕對座標

    int getNearestPortIndex(Point mouse); // 找出滑鼠附近的 port 索引，找不到則回傳 -1

    Rectangle getBounds(); // 回傳 port 所屬物件的邊界，供 hit-test 與框選使用
}
