package com.shrona.line_demo.line.application;

public interface LineService {

    public void saveLineMessage(String lineId, String content);

    public void followLineUserByLineId(String lineId);

    public void unfollowLineUserByLineId(String lineId);

}
