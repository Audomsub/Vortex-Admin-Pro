package com.vortexadmin.service;

import com.vortexadmin.dto.request.FileRequest;
import com.vortexadmin.dto.response.FileResponse;
import com.vortexadmin.entity.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service contract for file management operations including upload, download, rename, and
 * deletion of files associated with the currently authenticated user.
 */
public interface FileService {

    /**
     * Returns all files belonging to the currently authenticated user.
     *
     * @return a list of file responses for the calling user
     */
    List<FileResponse> getMyFiles();

    /**
     * Persists a file metadata record (e.g., for a remotely stored file) without handling
     * the binary upload itself.
     *
     * @param request the file metadata payload including name, size, and storage URL
     * @return the persisted file response
     */
    FileResponse uploadFileRecord(FileRequest request);

    /**
     * Accepts a multipart file upload, stores the binary content on the server, persists a
     * metadata record, and returns the resulting file response.
     *
     * @param file the multipart file submitted by the client
     * @return the persisted file response including the storage path and metadata
     * @throws com.vortexadmin.exception.ApiException if the file cannot be stored
     */
    FileResponse uploadFile(MultipartFile file);

    /**
     * Returns the stored file as a Spring {@link Resource} suitable for streaming in an HTTP
     * response.
     *
     * @param fileEntity the {@link File} entity whose binary content should be retrieved
     * @return a {@link Resource} pointing to the file's stored content
     * @throws com.vortexadmin.exception.ApiException if the file cannot be found on disk
     */
    Resource downloadFile(File fileEntity);

    /**
     * Retrieves the raw {@link File} entity by its primary key, used internally by controllers
     * that need to pass the entity to {@link #downloadFile(File)}.
     *
     * @param id the primary key of the file record
     * @return the matching {@link File} entity
     * @throws com.vortexadmin.exception.ApiException if no file with the given ID exists
     */
    File getFileEntity(Long id);

    /**
     * Renames the specified file.
     *
     * @param id   the primary key of the file to rename
     * @param name the new file name
     * @throws com.vortexadmin.exception.ApiException if the file is not found
     */
    void renameFile(Long id, String name);

    /**
     * Deletes the specified file record and its associated binary content from storage.
     *
     * @param id the primary key of the file to delete
     * @throws com.vortexadmin.exception.ApiException if the file is not found
     */
    void deleteFile(Long id);
}
