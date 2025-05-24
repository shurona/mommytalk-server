package com.shrona.line_demo.line.presentation.form;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MessageSendForm(
    String content,
    List<Long> includeGroup,
    List<Long> excludeGroup,
    LocalDate sendDate,
    Integer sendHour,
    Integer sendMinute,
    String targetType
) {

    public static MessageSendForm of(String content, LocalDateTime date, List<Long> includeGroup,
        List<Long> excludeGroup, TargetType type) {
        return new MessageSendForm(
            content, includeGroup, excludeGroup, date.toLocalDate(), date.getHour(),
            date.getMinute(), type.toString()
        );
    }


}
