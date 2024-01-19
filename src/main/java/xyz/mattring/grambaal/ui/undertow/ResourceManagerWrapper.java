package xyz.mattring.grambaal.ui.undertow;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;

import java.io.IOException;
import java.util.function.UnaryOperator;

/**
 * Delegates to an inner ResourceManager, optionally applying a UnaryOperator to the Resource returned by the delegate.
 */
public class ResourceManagerWrapper implements ResourceManager {
    private final ResourceManager delegate;
    private final UnaryOperator<Resource> optionalResourceMunger;

    public ResourceManagerWrapper(ResourceManager delegate, UnaryOperator<Resource> optionalResourceMunger) {
        this.delegate = delegate;
        this.optionalResourceMunger = optionalResourceMunger;
    }

    @Override
    public Resource getResource(String s) throws IOException {
        final Resource resource = delegate.getResource(s);
        return optionalResourceMunger != null ? optionalResourceMunger.apply(resource) : resource;
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return delegate.isResourceChangeListenerSupported();
    }

    @Override
    public void registerResourceChangeListener(ResourceChangeListener resourceChangeListener) {
        delegate.registerResourceChangeListener(resourceChangeListener);
    }

    @Override
    public void removeResourceChangeListener(ResourceChangeListener resourceChangeListener) {
        delegate.removeResourceChangeListener(resourceChangeListener);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
