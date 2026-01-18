package com.whatsthatclip.backend.gemini;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeminiResponse {
    private List<Candidate> candidates;
}
