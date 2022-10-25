package com.jayant;

import com.jayant.controller.StorageResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

@Slf4j
class FileUploaderTest {

    private static final String TENANT_ID = "mz.health.malaria.llin";
    private static final String MODULE = "sync-service";

    private URL fileStoreUrl;
    private HttpHeaders httpHeaders;
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws MalformedURLException {
        var host = new URL("http://localhost:8080");
        fileStoreUrl = new URL(host, "/filestore/v1/files");
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        restTemplate = new RestTemplate();
    }

    @Test
    void shouldUploadAFile() throws MalformedURLException {
        var file = getTestFile("files/test.txt");

        log.info(file.getName());

        MultiValueMap<String, String> fileMap = getFileMap(file,
                MediaType.asMediaType(MimeType.valueOf("text/plain")));

        HttpEntity<Resource> fileEntity = new HttpEntity<>(new FileSystemResource(file), fileMap);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", fileEntity);
        formData.add("tenantId", TENANT_ID);
        formData.add("module", MODULE);


        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formData, httpHeaders);
        timedCall(httpEntity);
    }

    @Test
    void shouldUploadMultipleFiles() throws MalformedURLException {
        var file = getTestFile("files/test-2.gz");
        var anotherFile = getTestFile("files/test.csv");

        MultiValueMap<String, String> fileMap = getFileMap(file,
                MediaType.asMediaType(MimeType.valueOf("application/gzip")));
        MultiValueMap<String, String> anotherFileMap = getFileMap(anotherFile,
                MediaType.asMediaType(MimeType.valueOf("application/csv")));

        HttpEntity<Resource> fileEntity = new HttpEntity<>(new FileSystemResource(file), fileMap);
        HttpEntity<Resource> anotherFileEntity = new HttpEntity<>(new FileSystemResource(anotherFile), anotherFileMap);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", fileEntity);
        formData.add("file", anotherFileEntity);
        formData.add("tenantId", TENANT_ID);
        formData.add("module", MODULE);


        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formData, httpHeaders);
        timedCall(httpEntity);
    }

    @Test
    void shouldUploadALargeFile() throws MalformedURLException {
        var file = getTestFile("files/large-file.deb");

        log.info(file.getName());

        MultiValueMap<String, String> fileMap = getFileMap(file,
                MediaType.asMediaType(MimeType.valueOf("application/deb")));

        HttpEntity<Resource> fileEntity = new HttpEntity<>(new FileSystemResource(file), fileMap);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("file", fileEntity);
        formData.add("tenantId", TENANT_ID);
        formData.add("module", MODULE);


        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formData, httpHeaders);
        timedCall(httpEntity);
    }

    private File getTestFile(String relativePath) {
        return new File(Objects.requireNonNull(getClass()
                .getClassLoader().getResource(relativePath)).getFile());
    }

    private MultiValueMap<String, String> getFileMap(File file, MediaType mediaType) {
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition.formData()
                .name("file")
                .filename(file.getName())
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        fileMap.add(HttpHeaders.CONTENT_TYPE, mediaType.toString());
        return fileMap;
    }

    private void timedCall(HttpEntity<MultiValueMap<String, Object>> httpEntity) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<StorageResponse> response = restTemplate
                .postForEntity(fileStoreUrl.toString(), httpEntity, StorageResponse.class);
        long totalTime = System.currentTimeMillis() - startTime;
        log.info(String.format("%s, %s", totalTime, response));
    }
}
