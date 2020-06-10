package com.hth.parking.adapter.DO;

import com.baidu.mapapi.model.LatLng;

public class SugData {

    private String key;
    private LatLng pt;

    public SugData(String key, LatLng pt){
        this.key = key;
        this.pt = pt;
    }

    public String getKey() {
        return key;
    }

    public LatLng getPt() {
        return pt;
    }
}
