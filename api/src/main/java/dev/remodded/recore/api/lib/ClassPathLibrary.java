package dev.remodded.recore.api.lib;

import org.jetbrains.annotations.NotNull;

/**
 * The classpath library interface represents libraries that are capable of registering themselves via
 * {@link #register(LibraryStore)} on any given {@link LibraryStore}. <br/>
 * This code comes from <a href="https://github.com/PaperMC/Paper/blob/master/LICENSE.md">Paper</a>
 */
public interface ClassPathLibrary {

    /**
     * Called to register the library this class path library represents into the passed library store.
     * This method may either be implemented by the plugins themselves if they need complex logic, or existing
     * API exposed implementations of this interface may be used.
     *
     * @param store the library store instance to register this library into
     * @throws LibraryLoadingException if library loading failed for this classpath library
     */
    void register(@NotNull LibraryStore store) throws LibraryLoadingException;
}
