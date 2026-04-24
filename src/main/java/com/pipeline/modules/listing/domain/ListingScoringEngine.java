package com.pipeline.modules.listing.domain;

import com.pipeline.modules.listing.domain.*;
import com.pipeline.modules.listing.infrastructure.ScoringModelRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.UUID;
import com.pipeline.core.shared.SellerType;

@Service
public class ListingScoringEngine {

    private final ScoringModelRepository scoringModelRepository;

    private static final Map<String, Integer> DEFAULT_WEIGHTS = Map.of(
        "description", 35,
        "photos", 30,
        "title", 15,
        "floor_info", 10,
        "seller_type", 10,
        "price_competitiveness", 20,
        "demand_context", 10,
        "competitor_density", 5
    );

    public ListingScoringEngine(ScoringModelRepository scoringModelRepository) {
        this.scoringModelRepository = scoringModelRepository;
    }

    public ScoringResult score(Listing listing) {
        ScoringModel model = getActiveModel();
        Map<String, Integer> weights = model != null ? model.getFactorWeights() : DEFAULT_WEIGHTS;
        UUID modelId = model != null ? model.getId() : null;

        List<ScoreFactor> factors = new ArrayList<>();

        factors.add(scoreDescription(listing.getDescription(), weights.getOrDefault("description", 35)));
        factors.add(scorePhotos(listing.getPhotosCount(), weights.getOrDefault("photos", 30)));
        factors.add(scoreTitle(listing.getTitle(), weights.getOrDefault("title", 15)));
        factors.add(scoreFloorInfo(listing.getFloor(), listing.getTotalFloors(), weights.getOrDefault("floor_info", 10)));
        factors.add(scoreSellerType(listing.getSellerType(), weights.getOrDefault("seller_type", 10)));

        int total = factors.stream()
            .mapToInt(ScoreFactor::points)
            .sum();

        return new ScoringResult(total, factors, Instant.now(), modelId);
    }

    public ScoringResult scoreWithContext(Listing listing, DistrictContext context) {
        ScoringModel model = getActiveModel();
        Map<String, Integer> weights = model != null ? model.getFactorWeights() : DEFAULT_WEIGHTS;
        UUID modelId = model != null ? model.getId() : null;

        List<ScoreFactor> factors = new ArrayList<>();

        // Internal factors
        factors.add(scoreDescription(listing.getDescription(), weights.getOrDefault("description", 35)));
        factors.add(scorePhotos(listing.getPhotosCount(), weights.getOrDefault("photos", 30)));
        factors.add(scoreTitle(listing.getTitle(), weights.getOrDefault("title", 15)));
        factors.add(scoreFloorInfo(listing.getFloor(), listing.getTotalFloors(), weights.getOrDefault("floor_info", 10)));
        factors.add(scoreSellerType(listing.getSellerType(), weights.getOrDefault("seller_type", 10)));

        // Market factors
        factors.add(scorePriceCompetitiveness(listing.getPriceDeviationPct(), weights.getOrDefault("price_competitiveness", 20)));
        factors.add(scoreDemandContext(context.demandIndex(), weights.getOrDefault("demand_context", 10)));
        factors.add(scoreCompetitorDensity(context.activeListingsCount(), weights.getOrDefault("competitor_density", 5)));

        // Anomaly penalty ( ثابت -10 points as per requirements, could also be dynamic if needed )
        if (Boolean.TRUE.equals(listing.getIsAnomaly())) {
            factors.add(new ScoreFactor("anomaly_penalty", -10, 0,
                "Объявление помечено как аномальное и отправлено на модерацию"));
        }

        int total = Math.max(0, Math.min(100,
            factors.stream().mapToInt(ScoreFactor::points).sum()
        ));

        return new ScoringResult(total, factors, Instant.now(), modelId);
    }

    private ScoringModel getActiveModel() {
        return scoringModelRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc().orElse(null);
    }

    private ScoreFactor scorePriceCompetitiveness(BigDecimal deviationPct, int maxPoints) {
        if (deviationPct == null) {
            return new ScoreFactor("price_competitiveness", maxPoints / 2, maxPoints,
                "Не удалось определить рыночный контекст района");
        }

        double deviation = deviationPct.doubleValue();

        if (deviation < -20) {
            return new ScoreFactor("price_competitiveness", maxPoints / 2, maxPoints,
                "Цена значительно ниже рынка — это может отпугнуть покупателей");
        }
        if (deviation < -5) {
            return new ScoreFactor("price_competitiveness", maxPoints, maxPoints, null);
        }
        if (deviation <= 5) {
            return new ScoreFactor("price_competitiveness", (int)(maxPoints * 0.8), maxPoints, null);
        }
        if (deviation <= 15) {
            return new ScoreFactor("price_competitiveness", maxPoints / 2, maxPoints,
                "Цена на " + String.format("%.0f", deviation) +
                "% выше медианы района. Рассмотрите снижение или улучшите описание");
        }
        return new ScoreFactor("price_competitiveness", maxPoints / 5, maxPoints,
            "Цена значительно выше рынка. Покупатели выберут более доступные варианты");
    }

