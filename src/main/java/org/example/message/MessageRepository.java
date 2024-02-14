package org.example.message;


import org.example.enrichments.EnrichmentType;

import java.util.HashSet;

public interface MessageRepository {
  public Message getMessage(Long id);
  public EnrichedMessage getEnrichedMessage(Long id);
  public void addMessage(Long id, Message message);
  public void editMessageContent(Long id, String content);
  public void addMessageEnrichments(Long id, HashSet<EnrichmentType> enrichmentTypes);
  public void resetMessageEnrichments(Long id, HashSet<EnrichmentType> enrichmentTypes);
  public void clearMessageEnrichments(Long id);

  public void unenrichMessage(Long id);
  public void transferMessage(Long id, EnrichedMessage enrichedMessage);
  public void deleteMessage(Long Id);
}
