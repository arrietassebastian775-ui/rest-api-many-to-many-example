package com.example.entities;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "tags")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "El nombre de la etiqueta es obligatorio")
    @Size(min = 2, max = 30, message = "El nombre de la etiqueta debe tener entre 2 y 30 caracteres")
    private String name;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY,
         cascade = {CascadeType.PERSIST, CascadeType.MERGE},
         mappedBy = "tags")
    @JsonIgnore
    @ToString.Exclude
    private Set<Tutorial> tutorials = new HashSet<>();

}
