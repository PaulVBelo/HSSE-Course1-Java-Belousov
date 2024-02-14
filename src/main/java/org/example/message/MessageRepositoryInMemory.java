package org.example.message;

import org.example.enrichments.EnrichmentType;
import org.example.exceptions.OverlapException;
import org.example.exceptions.ValidationException;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class MessageRepositoryInMemory implements MessageRepository {
  private ConcurrentHashMap<Long, Message> messages= new ConcurrentHashMap<>();
  private ConcurrentHashMap<Long, EnrichedMessage> enrichedMessages = new ConcurrentHashMap<>();

  @Override
  public synchronized Message getMessage(Long id) {
    return messages.get(id);
  }

  @Override
  public synchronized EnrichedMessage getEnrichedMessage(Long id) {
    return enrichedMessages.get(id);
  }

  @Override
  public  synchronized void addMessage(Long id, Message message) throws OverlapException {
    if (!messages.containsKey(id) && !enrichedMessages.containsKey(id)) {
      messages.put(id, message);
    } else {
      throw new OverlapException("Overlapping value", "ID", "" + id);
    }
  }

  @Override
  public synchronized void editMessageContent(Long id, String content) throws ValidationException {
    if (messages.containsKey(id)) {
      Message message = messages.get(id);
      message.setContent(content);
      messages.replace(id, message);
    } else {
      throw new ValidationException("Invalid message edit request", "No message with id " + id);
    }
  }

  @Override
  public synchronized void addMessageEnrichments(Long id, HashSet<EnrichmentType> enrichmentTypes) throws ValidationException {
    if (messages.containsKey(id)) {
      Message message = messages.get(id);
      message.addEnrichmentTypes(enrichmentTypes);
      messages.replace(id, message);
    } else {
      throw new ValidationException("Invalid message edit request", "No message with id " + id);
    }
  }

  @Override
  public synchronized void resetMessageEnrichments(Long id, HashSet<EnrichmentType> enrichmentTypes) throws ValidationException {
    if (messages.containsKey(id)) {
      Message message = messages.get(id);
      message.setEnrichmentTypes(enrichmentTypes);
      messages.replace(id, message);
    } else {
      throw new ValidationException("Invalid message edit request", "No message with id " + id);
    }
  }

  @Override
  public synchronized void clearMessageEnrichments(Long id) throws ValidationException {
    if (messages.containsKey(id)) {
      Message message = messages.get(id);
      message.deleteEnrichments();
      messages.replace(id, message);
    } else if (enrichedMessages.containsKey(id)) {
      EnrichedMessage enrichedMessage = enrichedMessages.get(id);
      Message message = new Message(enrichedMessage.getContent(), enrichedMessage.getMsisdn());
      enrichedMessages.remove(id);
      messages.put(id, message);
    } else {
      throw new ValidationException("Invalid message edit request", "No message with id " + id);
    }
  }

  @Override
  public synchronized void unenrichMessage(Long id) throws ValidationException {

  }

  @Override
  public synchronized void transferMessage(Long id, EnrichedMessage enrichedMessage) throws ValidationException {
    if (messages.containsKey(id)) {
      messages.remove(id);
      enrichedMessages.put(id, enrichedMessage);
    } else {
      throw new ValidationException("Invalid message transfer request", "No message with id " + id);
    }
  }

  @Override
  public synchronized void deleteMessage(Long id) {
    messages.remove(id);
    enrichedMessages.remove(id);
  }
}
