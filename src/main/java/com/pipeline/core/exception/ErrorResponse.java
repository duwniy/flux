package com.pipeline.core.exception;

import java.util.List;

public record ErrorResponse(String code, List<String> details) {}
