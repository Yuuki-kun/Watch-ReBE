package com.watchbe.watchbedemo.service.utils;

import com.watchbe.watchbedemo.dto.WatchDto;
import com.watchbe.watchbedemo.model.Promotion_Details;
import com.watchbe.watchbedemo.repository.ProductPromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Similarity {

    private final ProductPromotionRepository productPromotionRepository;

    public List<WatchDto> getSimilarWatchNames(List<WatchDto> watches, String watchName) {
        List<WatchDto> similarWatches = new ArrayList<>();
        for (WatchDto watchDto : watches) {
            //compare with the reference
            if (watchDto.getReference().equals(watchName)) {
                similarWatches.add(watchDto);
                continue;
            }

            //compare the watch name with the watch name in the list
            double similarityName = similarity(watchDto.getName(), watchName)*0.3;
            //compare the similarity with the description
            //compare with the brand
            double brandSimilarity = similarity(watchDto.getBrand().getBrandName(), watchName)*0.2;
            //compare with the family
            double familySimilarity = similarity(watchDto.getFamily().getFamilyName(), watchName)*0.2;
            //compare with the movement
            double movementSimilarity = similarity(watchDto.getMovement().getName(), watchName)*0.2;


            System.out.println("Name Similarity: " + similarityName);
            System.out.println("Brand Similarity: " + brandSimilarity);
            System.out.println("Family Similarity: " + familySimilarity);
            System.out.println("Movement Similarity: " + movementSimilarity);

            double similarity = Math.max(similarityName, similarityName);
            similarity += Math.max(similarity, brandSimilarity);
            similarity += Math.max(similarity, familySimilarity);
            similarity += Math.max(similarity, movementSimilarity);

            System.out.println("Similarity: " + similarity);
            if (similarity > 0.4) {
                List<Promotion_Details> promotionDetails = productPromotionRepository
                        .findCurrentPromotionDetailsByProductIdForProduct(watchDto.getId());
                System.out.println("id=" + watchDto.getId() + " prod=" + promotionDetails);

                if (promotionDetails != null && !promotionDetails.isEmpty()) {
                    float promotion = 0f;
                    for (Promotion_Details p : promotionDetails) {
                        if (p.getPromotion().getPriority() == 1 && p.getPromotion().isActive()) {
                            promotion += p.getValue();
                        }
                    }
                    watchDto.setDiscount(promotion);
                    watchDto.setEndDiscountDate(promotionDetails.get(0).getPromotion().getDateEnd());
                } else {
                    watchDto.setDiscount(0f);
                }
                similarWatches.add(watchDto);
            }

        }
        return similarWatches;
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        /* // If you have StringUtils, you can use it to calculate the edit distance:
        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                                                             (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];

    }

}
