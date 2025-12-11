package hu.pte.mik.prog5.prog5projekt.dto;

import java.time.Instant;

public record ListingResponseDto(
        Long id,
        Long bookId,
        String bookTitle,
        Long ownerId,
        String ownerName,
        String condition,
        String type,
        String status,
        int priceHuf,
        String note,
        String imageUrl,
        Instant createdAt
) {}
