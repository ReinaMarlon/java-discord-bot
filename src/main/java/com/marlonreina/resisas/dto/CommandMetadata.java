package com.marlonreina.resisas.dto;

public record CommandMetadata(String name,
                              String description,
                              String permissions,
                              boolean premium) {
}
