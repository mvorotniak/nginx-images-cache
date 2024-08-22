package com.demo.nginximagescache.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ImagesController {

    @Value("${nasa.url}")
    private String nasaUrl;

    @Value("${nasa.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    @GetMapping("/picture-of-the-day")
    public ResponseEntity<byte[]> getPictureOfTheDay(@RequestParam String date) {
        URI uri = UriComponentsBuilder.fromUriString(nasaUrl)
                .queryParam("api_key", apiKey)
                .queryParam("date", date)
                .build()
                .toUri();

        var response = restTemplate.getForEntity(uri, JsonNode.class);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.info("Unexpected issue occurred while getting image from Nasa: [{}]", response.getStatusCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return Optional.ofNullable(response.getBody())
            .flatMap(this::getImageBytes)
            .map(this::getResponseEntity)
            .orElseGet(() -> {
                log.info("No image found or unable to retrieve the image.");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            });
    }

    private Optional<byte[]> getImageBytes(JsonNode body) {
        String mediaType = body.path("media_type").asText();
        if (!"image".equals(mediaType)) {
            log.info("Media type is not an image: [{}]", mediaType);
            return Optional.empty();
        }

        String imageUrl = body.path("url").asText();
        return Optional.ofNullable(restTemplate.getForObject(imageUrl, byte[].class));
    }

    private ResponseEntity<byte[]> getResponseEntity(byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
