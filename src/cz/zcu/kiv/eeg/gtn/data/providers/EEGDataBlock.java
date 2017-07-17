/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.zcu.kiv.eeg.gtn.data.providers;

/**
 * @author Prokop
 */
public class EEGDataBlock {

    private final MessageType msgType;

    private final int blockNumber;

    private String msg;

    private EEGMarker[] markers;

    private float[][] data;

    public EEGDataBlock(MessageType msgType, int blockNumber) {
        this.msgType = msgType;
        this.blockNumber = blockNumber;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public void setMarkers(EEGMarker[] markers) {
        this.markers = markers;
    }

    public void setData(float[][] data) {
        this.data = data;
    }

    public EEGMarker[] getMarkers() {

        return markers;
    }

    public float[][] getData() {
        return data;
    }
}