package org.jglue.cdiunit.internal;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.github.classgraph.utils.AutoCloseableExecutorService;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.deltaspike.core.util.ExceptionUtils;

/**
 * @author Illya Kysil <a href="mailto:ikysil@ikysil.name">ikysil@ikysil.name</a>
 */
public class CachingClassGraphScanner implements ClasspathScanner {

	/**
	 * The default number of worker threads to use while scanning. This number gave the best results on a relatively
	 * modern laptop with SSD, while scanning a large classpath.
	 */
	static final int DEFAULT_NUM_WORKER_THREADS = Math.max(
		// Always scan with at least 2 threads
		2, //
		(int) Math.ceil(
			// Num IO threads (top out at 4, since most I/O devices won't scale better than this)
			Math.min(4.0f, Runtime.getRuntime().availableProcessors() * 0.75f) +
				// Num scanning threads (higher than available processors, because some threads can be blocked)
				Runtime.getRuntime().availableProcessors() * 1.25f) //
	);

	static final ExecutorService scanExecutor = new AutoCloseableExecutorService(DEFAULT_NUM_WORKER_THREADS);

	static ConcurrentHashMap<Object, Object> cache = new ConcurrentHashMap<>();

	private final BeanArchiveScanner beanArchiveScanner;

	public CachingClassGraphScanner(final BeanArchiveScanner beanArchiveScanner) {
		this.beanArchiveScanner = beanArchiveScanner;
	}

	@SuppressWarnings("unchecked")
	private <K, V> V computeIfAbsent(final K k, final Supplier<V> computeValue) {
		return (V) cache.computeIfAbsent(k, o -> computeValue.get());
	}

	private List<URL> getClasspathURLs() {
		return computeIfAbsent(getClass().getClassLoader(), this::computeClasspathUrls);
	}

	@Override
	public Collection<URL> getBeanArchives() {
		final List<URL> urls = getClasspathURLs();
		return computeIfAbsent(computeKey(urls.stream()), () -> findBeanArchives(urls));
	}

	private Collection<URL> findBeanArchives(final List<URL> urls) {
		try {
			return beanArchiveScanner.findBeanArchives(urls);
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		return Collections.emptyList();
	}

	private List<URL> computeClasspathUrls() {
		try (ScanResult scan = new ClassGraph()
			.disableNestedJarScanning()
			.scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
			return scan.getClasspathURLs();
		}
	}

	@Override
	public List<String> getClassNamesForClasspath(URL[] urls) {
		return computeIfAbsent(computeKey(Arrays.stream(urls)), () -> this.computeClassNamesForClasspath(urls));
	}

	private Object computeKey(final Stream<URL> urls) {
		return urls
			.map(URL::toString)
			.collect(Collectors.joining(File.pathSeparator));
	}

	private List<String> computeClassNamesForClasspath(URL[] urls) {
		try (ScanResult scan = new ClassGraph()
				.disableNestedJarScanning()
				.enableClassInfo()
				.ignoreClassVisibility()
				.overrideClasspath(urls)
				.scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
			return scan.getAllClasses().getNames();
		}
	}

	@Override
	public List<String> getClassNamesForPackage(String packageName, URL url) {
		return computeIfAbsent(computeKey(packageName, url), () -> this.computeClassNamesForPackage(packageName, url));
	}

	private Object computeKey(final String packageName, final URL url) {
		return String.format("%s@%s", packageName, url);
	}

	private List<String> computeClassNamesForPackage(String packageName, URL url) {
		try (ScanResult scan = new ClassGraph()
				.disableNestedJarScanning()
				.enableClassInfo()
				.ignoreClassVisibility()
				.overrideClasspath(url)
				.whitelistPackagesNonRecursive(packageName)
				.scan(scanExecutor, DEFAULT_NUM_WORKER_THREADS)) {
			return scan.getAllClasses().getNames();
		}
	}

}
