package xyz.mattring.grambaal.ui.undertow;

import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;

import java.io.IOException;

public class WrapperResourceManager implements io.undertow.server.handlers.resource.ResourceManager {
    private final io.undertow.server.handlers.resource.ResourceManager delegate;
    private final 

    public WrapperResourceManager(io.undertow.server.handlers.resource.ResourceManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public Resource getResource(String s) throws IOException {
        return delegate.getResource(s);
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
