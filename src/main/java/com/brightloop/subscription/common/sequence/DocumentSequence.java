package com.brightloop.subscription.common.sequence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_sequences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSequence {

    @Id
    @Column(length = 60)
    private String name;

    @Column(name = "next_value", nullable = false)
    private long nextValue;
}
