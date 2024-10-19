package com.contactlist.demo.repo;

import com.contactlist.demo.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact,String> {
    @Override
    Optional<Contact> findById(String s);
}
