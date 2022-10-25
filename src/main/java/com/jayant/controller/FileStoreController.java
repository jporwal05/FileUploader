package com.jayant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("filestore/v1")
@Slf4j
public class FileStoreController {

    @PostMapping("/files")
    public ResponseEntity<StorageResponse> storeFile(@RequestParam("file") List<MultipartFile> files,
                                                    @RequestParam("tenantId") String tenantId,
                                                    @RequestParam("module") String module) {
        files.forEach(file -> {
            log.info(file.getName());
            InputStream is = null;
            try {
                is = file.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            br.lines().forEach(line -> {
                line.toString();
            });
            log.info("File read successfully");
        });
        log.info(tenantId);
        log.info(module);
        return ResponseEntity.of(Optional.of(StorageResponse.builder().build()));
    }
}
