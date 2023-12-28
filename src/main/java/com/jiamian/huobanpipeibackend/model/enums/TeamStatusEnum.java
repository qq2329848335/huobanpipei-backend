package com.jiamian.huobanpipeibackend.model.enums;

import lombok.Data;


public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE (1,"私有"),
    SECRET(2,"加密");
    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatusEnum getTeamStatusEnum(int value){
        TeamStatusEnum[] values = values();
        for (TeamStatusEnum teamStatusEnum : values) {
            if (teamStatusEnum.value==value){
                return teamStatusEnum;
            }
        }
        return null;
    }


    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
