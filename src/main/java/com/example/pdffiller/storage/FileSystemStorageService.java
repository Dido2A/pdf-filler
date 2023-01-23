package com.example.pdffiller.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileSystemStorageService implements StorageService {

	private final Path rootLocation;
	private final boolean clearStorageOnStart;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
		this.clearStorageOnStart = properties.getClearStorageOnStart();
	}

	@Override
	public void store(MultipartFile file) {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(
					Paths.get(Objects.requireNonNull(file.getOriginalFilename())))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
					StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		}
	}

	/**
	 *
	 * @return the {@link Stream} of {@link Path}. It must be used with try-with-resources statement!
	 */
	@Override
	public Stream<Path> loadAll() {
		try {
			//noinspection resource
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(this.rootLocation::relativize);
		}
		catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		}
		catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {
		try {
			Files.createDirectories(rootLocation);
		}
		catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public void onStart(){
		if(clearStorageOnStart) {
			deleteAll();
			init();
		}
	}

	@Override
	public WritableResource tempResource() {
		try {
			Path tempFile = Files.createTempFile(
					rootLocation,
					LocalDate.now(ZoneId.systemDefault())
							.toString().replace("-","")
							.concat("_"),
					".pdf");
			WritableResource resource = new FileSystemResource(tempFile);
			if (resource.exists() || resource.isWritable()) {
				return resource;
			}
			else {
				throw new StorageFileNotFoundException("Could not create a temp file");
			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not create a temp file.", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


}
