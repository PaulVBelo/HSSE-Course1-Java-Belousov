package org.example.user;

import org.example.exceptions.OverlapException;
import org.example.exceptions.ValidationException;

import java.util.concurrent.ConcurrentHashMap;

public class UserRepositoryInMemory implements UserRepository {
  private final ConcurrentHashMap<String, User> users;

  public UserRepositoryInMemory (){
    users = new ConcurrentHashMap<>();
  }
  @Override
  public synchronized User getUser(String msisdn) {
    User user = users.get(msisdn);
    return user;
  }

  @Override
  public synchronized void addUser(String msisdn, User user) throws OverlapException {
    if (!users.containsKey(msisdn)) {
      users.put(msisdn, user);
    } else {
      throw new OverlapException("Overlapping value",  "MSIDSN", msisdn);
    }
  }

  @Override
  public synchronized void editUser(String msisdn, String firstName, String lastName) throws ValidationException {
    if (users.containsKey(msisdn)) {
      users.replace(msisdn, new User(firstName, lastName));
    } else {
      throw new ValidationException("Invalid edit request", "No user with msisdn " + msisdn);
    }
  }

  @Override
  public synchronized void deleteUser(String msisdn) throws ValidationException {
    users.remove(msisdn);
  }
}
