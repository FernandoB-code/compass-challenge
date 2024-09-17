package com.compass.challenge.duplicates.service.fileprocessor.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileProcessor {

  byte[] getPotentiallyDuplicates(MultipartFile file) throws IOException;
}
