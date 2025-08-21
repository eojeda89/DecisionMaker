package com.eojeda89.decididorapi.domain.service;

import com.eojeda89.decididorapi.domain.model.AlgorithmDetails;
import com.eojeda89.decididorapi.domain.model.Option;

import java.util.List;

public interface DecisionAlgorithm {
    AlgorithmDetails chooseWinnerIndex(List<Option> options);
}
