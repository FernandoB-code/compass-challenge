package com.compass.challenge.duplicates.service.fileprocessor.impl;

import com.compass.challenge.duplicates.dto.Contact;
import com.compass.challenge.duplicates.dto.Match;
import com.compass.challenge.duplicates.enums.SCORES;
import com.compass.challenge.duplicates.service.fileprocessor.interfaces.FileProcessor;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Log4j2
public class FileProcessorImpl implements FileProcessor {

  @Override
  public byte[] getPotentiallyDuplicates(MultipartFile file) throws IOException {

    List<Contact> contacts = generateContactListFromExcel(file);

    List<Match> potentialMatchListResult = new ArrayList<>();

    for (int i = 0; i < contacts.size(); i++) {

      for (int j = i + 1; j < contacts.size(); j++) {

        Match math = getScoreMatch(contacts.get(i), contacts.get(j));

        if (math != null) {
          potentialMatchListResult.add(math);
        }
      }
    }

    return generateExcelFromMatches(potentialMatchListResult);
  }

  public List<Contact> generateContactListFromExcel(MultipartFile file) {
    List<Contact> contacts = new ArrayList<>();

    try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

      String[] nextRecord;

      while ((nextRecord = csvReader.readNext()) != null) {
        Contact contact = new Contact();

        try {
          contact.setContactID(Integer.parseInt(nextRecord[0]));
          contact.setName(nextRecord[1]);
          contact.setName1(nextRecord[2]);
          contact.setEmail(nextRecord[3]);
          contact.setPostalZip(nextRecord[4]);
          contact.setAddress(nextRecord[5]);
        } catch (IndexOutOfBoundsException e) {
          log.error("CSV format issue: " + e.getMessage());
          continue;
        } catch (NumberFormatException e) {
          log.error("Invalid number in record: " + e.getMessage());
          continue;
        }

        contacts.add(contact);
      }

    } catch (IOException | CsvValidationException e) {
      throw new RuntimeException("Error processing the CSV file: " + e.getMessage(), e);
    }

    return contacts;
  }

  private Match getScoreMatch(Contact actual, Contact toCompare) {

    Match match = null;

    if (!actual.isDifferentObject(toCompare)) {

      match = new Match();
      match.setContactIdSource(actual.getContactID());
      match.setContactIdMatch(toCompare.getContactID());

      int score = 0;

      if (actual.getEmail().equalsIgnoreCase(toCompare.getEmail())) {
        score += 3;
      }

      if (actual.getAddress().equalsIgnoreCase(toCompare.getAddress())) {
        score += 2;
      }

      if (actual.getName().equalsIgnoreCase(toCompare.getName())) {
        score += 1;
      }

      if (actual.getName1().equalsIgnoreCase(toCompare.getName1())) {
        score += 1;
      }

      if (actual.getPostalZip().equals(toCompare.getPostalZip())) {
        score += 1;
      }

      if (score >= 4) {
        match.setAccuracy(SCORES.HIGH.toString());
      } else {
        match.setAccuracy(SCORES.LOW.toString());
      }
    }

    return match;
  }

  private byte[] generateExcelFromMatches(List<Match> potentialMatchListResult) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream))) {

      String[] header = {"Contact ID Source", "Contact ID Match", "Accuracy"};
      writer.writeNext(header);

      for (Match match : potentialMatchListResult) {
        String[] data = new String[header.length];
        data[0] = String.valueOf(match.getContactIdSource());
        data[1] = String.valueOf(match.getContactIdMatch());
        data[2] = match.getAccuracy();

        writer.writeNext(data);
      }

      writer.flush();
      return outputStream.toByteArray();
    }
  }
}
