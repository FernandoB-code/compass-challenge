package com.compass.challenge.duplicates.dto;

import java.util.Objects;
import lombok.Data;

@Data
public class Contact {

  private int contactID;
  private String name;
  private String name1;
  private String email;
  private String postalZip;
  private String address;

  public boolean isDifferentObject(Contact other) {
    if (other == null) return true;

    return !Objects.equals(this.contactID, other.contactID)
        && !Objects.equals(this.name, other.name)
        && !Objects.equals(this.name1, other.name1)
        && !Objects.equals(this.email, other.email)
        && !Objects.equals(this.postalZip, other.postalZip)
        && !Objects.equals(this.address, other.address);
  }
}
