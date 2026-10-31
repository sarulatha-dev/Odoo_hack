package com.brightloop.subscription.common.sequence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SequenceService {

    private final DocumentSequenceRepository repository;

    public SequenceService(DocumentSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public synchronized String nextNumber(String name, String prefix) {
        DocumentSequence sequence = repository.findByName(name)
                .orElseGet(() -> new DocumentSequence(name, 1L));
        long value = sequence.getNextValue();
        sequence.setNextValue(value + 1);
        repository.save(sequence);
        return String.format("%s-%04d", prefix, value);
    }
}
