package com.kyas.wolkandhold.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

@Data
@Entity
@Table(name = "polygons")
public class PolygonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    @Column
    private long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private UserEntity owner;

    @Getter
    @Setter
    @Column(columnDefinition = "geometry(MULTIPOLYGON, 4326)")
    private Geometry area;

    @Getter
    @Setter
    @Column
    private double square;

    @Getter
    @Setter
    @Column
    private long createdAt;

    @Getter
    @Setter
    @Column
    private long lastUpdated;

	@Getter
	@Setter
	@Column
	private String title;


    public PolygonEntity() {

    }
}
