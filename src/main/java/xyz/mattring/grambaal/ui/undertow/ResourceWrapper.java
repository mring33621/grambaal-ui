package xyz.mattring.grambaal.ui.undertow;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.util.ETag;
import io.undertow.util.MimeMappings;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Delegates to an inner Resource, optionally applying a UnaryOperator to the Sender passed to the serve method.
 */
public class ResourceWrapper implements Resource {
    private final Resource delegate;
    private final UnaryOperator<Sender> optionalSenderMunger;

    public ResourceWrapper(Resource delegate, UnaryOperator<Sender> optionalSenderMunger) {
        this.delegate = delegate;
        this.optionalSenderMunger = optionalSenderMunger;
    }

    @Override
    public String getPath() {
        return delegate.getPath();
    }

    @Override
    public Date getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public String getLastModifiedString() {
        return delegate.getLastModifiedString();
    }

    @Override
    public ETag getETag() {
        return delegate.getETag();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isDirectory() {
        return delegate.isDirectory();
    }

    @Override
    public List<Resource> list() {
        return delegate.list();
    }

    @Override
    public String getContentType(MimeMappings mimeMappings) {
        return delegate.getContentType(mimeMappings);
    }

    @Override
    public void serve(Sender sender, HttpServerExchange httpServerExchange, IoCallback ioCallback) {
        final Sender actualSender = optionalSenderMunger != null ? optionalSenderMunger.apply(sender) : sender;
        delegate.serve(actualSender, httpServerExchange, ioCallback);
    }

    @Override
    public Long getContentLength() {
        return delegate.getContentLength();
    }

    @Override
    public String getCacheKey() {
        return delegate.getCacheKey();
    }

    @Override
    public File getFile() {
        return delegate.getFile();
    }

    @Override
    public Path getFilePath() {
        return delegate.getFilePath();
    }

    @Override
    public File getResourceManagerRoot() {
        return delegate.getResourceManagerRoot();
    }

    @Override
    public Path getResourceManagerRootPath() {
        return delegate.getResourceManagerRootPath();
    }

    @Override
    public URL getUrl() {
        return delegate.getUrl();
    }
}
