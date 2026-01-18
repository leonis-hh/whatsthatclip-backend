package com.whatsthatclip.backend.gemini;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiRequest {
    private SystemInstruction system_instruction;
    private List<Content> contents;
    private GenerationConfig generation_config;
}
