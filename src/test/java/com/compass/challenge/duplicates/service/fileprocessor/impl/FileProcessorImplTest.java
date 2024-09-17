package com.compass.challenge.duplicates.service.fileprocessor.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.compass.challenge.duplicates.dto.Contact;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class FileProcessorImplTest {

  @InjectMocks private FileProcessorImpl fileProcessor;

  @Mock private MultipartFile multipartFile;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGenerateContactListFromExcel() throws IOException {
    String csvContent =
        "ContactID,First Name,Last Name,Email Address,Zip Code,Address\n"
            + "1001,C,F,mollis.lectus.pede@outlook.net,39746,449-6990 Tellus. Rd.\n"
            + "1002,C,F,mollis.lectus.pede@outlook.net,39746,449-6990 Tellus. Rd.";

    when(multipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

    List<Contact> contacts = fileProcessor.generateContactListFromExcel(multipartFile);

    assertEquals(2, contacts.size());
    assertEquals("mollis.lectus.pede@outlook.net", contacts.get(0).getEmail());
  }

  @Test
  public void testGetPotentiallyDuplicates() throws IOException {
    // Setup a CSV with two contacts
    String csvContent =
        "Contact ID,Name,Name1,Email,PostalZip,Address\n"
            + "1001,C,F,mollis.lectus.pede@outlook.net,39746,449-6990 Tellus. Rd.\n"
            + "1002,C,F,mollis.lectus.pede@outlook.net,39746,449-6990 Tellus. Rd.";

    when(multipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

    byte[] result = fileProcessor.getPotentiallyDuplicates(multipartFile);
    String resultString = new String(result);

    String expectedOutput = "\"1001\",\"1002\",\"HIGH\"";
    assertTrue(resultString.contains(expectedOutput));
  }

  @Test
  public void testNoDuplicatesFound() throws IOException {
    String csvContent =
        "Contact ID,Name,Name1,Email,PostalZip,Address\n"
            + "1001,A,B,email1@example.com,12345,Address 1\n"
            + "1002,C,D,email2@example.com,67890,Address 2";

    when(multipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

    byte[] result = fileProcessor.getPotentiallyDuplicates(multipartFile);
    String resultString = new String(result);

    assertFalse(resultString.contains("\"1001\",\"1002\""));
  }

  @Test
  public void testPartiallyMatchingContacts() throws IOException {
    String csvContent =
        "Contact ID,Name,Name1,Email,PostalZip,Address\n"
            + "1001,A,B,email1@example.com,12345,Road 45\n"
            + "1002,A,B,email2@example.com,67890,Road 26";

    when(multipartFile.getInputStream())
        .thenReturn(new ByteArrayInputStream(csvContent.getBytes()));

    byte[] result = fileProcessor.getPotentiallyDuplicates(multipartFile);
    String resultString = new String(result);

    String expectedOutput = "\"1001\",\"1002\",\"LOW\"";
    assertTrue(resultString.contains(expectedOutput));
  }
}
