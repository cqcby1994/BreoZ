package com.breo.baseble.breoble.msgbean;

/**
 * Created by chenqc on 2019/6/26 16:00
 */
public class ID5SRunBean {
    private boolean music_conn_state;
    private int time;
    private int battery;


    public boolean isMusic_conn_state() {
        return music_conn_state;
    }

    public void setMusic_conn_state(boolean music_conn_state) {
        this.music_conn_state = music_conn_state;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }


    @Override
    public String toString() {
        return "ID5SRunBean{" +
                "music_conn_state=" + music_conn_state +
                ", time=" + time +
                ", battery=" + battery +
                '}';
    }
}
