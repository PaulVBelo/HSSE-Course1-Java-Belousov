package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.enrichments.Enricher;
import org.example.enrichments.EnrichmentTool;
import org.example.enrichments.EnrichmentType;
import org.example.exceptions.OverlapException;
import org.example.exceptions.ValidationException;
import org.example.message.Message;
import org.example.message.MessageRepository;
import org.example.message.MessageRepositoryInMemory;
import org.example.user.User;
import org.example.user.UserRepository;
import org.example.user.UserRepositoryInMemory;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class E2ETest {
  @Test
  public void endToEndTest1(){
    ObjectMapper objectMapper = new ObjectMapper();
    MessageRepository messageRepository = new MessageRepositoryInMemory();
    UserRepository userRepository = new UserRepositoryInMemory();
    EnrichmentTool enricher = new Enricher(userRepository);
    EnrichmentService enrichmentService = new EnrichmentService(messageRepository, enricher, objectMapper);

    try{
      //Расчёт идёт только на обогащение. В нормальной проге я бы обернул это ещё в один сервис
      //или использовал бы REST контроллер и там ловил эти ошибки. Здесь я их сделал из привычки, а не для проверок.
      User user1 = new User("Bill", "Smith");
      User user2 = new User("James", "Rusty");
      User user3 = new User("Ivan", "Ivanov");

      userRepository.addUser("12345678999", user1);
      userRepository.addUser("88005553535", user2);
      userRepository.addUser("15054071505", user3);

      HashSet<EnrichmentType> enrichmentTypeHashSet1 = new HashSet<EnrichmentType>();
      enrichmentTypeHashSet1.add(EnrichmentType.MSISDN);

      HashSet<EnrichmentType> enrichmentTypeHashSet2 = new HashSet<EnrichmentType>();
      enrichmentTypeHashSet2.add(EnrichmentType.MSISDN);

      HashSet<EnrichmentType> enrichmentTypeHashSet3 = new HashSet<EnrichmentType>();
      enrichmentTypeHashSet3.add(EnrichmentType.MSISDN);

      Message message1 = new Message("I did it my way.", "12345678999");
      Message message2 = new Message("This is the dawning of the rest of our lives!", "7777777777");
      Message message3 = new Message("I'm not a part of the redneck agenda!", "88005553535", enrichmentTypeHashSet1);
      Message message4 = new Message("I've got some scattered pictures lying on my bedroom floor.",
          "15054071505", enrichmentTypeHashSet2);

      messageRepository.addMessage(1l, message1);
      messageRepository.addMessage(2l, message2);
      messageRepository.addMessage(3l, message3);
      messageRepository.addMessage(4l, message4);

      userRepository.editUser("12345678999", "Frank", "Sinatra");
      messageRepository.addMessageEnrichments(1l, enrichmentTypeHashSet3);
      messageRepository.editMessageContent(3l, "I'm not a part of the maga agenda!");
      messageRepository.clearMessageEnrichments(4l);

      String expected1 = "{\"content\":\"I did it my way.\",\"msisdn\":\"12345678999\",\"enrichmentTypes\":[\"MSISDN\"],\"enrichments\":{\"firstName\":\"Frank\",\"lastName\":\"Sinatra\"}}";
      String expected2 = "{\"content\":\"This is the dawning of the rest of our lives!\",\"msisdn\":\"7777777777\",\"enrichmentTypes\":[]}";
      String expected3 = "{\"content\":\"I'm not a part of the maga agenda!\",\"msisdn\":\"88005553535\",\"enrichmentTypes\":[\"MSISDN\"],\"enrichments\":{\"firstName\":\"James\",\"lastName\":\"Rusty\"}}";
      String expected4 = "{\"content\":\"I've got some scattered pictures lying on my bedroom floor.\",\"msisdn\":\"15054071505\",\"enrichmentTypes\":[]}";
      String expected5 = "Could not find a message with ID 5";

      String result1 = enrichmentService.enrich(1l);
      String result2 = enrichmentService.enrich(2l);
      String result3 = enrichmentService.enrich(3l);
      String result4 = enrichmentService.enrich(4l);

      JSONAssert.assertEquals(expected1, result1, JSONCompareMode.STRICT);
      JSONAssert.assertEquals(expected2, result2, JSONCompareMode.STRICT);
      JSONAssert.assertEquals(expected3, result3, JSONCompareMode.STRICT);
      JSONAssert.assertEquals(expected4, result4, JSONCompareMode.STRICT);
      assertEquals(expected5, enrichmentService.enrich(5l));
    } catch (OverlapException e) {
      throw new RuntimeException();
    } catch (ValidationException e) {
      throw new RuntimeException();
    } catch (JsonProcessingException e) {
      throw new RuntimeException();
    } catch (JSONException e) {
      throw new RuntimeException();
    }
  }
}