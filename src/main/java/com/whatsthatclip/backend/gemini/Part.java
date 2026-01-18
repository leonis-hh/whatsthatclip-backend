package com.whatsthatclip.backend.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Part {
    private String text;
    private InlineData inline_data;
}
