package com.home.extract;

import com.home.common.LogDocument;

/**
 * Interface for log document extractors.
 */
public interface LogDocExtractor {
    LogDocument parseNext();
    boolean hasNext();
    LogDocument current();
}
