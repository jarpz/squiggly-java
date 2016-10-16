package com.github.bohnman.squiggly.context.provider;

import com.github.bohnman.squiggly.context.SquigglyContext;

/**
 * Used for supplying a @{@link com.github.bohnman.squiggly.filter.SquigglyPropertyFilter} with a way to retrieve a
 * context.
 */
public interface SquigglyContextProvider {

    /**
     * Get the context.
     *
     * @return context
     */
    SquigglyContext getContext();
}