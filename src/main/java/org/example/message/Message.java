package org.example.message;

import org.example.enrichments.EnrichmentType;

import java.util.HashSet;

public class Message {

  private String content;
  private String msisdn;
  private HashSet<EnrichmentType> enrichmentTypes;
  public Message (String content, String msisdn, HashSet<EnrichmentType> enrichmentTypes) {
    this.content = content;
    this.msisdn = msisdn;
    this.enrichmentTypes = enrichmentTypes;
  }

  public Message(String content, String msisdn) {
    this.content = content;
    this.msisdn = msisdn;
    this.enrichmentTypes = new HashSet<EnrichmentType>();
  }

  public String getContent() {
    return content;
  }

  public String getMsisdn() {
    return msisdn;
  }

  public HashSet<EnrichmentType> getEnrichmentTypes() {
    return enrichmentTypes;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setEnrichmentTypes(HashSet<EnrichmentType> enrichmentTypes) {
    this.enrichmentTypes = enrichmentTypes;
  }

  public void addEnrichmentTypes(HashSet<EnrichmentType> enrichments) {
    this.enrichmentTypes.addAll(enrichments);
  }

  public void deleteEnrichments() {
    this.enrichmentTypes.clear();
  }
}
