package org.example.enrichments;

import org.example.message.EnrichedMessage;
import org.example.message.Message;

public interface EnrichesWithMSISDN {
  public void enrichWithMsisdn(EnrichedMessage enrichedMessage);
}
