package com.shrona.line_demo.admin.presentation.dtos;

public record AdminAddPhoneNumberRequestBody(
    Long channelId,
    String phoneNumber
) {

}
