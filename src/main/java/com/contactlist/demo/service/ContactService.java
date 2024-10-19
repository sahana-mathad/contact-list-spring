package com.contactlist.demo.service;

import com.contactlist.demo.entity.Contact;
import com.contactlist.demo.repo.ContactRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.contactlist.demo.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


@Service
@Slf4j
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
public class ContactService {
    private final ContactRepo contactRepo;


    public Page<Contact> getAllContacts(int page,int size){
        return contactRepo.findAll((PageRequest.of(page,size, Sort.by("name"))));
    }

    public Contact getContact(String id){
        return contactRepo.findById(id).orElseThrow(()->new RuntimeException("Contact Not Found!!!"));
    }

    public Contact createContact(Contact contact){
        return contactRepo.save(contact);
    }

    public void deleteContact(Contact contact){
         contactRepo.delete(contact);
        System.out.println("Contact deleted sucessfully!!!");
    }

    public String uploadPhoto(String id, MultipartFile file){
        log.info("saving picture for user id:{}",id);
        Contact contact=getContact(id);
        String photoUrl= photoFunction.apply(id,file);
        contact.setPhotoUrl(photoUrl);
        contactRepo.save(contact);
        log.info("Photo saving {}",photoUrl);
        return photoUrl;
    }

    private final Function<String,String> fileExtension = fileName ->
        Optional.of(fileName).filter(name-> name.contains("."))
                .map(name->"."+name.substring(fileName.lastIndexOf(".")+1))
                .orElse(".png");



    private final BiFunction<String,MultipartFile,String> photoFunction=(id,image) -> {
        String filename=id+fileExtension.apply(image.getOriginalFilename());
        try{
            Path fileStorageLocation= Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if(!Files.exists(fileStorageLocation)){
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(),fileStorageLocation.resolve(filename),REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/contacts/image/"+filename).toUriString();
        } catch (Exception exception){
            throw new RuntimeException("Unable to save the file");
        }
    };

}
