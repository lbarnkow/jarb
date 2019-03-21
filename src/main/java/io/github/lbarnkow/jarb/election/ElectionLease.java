package io.github.lbarnkow.jarb.election;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ElectionLease {
	private final static Logger logger = LoggerFactory.getLogger(ElectionLease.class);

	private static final int MAX_FILE_IO_RETRIES = 5;

	private final static ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());;

	private String leaderId;
	private long leaseAcquired;
	private long leaseExpiration;

	ElectionLease(String leaderId, long leaseAcquired, long leaseExpiration) {
		this.leaderId = leaderId;
		this.leaseAcquired = leaseAcquired;
		this.leaseExpiration = leaseExpiration;
	}

	public ElectionLease(String leaderId, long ttl) {
		this(leaderId, System.currentTimeMillis(), System.currentTimeMillis() + ttl);
	}

	public ElectionLease(ElectionLease oldLease, long ttl) {
		this(oldLease.leaderId, ttl);
	}

	// for Jackson deserialization
	@SuppressWarnings("unused")
	private ElectionLease() {
	}

	public boolean isExpired() {
		long now = System.currentTimeMillis();
		return leaseExpiration < now;
	}

	public boolean isOwnedBy(String candidateId) {
		return Objects.equals(leaderId, candidateId);
	}

	public static ElectionLease load(File file) throws IOException {
		if (!file.exists()) {
			return null;
		}

		ElectionLease lease = null;
		IOException lastException = null;
		int tries = 0;

		while (lease == null && tries < MAX_FILE_IO_RETRIES) {
			tries++;
			try {
				lease = MAPPER.readValue(file, ElectionLease.class);
			} catch (IOException e) {
				lastException = e;
			}
		}

		if (lease == null) {
			if (lastException instanceof JsonProcessingException) {
				logger.warn("Failed to deserialize election lease file; assuming file was empty!");
			} else {
				throw lastException;
			}
		}

		return lease;
	}

	public static void save(ElectionLease lease, File file) throws IOException {
		boolean success = false;
		IOException lastException = null;
		int tries = 0;

		Path tmp = Files.createTempFile(UUID.randomUUID().toString(), null);
		MAPPER.writeValue(tmp.toFile(), lease);

		while (!lease.isExpired() && !success && tries < MAX_FILE_IO_RETRIES) {
			try {
				Files.move(tmp, file.toPath(), REPLACE_EXISTING);
				success = true;
			} catch (IOException e) {
				lastException = e;
			}
		}

		if (tmp.toFile().exists()) {
			tmp.toFile().delete();
		}

		if (lastException == null && lease.isExpired()) {
			throw new ElectionLeaseExpiredException();
		} else if (!success) {
			throw lastException;
		}
	}
}