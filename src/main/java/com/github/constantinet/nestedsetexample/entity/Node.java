package com.github.constantinet.nestedsetexample.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nodes")
public class Node {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String value;

    @Column(name = "lft", unique = true, nullable = false)
    private Integer left;

    @Column(name = "rght", unique = true, nullable = false)
    private Integer right;

    public Node(final Long id, final String value) {
        this.id = id;
        this.value = value;
    }
}