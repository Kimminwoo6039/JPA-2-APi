package com.jpabook.www.service;

import com.jpabook.www.domain.item.Book;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;

@RunWith(Runner.class)
@SpringBootTest
public class ItemUpdateTest {

    @Autowired
    EntityManager en;

    @Test
    public void updateTest() throws Exception {
        Book book = en.find(Book.class,1L);


    }
}
