package com.kyas.wolkandhold.dto;

import com.kyas.wolkandhold.entity.PolygonEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PolygonResponse {
    private Long id;
    private UserDto owner;
    private double square;
    private long lastUpdated;
    private String wkt;
    private String title;

    public static PolygonResponse fromEntity(PolygonEntity entity) {

        return new PolygonResponse(entity.getId(),
                new UserDto(entity.getOwner().getId(), entity.getOwner().getUsername()),
                entity.getSquare(),
                entity.getLastUpdated(),
                entity.getArea() == null ? "" : entity.getArea().toString(),
                entity.getTitle());
    }
}
