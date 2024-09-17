package com.compass.challenge.duplicates.controller;

import com.compass.challenge.duplicates.service.fileprocessor.interfaces.FileProcessor;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

  private final FileProcessor fileProcessorService;

  @Autowired
  FileController(FileProcessor fileProcessorService) {

    this.fileProcessorService = fileProcessorService;
  }

  @PostMapping("/upload")
  public ResponseEntity<?> uploadAndDownloadCSV(@RequestParam("file") MultipartFile file)
      throws IOException {

    byte[] csvData = fileProcessorService.getPotentiallyDuplicates(file);

    ByteArrayResource resource = new ByteArrayResource(csvData);

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"matched_contacts.csv\"")
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(csvData.length)
        .body(resource);
  }
}
