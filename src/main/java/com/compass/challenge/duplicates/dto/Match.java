package com.compass.challenge.duplicates.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class Match {

  private int contactIdSource;
  private int contactIdMatch;
  private String accuracy;
}
