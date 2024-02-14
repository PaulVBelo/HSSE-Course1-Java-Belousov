package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.enrichments.Enricher;
import org.example.enrichments.EnrichmentTool;
import org.example.enrichments.EnrichmentType;
import org.example.message.Message;
import org.example.message.MessageRepository;
import org.example.message.MessageRepositoryInMemory;
import org.example.user.User;
import org.example.user.UserRepository;
import org.example.user.UserRepositoryInMemory;

import java.util.HashSet;

public class Main {
  public static void main(String[] args) {
    ObjectMapper objectMapper = new ObjectMapper();
    MessageRepository messageRepository = new MessageRepositoryInMemory();
    UserRepository userRepository = new UserRepositoryInMemory();
    EnrichmentTool enricher = new Enricher(userRepository);
    EnrichmentService enrichmentService = new EnrichmentService(messageRepository, enricher, objectMapper);

    try {
      Message message1 = new Message("Burning lights and blackouts", "+79862847148");

      HashSet<EnrichmentType> enrichmentTypeHashSet = new HashSet<>();
      enrichmentTypeHashSet.add(EnrichmentType.MSISDN);
      Message message2 = new Message("Someone kill the DJ!", "88005553535", enrichmentTypeHashSet);

      messageRepository.addMessage(1l, message1);
      messageRepository.addMessage(2l, message2);

      User treCool = new User("Tre", "Cool");
      User mikeDirnt = new User("Mike", "Dirnt");
      userRepository.addUser("88005553535", treCool);
      userRepository.addUser("+79862847938", mikeDirnt);

      System.out.println(enrichmentService.enrich(1l));
      System.out.println(enrichmentService.enrich(2l));
    } catch (JsonProcessingException e) {
    }
  }
}