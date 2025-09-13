package com.shrona.line_demo.line.presentation.form;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record MessageSendForm(
    String content,
    String headerLink,
    String bottomLink,
    List<Long> includeGroup,
    List<Long> excludeGroup,
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") // 이것때문이였으므로 적읍시다.
    LocalDateTime sendDateTime,
    ZonedDateTime sendDateTimeUtc,
    String targetType
) {

    public static MessageSendForm of(
        String content,
        String headerLink,
        String bottomLink,
        LocalDateTime date,
        List<Long> includeGroup,
        List<Long> excludeGroup,
        TargetType type) {
        return new MessageSendForm(
            content, headerLink, bottomLink,
            includeGroup, excludeGroup, date,
            ZonedDateTime.of(date, ZoneId.of("Asia/Seoul")), type.toString()
        );
    }


}
