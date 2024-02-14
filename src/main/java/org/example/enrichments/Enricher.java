package org.example.enrichments;

import org.example.message.EnrichedMessage;
import org.example.message.Message;
import org.example.user.User;
import org.example.user.UserRepository;

public class Enricher implements EnrichmentTool, EnrichesWithMSISDN{
  //Для добавления новых обогащений достаточно добавить больше интерфейсов этому классу
  private UserRepository userRepository;

  public Enricher(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void enrichWithMsisdn(EnrichedMessage enrichedMessage) {
    synchronized (userRepository) {
      String msisdn = enrichedMessage.getMsisdn();
      User user = userRepository.getUser(msisdn);
      if (user != null) {
        enrichedMessage.addEnrichments("firstName", user.getFirstName());
        enrichedMessage.addEnrichments("lastName", user.getLastName());
      }
    }
  }


  @Override
  public synchronized EnrichedMessage enrich(EnrichedMessage almostEnrichedMessage) {
    for (EnrichmentType enrichment:
         almostEnrichedMessage.getEnrichmentTypes()) {
      switch (enrichment) {
        //Добавить кейсы при расширении
        case MSISDN:
          enrichWithMsisdn(almostEnrichedMessage);
          break;
      }
    }
    return almostEnrichedMessage;
  }
}
