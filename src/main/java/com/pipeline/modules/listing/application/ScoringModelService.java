package com.pipeline.modules.listing.application;

import com.pipeline.modules.listing.domain.ScoringModel;
import com.pipeline.modules.listing.infrastructure.ScoringModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ScoringModelService {

    private final ScoringModelRepository repository;

    public ScoringModelService(ScoringModelRepository repository) {
        this.repository = repository;
    }

    public ScoringModel createModel(ScoringModel model) {
        if (model.getVersionNumber() == null) {
            // Simple auto-increment if not provided
            Integer maxVersion = repository.findAll().stream()
                    .map(ScoringModel::getVersionNumber)
                    .max(Integer::compareTo)
                    .orElse(0);
            model.setVersionNumber(maxVersion + 1);
        }
        return repository.save(model);
    }

    public List<ScoringModel> getAllModels() {
        return repository.findAll();
    }

    public void activateModel(UUID id) {
        // Deactivate current active models
        repository.findAllByIsActiveTrue().forEach(m -> {
            m.setIsActive(false);
            repository.save(m);
        });

        // Activate new model
        ScoringModel model = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scoring model not found: " + id));
        model.setIsActive(true);
        model.setActivatedAt(Instant.now());
        repository.save(model);
    }
}