    private ScoreFactor scoreDemandContext(BigDecimal demandIndex, int maxPoints) {
        if (demandIndex == null) {
            return new ScoreFactor("demand_context", maxPoints / 2, maxPoints, null);
        }
        double idx = demandIndex.doubleValue();
        if (idx >= 1.3) {
            return new ScoreFactor("demand_context", maxPoints, maxPoints, null);
        }
        if (idx >= 1.0) {
            return new ScoreFactor("demand_context", (int)(maxPoints * 0.7), maxPoints, null);
        }
        if (idx >= 0.7) {
            return new ScoreFactor("demand_context", (int)(maxPoints * 0.4), maxPoints,
                "Спрос в районе ниже среднего — уделите особое внимание цене и фото");
        }
        return new ScoreFactor("demand_context", (int)(maxPoints * 0.2), maxPoints,
            "Низкий спрос в районе. Рассмотрите агрессивное ценообразование");
    }

    private ScoreFactor scoreCompetitorDensity(Integer competitorCount, int maxPoints) {
        if (competitorCount == null) {
            return new ScoreFactor("competitor_density", (int)(maxPoints * 0.6), maxPoints, null);
        }
        if (competitorCount < 50) {
            return new ScoreFactor("competitor_density", maxPoints, maxPoints, null);
        }
        if (competitorCount < 150) {
            return new ScoreFactor("competitor_density", (int)(maxPoints * 0.6), maxPoints, null);
        }
        return new ScoreFactor("competitor_density", (int)(maxPoints * 0.2), maxPoints,
            "Высокая конкуренция в районе (" + competitorCount +
            " активных объявлений). Выделитесь ценой или качеством фото");
    }

    private ScoreFactor scoreDescription(String description, int maxPoints) {
        if (description == null || description.isBlank()) {
            return new ScoreFactor("description", 0, maxPoints,
                "Добавьте описание объявления");
        }
        int length = description.trim().length();
        if (length < 50) {
            return new ScoreFactor("description", (int)(maxPoints * 0.28), maxPoints,
                "Описание слишком короткое, добавьте минимум 50 символов");
        }
        if (length < 150) {
            return new ScoreFactor("description", (int)(maxPoints * 0.57), maxPoints,
                "Расширьте описание до 150+ символов для лучшей конверсии");
        }
        if (length < 300) {
            return new ScoreFactor("description", (int)(maxPoints * 0.8), maxPoints, null);
        }
        return new ScoreFactor("description", maxPoints, maxPoints, null);
    }

    private ScoreFactor scorePhotos(Integer photosCount, int maxPoints) {
        int count = photosCount != null ? photosCount : 0;
        if (count == 0) {
            return new ScoreFactor("photos", 0, maxPoints,
                "Добавьте хотя бы 3 фотографии");
        }
        if (count < 3) {
            return new ScoreFactor("photos", (int)(maxPoints * 0.33), maxPoints,
                "Добавьте ещё " + (3 - count) + " фото");
        }
        if (count < 7) {
            return new ScoreFactor("photos", (int)(maxPoints * 0.66), maxPoints,
                "Объявления с 7+ фото получают на 40% больше звонков");
        }
        if (count < 10) {
            return new ScoreFactor("photos", (int)(maxPoints * 0.83), maxPoints, null);
        }
        return new ScoreFactor("photos", maxPoints, maxPoints, null);
    }

    private ScoreFactor scoreTitle(String title, int maxPoints) {
        if (title == null || title.isBlank()) {
            return new ScoreFactor("title", 0, maxPoints, "Заполните заголовок");
        }
        int length = title.trim().length();
        if (length < 20) {
            return new ScoreFactor("title", (int)(maxPoints * 0.46), maxPoints,
                "Сделайте заголовок более информативным");
        }
        return new ScoreFactor("title", maxPoints, maxPoints, null);
    }

    private ScoreFactor scoreFloorInfo(Integer floor, Integer totalFloors, int maxPoints) {
        if (floor == null || totalFloors == null) {
            return new ScoreFactor("floor_info", 0, maxPoints,
                "Укажите этаж и этажность дома");
        }
        return new ScoreFactor("floor_info", maxPoints, maxPoints, null);
    }

    private ScoreFactor scoreSellerType(SellerType sellerType, int maxPoints) {
        return switch (sellerType) {
            case OWNER     -> new ScoreFactor("seller_type", maxPoints, maxPoints, null);
            case DEVELOPER -> new ScoreFactor("seller_type", (int)(maxPoints * 0.8), maxPoints, null);
            case AGENCY    -> new ScoreFactor("seller_type", (int)(maxPoints * 0.6), maxPoints, null);
        };
    }
}
