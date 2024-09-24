package com.duong.contactapi.controller;

import com.duong.contactapi.models.Contact;
import com.duong.contactapi.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.duong.contactapi.constants.Constant.PHOTO_DIRECTORY;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("/contacts")
@RequiredArgsConstructor //dependency injection for service
public class ContactController {
    private final ContactService service;

    /**
     * Mapping to handle HTTP post request to create new contact
     * @param contact Contact object passed in via JSON to be added to DB
     * @return HTTP response with URI location of object and the Contact itself included
     */
    @PostMapping
    public ResponseEntity<Contact> createContact(@RequestBody Contact contact) {
        return ResponseEntity.created(URI.create("/contacts/userID")).body(service.createContact(contact));
    }

    /**
     * Handles HTTP GET requests to retrieve a paginated list of contacts.
     *
     * @param page the page number of the results to be returned (0-indexed), defaults to 0 if not provided
     * @param size the number of contacts to return per page, defaults to 10 if not provided
     * @return a ResponseEntity containing a Page object with the list of contacts, and HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<Page<Contact>> getContacts(@RequestParam(value = "page", defaultValue = "0") int page,
                                                     @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok().body(service.getAllContact(page, size));
    }

    /**
     * Handles HTTP GET requests to retrieve a contact by its ID.
     *
     * @param id the ID of the contact to retrieve
     * @return a ResponseEntity containing the Contact and HTTP 200 OK status
     */
    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(@PathVariable("id") String id){
        return ResponseEntity.ok().body(service.getContact(id));
    }

    /**
     * Handles HTTP PUT requests to upload or update a photo for a specific contact.
     *
     * @param id the ID of the contact for whom the photo is being uploaded
     * @param file the MultipartFile representing the photo to be uploaded
     * @return a ResponseEntity containing a success message or URL, and HTTP 200 OK status
     */
    @PutMapping("/photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("id") String id, @RequestParam("file")MultipartFile file) {
        return ResponseEntity.ok().body(service.uploadPhoto(id, file));
    }

    /**
     * Handles HTTP GET requests to retrieve an image by its filename.
     *
     * @param filename the name of the image file to retrieve
     * @return a byte array representing the image file
     * @throws IOException if there is an error reading the image file
     */
    @GetMapping(path = "/image/{filename}", produces = {IMAGE_PNG_VALUE, IMAGE_JPEG_VALUE})
    public byte[] getPhoto(@PathVariable("filename") String filename) throws IOException {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + "/" + filename));
    }
}
