package org.example.message;

import org.example.enrichments.EnrichmentType;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class EnrichedMessage {
  private String content;
  private String msisdn;
  private HashSet<EnrichmentType> enrichmentTypes;
  private ConcurrentHashMap<String, String> enrichments;

  public EnrichedMessage(String content,
                         String msisdn,
                         HashSet<EnrichmentType> enrichmentTypes,
                         ConcurrentHashMap<String, String> enrichments) {
    this.content = content;
    this.msisdn = msisdn;
    this.enrichmentTypes = enrichmentTypes;
    this.enrichments = enrichments;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getMsisdn() {
    return msisdn;
  }

  public HashSet<EnrichmentType> getEnrichmentTypes() {
    return enrichmentTypes;
  }

  public void setEnrichmentTypes(HashSet<EnrichmentType> enrichmentTypes) {
    this.enrichmentTypes = enrichmentTypes;
  }

  public ConcurrentHashMap<String, String> getEnrichments() {
    return enrichments;
  }

  public void setEnrichments(ConcurrentHashMap<String, String> enrichments) {
    this.enrichments = enrichments;
  }

  public void addEnrichments(String key, String value) {
    if (enrichments.get(key) == null) {
      enrichments.put(key, value);
    }
  }
}
