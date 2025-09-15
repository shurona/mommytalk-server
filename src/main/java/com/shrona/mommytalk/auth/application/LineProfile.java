package com.shrona.mommytalk.auth.application;

import lombok.Data;

@Data
public class LineProfile {

    private String userId;
    private String displayName;
    private String pictureUrl;
    private String statusMessage;
}
