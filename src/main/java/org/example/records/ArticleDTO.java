package org.example.records;

import java.util.Map;
import java.util.Objects;

public record ArticleDTO(String title, Map<String, Long> tags) {
}
