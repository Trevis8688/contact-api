package com.duong.contactapi.service;

import com.duong.contactapi.models.Contact;
import com.duong.contactapi.repository.ContactRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.duong.contactapi.constants.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor //Creates dependency injection constructor for vars
public class ContactService {

    private final ContactRepo repo;

    /**
     * Service method to return all contacts in a pagination format
     * @param page page number
     * @param size number of contacts per page
     * @return Page object of type Contact
     */
    public Page<Contact> getAllContact(int page, int size){
        return repo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    /**
     * Finds contact in repo given id
     * @param id of contact to find
     * @return the contact with the given id or an exception if not found
     */
    public Contact getContact(String id){
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Contact not found"));
    }

    /**
     * Adds new contact to respoitory
     * @param contact to add
     * @return the contact that was saved
     */
    public Contact createContact(Contact contact){
        return repo.save(contact);
    }

    /**
     * removes contact from
     * @param id of contact to delete
     */
    public void deleteById(String id){
        repo.deleteById(id);
    }

    /**
     * Updates a contact's photo
     * @param id the contact to update
     * @param file the photo file
     * @return the URL of the photo that was uploaded
     */
    public String uploadPhoto(String id, MultipartFile file){
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoURL(photoUrl);
        repo.save(contact);
        return photoUrl;
    }

    /**
     * Helper function that takes a file name as a String
     * and returns the file extension as a String
     */
    private final Function<String, String> getFileExtension = (fileName) ->
            Optional.of(fileName)
                    .filter(name -> name.contains("."))
                    .map(name -> name.substring(fileName.lastIndexOf(".")))
                    .orElse(".png");

    /**
     * Defines a BiFunction that takes a String and File
     * as input and returns a String
     * Helper function that takes the id profile picture of a contact
     * and adds it to
     */
    public final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String fileName = id + getFileExtension.apply(image.getOriginalFilename());
        try {
            // Set up the file storage location
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY)
                    .toAbsolutePath()
                    .normalize();

            // Create the directory if it doesn't exist
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }

            // Copy the image to the correct location
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName), REPLACE_EXISTING);

            // Return the file's URI as a string
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/contacts/images/" + id + getFileExtension.apply(image.getOriginalFilename()))
                    .toUriString();

        } catch (Exception e) {
            throw new RuntimeException("Error saving image", e);
        }
    };

}
