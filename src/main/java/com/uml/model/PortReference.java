package com.uml.model;

import java.awt.*;

public record PortReference(PortOwner owner, int portIndex) { // 指向某個 PortOwner 上特定 port 的穩定參考

    public PortReference {
        if (owner == null) throw new IllegalArgumentException("owner must not be null");
        if (portIndex < 0) throw new IllegalArgumentException("portIndex must not be negative");
    }

    public Point getPoint() { // 取得此 port 目前的座標（物件移動後會自動反映）
        return owner.getPort(portIndex);
    }
}
