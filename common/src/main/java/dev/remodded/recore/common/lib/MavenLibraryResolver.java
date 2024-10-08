package dev.remodded.recore.common.lib;

import dev.remodded.recore.api.lib.ClassPathLibrary;
import dev.remodded.recore.api.lib.LibraryLoadingException;
import dev.remodded.recore.api.lib.LibraryStore;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The maven library resolver acts as a resolver for yet to be resolved jar libraries that may be pulled from a
 * remote maven repository.
 * <p>
 * Plugins may create and configure a {@link MavenLibraryResolver} by creating a new one and registering both
 * a dependency artifact that should be resolved to a library at runtime and the repository it is found in.
 * An example of this would be the inclusion of the jooq library for typesafe SQL queries:
 * <pre>{@code
 * MavenLibraryResolver resolver = new MavenLibraryResolver();
 * resolver.addDependency(new Dependency(new DefaultArtifact("org.jooq:jooq:3.17.7"), null));
 * resolver.addRepository(new RemoteRepository.Builder(
 *     "central", "default", "https://repo1.maven.org/maven2/"
 * ).build());
 * }</pre>
 * <p>
 * Plugins may create and register a {@link MavenLibraryResolver} after configuring it. <br/>
 * This code comes from <a href="https://github.com/PaperMC/Paper/blob/master/LICENSE.md">Paper</a>
 */
public class MavenLibraryResolver implements ClassPathLibrary {

    private static final Logger logger = LoggerFactory.getLogger(MavenLibraryResolver.class);

    private final RepositorySystem repository;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> repositories = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();

    /**
     * Creates a new maven library resolver instance.
     * <p>
     * The created instance will use the servers {@code libraries} folder to cache fetched libraries in.
     * Notably, the resolver is created without any repository, not even maven central.
     * It is hence crucial that plugins which aim to use this api register all required repositories before
     */
    public MavenLibraryResolver() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        this.repository = locator.getService(RepositorySystem.class);
        this.session = MavenRepositorySystemUtils.newSession();

        this.session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_FAIL);
        this.session.setLocalRepositoryManager(this.repository.newLocalRepositoryManager(this.session, new LocalRepository("libraries")));
        this.session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferInitiated(@NotNull TransferEvent event) {
                logger.info("Downloading {}", event.getResource().getRepositoryUrl() + event.getResource().getResourceName());
            }
        });
        this.session.setReadOnly();
    }

    /**
     * Adds the provided dependency to the library resolver.
     * The artifact from the first valid repository matching the passed dependency will be chosen.
     *
     * @param dependency the definition of the dependency the maven library resolver should resolve when running
     * @see MavenLibraryResolver#addRepository(RemoteRepository)
     */
    public void addDependency(@NotNull Dependency dependency) {
        this.dependencies.add(dependency);
    }

    /**
     * Adds the provided repository to the library resolver.
     * The order in which these are added does matter, as dependency resolving will start at the first added
     * repository.
     *
     * @param remoteRepository the configuration that defines the maven repository this library resolver should fetch
     *                         dependencies from
     */
    public void addRepository(@NotNull RemoteRepository remoteRepository) {
        this.repositories.add(remoteRepository);
    }

    /**
     * Resolves the provided dependencies and adds them to the library store.
     *
     * @param store the library store the then resolved and downloaded dependencies are registered into
     * @throws LibraryLoadingException if resolving a dependency failed
     */
    @Override
    public void register(@NotNull LibraryStore store) throws LibraryLoadingException {
        List<RemoteRepository> repos = this.repository.newResolutionRepositories(this.session, this.repositories);

        // Sponge API detection
        boolean isSponge = false;
        try {
            isSponge = Class.forName("dev.remodded.recore.sponge_api12.ReCoreSponge").getField("INSTANCE").get(null) != null;
        } catch (Exception ignored) {}

        DependencyResult result;
        try {
            boolean _isSponge = isSponge;
            result = this.repository.resolveDependencies(this.session, new DependencyRequest(new CollectRequest((Dependency) null, this.dependencies, repos), (node, parents) -> {
                Artifact artifact = node.getArtifact();
                boolean accept = true; // By default, accept the artifact

                // Ignore netty-buffer and netty-common on Sponge API 12 as they are already provided by the platform and are incompatible
                if (_isSponge)
                    accept = artifact == null || !(artifact.getArtifactId().equals("netty-buffer") || artifact.getArtifactId().equals("netty-common"));

                if (!accept)
                    logger.debug("Ignoring {} as it is incompatible", node.getArtifact());

                return accept;
            }));
        } catch (DependencyResolutionException ex) {
            throw new LibraryLoadingException("Error resolving libraries", ex);
        }

        for (ArtifactResult artifact : result.getArtifactResults()) {
            File file = artifact.getArtifact().getFile();
            store.addLibrary(file.toPath());
        }
    }
}
