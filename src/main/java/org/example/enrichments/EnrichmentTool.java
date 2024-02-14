package org.example.enrichments;

import org.example.message.EnrichedMessage;
import org.example.message.Message;

public interface EnrichmentTool {
  public EnrichedMessage enrich(EnrichedMessage almostEnrichedMessage);
}
