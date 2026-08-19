package com.an.tripora.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cancellation_policy_tiers")
public class CancellationPolicyTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private CancellationPolicy policy;

    @Column(nullable = false)
    private int daysBefore;

    @Column(nullable = false)
    private double cancellationFeePercent;
}
