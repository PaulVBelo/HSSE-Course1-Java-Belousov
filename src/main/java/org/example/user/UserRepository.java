package org.example.user;

import java.util.Optional;

public interface UserRepository {
  public User getUser(String msisdn);
  public void addUser(String msisdn, User user);
  public void editUser(String msisdn, String firstName, String lastName);
  public void deleteUser(String msisdn);
}
