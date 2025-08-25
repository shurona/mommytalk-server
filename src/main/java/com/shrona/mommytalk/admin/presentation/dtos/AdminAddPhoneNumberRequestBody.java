package com.shrona.mommytalk.admin.presentation.dtos;

public record AdminAddPhoneNumberRequestBody(
    Long channelId,
    String phoneNumber
) {

}
