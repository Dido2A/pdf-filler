package com.example.pdffiller.storage;

import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

	void init();

	void store(MultipartFile file);

	/**
	 *
	 * @return the {@link Stream} of {@link Path}. It must be used with try-with-resources statement!
	 */
	Stream<Path> loadAll();

	Path load(String filename);

	Resource loadAsResource(String filename);

	WritableResource tempResource();

	void deleteAll();

	void onStart();

}
