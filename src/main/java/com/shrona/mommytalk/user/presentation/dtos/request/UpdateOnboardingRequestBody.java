package com.shrona.mommytalk.user.presentation.dtos.request;

public record UpdateOnboardingRequestBody(
    String childName,
    Integer userLevel,
    Integer childLevel
) {

}
