package com.smart.home.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{room.name.required}")
    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appliance> appliances = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public void addAppliance(Appliance appliance) {
        if (appliance == null) {
            throw new IllegalArgumentException("{appliance.null}");
        }
        if (appliance.getRoom() != null && appliance.getRoom() != this) {
            throw new IllegalStateException("{appliance.reassign}");
        }
        if (appliances.contains(appliance)) {
            return;
        }
        appliances.add(appliance);
        appliance.setRoom(this);
    }

    public void removeAppliance(Appliance appliance) {
        if (appliance == null) {
            return;
        }
        appliances.remove(appliance);
        appliance.setRoom(null);
    }
}
