package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.enrichments.EnrichmentTool;
import org.example.message.EnrichedMessage;
import org.example.message.Message;
import org.example.message.MessageRepository;

import java.util.concurrent.ConcurrentHashMap;

public class EnrichmentService {
  private final MessageRepository messageRepository;
  private final EnrichmentTool enricher;
  private final ObjectMapper objectMapper;

  public EnrichmentService(MessageRepository messageRepository,
                           EnrichmentTool enricher,
                           ObjectMapper objectMapper) {
    this.messageRepository=messageRepository;
    this.enricher = enricher;
    this.objectMapper = objectMapper;
  }

  // возвращается обогащенный (или необогащенный content сообщения)
  public String enrich(Long id) throws JsonProcessingException {
    Message message = messageRepository.getMessage(id);
    EnrichedMessage enrichedMessage;
    if (message != null) {
      if (message.getEnrichmentTypes().size()==0) {

        return objectMapper.writeValueAsString(message);
      } else {
        enrichedMessage = new EnrichedMessage(message.getContent(),
            message.getMsisdn(),
            message.getEnrichmentTypes(),
            new ConcurrentHashMap<String, String>());
        EnrichedMessage trulyEnrichedMessage = enricher.enrich(enrichedMessage);
        messageRepository.transferMessage(id, trulyEnrichedMessage);
        return objectMapper.writeValueAsString(trulyEnrichedMessage);
      }
    } else {
      enrichedMessage = messageRepository.getEnrichedMessage(id);
      if (enrichedMessage == null) {
        return "Could not find a message with ID " + id;
      } else {
        return objectMapper.writeValueAsString(enrichedMessage);
      }
    }
  }
}