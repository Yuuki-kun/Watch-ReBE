package com.watchbe.watchbedemo.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 1000)
    private String comment;
    private float ratingStars;
    private Date datePosted;
    private long loves;

    //self relationship//
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
//    @JsonManagedReference
    private Review review;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @JsonBackReference
    private List<Review> childReviews = new ArrayList<>();
    //**self relationship**//

    @Column(nullable = false, columnDefinition = "int default 0")
    private int totalChildReviews;

    public void addChildReview(Review childReview){
        this.childReviews.add(childReview);
        childReview.setReview(this);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watch_id")
//    @JsonBackReference
    private Watch watch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
//    @JsonBackReference
    private Customer customer;
}
